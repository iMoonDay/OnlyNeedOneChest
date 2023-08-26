package com.imoonday.on1chest.blocks.entities;

import com.google.common.collect.ImmutableList;
import com.imoonday.on1chest.blocks.WirelessConnectorBlock;
import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.screen.WirelessNetworkScreenHandler;
import com.imoonday.on1chest.utils.NetworkRecorder;
import com.imoonday.on1chest.utils.NetworkState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WirelessConnectorBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {

    private @NotNull String network = "";
    private final List<Pair<World, BlockPos>> networks = new ArrayList<>();

    public WirelessConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WIRELESS_CONNECTOR_BLOCK_ENTITY, pos, state);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.on1chest.wireless_connector.title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new WirelessNetworkScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("network", NbtElement.STRING_TYPE)) {
            this.network = nbt.getString("network");
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putString("network", network);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public @NotNull String getNetwork() {
        return network;
    }

    public void setNetwork(@NotNull String network) {
        this.network = network;
    }

    public ImmutableList<Pair<World, BlockPos>> getNetworks() {
        return ImmutableList.copyOf(networks);
    }

    public static void tick(World world, BlockPos pos, BlockState state, WirelessConnectorBlockEntity entity) {
        if (world.getTime() % 20 == 0 && world instanceof ServerWorld serverWorld) {
            NetworkState networkState = NetworkState.getNetworkState(serverWorld.getServer());
            NetworkRecorder networkRecorder = networkState.getNetwork(world, pos, entity.network);
            if (networkRecorder.updateId(entity.network)) {
                networkState.markDirty();
            }
            entity.networks.clear();
            networkState.checkValid(serverWorld);
            if (!entity.network.isEmpty()) {
                List<NetworkRecorder> recorders = networkState.getOtherNetworks(entity.network);
                for (NetworkRecorder recorder : recorders) {
                    if (recorder.getWorld().equals(world.getRegistryKey().getValue().toString()) && recorder.getPos().equals(pos)) {
                        continue;
                    }
                    entity.networks.add(new Pair<>(recorder.getWorld(serverWorld), recorder.getPos()));
                }
            }
        }
        WirelessConnectorBlock.ConnectionStatus status = entity.network.isEmpty() ? WirelessConnectorBlock.ConnectionStatus.OFF : (entity.networks.isEmpty() ? WirelessConnectorBlock.ConnectionStatus.ON : WirelessConnectorBlock.ConnectionStatus.CONNECTED);
        world.setBlockState(pos, state.with(WirelessConnectorBlock.STATUS, status), Block.NOTIFY_LISTENERS);
    }
}
