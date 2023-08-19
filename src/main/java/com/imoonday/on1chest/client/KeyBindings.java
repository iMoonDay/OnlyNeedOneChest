package com.imoonday.on1chest.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static KeyBinding markItemStackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.on1chest.mark_item_stack", GLFW.GLFW_KEY_C, "key.categories.on1chest"));
    public static KeyBinding takeAllStacksKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.on1chest.take_all_stacks", GLFW.GLFW_KEY_SPACE, "key.categories.on1chest"));

    public static void register() {

    }
}
