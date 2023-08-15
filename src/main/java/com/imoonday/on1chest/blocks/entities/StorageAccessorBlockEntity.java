package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.blocks.StorageBlankBlock;
import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class StorageAccessorBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {

    protected final HashMap<UUID, StorageAssessorScreenHandler.Settings> screenSettings = new HashMap<>();

    public StorageAccessorBlockEntity(BlockEntityType<? extends StorageAccessorBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public StorageAccessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.STORAGE_ACCESSOR_BLOCK_ENTITY, pos, state);
    }

    @Override
    public Text getDisplayName() {
        return this.getCachedState().getBlock().getName();
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new StorageAssessorScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos));
    }

    public StorageAssessorScreenHandler.Settings getScreenSettings(UUID uuid) {
        StorageAssessorScreenHandler.Settings settings = screenSettings.computeIfAbsent(uuid, uuid1 -> new StorageAssessorScreenHandler.Settings());
        markDirty();
        return settings;
    }

    public void setScreenSettings(UUID uuid, StorageAssessorScreenHandler.Settings settings) {
        this.screenSettings.put(uuid, settings);
        markDirty();
    }

    public Set<ItemStack> getFavouriteStacks(UUID uuid) {
        return getScreenSettings(uuid).getFavouriteStacks();
    }

    public boolean addFavouriteStack(UUID uuid, ItemStack stack) {
        Set<ItemStack> stacks = getFavouriteStacks(uuid);
        return stacks.stream().noneMatch(stack1 -> ItemStack.canCombine(stack, stack1)) && stacks.add(stack);
    }

    public boolean removeFavouriteStack(UUID uuid, ItemStack stack) {
        return getFavouriteStacks(uuid).removeIf(stack1 -> ItemStack.canCombine(stack, stack1));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtList list = new NbtList();
        screenSettings.forEach((uuid, settings) -> {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putUuid("Uuid", uuid);
            nbtCompound.put("Settings", settings.toNBT());
            list.add(nbtCompound);
        });
        nbt.put("ScreenSettings", list);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("ScreenSettings", NbtElement.LIST_TYPE)) {
            nbt.getList("ScreenSettings", NbtElement.COMPOUND_TYPE).stream().filter(element -> element instanceof NbtCompound).map(element -> (NbtCompound) element).forEach(nbtCompound -> {
                UUID uuid = nbtCompound.getUuid("Uuid");
                NbtCompound settings = (NbtCompound) nbtCompound.get("Settings");
                if (uuid != null && settings != null) {
                    this.screenSettings.computeIfAbsent(uuid, uuid1 -> new StorageAssessorScreenHandler.Settings(settings));
                }
            });
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    public DefaultedList<ItemStack> createItemList(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return DefaultedList.of();
        }
        DefaultedList<ItemStack> itemList = DefaultedList.of();
        this.getConnectedBlocks(world, pos).stream().filter(blockPos -> world.getBlockEntity(blockPos) instanceof StorageMemoryBlockEntity).map(blockPos -> (StorageMemoryBlockEntity) world.getBlockEntity(blockPos)).filter(Objects::nonNull).map(StorageMemoryBlockEntity::getItems).forEach(itemList::addAll);
        int removedCount = itemList.size();
        itemList.removeIf(ItemStack::isEmpty);
        removedCount -= itemList.size();
        Map<ItemStack, Integer> itemStackMap = new HashMap<>();
        itemList.forEach(stack -> itemStackMap.entrySet().stream().filter(entry -> ItemStack.canCombine(stack, entry.getKey())).findFirst().ifPresentOrElse(entry -> itemStackMap.merge(entry.getKey(), stack.getCount(), Integer::sum), () -> itemStackMap.put(stack, stack.getCount())));
        DefaultedList<ItemStack> itemList1 = DefaultedList.ofSize(itemStackMap.size() + removedCount, ItemStack.EMPTY);
        int index = 0;
        for (Map.Entry<ItemStack, Integer> entry : itemStackMap.entrySet()) {
            ItemStack key = entry.getKey();
            Integer integer = entry.getValue();
            itemList1.set(index++, key.copyWithCount(integer));
        }
        return itemList1;
    }

    public List<BlockPos> getConnectedBlocks(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return List.of();
        }
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
        List<BlockPos> result = new ArrayList<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(pos);
        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            for (Direction direction : directions) {
                BlockPos adjacentPos = currentPos.offset(direction);
                BlockState adjacentState = world.getBlockState(adjacentPos);
                if ((adjacentState.getBlock() instanceof StorageMemoryBlock || adjacentState.getBlock() instanceof StorageBlankBlock) && !result.contains(adjacentPos)) {
                    result.add(adjacentPos);
                    queue.add(adjacentPos);
                }
            }
        }
        return result;
    }

    public List<Inventory> getAllInventories(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return List.of();
        }
        return getConnectedBlocks(world, pos).stream().filter(blockPos -> world.getBlockEntity(blockPos) instanceof StorageMemoryBlockEntity).map(blockPos -> (StorageMemoryBlockEntity) world.getBlockEntity(blockPos)).collect(Collectors.toCollection(ArrayList::new));
    }


    public boolean canInsert(World world, BlockPos pos, ItemStack stack) {
        if (world == null || pos == null) {
            return false;
        }
        return this.getAllInventories(world, pos).stream().anyMatch(inventory -> inventory.containsAny(stack1 -> ItemStack.canCombine(stack, stack1) && stack.getCount() + stack1.getCount() <= stack1.getMaxCount() || stack1.isEmpty()));
    }

    public boolean removeStack(World world, BlockPos pos, ItemStack stack, int removeCount, Slot slot) {
        if (world == null || pos == null) {
            return false;
        }
        boolean removed = false;
        Set<Inventory> inventories = new HashSet<>();
        if (slot instanceof StorageAssessorScreenHandler.MemorySlot memorySlot) {
            inventories.addAll(memorySlot.getActualInventories());
        }
        inventories.addAll(this.getAllInventories(world, pos));
        a:
        for (Inventory inventory : inventories) {
            boolean markDirty = false;
            for (int i = 0; i < inventory.size(); ++i) {
                if (removeCount <= 0) {
                    break a;
                }
                ItemStack itemStack = inventory.getStack(i);
                int maxCount = Math.min(removeCount, stack.getMaxCount());
                if (ItemStack.canCombine(itemStack, stack)) {
                    int stackCount = itemStack.getCount();
                    if (stackCount >= maxCount) {
                        inventory.removeStack(i, maxCount);
                        removeCount -= maxCount;
                    } else {
                        inventory.removeStack(i);
                        removeCount -= stackCount;
                    }
                    removed = true;
                    markDirty = true;
                }
            }
            if (markDirty) {
                inventory.markDirty();
            }
        }
        if (slot instanceof StorageAssessorScreenHandler.MemorySlot memorySlot && removed) {
            memorySlot.updateActualInventories();
        }
        return removed;
    }

    public boolean addStack(World world, BlockPos pos, ItemStack stack, int count, Slot slot) {
        if (world == null || pos == null) {
            return false;
        }
        boolean added = false;
        List<Inventory> inventories = this.getAllInventories(world, pos);
        int remainingCount = count;
        a:
        for (Inventory inventory : inventories) {
            boolean markDirty = false;
            for (int i = 0; i < inventory.size(); i++) {
                if (remainingCount <= 0) {
                    break a;
                }
                ItemStack stack1 = inventory.getStack(i);
                if (stack1.getCount() >= stack1.getMaxCount()) {
                    continue;
                }
                int maxCount = Math.min(remainingCount, stack.getMaxCount());
                if (ItemStack.canCombine(stack1, stack) && stack1.getCount() < stack1.getMaxCount()) {
                    if (maxCount + stack1.getCount() <= stack1.getMaxCount()) {
                        stack1.increment(maxCount);
                        remainingCount -= maxCount;
                    } else {
                        int amount = stack1.getMaxCount() - stack1.getCount();
                        stack1.increment(amount);
                        remainingCount -= amount;
                    }
                    added = true;
                    markDirty = true;
                }
            }
            if (markDirty) {
                inventory.markDirty();
            }
        }
        b:
        if (remainingCount > 0) {
            for (Inventory inventory : inventories) {
                boolean markDirty = false;
                for (int i = 0; i < inventory.size(); i++) {
                    if (remainingCount <= 0) {
                        break b;
                    }
                    ItemStack stack1 = inventory.getStack(i);
                    int maxCount = Math.min(remainingCount, stack.getMaxCount());
                    if (stack1.isEmpty()) {
                        inventory.setStack(i, stack.copyWithCount(maxCount));
                        remainingCount -= maxCount;
                        added = true;
                        markDirty = true;
                        if (slot instanceof StorageAssessorScreenHandler.MemorySlot memorySlot) {
                            memorySlot.addActualInventory(inventory);
                        }
                    }
                }
                if (markDirty) {
                    inventory.markDirty();
                }
            }
        }
        stack.decrement(count - remainingCount);
        return added;
    }
}
