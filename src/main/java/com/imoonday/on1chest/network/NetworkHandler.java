package com.imoonday.on1chest.network;

import com.imoonday.on1chest.OnlyNeedOneChest;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class NetworkHandler {

    public static final Identifier S2C = OnlyNeedOneChest.id("s2c");
    public static final Identifier UPDATE_RECIPE = OnlyNeedOneChest.id("update_recipe");
    public static final Identifier C2S = OnlyNeedOneChest.id("c2s");

    public static void sendToServer(NbtCompound nbtCompound) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeNbt(nbtCompound);
        ClientPlayNetworking.send(C2S, buf);
    }

    public static void sendToClient(PlayerEntity player, NbtCompound nbtCompound) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeNbt(nbtCompound);
            ServerPlayNetworking.send(serverPlayer, S2C, buf);
        }
    }

    public static void updateRecipeManager(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, UPDATE_RECIPE, PacketByteBufs.empty());
    }

}
