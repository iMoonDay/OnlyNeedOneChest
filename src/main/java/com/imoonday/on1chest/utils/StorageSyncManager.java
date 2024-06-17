package com.imoonday.on1chest.utils;

import com.imoonday.on1chest.api.InteractHandler;
import com.imoonday.on1chest.network.NetworkHandler;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.function.Consumer;

public class StorageSyncManager {
    private static final int MAX_PACKET_SIZE = 64000;
    private final Object2IntMap<CombinedItemStack> idMap = new Object2IntOpenHashMap<>();
    private final Int2ObjectMap<CombinedItemStack> idMap2 = new Int2ObjectArrayMap<>();
    private final Object2LongMap<CombinedItemStack> items = new Object2LongOpenHashMap<>();
    private final Map<CombinedItemStack, CombinedItemStack> itemList = new HashMap<>();
    private int lastId = 1;
    private final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer(MAX_PACKET_SIZE, MAX_PACKET_SIZE * 2));

    private void write(PacketByteBuf buf, CombinedItemStack stack) {
        ItemStack st = stack.getStack();
        Item item = st.getItem();
        NbtCompound nbtCompound = getSyncNbt(st);
        byte flags = (byte) ((stack.getCount() == 0 ? 1 : 0) | (nbtCompound != null ? 2 : 0));
        boolean wr = true;
        int id = idMap.getInt(stack);
        if (id != 0) {
            flags |= 4;
            wr = false;
        }
        buf.writeByte(flags);
        buf.writeVarInt(idMap.computeIfAbsent(stack, s -> {
            int i = lastId++;
            idMap2.put(i, (CombinedItemStack) s);
            return i;
        }));
        if (wr) buf.writeIdentifier(Registries.ITEM.getId(item));
        if (stack.getCount() != 0) buf.writeVarLong(stack.getCount());
        if (wr && nbtCompound != null) buf.writeNbt(nbtCompound);
    }

    public static NbtCompound getSyncNbt(ItemStack stack) {
        Item item = stack.getItem();
        NbtCompound NbtCompound = null;
        if (stack.isDamageable() || item.isNbtSynced()) {
            NbtCompound = stack.getNbt();
        }
        return NbtCompound;
    }

    private void writeMiniStack(PacketByteBuf buf, CombinedItemStack stack) {
        int id = idMap.getInt(stack);
        byte flags = (byte) ((stack.getCount() == 0 ? 1 : 0) | 2);
        buf.writeByte(flags);
        buf.writeVarInt(id);
        buf.writeIdentifier(Registries.ITEM.getId(stack.getStack().getItem()));
        if (stack.getCount() != 0) buf.writeVarLong(stack.getCount());
        NbtCompound tag = new NbtCompound();
        NbtCompound d = new NbtCompound();
        tag.put("display", d);
        NbtList lore = new NbtList();
        d.put("Lore", lore);
        lore.add(NbtString.of(NbtString.escape("{\"translate\":\"tooltip.on1chest.nbt_overflow\",\"color\":\"red\"}")));
        tag.putInt("uid", id);
        buf.writeNbt(tag);
    }

    private CombinedItemStack read(PacketByteBuf buf) {
        byte flags = buf.readByte();
        int id = buf.readVarInt();
        boolean rd = (flags & 4) == 0;
        CombinedItemStack stack;
        if (rd) {
            stack = new CombinedItemStack(new ItemStack(Registries.ITEM.get(buf.readIdentifier())));
        } else {
            stack = new CombinedItemStack(idMap2.get(id).getStack());
        }
        long count = (flags & 1) != 0 ? 0 : buf.readVarLong();
        stack.setCount(count);
        if (rd && (flags & 2) != 0) {
            stack.getStack().setNbt(buf.readNbt());
        }
        idMap.put(stack, id);
        idMap2.put(id, stack);
        return stack;
    }

    public void update(Map<CombinedItemStack, Long> items, ServerPlayerEntity player, Consumer<NbtCompound> extraSync) {
        List<CombinedItemStack> toWrite = new ArrayList<>();
        Set<CombinedItemStack> found = new HashSet<>();
        items.forEach((s, c) -> {
            long pc = this.items.getLong(s);
            if (pc != 0L) found.add(s);
            if (pc != c) {
                toWrite.add(new CombinedItemStack(s.getStack(), c));
            }
        });
        this.items.forEach((s, c) -> {
            if (!found.contains(s)) {
                toWrite.add(new CombinedItemStack(s.getStack(), 0L));
            }
        });
        this.items.clear();
        this.items.putAll(items);
        if (!toWrite.isEmpty()) {
            buf.writerIndex(0);
            int j = 0;
            for (int i = 0; i < toWrite.size(); i++, j++) {
                CombinedItemStack stack = toWrite.get(i);
                int li = buf.writerIndex();
                try {
                    write(buf, stack);
                } catch (IndexOutOfBoundsException e) {
                    buf.writerIndex(li);
                    writeMiniStack(buf, stack);
                }
                int s = buf.writerIndex();
                if ((s > MAX_PACKET_SIZE || j > 32000) && j > 1) {
                    NbtCompound t = writeBuf("d", buf, li);
                    t.putShort("l", (short) j);
                    NetworkHandler.sendToClient(player, t);
                    j = 0;
                    buf.writerIndex(0);
                    if (s - li > MAX_PACKET_SIZE) {
                        writeMiniStack(buf, stack);
                    } else {
                        buf.writeBytes(buf, li, s - li);
                    }
                }
            }
            if (j > 0 || extraSync != null) {
                NbtCompound t;
                if (j > 0) {
                    t = writeBuf("d", buf, buf.writerIndex());
                    t.putShort("l", (short) j);
                } else {
                    t = new NbtCompound();
                }
                if (extraSync != null) extraSync.accept(t);
                NetworkHandler.sendToClient(player, t);
            }
        } else if (extraSync != null) {
            NbtCompound t = new NbtCompound();
            extraSync.accept(t);
            NetworkHandler.sendToClient(player, t);
        }
    }

    public boolean receiveUpdate(NbtCompound tag) {
        if (tag.contains("d")) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.wrappedBuffer(tag.getByteArray("d")));
            List<CombinedItemStack> in = new ArrayList<>();
            short len = tag.getShort("l");
            for (int i = 0; i < len; i++) {
                in.add(read(buf));
            }
            in.forEach(stack -> {
                if (stack.getCount() == 0) {
                    this.itemList.remove(stack);
                } else {
                    this.itemList.put(stack, stack);
                }
            });
            return true;
        }
        return false;
    }

    public void sendInteract(CombinedItemStack intStack, SlotAction action, boolean mod) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        int flags = mod ? 1 : 0;
        if (intStack == null) {
            buf.writeByte(flags | 2);
        } else {
            buf.writeByte(flags);
            buf.writeVarInt(idMap.getInt(intStack));
            buf.writeVarLong(intStack.getCount());
        }
        buf.writeEnumConstant(action);
        NetworkHandler.sendToServer(writeBuf("a", buf, buf.writerIndex()));
    }

    private NbtCompound writeBuf(String id, PacketByteBuf buf, int len) {
        byte[] data = new byte[len];
        buf.getBytes(0, data);
        NbtCompound tag = new NbtCompound();
        tag.putByteArray(id, data);
        return tag;
    }

    public void receiveInteract(NbtCompound nbtCompound, InteractHandler handler) {
        if (nbtCompound.contains("a")) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.wrappedBuffer(nbtCompound.getByteArray("a")));
            byte flags = buf.readByte();
            CombinedItemStack stack;
            if ((flags & 2) != 0) {
                stack = null;
            } else {
                stack = new CombinedItemStack(idMap2.get(buf.readVarInt()).getStack());
                long count = buf.readVarLong();
                stack.setCount(count);
            }
            handler.onInteract(stack, buf.readEnumConstant(SlotAction.class), (flags & 1) != 0);
        }
    }

    public List<CombinedItemStack> getAsList() {
        return new ArrayList<>(this.itemList.values());
    }

//    public void fillStackedContents(StackedContents stc) {
//        items.forEach((s, c) -> {
//            ItemStack st = s.getActualStack();
//            st.setCount(c.intValue());
//            stc.accountSimpleStack(st);
//        });
//    }

    public long getCount(CombinedItemStack stack) {
        CombinedItemStack s = itemList.get(stack);
        return s != null ? s.getCount() : 0L;
    }
}
