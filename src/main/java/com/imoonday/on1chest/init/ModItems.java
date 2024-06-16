package com.imoonday.on1chest.init;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.items.RecipeRecordCardItem;
import com.imoonday.on1chest.items.RemoteAccessorItem;
import com.imoonday.on1chest.items.VanillaToWoodConversionModuleItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class ModItems {

    public static final RegistryKey<ItemGroup> STORAGES = RegistryKey.of(RegistryKeys.ITEM_GROUP, OnlyNeedOneChest.id("storages"));

    public static final Item VANILLA_TO_WOOD_CONVERSION_MODULE = register("vanilla_to_wood_conversion_module", new VanillaToWoodConversionModuleItem(new FabricItemSettings()));
    public static final Item WOOD_TO_COPPER_EXPAND_MODULE = register("wood_to_copper_expansion_module", new Item(new FabricItemSettings()));
    public static final Item COPPER_TO_IRON_EXPAND_MODULE = register("copper_to_iron_expansion_module", new Item(new FabricItemSettings()));
    public static final Item IRON_TO_GOLD_EXPAND_MODULE = register("iron_to_gold_expansion_module", new Item(new FabricItemSettings()));
    public static final Item GOLD_TO_DIAMOND_EXPAND_MODULE = register("gold_to_diamond_expansion_module", new Item(new FabricItemSettings()));
    public static final Item DIAMOND_TO_NETHERITE_EXPAND_MODULE = register("diamond_to_netherite_expansion_module", new Item(new FabricItemSettings()));
    public static final Item GOLD_TO_OBSIDIAN_EXPAND_MODULE = register("gold_to_obsidian_expansion_module", new Item(new FabricItemSettings()));
    public static final Item COMPRESSION_UPGRADE_MODULE = register("compression_upgrade_module", new Item(new FabricItemSettings()));

    public static final Item BASIC_REMOTE_ACCESSOR = register("basic_remote_accessor", new RemoteAccessorItem(new FabricItemSettings().maxCount(1), false));
    public static final Item ADVANCED_REMOTE_ACCESSOR = register("advanced_remote_accessor", new RemoteAccessorItem(new FabricItemSettings().maxCount(1), true));

    public static final Item RECIPE_RECORD_CARD = register("recipe_record_card", new RecipeRecordCardItem(new FabricItemSettings().maxCount(1)));

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, STORAGES, FabricItemGroup.builder().displayName(Text.translatable("group.on1chest.storages")).icon(() -> new ItemStack(ModBlocks.STORAGE_ACCESSOR_BLOCK)).build());
        ModBlocks.BLOCK_WITH_ITEMS.forEach((id, block) -> {
            BlockItem blockItem = new BlockItem(block, new FabricItemSettings());
            Registry.register(Registries.ITEM, OnlyNeedOneChest.id(id), blockItem);
            ItemGroupEvents.modifyEntriesEvent(STORAGES).register(entries -> entries.add(blockItem));
        });
    }

    public static Item register(String id, Item item) {
        ItemGroupEvents.modifyEntriesEvent(STORAGES).register(entries -> entries.add(item));
        return Registry.register(Registries.ITEM, OnlyNeedOneChest.id(id), item);
    }
}
