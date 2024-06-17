package com.imoonday.on1chest;

import com.imoonday.on1chest.api.IScreenDataReceiver;
import com.imoonday.on1chest.init.*;
import com.imoonday.on1chest.network.NetworkHandler;
import com.imoonday.on1chest.utils.CraftingRecipeTreeManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class OnlyNeedOneChest implements ModInitializer {

    public static boolean clothConfig = FabricLoader.getInstance().isModLoaded("cloth-config");

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
                System.out.println("On1chest: Server recipe loaded successfully");
            }
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                CraftingRecipeTreeManager.getOrCreate(server.getRecipeManager(), server.getRegistryManager()).reload();
                PlayerLookup.all(server).forEach(NetworkHandler::updateRecipeManager);
                System.out.println("On1chest: Server recipe reloaded successfully");
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