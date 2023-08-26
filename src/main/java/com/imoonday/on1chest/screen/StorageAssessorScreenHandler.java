package com.imoonday.on1chest.screen;

import com.imoonday.on1chest.blocks.entities.StorageAccessorBlockEntity;
import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.items.RemoteAccessorItem;
import com.imoonday.on1chest.mixin.SlotAccessor;
import com.imoonday.on1chest.network.NetworkHandler;
import com.imoonday.on1chest.utils.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;

public class StorageAssessorScreenHandler extends AbstractRecipeScreenHandler<RecipeInputInventory> implements IScreenDataReceiver, InteractHandler {

    protected StorageAccessorBlockEntity accessor;
    public int rows;
    public int playerInventoryStartIndex;
    public final PlayerEntity player;
    public StorageSyncManager manager = new StorageSyncManager();
    public List<StorageSlot> storageSlotList = new ArrayList<>();
    public List<CombinedItemStack> itemList = new ArrayList<>();
    public List<CombinedItemStack> itemListClient = new ArrayList<>();
    public List<CombinedItemStack> itemListClientSorted = new ArrayList<>();
    public List<SlotData> slotData = new ArrayList<>();
    private final Set<ChunkPos> forceLoadingPoses = new HashSet<>();
    public boolean noSort;

    public StorageAssessorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    public StorageAssessorScreenHandler(ScreenHandlerType<? extends StorageAssessorScreenHandler> type, int syncId, PlayerInventory playerInventory, StorageAccessorBlockEntity accessor) {
        super(type, syncId);
        this.player = playerInventory.player;
        this.accessor = accessor;
        if (this.accessor != null) {
            World world = this.accessor.getWorld();
            if (world instanceof ServerWorld serverWorld) {
                BlockPos pos = this.accessor.getPos();
                tryForceLoadChunk(serverWorld, pos);
                for (Pair<World, BlockPos> pair : ConnectBlock.getConnectedBlocks(world, pos)) {
                    World world1 = pair.getLeft();
                    if (world != world1) {
                        tryForceLoadChunk((ServerWorld) world1, pair.getRight());
                    } else {
                        tryForceLoadChunk(serverWorld, pair.getRight());
                    }
                }
            }
        }
    }

    public StorageAssessorScreenHandler(int syncId, PlayerInventory playerInventory, StorageAccessorBlockEntity accessor) {
        this(ModScreens.STORAGE_ASSESSOR_SCREEN_HANDLER, syncId, playerInventory, accessor);
        this.addPlayerInventorySlots(playerInventory, 103, 161);
        this.addStorageSlots(6, 18);
    }

    public int getSlotLeftX() {
        return 9;
    }

