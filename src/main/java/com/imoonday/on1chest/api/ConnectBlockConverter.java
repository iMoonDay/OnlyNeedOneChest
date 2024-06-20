package com.imoonday.on1chest.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ConnectBlockConverter {

    BlockPos getConvertedPos(World world, BlockPos pos, BlockState state);

    boolean isActive(World world, BlockPos pos, BlockState state);
}
