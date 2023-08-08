package com.imoonday.on1chest.blocks;

import com.imoonday.on1chest.blocks.entities.StorageMemoryBlockEntity;
import com.imoonday.on1chest.client.screen.StorageAssessorScreen;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class StorageAccessorBlock extends StorageBlankBlock {
    public StorageAccessorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            if (player.isSneaking()) {
                int occupied = 0;
                int total = 0;
                int count = 0;
                for (BlockPos blockPos : getConnectedBlocks(world, pos)) {
                    if (world.getBlockEntity(blockPos) instanceof StorageMemoryBlockEntity entity) {
                        occupied += entity.getOccupiedSize();
                        total += entity.getStorageSize();
                        count++;
                    }
                }
                player.sendMessage(Text.literal("%d/%d(%d)".formatted(occupied, total, count)), true);
            } else {
                NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
                if (screenHandlerFactory != null) {
                    player.openHandledScreen(screenHandlerFactory);
                }
            }
        }
        return ActionResult.success(world.isClient);
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return state.getBlock().getName();
            }

            @Override
            public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new StorageAssessorScreen.StorageAssessorScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos));
            }
        };
    }

    public DefaultedList<ItemStack> createItemList(World world, BlockPos pos) {
        DefaultedList<ItemStack> itemList = DefaultedList.of();
        getConnectedBlocks(world, pos).stream().filter(blockPos -> world.getBlockEntity(blockPos) instanceof StorageMemoryBlockEntity).map(blockPos -> (StorageMemoryBlockEntity) world.getBlockEntity(blockPos)).map(entity -> entity.getItems()).forEach(itemList::addAll);
        int removedCount = itemList.size();
        itemList.removeIf(ItemStack::isEmpty);
        removedCount -= itemList.size();
        Map<ItemStack, Integer> itemStackMap = new HashMap<>();
        itemList.forEach(stack -> itemStackMap.entrySet().stream().filter(entry -> ItemStack.canCombine(stack, entry.getKey())).findFirst().ifPresentOrElse(entry -> itemStackMap.merge(entry.getKey(), stack.getCount(), Integer::sum), () -> itemStackMap.put(stack, stack.getCount())));
        DefaultedList<ItemStack> itemList1 = DefaultedList.ofSize(itemStackMap.size() + removedCount, ItemStack.EMPTY);
        int index = 0;
        for (Map.Entry<ItemStack, Integer> entry : itemStackMap.entrySet()) {
            ItemStack key = entry.getKey();
            Integer integer = entry.getValue();
            itemList1.set(index++, key.copyWithCount(integer));
        }
        return itemList1;
    }

    public List<BlockPos> getConnectedBlocks(World world, BlockPos pos) {
        // 创建一个方向数组，包含六个基本方向
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
        // 创建一个BlockPos列表，用来存储所有连接着的方块位置
        List<BlockPos> result = new ArrayList<>();
        // 创建一个BlockPos队列，用来存储待处理的方块位置
        Queue<BlockPos> queue = new LinkedList<>();
        // 将初始方块位置加入到队列中
        queue.add(pos);
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

    public List<Inventory> getAllInventories(World world, BlockPos pos) {
        return getConnectedBlocks(world, pos).stream().filter(blockPos -> world.getBlockEntity(blockPos) instanceof StorageMemoryBlockEntity).map(blockPos -> (StorageMemoryBlockEntity) world.getBlockEntity(blockPos)).collect(Collectors.toCollection(ArrayList::new));
    }
}
