package com.imoonday.on1chest.blocks;

import com.imoonday.on1chest.api.ConnectBlockConverter;
import com.imoonday.on1chest.blocks.entities.MemoryConverterBlockEntity;
import com.imoonday.on1chest.init.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryConverterBlock extends ConnectableBlock implements BlockEntityProvider, ConnectBlockConverter {

    private static final Map<Direction, VoxelShape> BOUNDING_SHAPES = Util.make(() -> {
        Map<Direction, VoxelShape> map = new HashMap<>();

        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 1, 0.0625, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.0625, 0.375, 0.625, 0.625, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.125, 0.125, 0.875, 0.1875, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.3125, 0.75));
        map.put(Direction.UP, shape);

        shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0.9375, 0, 1, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.375, 0.375, 0.625, 0.9375, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.8125, 0.125, 0.875, 0.875, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.6875, 0.25, 0.75, 0.75, 0.75));
        map.put(Direction.DOWN, shape);

        shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0.9375, 1, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.375, 0.375, 0.625, 0.625, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.125, 0.8125, 0.875, 0.875, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.25, 0.6875, 0.75, 0.75, 0.75));
        map.put(Direction.NORTH, shape);

        shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 1, 1, 0.0625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.375, 0.0625, 0.625, 0.625, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.125, 0.125, 0.875, 0.875, 0.1875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.3125));
        map.put(Direction.SOUTH, shape);

        shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.9375, 0, 0, 1, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.375, 0.375, 0.9375, 0.625, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.8125, 0.125, 0.125, 0.875, 0.875, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0.25, 0.25, 0.75, 0.75, 0.75));
        map.put(Direction.WEST, shape);

        shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 0.0625, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.375, 0.375, 0.625, 0.625, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.125, 0.125, 0.1875, 0.875, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.3125, 0.75, 0.75));
        map.put(Direction.EAST, shape);

        return map;
    });
    public static final EnumProperty<StorageMemoryBlock.UsedCapacity> USED_CAPACITY = EnumProperty.of("used_capacity", StorageMemoryBlock.UsedCapacity.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    public MemoryConverterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(USED_CAPACITY, StorageMemoryBlock.UsedCapacity.ZERO).with(ACTIVE, true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(USED_CAPACITY, ACTIVE);
    }

    @Override
    protected Map<Direction, VoxelShape> getBoundingShapes() {
        return BOUNDING_SHAPES;
    }

    @Override
    public Direction getConvertingDirection(World world, BlockPos pos, BlockState state) {
        return state.get(FACING).getOpposite();
    }

    @Override
    public boolean isActive(World world, BlockPos pos, BlockState state) {
        return state.get(ACTIVE);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MemoryConverterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return !world.isClient && type == ModBlockEntities.MEMORY_CONVERTER_BLOCK_ENTITY ? (world1, pos, state1, blockEntity) -> MemoryConverterBlockEntity.tick(world1, pos, state1, (MemoryConverterBlockEntity) blockEntity) : null;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            world.setBlockState(pos, state.with(ACTIVE, !state.get(ACTIVE)));
            world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3f, state.get(ACTIVE) ? 0.5f : 0.6f);
        }
        return ActionResult.success(world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        tooltip.add(Text.translatable("block.on1chest.memory_converter.tooltip").formatted(Formatting.GRAY));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient && world.getBlockEntity(pos) instanceof MemoryConverterBlockEntity entity) {
            StorageMemoryBlock.UsedCapacity usedCapacity = entity.getUsedCapacity();
            world.setBlockState(pos, state.with(MemoryConverterBlock.USED_CAPACITY, usedCapacity), Block.NOTIFY_LISTENERS);
        }
    }
}
