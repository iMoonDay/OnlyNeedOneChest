package com.imoonday.on1chest.init;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.blocks.StorageAccessorBlock;
import com.imoonday.on1chest.blocks.StorageBlankBlock;
import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.blocks.StorageProcessorBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class ModBlocks {

    public static final Map<String, Block> BLOCK_WITH_ITEMS = new HashMap<>();

    public static final Block STORAGE_BLANK_BLOCK = register("storage_blank_block", new StorageBlankBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final Block STORAGE_MEMORY_BLOCK = register("storage_memory_block", new StorageMemoryBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final Block STORAGE_ACCESSOR_BLOCK = register("storage_accessor_block", new StorageAccessorBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));
    public static final Block STORAGE_PROCESSOR_BLOCK = register("storage_processor_block", new StorageProcessorBlock(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));

    public static void register(){

    }

    private static <T extends Block> T register(String id, T block) {
        Registry.register(Registries.BLOCK, OnlyNeedOneChest.id(id), block);
        BLOCK_WITH_ITEMS.put(id, block);
        return block;
    }
}
