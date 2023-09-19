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

public class NetworkHandler {

    public static void sendToServer(NbtCompound nbtCompound) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeNbt(nbtCompound);
        ClientPlayNetworking.send(OnlyNeedOneChest.C2S, buf);
    }

    public static void sendToClient(PlayerEntity player, NbtCompound nbtCompound) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeNbt(nbtCompound);
            ServerPlayNetworking.send(serverPlayer, OnlyNeedOneChestClient.S2C, buf);
        }
    }

    public static void updateRecipeManager(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, OnlyNeedOneChestClient.UPDATE_RECIPE, PacketByteBufs.empty());
    }

}
