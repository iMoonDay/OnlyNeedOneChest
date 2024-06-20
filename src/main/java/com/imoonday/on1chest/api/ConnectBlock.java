package com.imoonday.on1chest.api;

import com.imoonday.on1chest.blocks.entities.WirelessConnectorBlockEntity;
import com.imoonday.on1chest.utils.PositionPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public interface ConnectBlock {

    static List<Pair<World, BlockPos>> getConnectedBlocks(World world, BlockPos pos, PositionPredicate... ignore) {
        if (world == null || pos == null) return List.of();
        Set<Pair<World, BlockPos>> result = new LinkedHashSet<>();
        Queue<Pair<World, BlockPos>> queue = new LinkedList<>();
        queue.add(new Pair<>(world, pos));
        while (!queue.isEmpty()) {
            Pair<World, BlockPos> currentPair = queue.poll();
            World currentWorld = currentPair.getLeft();
            BlockPos currentPos = currentPair.getRight();
            Direction[] directions = Direction.values();
            BlockState currentState = currentWorld.getBlockState(currentPos);
            Block currentBlock = currentState.getBlock();
            if (currentBlock instanceof ConnectBlock connectBlock) {
                directions = connectBlock.getValidDirections(currentState);
            }
            for (Direction direction : directions) {
                if (direction == null) {
                    System.err.println(currentBlock.getName().getString() + ": direction is null");
                    continue;
                }
                BlockPos adjacentPos = currentPos.offset(direction);
                if (result.stream().anyMatch(pair1 -> pair1.getLeft().equals(currentWorld) && pair1.getRight().equals(adjacentPos))) {
                    continue;
                }
                if (Arrays.stream(ignore).anyMatch(predicate -> predicate.test(currentWorld, adjacentPos))) continue;
                BlockState adjacentState = currentWorld.getBlockState(adjacentPos);
                Pair<World, BlockPos> pair = new Pair<>(currentWorld, adjacentPos);
                if (adjacentState.getBlock() instanceof ConnectBlock block) {
                    result.add(pair);
                    if (block.canContinue(currentWorld, adjacentPos, adjacentState)) {
                        queue.add(pair);
                    }
                    if (currentWorld.getBlockEntity(adjacentPos) instanceof WirelessConnectorBlockEntity entity) {
                        queue.addAll(entity.getNetworks());
                    }
                } else {
                    for (CustomConnectBlock custom : CustomConnectBlock.CUSTOMS) {
                        if (custom.canConnect(currentWorld, adjacentPos, adjacentState)) {
                            result.add(pair);
                            if (custom.canContinue(currentWorld, adjacentPos, adjacentState)) {
                                queue.add(pair);
                            }
                            if (currentWorld.getBlockEntity(adjacentPos) instanceof WirelessConnectorBlockEntity entity) {
                                queue.addAll(entity.getNetworks());
                            }
                            break;
                        }
                    }
                }
            }
        }
        return new ArrayList<>(result);
    }

    static void registerCustomConnectBlock(Block block) {
        registerCustomConnectBlock(new CustomConnectBlock(block));
    }

    static void registerCustomConnectBlock(CustomConnectBlock custom) {
        CustomConnectBlock.CUSTOMS.add(custom);
    }

    default boolean canContinue(World world, BlockPos pos, BlockState state) {
        return true;
    }

    default Direction[] getValidDirections(BlockState state) {
        return Direction.values();
    }

    default boolean canConnect(BlockState state, Direction direction) {
        return true;
    }

    record CustomConnectBlock(Block block) implements ConnectBlock {

        private static final List<CustomConnectBlock> CUSTOMS = new ArrayList<>();

        public boolean canConnect(World world, BlockPos pos, BlockState state) {
            return state.getBlock() == block;
        }
    }
}
