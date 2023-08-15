package com.imoonday.on1chest.blocks;

import com.imoonday.on1chest.blocks.entities.StorageMemoryBlockEntity;
import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class StorageMemoryBlock extends BlockWithEntity {

    public static final EnumProperty<UsedCapacity> USED_CAPACITY = EnumProperty.of("used_capacity", UsedCapacity.class);

    public StorageMemoryBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(USED_CAPACITY, UsedCapacity.ZERO));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(USED_CAPACITY);
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
        return checkType(type, ModBlocks.STORAGE_MEMORY_BLOCK_ENTITY, StorageMemoryBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            if (world.getBlockEntity(pos) instanceof StorageMemoryBlockEntity entity) {
                ItemStack stack = player.getStackInHand(hand);
                StorageMemoryBlock memoryBlock = getLevelUpEntries().get(stack.getItem());
                if (canLevelUp() && memoryBlock != null) {
                    if (!player.isCreative()) {
                        stack.decrement(1);
                    }
                    world.setBlockState(pos, memoryBlock.getDefaultState().with(USED_CAPACITY, state.get(USED_CAPACITY)), Block.NOTIFY_LISTENERS);
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
                update(world, pos);
            }
        }
    }

    protected void update(World world, BlockPos pos) {
        if (world instanceof ServerWorld serverWorld) {
            PlayerLookup.tracking(serverWorld, pos).stream().filter(player -> player.currentScreenHandler instanceof StorageAssessorScreenHandler).map(player -> (StorageAssessorScreenHandler) player.currentScreenHandler).forEach(StorageAssessorScreenHandler::updateItemList);
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
