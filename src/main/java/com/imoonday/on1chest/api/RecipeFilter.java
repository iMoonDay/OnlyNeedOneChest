package com.imoonday.on1chest.api;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

public interface RecipeFilter {
    default boolean shouldFilter() {
        return true;
    }

    boolean testIngredient(MinecraftServer server, ItemStack stack);

    boolean testOutput(MinecraftServer server, ItemStack stack);
}
