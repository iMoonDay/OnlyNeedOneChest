package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlockEntities;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        super(ModBlockEntities.STORAGE_ACCESSOR_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, StorageAccessorBlockEntity blockEntity) {
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
        return Text.translatable(this.getCachedState().getBlock().getTranslationKey() + ".title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new StorageAssessorScreenHandler(syncId, playerInventory, this);
    }

    public List<Inventory> getAllInventories(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return List.of();
        }
        int limit = world.getGameRules().getInt(ModGameRules.MAX_MEMORY_RANGE);
        return limit <= 0 ? new ArrayList<>() : ConnectBlock.getConnectedBlocks(world, pos).stream().filter(pair -> pair.getLeft().getBlockState(pair.getRight()).getBlock() instanceof StorageMemoryBlock && pair.getLeft().getBlockState(pair.getRight()).get(StorageMemoryBlock.ACTIVATED) && pair.getLeft().getBlockEntity(pair.getRight()) instanceof StorageMemoryBlockEntity).map(pair -> (StorageMemoryBlockEntity) pair.getLeft().getBlockEntity(pair.getRight())).limit(limit).collect(Collectors.toCollection(ArrayList::new));
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
}
