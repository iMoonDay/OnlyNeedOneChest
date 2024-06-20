package com.imoonday.on1chest.blocks;

import com.imoonday.on1chest.api.ConnectBlock;
import com.imoonday.on1chest.api.Connectable;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectionCableBlock extends ConnectingBlock implements ConnectBlock, Waterloggable, Connectable {

    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final VoxelShape BASE_SHAPE = Util.make(() -> {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.625, 0.4375, 0.4375, 0.6875, 0.5625, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.4375, 0.625, 0.5625, 0.5625, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.625, 0.4375, 0.5625, 0.6875, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.3125, 0.4375, 0.5625, 0.375, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.4375, 0.3125, 0.5625, 0.5625, 0.375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.4375, 0.4375, 0.375, 0.5625, 0.5625));
        return shape;
    });

    protected ConnectionCableBlock(float radius, Settings settings) {
        super(radius, settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(NORTH, false).with(EAST, false).with(WEST, false).with(SOUTH, false).with(UP, false).with(DOWN, false).with(WATERLOGGED, false));
    }

    public ConnectionCableBlock(Settings settings) {
        this(0.125f, settings);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, UP, DOWN, WATERLOGGED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        BlockState state = this.getDefaultState().with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER);
        return this.handleConnection(state, world, pos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return handleConnection(state, world, pos);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 ->
                    state.with(NORTH, state.get(SOUTH)).with(EAST, state.get(WEST)).with(SOUTH, state.get(NORTH)).with(WEST, state.get(EAST));
            case CLOCKWISE_90 ->
                    state.with(NORTH, state.get(EAST)).with(EAST, state.get(SOUTH)).with(SOUTH, state.get(WEST)).with(WEST, state.get(NORTH));
            case COUNTERCLOCKWISE_90 ->
                    state.with(NORTH, state.get(WEST)).with(EAST, state.get(NORTH)).with(SOUTH, state.get(EAST)).with(WEST, state.get(SOUTH));
            default -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return switch (mirror) {
            case FRONT_BACK -> state.with(NORTH, state.get(SOUTH)).with(SOUTH, state.get(NORTH));
            case LEFT_RIGHT -> state.with(EAST, state.get(WEST)).with(WEST, state.get(EAST));
            default -> super.mirror(state, mirror);
        };
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.union(super.getOutlineShape(state, world, pos, context), BASE_SHAPE);
    }

    @Override
    public Direction[] getValidDirections(BlockState state) {
        List<Direction> list = new ArrayList<>();
        for (Map.Entry<Direction, BooleanProperty> entry : FACING_PROPERTIES.entrySet()) {
            if (state.get(entry.getValue())) {
                Direction direction = entry.getKey();
                if (direction != null) {
                    list.add(direction);
                }
            }
        }
        return list.toArray(new Direction[0]);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.isSneaking() && hand == Hand.MAIN_HAND) {
            if (player.getMainHandStack().isEmpty()) {
                if (!world.isClient) {
                    if (!player.isCreative()) {
                        ItemStack stack = this.asItem().getDefaultStack();
                        player.getInventory().offerOrDrop(stack);
                    }
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }
}