    private void tryForceLoadChunk(ServerWorld serverWorld, BlockPos blockPos) {
        ChunkPos chunkPos;
        chunkPos = new ChunkPos(blockPos);
        if (!serverWorld.getForcedChunks().contains(chunkPos.toLong())) {
            serverWorld.setChunkForced(chunkPos.x, chunkPos.z, true);
            this.forceLoadingPoses.add(chunkPos);
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (!this.forceLoadingPoses.isEmpty() && this.accessor != null) {
            World world = this.accessor.getWorld();
            if (world instanceof ServerWorld serverWorld) {
                this.forceLoadingPoses.forEach(chunkPos -> serverWorld.setChunkForced(chunkPos.x, chunkPos.z, false));
            }
        }
    }

    protected void addPlayerInventorySlots(PlayerInventory playerInventory, int y1, int y2) {
        this.playerInventoryStartIndex = this.slots.size();
        for (int j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, this.getSlotLeftX() + k * 18, y1 + j * 18 + 36 + 1));
            }
        }
        for (int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, this.getSlotLeftX() + j * 18, y2 + 36 + 1));
        }
    }

    @Override
    protected Slot addSlot(Slot slot) {
        slotData.add(new SlotData(slot));
        return super.addSlot(slot);
    }

    public void setOffset(int x, int y) {
        slotData.forEach(d -> d.setOffset(x, y));
    }

    public static boolean checkTextFilter(ItemStack stack, String filter) {
        if (filter == null || filter.isEmpty()) return true;
        List<String> stringList = new ArrayList<>();
        stringList.add(stack.getTranslationKey());
        stringList.add(stack.getItem().getTranslationKey());
        if (!filter.startsWith("#")) {
            stringList.add(stack.getName().getString());
            stringList.add(stack.getItem().getName().getString());
            try {
                stringList.add(ChineseUtils.toPinyin(stack.getName().getString()));
                stringList.add(ChineseUtils.toFirstChar(stack.getName().getString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return stringList.stream().anyMatch(s -> s.toLowerCase().contains(filter.replaceFirst("#", "").toLowerCase()));
    }

    protected int getOverflowRows() {
        return MathHelper.ceilDiv(itemListClientSorted.size(), 9) - rows;
    }

    public int getRow(float scroll) {
        return Math.max((int) ((double) (scroll * (float) this.getOverflowRows()) + 0.5), 0);
    }

    public float getScrollPosition(int row) {
        return MathHelper.clamp((float) row / (float) this.getOverflowRows(), 0.0f, 1.0f);
    }

    public float getScrollPosition(float current, double amount) {
        if (itemListClientSorted.size() < this.rows * 9) {
            return 0.0f;
        }
        return MathHelper.clamp(current - (float) (amount / (double) this.getOverflowRows()), 0.0f, 1.0f);
    }

    public void scrollItems(float position) {
        int i = this.getRow(position);
        for (int j = 0; j < rows; ++j) {
            for (int k = 0; k < 9; ++k) {
                int l = k + (j + i) * 9;
                if (l >= 0 && l < this.itemListClientSorted.size()) {
                    setSlotContents(k + j * 9, this.itemListClientSorted.get(l));
                    continue;
                }
                setSlotContents(k + j * 9, null);
            }
        }
    }

    public final void setSlotContents(int id, CombinedItemStack stack) {
        storageSlotList.get(id).stack = stack;
    }

    public final StorageSlot getSlotByID(int id) {
        return storageSlotList.get(id);
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        if (slots.size() > invSlot) {
            if (invSlot >= playerInventoryStartIndex && accessor != null) {
                if (slots.get(invSlot) != null && slots.get(invSlot).hasStack()) {
                    Slot slot = slots.get(invSlot);
                    ItemStack slotStack = slot.getStack();
                    if (!canInsert(player, slotStack)) {
                        return ItemStack.EMPTY;
                    }
                    CombinedItemStack c = accessor.insertStack(new CombinedItemStack(slotStack, slotStack.getCount()));
                    ItemStack itemstack = c != null ? c.getActualStack() : ItemStack.EMPTY;
                    slot.setStack(itemstack);
                    if (!player.getWorld().isClient) {
                        sendContentUpdates();
                    }
                }
            } else {
                return shiftClickItems(player, invSlot);
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean canInsert(PlayerEntity player, ItemStack stack) {
        if (accessor == null) {
            return true;
        }
        boolean canInsert = true;
        if (stack.getItem() instanceof RemoteAccessorItem accessorItem) {
            BlockPos pos = accessorItem.getPos(stack);
            if (pos != null) {
                BlockPos accessorPos = accessor.getPos();
                if (pos.equals(accessorPos)) {
                    World world = accessor.getWorld();
                    if (world != null) {
                        if (!player.getWorld().getRegistryKey().equals(world.getRegistryKey()) || player.getPos().distanceTo(accessorPos.toCenterPos()) > 8) {
                            canInsert = false;
                        }
                    }
                } else {
                    return true;
                }
            }
        }
        return canInsert;
    }

    protected ItemStack shiftClickItems(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }

    public void sendMessage(NbtCompound compound) {
        NetworkHandler.sendToServer(compound);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.accessor != null;
    }

    @Override
    public void sendContentUpdates() {
        if (accessor == null) {
            return;
        }
        Map<CombinedItemStack, Long> stacks = this.accessor.getStacks();
        manager.update(stacks, (ServerPlayerEntity) player, null);
        super.sendContentUpdates();
    }

    public final void receiveClientNBTPacket(NbtCompound message) {
        if (manager.receiveUpdate(message)) {
            itemList = manager.getAsList();
            if (noSort) {
                itemListClient.forEach(s -> s.setCount(manager.getCount(s)));
            } else {
                itemListClient = new ArrayList<>(itemList);
            }
            player.getInventory().markDirty();
        }
    }

    @Override
    public void receive(NbtCompound nbt) {
        if (player.isSpectator()) return;
        manager.receiveInteract(nbt, this);
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {

    }

    @Override
    public void clearCraftingSlots() {

    }

    @Override
    public boolean matches(Recipe<? super RecipeInputInventory> recipe) {
        return false;
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return -1;
    }

    @Override
    public int getCraftingWidth() {
        return 0;
    }

    @Override
    public int getCraftingHeight() {
        return 0;
    }

    @Override
    public int getCraftingSlotCount() {
        return 0;
    }

    @Override
    public RecipeBookCategory getCategory() {
        return null;
    }

    @Override
    public boolean canInsertIntoSlot(int index) {
        return true;
    }

    @Override
    public void onInteract(CombinedItemStack clicked, SlotAction act, boolean shift) {
        ((ServerPlayerEntity) player).updateLastActionTime();
        if (accessor == null) {
            return;
        }
        switch (act) {
            case LEFT_CLICK -> {
                ItemStack stack = getCursorStack();
                if (!stack.isEmpty()) {
                    CombinedItemStack rem = accessor.insertStack(new CombinedItemStack(stack));
                    ItemStack itemstack = rem == null ? ItemStack.EMPTY : rem.getActualStack();
                    setCursorStack(itemstack);
                } else {
                    if (clicked == null) return;
                    CombinedItemStack pulled = accessor.takeStack(clicked, clicked.getMaxCount());
                    if (pulled != null) {
                        setCursorStack(pulled.getActualStack());
                    }
                }
            }
            case RIGHT_CLICK -> {
                ItemStack stack = getCursorStack();
                if (clicked == null) return;
                if (shift) {
                    CombinedItemStack pulled = accessor.takeStack(clicked, 1);
                    if (pulled != null) {
                        ItemStack itemstack = pulled.getActualStack();
                        this.insertItem(itemstack, playerInventoryStartIndex, this.slots.size(), true);
                        if (itemstack.getCount() > 0) {
                            accessor.insertOrDrop(itemstack);
                        }
                        player.getInventory().markDirty();
                    }
                } else {
                    if (!stack.isEmpty()) {
                        if (ItemStack.canCombine(stack, clicked.getStack()) && stack.getCount() + 1 <= stack.getMaxCount()) {
                            CombinedItemStack pulled = accessor.takeStack(clicked, 1);
                            if (pulled != null) {
                                stack.increment(1);
                            }
                        }
                    } else {
                        CombinedItemStack pulled = accessor.takeStack(clicked, 1);
                        if (pulled != null) {
                            setCursorStack(pulled.getActualStack());
                        }
                    }
                }
            }
            case COPY -> {
                if (clicked == null) return;
                if (!player.isCreative()) return;
                if (this.getCursorStack().isEmpty()) {
                    this.setCursorStack(clicked.getActualStack(clicked.getMaxCount()));
                }
            }
            case TAKE_ALL -> {
                if (clicked == null) return;
                CombinedItemStack pulled;
                while ((pulled = accessor.takeStack(clicked, clicked.getMaxCount())) != null) {
                    ItemStack itemstack = pulled.getActualStack();
                    this.insertItem(itemstack, playerInventoryStartIndex, this.slots.size(), true);
                    if (itemstack.getCount() > 0) {
                        accessor.insertOrDrop(itemstack);
                        break;
                    }
                }
                player.getInventory().markDirty();
            }
            case QUICK_MOVE -> {
                if (clicked == null) return;
                CombinedItemStack pulled = accessor.takeStack(clicked, clicked.getMaxCount());
                if (pulled != null) {
                    ItemStack itemstack = pulled.getActualStack();
                    this.insertItem(itemstack, playerInventoryStartIndex, this.slots.size(), true);
                    if (itemstack.getCount() > 0) {
                        accessor.insertOrDrop(itemstack);
                    }
                    player.getInventory().markDirty();
                }
            }
        }
    }

    public final void addStorageSlots(int rows, int y) {
        storageSlotList.clear();
        this.rows = rows;
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new StorageSlot(this.accessor, i * 9 + j, this.getSlotLeftX() + j * 18, y + i * 18));
            }
        }
        scrollItems(0.0f);
    }

    protected final void addSlotToContainer(StorageSlot slotStorage) {
        storageSlotList.add(slotStorage);
    }

    public static class StorageSlot {
        public int x;
        public int y;
        private final int index;
        public final StorageAccessorBlockEntity accessor;
        public CombinedItemStack stack;

        public StorageSlot(StorageAccessorBlockEntity accessor, int index, int x, int y) {
            this.x = x;
            this.y = y;
            this.index = index;
            this.accessor = accessor;
        }

        public ItemStack takeItem(long max) {
            if (stack == null || max < 1 || accessor == null) {
                return ItemStack.EMPTY;
            }
            CombinedItemStack itemStack = accessor.takeStack(stack, max);
            return itemStack != null ? itemStack.getActualStack() : ItemStack.EMPTY;
        }

        public ItemStack insertItem(ItemStack stack) {
            if (accessor == null) return stack;
            CombinedItemStack itemStack = accessor.insertStack(new CombinedItemStack(stack));
            if (itemStack != null) {
                return itemStack.getActualStack();
            } else {
                return ItemStack.EMPTY;
            }
        }

        public int getSlotIndex() {
            return index;
        }
    }

    public record SlotData(Slot slot, int x, int y) {

        public SlotData(Slot slot) {
            this(slot, slot.x, slot.y);
        }

        public void setOffset(int x, int y) {
            ((SlotAccessor) slot).setX(this.x + x);
            ((SlotAccessor) slot).setY(this.y + y);
        }
    }
}
