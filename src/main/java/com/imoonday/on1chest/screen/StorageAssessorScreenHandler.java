package com.imoonday.on1chest.screen;

import com.imoonday.on1chest.blocks.StorageAccessorBlock;
import com.imoonday.on1chest.blocks.entities.StorageAccessorBlockEntity;
import com.imoonday.on1chest.blocks.entities.StorageMemoryBlockEntity;
import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.mixin.ScreenHandlerInvoker;
import com.imoonday.on1chest.network.NetworkHandler;
import com.imoonday.on1chest.utils.ChineseUtils;
import com.imoonday.on1chest.utils.IScreenDataReceiver;
import com.imoonday.on1chest.utils.ItemStackFilter;
import com.imoonday.on1chest.utils.SortComparator;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ClickType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class StorageAssessorScreenHandler extends AbstractRecipeScreenHandler<RecipeInputInventory> implements IScreenDataReceiver {

    public final int slotStartX;
    public final int slotStartY;
    public final int rows;
    public int inventoryStartIndex;
    public int playerInventoryStartIndex;
    public final PlayerEntity player;
    public final ScreenHandlerContext context;
    public DefaultedList<ItemStack> itemList = DefaultedList.of();
    public List<Inventory> inventories = new ArrayList<>();
    public boolean isPressingAlt;
    public boolean isPressingCtrl;
    public final MemoryInventory inventory;
    private final Set<ChunkPos> forceLoadingPoses = new HashSet<>();

    public StorageAssessorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public StorageAssessorScreenHandler(ScreenHandlerType<? extends StorageAssessorScreenHandler> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, int slotStartX, int slotStartY, int rows) {
        super(type, syncId);
        this.player = playerInventory.player;
        this.context = context;
        this.slotStartX = slotStartX;
        this.slotStartY = slotStartY;
        this.rows = rows;
        this.inventory = new MemoryInventory(rows * 9);
        updateItemList();
        this.context.run((world, pos) -> {
            if (world instanceof ServerWorld serverWorld) {
                tryForceLoadChunk(serverWorld, pos);
                this.inventories.stream().filter(inventory -> inventory instanceof StorageMemoryBlockEntity).map(inventory -> (StorageMemoryBlockEntity) inventory).forEach(entity -> tryForceLoadChunk(serverWorld, entity.getPos()));
            }
        });
    }

    private void tryForceLoadChunk(ServerWorld serverWorld, BlockPos blockPos) {
        ChunkPos chunkPos;
        chunkPos = new ChunkPos(blockPos);
        if (!serverWorld.getForcedChunks().contains(chunkPos.toLong())) {
            serverWorld.setChunkForced(chunkPos.x, chunkPos.z, true);
            this.forceLoadingPoses.add(chunkPos);
        }
    }

    public StorageAssessorScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        this(ModScreens.STORAGE_ASSESSOR_SCREEN_HANDLER, syncId, playerInventory, context, 27, 18, 6);
        this.addInventorySlots();
        this.addPlayerInventorySlots(playerInventory, 27, 103, 27, 161);
        this.scrollItems(0.0f);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (!this.forceLoadingPoses.isEmpty()) {
            this.context.run((world, pos) -> {
                if (world instanceof ServerWorld serverWorld) {
                    this.forceLoadingPoses.forEach(chunkPos -> serverWorld.setChunkForced(chunkPos.x, chunkPos.z, false));
                }
            });
        }
    }

    public static void update(World world) {
        if (world instanceof ServerWorld serverWorld) {
            PlayerLookup.all(serverWorld.getServer()).stream().filter(player -> player.currentScreenHandler instanceof StorageAssessorScreenHandler).map(player -> (StorageAssessorScreenHandler) player.currentScreenHandler).forEach(StorageAssessorScreenHandler::updateItemList);
        }
    }

    protected void addInventorySlots() {
        this.inventoryStartIndex = this.slots.size();
        for (int j = 0; j < rows; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new MemorySlot(inventory, k + j * 9, slotStartX + k * 18, slotStartY + j * 18));
            }
        }
    }

    protected void addPlayerInventorySlots(PlayerInventory playerInventory, int x1, int y1, int x2, int y2) {
        this.playerInventoryStartIndex = this.slots.size();
        for (int j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, x1 + k * 18, y1 + j * 18 + 36 + 1));
            }
        }
        for (int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, x2 + j * 18, y2 + 36 + 1));
        }
    }

    public void updateFavouriteCount() {
        NetworkHandler.sendToClient(player, nbtCompound -> nbtCompound.putInt("favouriteCount", getFavouriteCount()));
    }

    public List<ItemStack> getDisplayItemStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        getSettings().ifPresent(settings -> this.itemList.stream().filter(stack -> !stack.isEmpty() && isFilterPassed(stack, settings.nameFilter) && settings.getStackFilters().stream().allMatch(filter -> filter.getPredicate().test(stack))).sorted(settings.getComparator().getComparator(this.getFavouriteStacks(), settings.isReversed()).thenComparing(ItemStack::isEmpty)).forEach(stacks::add));
        return stacks;
    }

    public static boolean isFilterPassed(ItemStack stack, String filter) {
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
        return MathHelper.ceilDiv((int) getDisplayItemStacks().stream().filter(stack -> !stack.isEmpty()).count(), 9) - rows;
    }

    public int getRow(float scroll) {
        return Math.max((int) ((double) (scroll * (float) this.getOverflowRows()) + 0.5), 0);
    }

    public float getScrollPosition(float current, double amount) {
        if (getDisplayItemStacks().size() <= inventory.size()) {
            return 0.0f;
        }
        return MathHelper.clamp(current - (float) (amount / (double) this.getOverflowRows()), 0.0f, 1.0f);
    }

    public void updateItems() {
        NetworkHandler.sendToClient(player, nbtCompound -> nbtCompound.putInt("updateItems", getFavouriteCount()));
    }

    public void scrollItems(float position) {
        int i = this.getRow(position);
        List<ItemStack> stacks = getDisplayItemStacks();
        for (int j = 0; j < rows; ++j) {
            for (int k = 0; k < 9; ++k) {
                int l = k + (j + i) * 9;
                if (l >= 0 && l < stacks.size()) {
                    ItemStack stack = stacks.get(l);
                    inventory.setStack(k + j * 9, stack);
                    continue;
                }
                inventory.setStack(k + j * 9, ItemStack.EMPTY);
            }
        }
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        if (player.getWorld().isClient) {
            return ItemStack.EMPTY;
        }
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack().copy();
            newStack = originalStack.copy();
            if (invSlot >= inventoryStartIndex && invSlot < playerInventoryStartIndex) {
                if (this.insertItem(originalStack, playerInventoryStartIndex, playerInventoryStartIndex + 35, true)) {
                    this.removeStack(newStack, slot);
                } else {
                    return ItemStack.EMPTY;
                }
            } else if (invSlot >= playerInventoryStartIndex && this.canInsert(originalStack)) {
                this.addStack(originalStack, slot);
            } else {
                return ItemStack.EMPTY;
            }

            int count = slot.getStack().getCount();
            if (originalStack.isEmpty() && count <= slot.getStack().getMaxCount()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                newStack = originalStack;
                slot.setStack(newStack);
                slot.markDirty();
            }
            updateItemList();
        }

        return newStack;
    }

    @Override
    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        ItemStack itemStack;
        Slot slot;
        boolean bl = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }
        if (stack.isStackable()) {
            while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
                slot = this.slots.get(i);
                itemStack = slot.getStack();
                if (!itemStack.isEmpty() && ItemStack.canCombine(stack, itemStack)) {
                    int j = itemStack.getCount() + stack.getCount();
                    if (j <= stack.getMaxCount()) {
                        stack.setCount(0);
                        itemStack.setCount(j);
                        slot.markDirty();
                        bl = true;
                    } else if (itemStack.getCount() < stack.getMaxCount()) {
                        stack.decrement(stack.getMaxCount() - itemStack.getCount());
                        itemStack.setCount(stack.getMaxCount());
                        slot.markDirty();
                        bl = true;
                    }
                }
                if (fromLast) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        if (!stack.isEmpty()) {
            i = fromLast ? endIndex - 1 : startIndex;
            while (fromLast ? i >= startIndex : i < endIndex) {
                slot = this.slots.get(i);
                itemStack = slot.getStack();
                if (itemStack.isEmpty() && slot.canInsert(stack)) {
                    if (stack.getCount() > stack.getMaxCount()) {
                        slot.setStack(stack.split(stack.getMaxCount()));
                    } else {
                        slot.setStack(stack.split(stack.getCount()));
                    }
                    slot.markDirty();
                    bl = true;
                    if (stack.isEmpty() || !isPressingCtrl) {
                        break;
                    }
                }
                if (fromLast) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        return bl;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return context.get((world, pos) -> world.getBlockState(pos).getBlock() instanceof StorageAccessorBlock, true);
    }

    public void updateItemList() {
        context.run((world, pos) -> {
            if (world.getBlockEntity(pos) instanceof StorageAccessorBlockEntity entity) {
                this.itemList = entity.createItemList(world, pos);
                this.inventories = entity.getAllInventories(world, pos);
//                if (!this.slots.isEmpty()) {
//                    IntStream.range(this.inventoryStartIndex, this.inventoryStartIndex + this.inventory.size()).filter(i -> this.slots.get(i) instanceof MemorySlot).mapToObj(i -> (MemorySlot) this.slots.get(i)).forEach(MemorySlot::updateActualInventories);
//                }
            }
            this.updateItems();
        });
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (actionType == null) {
            return;
        }
        if (slotIndex >= inventoryStartIndex + inventory.size() || slotIndex < inventoryStartIndex || !(this.slots.get(slotIndex) instanceof MemorySlot slot)) {
            super.onSlotClick(slotIndex, button, actionType, player);
            return;
        }
        ClickType clickType = button == 0 ? ClickType.LEFT : ClickType.RIGHT;
        switch (actionType) {
            case QUICK_MOVE -> {
                if (slot.canTakeItems(player)) {
                    isPressingCtrl = false;
                    this.quickMove(player, slotIndex);
                    slot.markDirty();
                    return;
                }
            }
            case PICKUP -> {
                ItemStack slotStack = slot.getStack();
                if (isPressingAlt) {
                    if (clickType == ClickType.LEFT) {
                        addFavouriteStack(slotStack);
                    } else {
                        removeFavouriteStack(slotStack);
                    }
                } else if (isPressingCtrl) {
                    if (slot.canTakeItems(player)) {
                        this.quickMove(player, slotIndex);
                        slot.markDirty();
                        return;
                    }
                } else {
                    ItemStack cursorStack = this.getCursorStack();
                    player.onPickupSlotClick(cursorStack, slot.getStack(), clickType);
                    if (!(((ScreenHandlerInvoker) this).invokeHandleSlotClick(player, clickType, slot, slotStack, cursorStack))) {
                        if (slotStack.isEmpty()) {
                            if (!cursorStack.isEmpty()) {
                                int o = clickType == ClickType.LEFT ? cursorStack.getCount() : 1;
                                this.setCursorStack(slot.insertStack(cursorStack, o));
                            }
                        } else if (slot.canTakeItems(player)) {
                            if (cursorStack.isEmpty()) {
                                int count = clickType == ClickType.LEFT ? Math.min(slotStack.getCount(), slotStack.getMaxCount()) : 1;
                                Optional<ItemStack> optional = slot.tryTakeStackRange(count, Integer.MAX_VALUE, player);
                                optional.ifPresent(stack -> {
                                    this.setCursorStack(stack);
                                    slot.onTakeItem(player, stack);
                                });
                            } else {
                                if (clickType == ClickType.LEFT) {
                                    if (slot.canInsert(cursorStack)) {
                                        this.setCursorStack(slot.insertStack(cursorStack));
                                    } else if (ItemStack.canCombine(slotStack, cursorStack)) {
                                        Optional<ItemStack> optional2 = slot.tryTakeStackRange(slotStack.getCount(), cursorStack.getMaxCount() - cursorStack.getCount(), player);
                                        optional2.ifPresent(stack -> {
                                            cursorStack.increment(stack.getCount());
                                            slot.onTakeItem(player, stack);
                                        });
                                    }
                                } else {
                                    if (ItemStack.canCombine(slotStack, cursorStack)) {
                                        Optional<ItemStack> optional2 = slot.tryTakeStackRange(1, cursorStack.getMaxCount() - cursorStack.getCount(), player);
                                        optional2.ifPresent(stack -> {
                                            cursorStack.increment(stack.getCount());
                                            slot.onTakeItem(player, stack);
                                        });
                                    } else if (slot.canInsert(cursorStack)) {
                                        this.setCursorStack(slot.insertStack(cursorStack, 1));
                                    }
                                }
                            }
                        }
                    }
                }
                slot.markDirty();
                return;
            }
            case PICKUP_ALL -> {
                return;
            }
            case SWAP -> {
                ItemStack slotStack = slot.getStack();
                PlayerInventory playerInventory = player.getInventory();
                ItemStack inventoryStack = playerInventory.getStack(button);
                if (inventoryStack.isEmpty() && slotStack.isEmpty()) {
                    return;
                }
                if (!inventoryStack.isEmpty()) {
                    slot.insertStack(inventoryStack);
                    slot.markDirty();
                }
                if (!slot.canTakeItems(player)) {
                    return;
                }
                int count = Math.min(slotStack.getCount(), slotStack.getMaxCount());
                ItemStack swapStack = slotStack.copyWithCount(count);
                playerInventory.setStack(button, swapStack);
                slot.onTake(count);
                slot.setStack(ItemStack.EMPTY);
                slot.onTakeItem(player, swapStack);
                return;
            }
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public void receive(NbtCompound nbt) {
        boolean updateItems = true;

        if (nbt.contains("nameFilter", NbtElement.STRING_TYPE)) {
            getSettings().ifPresent(settings -> settings.setNameFilter(nbt.getString("nameFilter")));
        }

        if (nbt.contains("comparator", NbtElement.BYTE_TYPE)) {
            getSettings().ifPresent(settings -> {
                settings.nextComparator();
                if (nbt.getBoolean("comparator")) {
                    NetworkHandler.sendToClient(player, nbtCompound -> nbtCompound.putInt("comparator", settings.getComparator().ordinal()));
                }
            });
        }

        if (nbt.contains("addStackFilter", NbtElement.INT_TYPE)) {
            int index = nbt.getInt("addStackFilter");
            if (index >= 0 && index < ItemStackFilter.values().length) {
                getSettings().ifPresent(settings -> settings.getStackFilters().add(ItemStackFilter.values()[index]));
            }
        }

        if (nbt.contains("removeStackFilter", NbtElement.INT_TYPE)) {
            int index = nbt.getInt("removeStackFilter");
            if (index >= 0 && index < ItemStackFilter.values().length) {
                getSettings().ifPresent(settings -> settings.getStackFilters().remove(ItemStackFilter.values()[index]));
            }
        }

        if (nbt.contains("scrollPosition", NbtElement.FLOAT_TYPE)) {
            float scrollPosition = nbt.getFloat("scrollPosition");
            scrollItems(scrollPosition);
            updateFavouriteCount();
            updateItems = false;
        }

        if (nbt.contains("reversed")) {
            getSettings().ifPresent(Settings::reverse);
        }

        if (nbt.contains("getScrollPosition", NbtElement.COMPOUND_TYPE)) {
            NbtCompound nbtCompound = nbt.getCompound("getScrollPosition");
            if (nbtCompound.contains("scrollPosition", NbtElement.FLOAT_TYPE) && nbtCompound.contains("amount", NbtElement.DOUBLE_TYPE)) {
                float scrollPosition = nbtCompound.getFloat("scrollPosition");
                double amount = nbtCompound.getDouble("amount");
                NetworkHandler.sendToClient(player, nbtCompound1 -> nbtCompound1.putFloat("scrollPosition", this.getScrollPosition(scrollPosition, amount)));
            }
        }

        if (nbt.contains("Alt", NbtElement.BYTE_TYPE)) {
            this.isPressingAlt = nbt.getBoolean("Alt");
        }

        if (nbt.contains("Ctrl", NbtElement.BYTE_TYPE)) {
            this.isPressingCtrl = nbt.getBoolean("Ctrl");
        }

        if (nbt.contains("init")) {
            getSettings().ifPresent(settings -> NetworkHandler.sendToClient(player, nbtCompound -> {
                nbtCompound.putInt("comparator", settings.getComparator().ordinal());
                nbtCompound.putIntArray("stackFilters", settings.getStackFilters().stream().map(Enum::ordinal).collect(Collectors.toList()));
                nbtCompound.putInt("favouriteCount", getFavouriteCount());
                if (settings.nameFilter != null) {
                    nbtCompound.putString("nameFilter", settings.nameFilter);
                }
            }));
        }

        if (updateItems) {
            this.updateItemList();
        }
    }

    public Optional<Settings> getSettings() {
        return context.get((world, pos) -> world.getBlockEntity(pos) instanceof StorageAccessorBlockEntity entity ? entity.getScreenSettings(player.getUuid()) : null);
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

    public class MemorySlot extends Slot {

        private final Set<Inventory> actualInventories = new HashSet<>();

        public MemorySlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        public Set<Inventory> getActualInventories() {
            return actualInventories;
        }

        public void updateActualInventories() {
            actualInventories.clear();
            inventories.stream().filter(inventory -> inventory.containsAny(stack -> ItemStack.canCombine(stack, this.getStack()))).forEach(actualInventories::add);
        }

        public void addActualInventory(Inventory inventory) {
            this.actualInventories.add(inventory);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return StorageAssessorScreenHandler.this.canInsert(stack);
        }

        @Override
        public ItemStack insertStack(ItemStack stack, int count) {
            return addStack(stack, count, this);
        }

        @Override
        public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
            return player.getWorld().isClient ? Optional.empty() : super.tryTakeStackRange(min, max, player);
        }

        @Override
        protected void onTake(int amount) {
            super.onTake(amount);
        }

        @Override
        public ItemStack takeStack(int amount) {
            int maxCount = this.getStack().getMaxCount();
            if (amount > maxCount) {
                amount = maxCount;
            }
            return super.takeStack(amount);
        }

        @Override
        public void setStack(ItemStack stack) {
            removeStack(this.getStack(), this);
            insertStack(stack);
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            if (!player.getWorld().isClient) {
                removeStack(stack, this);
            }
            super.onTakeItem(player, stack);
        }

        @Override
        public void markDirty() {
            super.markDirty();
            updateItemList();
        }
    }

    public boolean canInsert(ItemStack stack) {
        return context.get((world, pos) -> world.getBlockEntity(pos) instanceof StorageAccessorBlockEntity entity && entity.canInsert(world, pos, stack), false);
    }

    public void removeStack(ItemStack stack, Slot slot) {
        removeStack(stack, stack.getCount(), slot);
    }

    public void removeStack(ItemStack stack, int removeCount, Slot slot) {
        context.run((world, pos) -> {
            if (world.getBlockEntity(pos) instanceof StorageAccessorBlockEntity entity && entity.removeStack(world, pos, stack, removeCount, slot)) {
                updateItemList();
            }
        });
    }

    public ItemStack addStack(ItemStack stack, Slot slot) {
        return addStack(stack, stack.getCount(), slot);
    }

    public ItemStack addStack(ItemStack stack, int count, Slot slot) {
        context.run((world, pos) -> {
            if (world.getBlockEntity(pos) instanceof StorageAccessorBlockEntity entity && entity.addStack(world, pos, stack, count, slot)) {
                updateItemList();
            }
        });
        return stack;
    }

    public void addFavouriteStack(ItemStack stack) {
        if (context.get((world, pos) -> world.getBlockEntity(pos) instanceof StorageAccessorBlockEntity entity && entity.addFavouriteStack(player.getUuid(), stack.copyWithCount(1)), false)) {
            updateFavouriteCount();
        }
    }

    public void removeFavouriteStack(ItemStack stack) {
        if (context.get((world, pos) -> world.getBlockEntity(pos) instanceof StorageAccessorBlockEntity entity && entity.removeFavouriteStack(player.getUuid(), stack.copyWithCount(1)), false)) {
            updateFavouriteCount();
        }
    }

    public Set<ItemStack> getFavouriteStacks() {
        return context.get((world, pos) -> world.getBlockEntity(pos) instanceof StorageAccessorBlockEntity entity ? entity.getFavouriteStacks(player.getUuid()) : new HashSet<>(), new HashSet<>());
    }

    public int getFavouriteCount() {
        return getFavouriteStacks().stream().filter(stack -> inventory.containsAny(stack1 -> ItemStack.canCombine(stack, stack1))).toList().size();
    }

    public class MemoryInventory extends SimpleInventory {

        public MemoryInventory(int size) {
            super(size);
        }

        @Override
        public int getMaxCountPerStack() {
            return Integer.MAX_VALUE;
        }

    }

    public static class Settings {
        private Set<ItemStack> favouriteStacks;
        private Set<ItemStackFilter> stackFilters;
        private SortComparator comparator;
        private boolean reversed;
        private String nameFilter;

        public Settings() {
            this(new HashSet<>(), new HashSet<>(), SortComparator.ID, false, null);
        }

        public Settings(ScreenHandlerContext context, PlayerEntity player) {
            this(context.get((world, pos) -> world.getBlockEntity(pos) instanceof StorageAccessorBlockEntity entity ? entity.getScreenSettings(player.getUuid()) : new Settings(), new Settings()));
        }

        public Settings(Set<ItemStack> favouriteStacks, Set<ItemStackFilter> stackFilters, SortComparator comparator, boolean reversed, String nameFilter) {
            this.favouriteStacks = favouriteStacks;
            this.stackFilters = stackFilters;
            this.setComparator(comparator);
            this.setReversed(reversed);
            this.setNameFilter(nameFilter);
        }

        private Settings(Settings settings) {
            this.favouriteStacks = settings.getFavouriteStacks();
            this.stackFilters = settings.getStackFilters();
            this.setNameFilter(settings.getNameFilter());
            this.setComparator(settings.getComparator());
            this.setReversed(settings.isReversed());
        }

        public Set<ItemStack> getFavouriteStacks() {
            return favouriteStacks;
        }

        public Set<ItemStackFilter> getStackFilters() {
            return stackFilters;
        }

        public SortComparator getComparator() {
            return comparator;
        }

        public void setComparator(SortComparator comparator) {
            this.comparator = comparator;
        }

        public void nextComparator() {
            this.comparator = this.comparator.next();
        }

        public boolean isReversed() {
            return reversed;
        }

        public void setReversed(boolean reversed) {
            this.reversed = reversed;
        }

        public void reverse() {
            this.setReversed(!this.isReversed());
        }

        public String getNameFilter() {
            return nameFilter;
        }

        public void setNameFilter(String nameFilter) {
            this.nameFilter = nameFilter;
        }

        public NbtCompound toNBT() {
            NbtCompound nbtCompound = new NbtCompound();
            NbtList favouriteStackList = new NbtList();
            this.favouriteStacks.forEach(stack -> favouriteStackList.add(stack.writeNbt(new NbtCompound())));
            nbtCompound.put("favouriteStackList", favouriteStackList);
            nbtCompound.putIntArray("stackFilters", this.stackFilters.stream().map(Enum::ordinal).collect(Collectors.toList()));
            if (this.nameFilter != null) {
                nbtCompound.putString("nameFilter", this.nameFilter);
            }
            nbtCompound.putBoolean("reversed", this.reversed);
            nbtCompound.putInt("comparator", this.comparator.ordinal());
            return nbtCompound;
        }

        public Settings(NbtCompound nbtCompound) {
            if (nbtCompound.contains("favouriteStackList", NbtElement.LIST_TYPE)) {
                this.favouriteStacks = nbtCompound.getList("favouriteStackList", NbtElement.COMPOUND_TYPE).stream().map(element -> ItemStack.fromNbt(((NbtCompound) element))).filter(stack -> stack != null && !stack.isEmpty()).collect(Collectors.toSet());
            }
            if (nbtCompound.contains("stackFilters", NbtElement.INT_ARRAY_TYPE)) {
                this.stackFilters = Arrays.stream(nbtCompound.getIntArray("stackFilters")).filter(value -> value >= 0 && value < ItemStackFilter.values().length).mapToObj(i -> ItemStackFilter.values()[i]).collect(Collectors.toSet());
            }
            if (nbtCompound.contains("nameFilter", NbtElement.STRING_TYPE)) {
                this.nameFilter = nbtCompound.getString("nameFilter");
            }
            if (nbtCompound.contains("reversed", NbtElement.BYTE_TYPE)) {
                this.reversed = nbtCompound.getBoolean("reversed");
            }
            if (nbtCompound.contains("comparator", NbtElement.INT_TYPE)) {
                int index = nbtCompound.getInt("comparator");
                if (index >= 0 && index < SortComparator.values().length) {
                    this.comparator = SortComparator.values()[index];
                }
            }
        }
    }
}
