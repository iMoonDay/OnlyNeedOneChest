package com.imoonday.on1chest.client;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.config.Config;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static KeyBinding screenKey;

    public static void registerKeys() {
        screenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.on1chest.setting_screen", GLFW.GLFW_KEY_N, "group.on1chest.storages"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!OnlyNeedOneChest.clothConfig) {
                return;
            }
            while (screenKey.wasPressed()) {
                client.setScreen(Config.createConfigScreen(client.currentScreen));
            }
        });
    }
}
