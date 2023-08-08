package com.imoonday.on1chest.init;

import com.imoonday.on1chest.OnlyNeedOneChest;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {

    public static void register() {
        ModBlocks.BLOCK_WITH_ITEMS.forEach((s, block) -> Registry.register(Registries.ITEM, OnlyNeedOneChest.id(s), new BlockItem(block, new FabricItemSettings())));
    }
}
