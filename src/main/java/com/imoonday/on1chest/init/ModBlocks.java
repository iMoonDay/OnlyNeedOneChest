package com.imoonday.on1chest.init;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.blocks.*;
import com.imoonday.on1chest.blocks.memories.*;
import com.imoonday.on1chest.datagen.BlockTagGen;
import com.imoonday.on1chest.utils.ConnectBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModBlocks {

    public static final Map<String, Block> BLOCK_WITH_ITEMS = new LinkedHashMap<>();

    public static final StorageMemoryBlock WOOD_STORAGE_MEMORY_BLOCK = register("wood_storage_memory_block", new WoodStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final StorageMemoryBlock COPPER_STORAGE_MEMORY_BLOCK = register("copper_storage_memory_block", new CopperStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final StorageMemoryBlock IRON_STORAGE_MEMORY_BLOCK = register("iron_storage_memory_block", new IronStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final StorageMemoryBlock GOLD_STORAGE_MEMORY_BLOCK = register("gold_storage_memory_block", new GoldStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final StorageMemoryBlock DIAMOND_STORAGE_MEMORY_BLOCK = register("diamond_storage_memory_block", new DiamondStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final StorageMemoryBlock NETHERITE_STORAGE_MEMORY_BLOCK = register("netherite_storage_memory_block", new NetheriteStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final StorageMemoryBlock OBSIDIAN_STORAGE_MEMORY_BLOCK = register("obsidian_storage_memory_block", new ObsidianStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN)));
    public static final StorageMemoryBlock GLASS_STORAGE_MEMORY_BLOCK = register("glass_storage_memory_block", new GlassStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.GLASS)));
    public static final StorageMemoryBlock COMPRESSED_STORAGE_MEMORY_BLOCK = register("compressed_storage_memory_block", new CompressedStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.STONE)));

    public static final Block STORAGE_BLANK_BLOCK = register("storage_blank_block", new StorageBlankBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).burnable()));
    public static final Block STORAGE_ACCESSOR_BLOCK = register("storage_accessor_block", new StorageAccessorBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).burnable()));
    public static final Block STORAGE_PROCESSOR_BLOCK = register("storage_processor_block", new StorageProcessorBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).burnable()));
    public static final Block STORAGE_RECYCLE_BIN = register("storage_recycle_bin", new StorageRecycleBinBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE)));
    public static final Block RECIPE_PROCESSOR = register("recipe_processor", new RecipeProcessorBlock(FabricBlockSettings.copyOf(Blocks.COBBLESTONE)));
    public static final Block QUICK_CRAFTING_TABLE = register("quick_crafting_table", new QuickCraftingTableBlock(FabricBlockSettings.copyOf(Blocks.CRAFTING_TABLE)));

    public static final Block CONNECTION_CABLE = register("connection_cable", new ConnectionCableBlock(FabricBlockSettings.create().mapColor(MapColor.OAK_TAN).sounds(BlockSoundGroup.WOOD).strength(2)));
    public static final Block ITEM_EXPORTER = register("item_exporter", new ItemExporterBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final Block MEMORY_EXTRACTOR = register("memory_extractor", new MemoryExtractorBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final Block WIRELESS_CONNECTOR = register("wireless_connector", new WirelessConnectorBlock(FabricBlockSettings.copyOf(Blocks.LIGHTNING_ROD).mapColor(MapColor.OFF_WHITE)));

    public static void register() {

    }

    private static <T extends Block> T register(String id, T block) {
        Registry.register(Registries.BLOCK, OnlyNeedOneChest.id(id), block);
        BLOCK_WITH_ITEMS.put(id, block);
        if (block instanceof ConnectBlock) {
            BlockTagGen.TAGS.put(block, ModTags.CONNECT_BLOCK);
        }
        return block;
    }
}
