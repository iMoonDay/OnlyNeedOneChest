package com.imoonday.on1chest.client;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.config.ScreenConfig;
import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.utils.IScreenDataReceiver;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class OnlyNeedOneChestClient implements ClientModInitializer {

    public static final Identifier S2C = OnlyNeedOneChest.id("s2c");

    @Override
    public void onInitializeClient() {
        ScreenConfig.initConfig();
        ModScreens.registerClient();
        registerGlobalReceiver();
        KeyBindings.register();
    }

    @Environment(EnvType.CLIENT)
    private void registerGlobalReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(S2C, (client, handler, buf, sender) -> {
            NbtCompound nbt = buf.readUnlimitedNbt();
            client.execute(() -> {
                if (client.currentScreen instanceof IScreenDataReceiver receiver) {
                    receiver.receive(nbt);
                }
            });
        });
    }
}
