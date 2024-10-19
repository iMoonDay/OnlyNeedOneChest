package com.imoonday.on1chest.blocks.memories;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.blocks.entities.GlassStorageMemoryBlockEntity;
import com.imoonday.on1chest.init.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GlassStorageMemoryBlock extends StorageMemoryBlock {

    public GlassStorageMemoryBlock(Settings settings) {
        super(settings);
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GlassStorageMemoryBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, ModBlockEntities.GLASS_STORAGE_MEMORY_BLOCK_ENTITY, GlassStorageMemoryBlockEntity::tick);
    }
}
