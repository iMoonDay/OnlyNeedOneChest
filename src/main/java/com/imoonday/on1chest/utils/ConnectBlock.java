package com.imoonday.on1chest.utils;

import com.imoonday.on1chest.blocks.entities.WirelessConnectorBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public interface ConnectBlock {

    static List<Pair<World, BlockPos>> getConnectedBlocks(World world, BlockPos pos, PositionPredicate... ignore) {
        if (world == null || pos == null) {
            return new ArrayList<>();
        }
        List<Pair<World, BlockPos>> result = new ArrayList<>();
        Queue<Pair<World, BlockPos>> queue = new LinkedList<>();
        queue.add(new Pair<>(world, pos));
        while (!queue.isEmpty()) {
            Pair<World, BlockPos> currentPair = queue.poll();
            World currentWorld = currentPair.getLeft();
            BlockPos currentPos = currentPair.getRight();
            Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
            if (currentWorld.getBlockEntity(currentPos) instanceof ConnectBlock connectBlock) {
                directions = connectBlock.getValidDirections(currentWorld.getBlockState(currentPos));
            }
            for (Direction direction : directions) {
                BlockPos adjacentPos = currentPos.offset(direction);
                if (Arrays.stream(ignore).anyMatch(predicate -> predicate.test(currentWorld, adjacentPos))) {
                    continue;
                }
                BlockState adjacentState = currentWorld.getBlockState(adjacentPos);
                if (adjacentState.getBlock() instanceof ConnectBlock block && result.stream().noneMatch(pair -> pair.getLeft().equals(currentWorld) && pair.getRight().equals(adjacentPos))) {
                    Pair<World, BlockPos> pair = new Pair<>(currentWorld, adjacentPos);
                    result.add(pair);
                    if (block.canContinue(currentWorld, adjacentPos, adjacentState)) {
                        queue.add(pair);
                    }
                    if (currentWorld.getBlockEntity(adjacentPos) instanceof WirelessConnectorBlockEntity entity) {
                        queue.addAll(entity.getNetworks());
                    }
                }
            }
        }
        return result;
    }

    default boolean canContinue(World world, BlockPos pos, BlockState state) {
        return true;
    }

    default Direction[] getValidDirections(BlockState state) {
        return new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
    }

    default boolean canConnect(BlockState state, Direction direction) {
        return true;
    }
}
