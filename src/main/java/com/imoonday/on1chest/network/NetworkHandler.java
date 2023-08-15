package com.imoonday.on1chest.network;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.client.OnlyNeedOneChestClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

public class NetworkHandler {

    public static void sendToServer(Consumer<NbtCompound> consumer) {
        PacketByteBuf buf = PacketByteBufs.create();
        NbtCompound nbt = new NbtCompound();
        if (consumer != null) {
            consumer.accept(nbt);
        }
        buf.writeNbt(nbt);
        ClientPlayNetworking.send(OnlyNeedOneChest.C2S, buf);
    }

    public static void sendToClient(PlayerEntity player, Consumer<NbtCompound> consumer) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            PacketByteBuf buf = PacketByteBufs.create();
            NbtCompound nbt = new NbtCompound();
            if (consumer != null) {
                consumer.accept(nbt);
            }
            buf.writeNbt(nbt);
            ServerPlayNetworking.send(serverPlayer, OnlyNeedOneChestClient.S2C, buf);
        }
    }

}
