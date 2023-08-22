package com.imoonday.on1chest.init;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.blocks.StorageAccessorBlock;
import com.imoonday.on1chest.blocks.StorageBlankBlock;
import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.blocks.StorageProcessorBlock;
import com.imoonday.on1chest.blocks.entities.GlassStorageMemoryBlockEntity;
import com.imoonday.on1chest.blocks.entities.StorageAccessorBlockEntity;
import com.imoonday.on1chest.blocks.entities.StorageMemoryBlockEntity;
import com.imoonday.on1chest.blocks.entities.StorageProcessorBlockEntity;
import com.imoonday.on1chest.blocks.memories.*;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModBlocks {

    public static final Map<String, Block> BLOCK_WITH_ITEMS = new LinkedHashMap<>();

    public static final Block STORAGE_BLANK_BLOCK = register("storage_blank_block", new StorageBlankBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).burnable()));
    public static final Block STORAGE_ACCESSOR_BLOCK = register("storage_accessor_block", new StorageAccessorBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).burnable()));
    public static final Block STORAGE_PROCESSOR_BLOCK = register("storage_processor_block", new StorageProcessorBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).burnable()));

    public static final StorageMemoryBlock WOOD_STORAGE_MEMORY_BLOCK = register("wood_storage_memory_block", new WoodStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).burnable()));
    public static final StorageMemoryBlock COPPER_STORAGE_MEMORY_BLOCK = register("copper_storage_memory_block", new CopperStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final StorageMemoryBlock IRON_STORAGE_MEMORY_BLOCK = register("iron_storage_memory_block", new IronStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final StorageMemoryBlock GOLD_STORAGE_MEMORY_BLOCK = register("gold_storage_memory_block", new GoldStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final StorageMemoryBlock DIAMOND_STORAGE_MEMORY_BLOCK = register("diamond_storage_memory_block", new DiamondStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final StorageMemoryBlock NETHERITE_STORAGE_MEMORY_BLOCK = register("netherite_storage_memory_block", new NetheriteStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final StorageMemoryBlock OBSIDIAN_STORAGE_MEMORY_BLOCK = register("obsidian_storage_memory_block", new ObsidianStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN)));
    public static final StorageMemoryBlock GLASS_STORAGE_MEMORY_BLOCK = register("glass_storage_memory_block", new GlassStorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.GLASS)));

    public static final BlockEntityType<StorageMemoryBlockEntity> STORAGE_MEMORY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, OnlyNeedOneChest.id("storage_memory_block"), FabricBlockEntityTypeBuilder.create(StorageMemoryBlockEntity::new, WOOD_STORAGE_MEMORY_BLOCK, COPPER_STORAGE_MEMORY_BLOCK, IRON_STORAGE_MEMORY_BLOCK, GOLD_STORAGE_MEMORY_BLOCK, DIAMOND_STORAGE_MEMORY_BLOCK, NETHERITE_STORAGE_MEMORY_BLOCK, OBSIDIAN_STORAGE_MEMORY_BLOCK).build());
    public static final BlockEntityType<GlassStorageMemoryBlockEntity> GLASS_STORAGE_MEMORY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, OnlyNeedOneChest.id("glass_storage_memory_block"), FabricBlockEntityTypeBuilder.create(GlassStorageMemoryBlockEntity::new, GLASS_STORAGE_MEMORY_BLOCK).build());
    public static final BlockEntityType<StorageAccessorBlockEntity> STORAGE_ACCESSOR_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, OnlyNeedOneChest.id("storage_accessor_block"), FabricBlockEntityTypeBuilder.create(StorageAccessorBlockEntity::new, STORAGE_ACCESSOR_BLOCK).build());
    public static final BlockEntityType<StorageProcessorBlockEntity> STORAGE_PROCESSOR_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, OnlyNeedOneChest.id("storage_processor_block"), FabricBlockEntityTypeBuilder.create(StorageProcessorBlockEntity::new, STORAGE_PROCESSOR_BLOCK).build());

    public static void register() {

    }

    private static <T extends Block> T register(String id, T block) {
        Registry.register(Registries.BLOCK, OnlyNeedOneChest.id(id), block);
        BLOCK_WITH_ITEMS.put(id, block);
        return block;
    }
}
