package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModGameRules;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import com.imoonday.on1chest.utils.CombinedItemStack;
import com.imoonday.on1chest.utils.ConnectBlock;
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
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StorageAccessorBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {

    private final MultiInventory inventory = new MultiInventory();
    private final Map<CombinedItemStack, Long> items = new HashMap<>();
    private boolean updateItems = true;

    public StorageAccessorBlockEntity(BlockEntityType<? extends StorageAccessorBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public StorageAccessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.STORAGE_ACCESSOR_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, StorageAccessorBlockEntity blockEntity) {
        if (world.isClient) {
            return;
        }
        blockEntity.inventory.clear();
        blockEntity.getAllInventories(world, pos).forEach(blockEntity.inventory::add);
        blockEntity.inventory.refresh();
        if (blockEntity.updateItems) {
            blockEntity.items.clear();
            IntStream.range(0, blockEntity.inventory.size())
                    .mapToObj(blockEntity.inventory::getStack)
                    .filter(s -> !s.isEmpty())
                    .map(CombinedItemStack::new)
                    .forEach(s -> blockEntity.items.merge(s, s.getCount(), Long::sum));
            blockEntity.updateItems = false;
        }
    }

    @Override
    public Text getDisplayName() {
        return this.getCachedState().getBlock().getName();
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new StorageAssessorScreenHandler(syncId, playerInventory, this);
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
                if ((adjacentState.getBlock() instanceof ConnectBlock) && !result.contains(adjacentPos)) {
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
        int limit = world.getGameRules().getInt(ModGameRules.MAX_MEMORY_RANGE);
        return limit <= 0 ? new ArrayList<>() : getConnectedBlocks(world, pos).stream().filter(blockPos -> world.getBlockEntity(blockPos) instanceof StorageMemoryBlockEntity).map(blockPos -> (StorageMemoryBlockEntity) world.getBlockEntity(blockPos)).limit(limit).collect(Collectors.toCollection(ArrayList::new));
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

    public CombinedItemStack insertStack(CombinedItemStack stack) {
        if (stack != null && inventory != null) {
            ItemStack stack1 = stack.getActualStack();
            ItemStack itemStack = inventory.insertItem(stack1);
            if (itemStack.isEmpty()) {
                return null;
            } else {
                return new CombinedItemStack(itemStack);
            }
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
}
