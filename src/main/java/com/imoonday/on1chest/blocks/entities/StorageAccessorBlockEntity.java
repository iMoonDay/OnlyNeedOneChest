package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.api.ConnectBlock;
import com.imoonday.on1chest.api.ConnectInventoryProvider;
import com.imoonday.on1chest.api.IgnoredInventory;
import com.imoonday.on1chest.api.StorageAccessorEvent;
import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.init.ModGameRules;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import com.imoonday.on1chest.utils.CombinedItemStack;
import com.imoonday.on1chest.utils.ItemStack2ObjectMap;
import com.imoonday.on1chest.utils.MultiInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StorageAccessorBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Inventory, IgnoredInventory {

    protected final MultiInventory inventory = new MultiInventory();
    protected final Map<CombinedItemStack, Long> items = new HashMap<>();
    protected boolean updateItems = true;

    public StorageAccessorBlockEntity(BlockEntityType<? extends StorageAccessorBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory.addInsertionPredicate(StorageAccessorBlockEntity::canInsert);
        inventory.addRemovalPredicate(StorageAccessorBlockEntity::canRemove);
    }

    public StorageAccessorBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.STORAGE_ACCESSOR_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, StorageAccessorBlockEntity entity) {
        entity.clear();
        entity.getAllMemories(world, pos).forEach(entity.inventory::add);
        entity.refresh();
        if (entity.updateItems) {
            entity.items.clear();
            IntStream.range(0, entity.size())
                    .mapToObj(entity::getStack)
                    .filter(s -> !s.isEmpty())
                    .map(CombinedItemStack::new)
                    .forEach(s -> entity.items.merge(s, s.getCount(), Long::sum));
            entity.updateItems = false;
        }
    }

    private static boolean canInsert(Inventory inventory, int slot, ItemStack stack) {
        return StorageAccessorEvent.INSERT.invoker().canInsert(inventory, slot, stack);
    }

    private static boolean canRemove(Inventory inventory, int slot) {
        return StorageAccessorEvent.REMOVE.invoker().canRemove(inventory, slot);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(this.getCachedState().getBlock().getTranslationKey() + ".title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new StorageAssessorScreenHandler(syncId, playerInventory, this);
    }

    public List<Inventory> getAllMemories(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return List.of();
        }
        int limit = world.getGameRules().getInt(ModGameRules.MAX_MEMORY_RANGE);
        return limit <= 0 ? new ArrayList<>() : ConnectBlock.getConnectedBlocks(world, pos).stream().map(pair -> {
            World world1 = pair.getLeft();
            BlockPos pos1 = pair.getRight();
            BlockState state = world1.getBlockState(pos1);
            BlockEntity blockEntity = world1.getBlockEntity(pos1);
            if (IgnoredInventory.isIgnored(blockEntity)) return null;
            if (state.getBlock() instanceof StorageMemoryBlock && state.get(StorageMemoryBlock.ACTIVATED) && blockEntity instanceof StorageMemoryBlockEntity entity) {
                return entity;
            } else if (blockEntity instanceof ConnectInventoryProvider provider) {
                Inventory inventory1 = provider.getInventory();
                return IgnoredInventory.isIgnored(inventory1) ? null : inventory1;
            }
            return null;
        }).filter(Objects::nonNull).limit(limit).collect(Collectors.toCollection(ArrayList::new));
    }

    public MultiInventory getInventory() {
        return inventory;
    }

    public boolean hasInventory() {
        return getInventory() != null;
    }

    public Map<CombinedItemStack, Long> getStacks() {
        updateItems = true;
        return items;
    }

    public int getFreeSlotCount() {
        int empty = 0;
        for (int i = 0; i < size(); i++) {
            if (getStack(i).isEmpty()) empty++;
        }
        return empty;
    }

    public CombinedItemStack takeStack(CombinedItemStack stack, long max) {
        if (stack != null && hasInventory() && max > 0) {
            ItemStack st = stack.getStack();
            CombinedItemStack cis = null;
            for (int i = size() - 1; i >= 0; i--) {
                ItemStack s = getStack(i);
                if (ItemStack.canCombine(s, st)) {
                    ItemStack pulled = removeStack(i, (int) max);
                    if (!pulled.isEmpty()) {
                        if (cis == null) {
                            cis = new CombinedItemStack(pulled);
                        } else {
                            cis.increment(pulled.getCount());
                        }
                        max -= pulled.getCount();
                        if (max < 1) break;
                    }
                }
            }
            return cis;
        }
        return null;
    }

    public ItemStack takeStack(ItemStack itemStack) {
        CombinedItemStack stack = takeStack(new CombinedItemStack(itemStack), 1);
        return stack == null ? ItemStack.EMPTY : stack.getActualStack();
    }

    public CombinedItemStack insertStack(CombinedItemStack stack) {
        if (stack != null && hasInventory()) {
            ItemStack stack1 = stack.getActualStack();
            ItemStack itemStack = inventory.insertItem(stack1);
            return itemStack.isEmpty() ? null : new CombinedItemStack(itemStack);
        }
        return stack;
    }

    public ItemStack insertStack(ItemStack itemstack) {
        CombinedItemStack stack = insertStack(new CombinedItemStack(itemstack));
        return stack == null ? ItemStack.EMPTY : stack.getActualStack();
    }

    public void insertOrDrop(ItemStack stack) {
        if (stack.isEmpty()) return;
        CombinedItemStack itemStack = insertStack(new CombinedItemStack(stack));
        if (itemStack != null && world != null) {
            ItemScatterer.spawn(world, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, itemStack.getActualStack());
        }
    }

    public boolean contains(ItemStack2ObjectMap<Integer> stacks) {
        this.updateItems = true;
        ItemStack2ObjectMap<Boolean> stackMap = stacks.map(ItemStack::copyWithCount, (stack, integer) -> false, (stack, aBoolean) -> false);
        for (ItemStack itemStack : stackMap.keySet()) {
            for (Map.Entry<CombinedItemStack, Long> entry : this.items.entrySet()) {
                ItemStack stack = entry.getKey().getStack();
                if (ItemStack.canCombine(itemStack, stack)) {
                    if (entry.getValue() >= itemStack.getCount()) {
                        stackMap.put(itemStack, true);
                    }
                    break;
                }
            }
        }
        return !stackMap.containsValue(false);
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return inventory.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return inventory.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.setStack(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public int getMaxCountPerStack() {
        return inventory.getMaxCountPerStack();
    }

    @Override
    public void onOpen(PlayerEntity player) {
        inventory.onOpen(player);
    }

    @Override
    public void onClose(PlayerEntity player) {
        inventory.onClose(player);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return inventory.isValid(slot, stack);
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return inventory.canTransferTo(hopperInventory, slot, stack);
    }

    @Override
    public int count(Item item) {
        return inventory.count(item);
    }

    @Override
    public boolean containsAny(Set<Item> items) {
        return inventory.containsAny(items);
    }

    @Override
    public boolean containsAny(Predicate<ItemStack> predicate) {
        return inventory.containsAny(predicate);
    }

    @Override
    public void markDirty() {
        inventory.markDirty();
        super.markDirty();
    }

    public void refresh() {
        inventory.refresh();
    }
}
