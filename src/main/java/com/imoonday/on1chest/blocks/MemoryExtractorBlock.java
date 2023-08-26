package com.imoonday.on1chest.blocks;

import com.imoonday.on1chest.blocks.entities.MemoryExtractorBlockEntity;
import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.utils.ConnectBlock;
import com.imoonday.on1chest.utils.Connectable;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MemoryExtractorBlock extends ConnectingBlock implements ConnectBlock, BlockEntityProvider, Waterloggable, Connectable {

    public static final DirectionProperty FACING = Properties.FACING;
    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final Map<Direction, VoxelShape> BOUNDING_SHAPES = Util.make(() -> {
        Map<Direction, VoxelShape> map = new HashMap<>();
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0, 0.375, 0.625, 0.625, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.0625, 0.25, 0.6875, 0.125, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.0625, 0.6875, 0.6875, 0.125, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.0625, 0.3125, 0.3125, 0.125, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0.0625, 0.3125, 0.75, 0.125, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.1875, 0.25, 0.6875, 0.25, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.1875, 0.6875, 0.6875, 0.25, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.1875, 0.3125, 0.3125, 0.25, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0.1875, 0.3125, 0.75, 0.25, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.3125, 0.25, 0.6875, 0.375, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.3125, 0.6875, 0.6875, 0.375, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.3125, 0.3125, 0.3125, 0.375, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0.3125, 0.3125, 0.75, 0.375, 0.6875));
        map.put(Direction.UP, shape);
        shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.375, 0.375, 0.625, 1, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.875, 0.6875, 0.6875, 0.9375, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.875, 0.25, 0.6875, 0.9375, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.875, 0.3125, 0.3125, 0.9375, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0.875, 0.3125, 0.75, 0.9375, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.75, 0.6875, 0.6875, 0.8125, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.75, 0.25, 0.6875, 0.8125, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.75, 0.3125, 0.3125, 0.8125, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0.75, 0.3125, 0.75, 0.8125, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.625, 0.6875, 0.6875, 0.6875, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.625, 0.25, 0.6875, 0.6875, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.625, 0.3125, 0.3125, 0.6875, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0.625, 0.3125, 0.75, 0.6875, 0.6875));
        map.put(Direction.DOWN, shape);
        shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.375, 0.375, 0.625, 0.625, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.25, 0.875, 0.6875, 0.3125, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.6875, 0.875, 0.6875, 0.75, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.3125, 0.875, 0.3125, 0.6875, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0.3125, 0.875, 0.75, 0.6875, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.25, 0.75, 0.6875, 0.3125, 0.8125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.6875, 0.75, 0.6875, 0.75, 0.8125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.3125, 0.75, 0.3125, 0.6875, 0.8125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0.3125, 0.75, 0.75, 0.6875, 0.8125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.25, 0.625, 0.6875, 0.3125, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.6875, 0.625, 0.6875, 0.75, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.3125, 0.625, 0.3125, 0.6875, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0.3125, 0.625, 0.75, 0.6875, 0.6875));
        map.put(Direction.NORTH, shape);
        shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.375, 0, 0.625, 0.625, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.6875, 0.0625, 0.6875, 0.75, 0.125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.25, 0.0625, 0.6875, 0.3125, 0.125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.3125, 0.0625, 0.3125, 0.6875, 0.125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0.3125, 0.0625, 0.75, 0.6875, 0.125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.6875, 0.1875, 0.6875, 0.75, 0.25));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.25, 0.1875, 0.6875, 0.3125, 0.25));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.3125, 0.1875, 0.3125, 0.6875, 0.25));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0.3125, 0.1875, 0.75, 0.6875, 0.25));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.6875, 0.3125, 0.6875, 0.75, 0.375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.25, 0.3125, 0.6875, 0.3125, 0.375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.3125, 0.3125, 0.3125, 0.6875, 0.375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.6875, 0.3125, 0.3125, 0.75, 0.6875, 0.375));
        map.put(Direction.SOUTH, shape);
        shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0.375, 0.375, 0.625, 0.625, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.3125, 0.25, 0.125, 0.6875, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.3125, 0.6875, 0.125, 0.6875, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.6875, 0.3125, 0.125, 0.75, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.25, 0.3125, 0.125, 0.3125, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.1875, 0.3125, 0.25, 0.25, 0.6875, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.1875, 0.3125, 0.6875, 0.25, 0.6875, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.1875, 0.6875, 0.3125, 0.25, 0.75, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.1875, 0.25, 0.3125, 0.25, 0.3125, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.3125, 0.25, 0.375, 0.6875, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.3125, 0.6875, 0.375, 0.6875, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.6875, 0.3125, 0.375, 0.75, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.25, 0.3125, 0.375, 0.3125, 0.6875));
        map.put(Direction.EAST, shape);
        shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.375, 0.375, 1, 0.625, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.875, 0.3125, 0.25, 0.9375, 0.6875, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.875, 0.3125, 0.6875, 0.9375, 0.6875, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.875, 0.25, 0.3125, 0.9375, 0.3125, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.875, 0.6875, 0.3125, 0.9375, 0.75, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.75, 0.3125, 0.25, 0.8125, 0.6875, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.75, 0.3125, 0.6875, 0.8125, 0.6875, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.75, 0.25, 0.3125, 0.8125, 0.3125, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.75, 0.6875, 0.3125, 0.8125, 0.75, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.625, 0.3125, 0.25, 0.6875, 0.6875, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.625, 0.3125, 0.6875, 0.6875, 0.6875, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.625, 0.25, 0.3125, 0.6875, 0.3125, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.625, 0.6875, 0.3125, 0.6875, 0.75, 0.6875));
        map.put(Direction.WEST, shape);
        return map;
    });

    public MemoryExtractorBlock(Settings settings) {
        super(0.125f, settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.UP).with(WATERLOGGED, false).with(NORTH, false).with(EAST, false).with(WEST, false).with(SOUTH, false).with(UP, false).with(DOWN, false));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MemoryExtractorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient || type != ModBlockEntities.MEMORY_EXTRACTOR_BLOCK_ENTITY) {
            return null;
        }
        return (world1, pos, state1, entity) -> MemoryExtractorBlockEntity.tick(world1, pos, state1, (MemoryExtractorBlockEntity) entity);
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
        return VoxelShapes.union(BOUNDING_SHAPES.get(state.get(FACING)), super.getOutlineShape(state, world, pos, context));
    }

    @Override
    public Direction[] getValidDirections(BlockState state) {
        return FACING_PROPERTIES.entrySet().stream().filter(entry -> state.get(entry.getValue()) || state.get(FACING) == entry.getKey()).map(Map.Entry::getKey).toList().toArray(new Direction[6]);
    }

    @Override
    public boolean canConnect(BlockState state, Direction direction) {
        return direction != state.get(FACING).getOpposite();
    }

    @Override
    public BlockState handleConnection(BlockState state, WorldAccess world, BlockPos pos) {
        return Connectable.super.handleConnection(state, world, pos).with(FACING_PROPERTIES.get(state.get(FACING).getOpposite()), false);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (hand == Hand.OFF_HAND) {
            return ActionResult.PASS;
        }
        if (!(world.getBlockEntity(pos) instanceof MemoryExtractorBlockEntity entity)) {
            return ActionResult.PASS;
        }
        ItemStack stack = player.getMainHandStack();
        boolean success = false;
        if (!stack.isEmpty()) {
            success = entity.setTarget(stack.getItem());
        } else if (player.isSneaking()) {
            if (entity.getTarget() == null) {
                entity.setMatchMode(!entity.isMatchMode());
            } else {
                success = entity.setTarget(null);
            }
        }
        if (success) {
            entity.uniqueOffset = world.random.nextFloat() * 360;
        }
        world.updateListeners(pos, state, state, NOTIFY_LISTENERS);
        Item target = entity.getTarget();
        player.sendMessage(target != null ? target.getName() : Text.translatable("block.on1chest.item_exporter." + (entity.isMatchMode() ? "match" : "any")), true);
        return ActionResult.SUCCESS;
    }
}
