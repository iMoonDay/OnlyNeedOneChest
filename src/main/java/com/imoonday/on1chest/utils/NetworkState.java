package com.imoonday.on1chest.utils;

import com.imoonday.on1chest.blocks.entities.WirelessConnectorBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NetworkState extends PersistentState {

    Set<NetworkRecorder> networks = new HashSet<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        networks.forEach(networkRecorder -> list.add(networkRecorder.toNbt()));
        nbt.put("networks", list);
        return nbt;
    }

    public static NetworkState createFromNbt(NbtCompound nbtCompound) {
        NetworkState networkState = new NetworkState();
        NbtList list = nbtCompound.getList("networks", NbtElement.COMPOUND_TYPE);
        for (NbtElement nbtElement : list) {
            if (nbtElement instanceof NbtCompound nbt) {
                NetworkRecorder recorder = NetworkRecorder.fromNbt(nbt);
                if (recorder != null) {
                    networkState.networks.add(recorder);
                }
            }
        }
        return networkState;
    }

    public static NetworkState getNetworkState(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(NetworkState::createFromNbt, NetworkState::new, "on1chest");
    }

    public List<NetworkRecorder> getOtherNetworks(String id) {
        return networks.stream().filter(networkRecorder -> networkRecorder.getId().equals(id)).collect(Collectors.toList());
    }

    public void checkValid(ServerWorld serverWorld) {
        networks.removeIf(networkRecorder -> {
            World world = networkRecorder.getWorld(serverWorld);
            if (world == null) {
                return true;
            }
            if (!(world.getBlockEntity(networkRecorder.getPos()) instanceof WirelessConnectorBlockEntity entity)) {
                return true;
            }
            networkRecorder.updateId(entity.getNetwork());
            return false;
        });
        markDirty();
    }

    public NetworkRecorder getNetwork(World world, BlockPos pos, String id) {
        return networks.stream().filter(networkRecorder -> world.getRegistryKey().getValue().toString().equals(networkRecorder.getWorld()) && pos.equals(networkRecorder.getPos()) && id.equals(networkRecorder.getId())).findFirst().orElseGet(() -> {
            NetworkRecorder recorder = new NetworkRecorder(world.getRegistryKey().getValue().toString(), pos, id);
            networks.add(recorder);
            markDirty();
            return recorder;
        });
    }
}
