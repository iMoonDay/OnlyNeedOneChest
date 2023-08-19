package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.screen.StorageProcessorScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class StorageProcessorBlockEntity extends StorageAccessorBlockEntity {
    public StorageProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.STORAGE_PROCESSOR_BLOCK_ENTITY, pos, state);
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new StorageProcessorScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos), this);
    }
}
