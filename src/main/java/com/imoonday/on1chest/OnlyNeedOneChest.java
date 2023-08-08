package com.imoonday.on1chest;

import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class OnlyNeedOneChest implements ModInitializer {

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModItems.register();
    }

    public static Identifier id(String id) {
        return new Identifier("on1chest", id);
    }
}