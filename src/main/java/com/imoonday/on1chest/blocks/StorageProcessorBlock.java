package com.imoonday.on1chest.blocks;

import com.imoonday.on1chest.blocks.entities.StorageAccessorBlockEntity;
import com.imoonday.on1chest.blocks.entities.StorageProcessorBlockEntity;
import com.imoonday.on1chest.init.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
                List<Inventory> inventories = processorBlock.getAllMemories(world, pos);
                int size = processorBlock.size();
                int occupied = size - processorBlock.getFreeSlotCount();
                player.sendMessage(Text.literal("%d/%d(%d)".formatted(occupied, size, inventories.size())), true);
            } else {
                player.openHandledScreen(processorBlock);
            }
        }
        return ActionResult.success(world.isClient);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, ModBlockEntities.STORAGE_PROCESSOR_BLOCK_ENTITY, StorageAccessorBlockEntity::tick);
    }
}
