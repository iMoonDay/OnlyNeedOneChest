package com.imoonday.on1chest.blocks;

import com.imoonday.on1chest.blocks.entities.StorageMemoryBlockEntity;
import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.utils.ConnectBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StorageMemoryBlock extends BlockWithEntity implements ConnectBlock {

    public static final EnumProperty<UsedCapacity> USED_CAPACITY = EnumProperty.of("used_capacity", UsedCapacity.class);
    public static final BooleanProperty ACTIVATED = BooleanProperty.of("activated");

    public StorageMemoryBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(USED_CAPACITY, UsedCapacity.ZERO).with(ACTIVATED, true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(USED_CAPACITY, ACTIVATED);
    }

    public abstract int getLevel();

    public boolean canLevelUp() {
        return !getLevelUpEntries().isEmpty();
    }

    public Map<Item, StorageMemoryBlock> getLevelUpEntries() {
        return new HashMap<>();
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StorageMemoryBlockEntity(pos, state, getLevel());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlockEntities.STORAGE_MEMORY_BLOCK_ENTITY, StorageMemoryBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            if (player.isSneaking()) {
                world.setBlockState(pos, state.with(ACTIVATED, !state.get(ACTIVATED)), NOTIFY_LISTENERS);
                return ActionResult.SUCCESS;
            }
            if (world.getBlockEntity(pos) instanceof StorageMemoryBlockEntity entity) {
                ItemStack stack = player.getStackInHand(hand);
                StorageMemoryBlock memoryBlock = getLevelUpEntries().get(stack.getItem());
                if (canLevelUp() && memoryBlock != null) {
                    if (!player.isCreative()) {
                        stack.decrement(1);
                    }
                    world.setBlockState(pos, memoryBlock.getDefaultState().with(USED_CAPACITY, state.get(USED_CAPACITY)).with(ACTIVATED, state.get(ACTIVATED)), Block.NOTIFY_LISTENERS);
                    world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                player.sendMessage(Text.literal(entity.getOccupiedSize() + "/" + entity.getStorageSize()), true);
            }
        }
        return ActionResult.success(world.isClient);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof StorageMemoryBlockEntity entity) {
                if (newState.getBlock() instanceof StorageMemoryBlock block) {
                    entity.updateLevel(block);
                } else {
                    ItemScatterer.spawn(world, pos, entity);
                    world.updateComparators(pos, this);
                }
            }
            if (state.hasBlockEntity() && !(newState.getBlock() instanceof StorageMemoryBlock)) {
                world.removeBlockEntity(pos);
            }
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        tooltip.add(Text.translatable("block.on1chest.storage_memory_block.tooltip", 27 * (this.getLevel() + 1)).formatted(Formatting.GRAY));
    }

    public enum UsedCapacity implements StringIdentifiable {

        ZERO("zero"),
        LOW("low"),
        HIGH("high"),
        FULL("full");

        private final String name;

        UsedCapacity(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
