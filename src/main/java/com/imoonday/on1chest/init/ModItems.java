package com.imoonday.on1chest.init;

import com.imoonday.on1chest.OnlyNeedOneChest;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class ModItems {

    public static final RegistryKey<ItemGroup> STORAGES = RegistryKey.of(RegistryKeys.ITEM_GROUP, OnlyNeedOneChest.id("storages"));

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, STORAGES, FabricItemGroup.builder().displayName(Text.translatable("group.on1chest.storages")).icon(() -> new ItemStack(ModBlocks.STORAGE_ACCESSOR_BLOCK)).build());
        ModBlocks.BLOCK_WITH_ITEMS.forEach((id, block) -> {
            BlockItem blockItem = new BlockItem(block, new FabricItemSettings());
            Registry.register(Registries.ITEM, OnlyNeedOneChest.id(id), blockItem);
            ItemGroupEvents.modifyEntriesEvent(STORAGES).register(entries -> entries.add(blockItem));
        });
    }
}
