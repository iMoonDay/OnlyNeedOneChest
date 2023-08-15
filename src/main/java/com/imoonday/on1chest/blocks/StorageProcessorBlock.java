package com.imoonday.on1chest.blocks;

import com.imoonday.on1chest.blocks.entities.StorageMemoryBlockEntity;
import com.imoonday.on1chest.blocks.entities.StorageProcessorBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class StorageProcessorBlock extends StorageAccessorBlock {
    public StorageProcessorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StorageProcessorBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof StorageProcessorBlockEntity processorBlock)) {
            return ActionResult.success(world.isClient);
        }
        if (!world.isClient) {
            if (player.isSneaking()) {
                int occupied = 0;
                int total = 0;
                int count = 0;
                for (BlockPos blockPos : processorBlock.getConnectedBlocks(world, pos)) {
                    if (world.getBlockEntity(blockPos) instanceof StorageMemoryBlockEntity memoryBlock) {
                        occupied += memoryBlock.getOccupiedSize();
                        total += memoryBlock.getStorageSize();
                        count++;
                    }
                }
                player.sendMessage(Text.literal("%d/%d(%d)".formatted(occupied, total, count)), true);
            } else {
                player.openHandledScreen(processorBlock);
            }
        }
        return ActionResult.success(world.isClient);
    }
}
