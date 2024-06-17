package com.imoonday.on1chest.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Arrays;

public interface ConnectBlockConverter {

    Direction getConvertingDirection(World world, BlockPos pos, BlockState state);

    boolean isActive(World world, BlockPos pos, BlockState state);

    static boolean isConverted(World world, BlockPos pos) {
        return Arrays.stream(Direction.values()).anyMatch(direction -> {
            BlockState state = world.getBlockState(pos.offset(direction));
            return state.getBlock() instanceof ConnectBlockConverter converter && converter.isActive(world, pos, state) && converter.getConvertingDirection(world, pos, state).getOpposite() == direction;
        });
    }
}
