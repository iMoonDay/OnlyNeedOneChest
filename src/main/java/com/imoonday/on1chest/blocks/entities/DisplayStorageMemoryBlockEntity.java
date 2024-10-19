package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.api.PrioritizedInventory;
import com.imoonday.on1chest.init.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DisplayStorageMemoryBlockEntity extends GlassStorageMemoryBlockEntity implements PrioritizedInventory {

    public DisplayStorageMemoryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISPLAY_STORAGE_MEMORY_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, DisplayStorageMemoryBlockEntity entity) {
        StorageMemoryBlockEntity.tick(world, pos, state, entity);
        int count = entity.getItems().stream().filter(itemStack -> ItemStack.canCombine(entity.displayItem, itemStack)).mapToInt(ItemStack::getCount).sum();
        if (count <= 0) {
            count = 1;
        }
        if (entity.displayItem.getCount() != count) {
            entity.displayItem.setCount(count);
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }
    }

    public boolean updateDisplayItem(ItemStack stack) {
        if (ItemStack.canCombine(this.displayItem, stack) || this.containsAny(itemStack -> (!itemStack.isEmpty() || stack.isEmpty()) && !ItemStack.canCombine(itemStack, stack))) {
            return false;
        }
        this.displayItem = stack.copyWithCount(1);
        if (world != null) {
            BlockState state = getCachedState();
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }
        this.markDirty();
        return true;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return isDisplayingItem(stack);
    }

    @Override
    public boolean isPrioritizedFor(ItemStack stack) {
        return isDisplayingItem(stack);
    }

    public boolean isDisplayingItem(ItemStack stack) {
        return ItemStack.canCombine(this.displayItem, stack);
    }
}
