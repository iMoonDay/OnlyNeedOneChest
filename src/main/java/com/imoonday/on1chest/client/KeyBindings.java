package com.imoonday.on1chest.client;

import com.imoonday.on1chest.config.ConfigScreenHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static KeyBinding screenKey;

    public static void registerKeys() {
        if (!OnlyNeedOneChestClient.clothConfig) return;
        screenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.on1chest.setting_screen", GLFW.GLFW_KEY_N, "group.on1chest.storages"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (screenKey.wasPressed()) {
                client.setScreen(ConfigScreenHandler.createConfigScreen(client.currentScreen));
            }
        });
    }
}
