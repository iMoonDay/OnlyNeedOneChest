package com.imoonday.on1chest.blocks;

import com.imoonday.on1chest.api.ConnectBlock;
import com.imoonday.on1chest.blocks.entities.StorageAccessorBlockEntity;
import com.imoonday.on1chest.init.ModBlockEntities;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
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

public class StorageAccessorBlock extends BlockWithEntity implements ConnectBlock {

    public StorageAccessorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof StorageAccessorBlockEntity accessorBlock)) {
            return ActionResult.success(world.isClient);
        }
        if (!world.isClient) {
            if (player.isSneaking()) {
                List<Inventory> inventories = accessorBlock.getAllMemories(world, pos);
                int size = accessorBlock.getInventory().size();
                int occupied = size - accessorBlock.getFreeSlotCount();
                player.sendMessage(Text.literal("%d/%d(%d)".formatted(occupied, size, inventories.size())), true);
            } else {
                player.openHandledScreen(accessorBlock);
            }
        }
        return ActionResult.success(world.isClient);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StorageAccessorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, ModBlockEntities.STORAGE_ACCESSOR_BLOCK_ENTITY, StorageAccessorBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
