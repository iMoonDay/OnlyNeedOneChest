package com.imoonday.on1chest.blocks;

import com.imoonday.on1chest.blocks.entities.MemoryExtractorBlockEntity;
import com.imoonday.on1chest.blocks.entities.TransferBlockEntity;
import com.imoonday.on1chest.init.ModBlockEntities;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MemoryExtractorBlock extends ConnectableBlock implements BlockEntityProvider {

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
        super(settings);
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
    public Map<Direction, VoxelShape> getBoundingShapes() {
        return BOUNDING_SHAPES;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (!(world.getBlockEntity(pos) instanceof TransferBlockEntity entity)) {
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
