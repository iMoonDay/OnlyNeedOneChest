package com.imoonday.on1chest.utils;

import com.imoonday.on1chest.blocks.ConnectionCableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public interface Connectable {
    static boolean shouldConnect(WorldAccess world, BlockPos pos, Direction direction) {
        BlockState state = world.getBlockState(pos.offset(direction));
        return state.getBlock() instanceof ConnectBlock connectBlock && connectBlock.canConnect(state, direction.getOpposite());
    }

    default BlockState handleConnection(BlockState state, WorldAccess world, BlockPos pos) {
        return state.with(ConnectionCableBlock.NORTH, shouldConnect(world, pos, Direction.NORTH)).with(ConnectionCableBlock.EAST, shouldConnect(world, pos, Direction.EAST)).with(ConnectionCableBlock.WEST, shouldConnect(world, pos, Direction.WEST)).with(ConnectionCableBlock.SOUTH, shouldConnect(world, pos, Direction.SOUTH)).with(ConnectionCableBlock.UP, shouldConnect(world, pos, Direction.UP)).with(ConnectionCableBlock.DOWN, shouldConnect(world, pos, Direction.DOWN));
    }
}
