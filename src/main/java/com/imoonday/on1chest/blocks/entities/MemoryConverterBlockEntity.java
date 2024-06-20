package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.api.ConnectBlockConverter;
import com.imoonday.on1chest.api.ConnectInventoryProvider;
import com.imoonday.on1chest.blocks.MemoryConverterBlock;
import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MemoryConverterBlockEntity extends BlockEntity implements ConnectInventoryProvider {

    public MemoryConverterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MEMORY_CONVERTER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, MemoryConverterBlockEntity entity) {
        if (world.isClient) return;
        StorageMemoryBlock.UsedCapacity usedCapacity = entity.getUsedCapacity();
        StorageMemoryBlock.UsedCapacity usedCapacity1 = state.get(MemoryConverterBlock.USED_CAPACITY);
        if (usedCapacity != usedCapacity1) {
            world.setBlockState(pos, state.with(MemoryConverterBlock.USED_CAPACITY, usedCapacity), Block.NOTIFY_LISTENERS);
        }
    }

    @Nullable
    public Inventory getInventory() {
        if (world == null) return null;
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ConnectBlockConverter converter && converter.isActive(world, pos, state)) {
            BlockPos blockPos = converter.getConvertedPos(world, pos, state);
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.getBlock() instanceof ChestBlock block) {
                Inventory inventory = ChestBlock.getInventory(block, blockState, world, blockPos, true);
                if (inventory != null) {
                    return inventory;
                }
            }
            if (world.getBlockEntity(blockPos) instanceof Inventory inventory) {
                return inventory;
            }
        }
        return null;
    }

    public StorageMemoryBlock.UsedCapacity getUsedCapacity() {
        Inventory inventory = getInventory();
        if (inventory == null) {
            return StorageMemoryBlock.UsedCapacity.ZERO;
        }
        int size = inventory.size();
        int occupied = 0;
        for (int i = 0; i < size; i++) {
            if (!inventory.getStack(i).isEmpty()) {
                occupied++;
            }
        }
        if (occupied <= 0) {
            return StorageMemoryBlock.UsedCapacity.ZERO;
        } else if (occupied < size / 2) {
            return StorageMemoryBlock.UsedCapacity.LOW;
        } else if (occupied < size) {
            return StorageMemoryBlock.UsedCapacity.HIGH;
        } else {
            return StorageMemoryBlock.UsedCapacity.FULL;
        }
    }
}
