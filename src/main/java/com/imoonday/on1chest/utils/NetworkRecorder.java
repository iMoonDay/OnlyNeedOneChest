package com.imoonday.on1chest.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class NetworkRecorder {

    private final @NotNull String world;
    private final @NotNull BlockPos pos;
    private @NotNull String id;

    public NetworkRecorder(@NotNull String world, @NotNull BlockPos pos, @NotNull String id) {
        this.world = world;
        this.pos = pos;
        this.id = id;
    }

    public NbtCompound toNbt() {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("world", world);
        nbtCompound.putIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
        nbtCompound.putString("id", id);
        return nbtCompound;
    }

    @Nullable
    public static NetworkRecorder fromNbt(NbtCompound nbtCompound) {
        try {
            String world = nbtCompound.getString("world");
            int[] posArray = nbtCompound.getIntArray("pos");
            BlockPos pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
            String id = nbtCompound.getString("id");
            if (world != null && id != null) {
                return new NetworkRecorder(world, pos, id);
            }
            return null;
        } catch (Throwable throwable) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkRecorder that)) return false;
        return Objects.equals(world, that.world) && Objects.equals(pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, pos);
    }

    public @NotNull String getWorld() {
        return world;
    }

    @Nullable
    public World getWorld(ServerWorld world) {
        Identifier identifier = Identifier.tryParse(this.world);
        if (identifier == null) {
            return null;
        }
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, identifier);
        if (worldKey == null) {
            return null;
        }
        return world.getServer().getWorld(worldKey);
    }

    public @NotNull BlockPos getPos() {
        return pos;
    }

    public @NotNull String getId() {
        return id;
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

    public boolean updateId(@NotNull String id) {
        if (!this.getId().equals(id)) {
            this.setId(id);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "NetworkRecorder[" +
                "world=" + world + ", " +
                "pos=" + pos + ", " +
                "id=" + id + ']';
    }

}
