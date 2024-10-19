package com.imoonday.on1chest.blocks.memories;

import com.imoonday.on1chest.blocks.entities.DisplayStorageMemoryBlockEntity;
import com.imoonday.on1chest.init.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DisplayStorageMemoryBlock extends GlassStorageMemoryBlock {

    public DisplayStorageMemoryBlock(Settings settings) {
        super(settings);
    }

    @Override
    public int getLevel() {
        return 36;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DisplayStorageMemoryBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, ModBlockEntities.DISPLAY_STORAGE_MEMORY_BLOCK_ENTITY, DisplayStorageMemoryBlockEntity::tick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof DisplayStorageMemoryBlockEntity entity) {
            ItemStack stack = player.getStackInHand(hand);
            if (!player.isSneaking() && (!stack.isEmpty() || !entity.getDisplayItem().isEmpty()) && entity.updateDisplayItem(stack)) {
                return ActionResult.SUCCESS;
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        updateLevel(world, pos);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        tooltip.add(Text.translatable("block.on1chest.display_storage_memory_block.tooltip.1").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("block.on1chest.display_storage_memory_block.tooltip.2").formatted(Formatting.RED));
    }
}