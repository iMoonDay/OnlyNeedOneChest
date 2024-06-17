package com.imoonday.on1chest.utils;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PositionPredicate {

    private final List<Predicate> predicates = new ArrayList<>();

    private PositionPredicate(@NotNull World world, @NotNull BlockPos pos) {
        this(testPos(world, pos));
    }

    private static Predicate testPos(World world, BlockPos pos) {
        return (world1, pos1) -> Objects.equals(world1, world) && Objects.equals(pos1, pos);
    }

    private PositionPredicate(@NotNull Block block) {
        this(testBlock(block));
    }

    private static Predicate testBlock(Block block) {
        return (world, pos) -> world.getBlockState(pos).isOf(block);
    }

    private PositionPredicate(@NotNull Predicate predicate) {
        this.predicates.add(predicate);
    }

    public static PositionPredicate create(@NotNull World world, @NotNull BlockPos pos) {
        return new PositionPredicate(world, pos);
    }

    public static PositionPredicate create(@NotNull Block block) {
        return new PositionPredicate(block);
    }

    public static PositionPredicate create(@NotNull Predicate predicate) {
        return new PositionPredicate(predicate);
    }

    public PositionPredicate add(@NotNull World world, @NotNull BlockPos pos) {
        this.predicates.add(testPos(world, pos));
        return this;
    }

    public PositionPredicate add(@NotNull Block block) {
        this.predicates.add(testBlock(block));
        return this;
    }

    public PositionPredicate add(@NotNull Predicate predicate) {
        this.predicates.add(predicate);
        return this;
    }

    public boolean test(World world, BlockPos pos) {
        return this.predicates.stream().anyMatch(predicate -> predicate.test(world, pos));
    }

    public interface Predicate {
        boolean test(World world, BlockPos pos);
    }
}
