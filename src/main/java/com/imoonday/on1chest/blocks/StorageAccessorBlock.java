package com.imoonday.on1chest.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class StorageAccessorBlock extends Block {
    public StorageAccessorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        System.out.println(getAllConnectedBlocks(world, pos));
        return ActionResult.success(world.isClient);
    }

    protected List<BlockPos> getAllConnectedBlocks(World world, BlockPos blockPos) {
        // 创建一个方向数组，包含六个基本方向
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
        // 创建一个BlockPos列表，用来存储所有连接着的方块位置
        List<BlockPos> result = new ArrayList<>();
        // 创建一个BlockPos队列，用来存储待处理的方块位置
        Queue<BlockPos> queue = new LinkedList<>();
        // 将初始方块位置加入到队列中
        queue.add(blockPos);
        // 当队列不为空时，循环处理队列中的每个方块位置
        while (!queue.isEmpty()) {
            // 从队列中弹出一个方块位置
            BlockPos currentPos = queue.poll();
            // 遍历每个方向
            for (Direction direction : directions) {
                // 获取当前方向上相邻的方块位置
                BlockPos adjacentPos = currentPos.offset(direction);
                // 获取相邻方块的状态
                BlockState adjacentState = world.getBlockState(adjacentPos);
                // 判断相邻方块是否是StorageMemoryBlock或StorageBlankBlock类型，并且是否已经在结果列表中
                if ((adjacentState.getBlock() instanceof StorageMemoryBlock || adjacentState.getBlock() instanceof StorageBlankBlock) && !result.contains(adjacentPos)) {
                    // 如果是，就将该方块位置加入到结果列表和队列中
                    result.add(adjacentPos);
                    queue.add(adjacentPos);
                }
            }
        }
        // 返回结果列表
        return result;
    }
}
