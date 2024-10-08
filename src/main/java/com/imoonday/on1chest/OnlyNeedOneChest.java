package com.imoonday.on1chest;

import com.imoonday.on1chest.api.IScreenDataReceiver;
import com.imoonday.on1chest.init.*;
import com.imoonday.on1chest.network.NetworkHandler;
import com.imoonday.on1chest.utils.CraftingRecipeTreeManager;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public class OnlyNeedOneChest implements ModInitializer {

    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModRecipes.register();
        ModScreens.register();
        ModGameRules.register();
        registerGlobalReceiver();
        registerRecipeTreeManagerEvents();
    }

    private static void registerRecipeTreeManagerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (CraftingRecipeTreeManager.getOrCreate(server.getRecipeManager(), server.getRegistryManager()) != null) {
                LOGGER.info("Server recipe loaded successfully");
            }
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                CraftingRecipeTreeManager.getOrCreate(server.getRecipeManager(), server.getRegistryManager()).reload();
                PlayerLookup.all(server).forEach(NetworkHandler::updateRecipeManager);
                LOGGER.info("Server recipe reloaded successfully");
            }
        });
    }

    private void registerGlobalReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(NetworkHandler.C2S, (server, player, handler, buf, sender) -> {
            NbtCompound nbt = buf.readUnlimitedNbt();
            server.execute(() -> {
                if (player.currentScreenHandler instanceof IScreenDataReceiver receiver) {
                    receiver.receive(nbt);
                }
            });
        });
    }

    public static Identifier id(String id) {
        return new Identifier("on1chest", id);
    }
}