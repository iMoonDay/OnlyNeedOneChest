package com.imoonday.on1chest.blocks;

import com.imoonday.on1chest.api.ConnectBlock;
import com.imoonday.on1chest.api.Connectable;
import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
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

public abstract class ConnectableBlock extends ConnectingBlock implements ConnectBlock, Waterloggable, Connectable {
    public static final DirectionProperty FACING = Properties.FACING;
    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public ConnectableBlock(Settings settings) {
        this(0.125f, settings);
    }

    public ConnectableBlock(float radius, Settings settings) {
        super(radius, settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.UP).with(WATERLOGGED, false).with(NORTH, false).with(EAST, false).with(WEST, false).with(SOUTH, false).with(UP, false).with(DOWN, false));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        return this.handleConnection(this.getDefaultState().with(FACING, ctx.getSide()).with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER), world, pos);
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
        return (switch (rotation) {
            case CLOCKWISE_180 ->
                    state.with(NORTH, state.get(SOUTH)).with(EAST, state.get(WEST)).with(SOUTH, state.get(NORTH)).with(WEST, state.get(EAST));
            case CLOCKWISE_90 ->
                    state.with(NORTH, state.get(EAST)).with(EAST, state.get(SOUTH)).with(SOUTH, state.get(WEST)).with(WEST, state.get(NORTH));
            case COUNTERCLOCKWISE_90 ->
                    state.with(NORTH, state.get(WEST)).with(EAST, state.get(NORTH)).with(SOUTH, state.get(EAST)).with(WEST, state.get(SOUTH));
            default -> state;
        }).with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return (switch (mirror) {
            case FRONT_BACK -> state.with(NORTH, state.get(SOUTH)).with(SOUTH, state.get(NORTH));
            case LEFT_RIGHT -> state.with(EAST, state.get(WEST)).with(WEST, state.get(EAST));
            default -> super.mirror(state, mirror);
        }).rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, NORTH, EAST, WEST, SOUTH, UP, DOWN);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Map<Direction, VoxelShape> boundingShapes = this.getBoundingShapes();
        if (boundingShapes == null || !boundingShapes.containsKey(state.get(FACING))) {
            return super.getOutlineShape(state, world, pos, context);
        }
        return VoxelShapes.union(boundingShapes.get(state.get(FACING)), super.getOutlineShape(state, world, pos, context));
    }

    protected abstract Map<Direction, VoxelShape> getBoundingShapes();

    @Override
    public Direction[] getValidDirections(BlockState state) {
        List<Direction> list = new ArrayList<>();
        for (Map.Entry<Direction, BooleanProperty> entry : FACING_PROPERTIES.entrySet()) {
            Direction direction = entry.getKey();
            if (state.get(entry.getValue()) || state.get(FACING) == direction) {
                if (direction != null) {
                    list.add(direction);
                }
            }
        }
        return list.toArray(new Direction[0]);
    }

    @Override
    public boolean canConnect(BlockState state, Direction direction) {
        return direction != state.get(FACING).getOpposite();
    }

    @Override
    public BlockState handleConnection(BlockState state, WorldAccess world, BlockPos pos) {
        return Connectable.super.handleConnection(state, world, pos).with(FACING_PROPERTIES.get(state.get(FACING).getOpposite()), false);
    }
}
