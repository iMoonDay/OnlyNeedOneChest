package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.api.ConnectBlock;
import com.imoonday.on1chest.api.ConnectBlockConverter;
import com.imoonday.on1chest.api.ConnectInventoryProvider;
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
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StorageAccessorBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {

    private static final List<MultiInventory.InsertionPredicate> INSERTION_PREDICATES = new ArrayList<>();
    private static final List<MultiInventory.RemovalPredicate> REMOVAL_PREDICATES = new ArrayList<>();
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
        entity.inventory.clear();
        entity.getAllMemories(world, pos).forEach(entity.inventory::add);
        entity.inventory.refresh();
        if (entity.updateItems) {
            entity.items.clear();
            IntStream.range(0, entity.inventory.size())
                    .mapToObj(entity.inventory::getStack)
                    .filter(s -> !s.isEmpty())
                    .map(CombinedItemStack::new)
                    .forEach(s -> entity.items.merge(s, s.getCount(), Long::sum));
            entity.updateItems = false;
        }
    }

    private static boolean canInsert(Inventory inventory, int slot, ItemStack stack) {
        return INSERTION_PREDICATES.stream().allMatch(predicate -> predicate.canInsert(inventory, slot, stack));
    }

    private static boolean canRemove(Inventory inventory, int slot) {
        return REMOVAL_PREDICATES.stream().allMatch(predicate -> predicate.canRemove(inventory, slot));
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
            if (state.getBlock() instanceof StorageMemoryBlock && state.get(StorageMemoryBlock.ACTIVATED) && blockEntity instanceof StorageMemoryBlockEntity entity) {
                return entity;
            } else if (ConnectBlockConverter.isConverted(world1, pos1) && blockEntity instanceof Inventory inventory) {
                return inventory;
            } else if (blockEntity instanceof ConnectInventoryProvider provider) {
                return provider.getInventory();
            }
            return null;
        }).filter(Objects::nonNull).limit(limit).collect(Collectors.toCollection(ArrayList::new));
    }

    public MultiInventory getInventory() {
        return inventory;
    }

    public Map<CombinedItemStack, Long> getStacks() {
        updateItems = true;
        return items;
    }

    public int getFreeSlotCount() {
        int empty = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isEmpty()) empty++;
        }
        return empty;
    }

    public CombinedItemStack takeStack(CombinedItemStack stack, long max) {
        if (stack != null && inventory != null && max > 0) {
            ItemStack st = stack.getStack();
            CombinedItemStack cis = null;
            for (int i = inventory.size() - 1; i >= 0; i--) {
                ItemStack s = inventory.getStack(i);
                if (ItemStack.canCombine(s, st)) {
                    ItemStack pulled = inventory.removeStack(i, (int) max);
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
        if (stack != null && inventory != null) {
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

    public static void addInsertionPredicate(MultiInventory.InsertionPredicate insertionPredicate) {
        INSERTION_PREDICATES.add(insertionPredicate);
    }

    public static void addRemovalPredicate(MultiInventory.RemovalPredicate removalPredicate) {
        REMOVAL_PREDICATES.add(removalPredicate);
    }
}
