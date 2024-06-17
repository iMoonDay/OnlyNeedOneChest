package com.imoonday.on1chest.datagen;

import com.imoonday.on1chest.init.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlockTagGen extends FabricTagProvider<Block> {

    public static final Map<Block, TagKey<Block>> TAGS = new HashMap<>();

    public BlockTagGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.BLOCK, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.OBSIDIAN_STORAGE_MEMORY_BLOCK);
        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.QUICK_CRAFTING_TABLE);
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ModBlocks.OBSIDIAN_STORAGE_MEMORY_BLOCK)
                .add(ModBlocks.COMPRESSED_STORAGE_MEMORY_BLOCK)
                .add(ModBlocks.STORAGE_RECYCLE_BIN)
                .add(ModBlocks.WIRELESS_CONNECTOR)
                .add(ModBlocks.RECIPE_PROCESSOR);
        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(ModBlocks.STORAGE_BLANK_BLOCK)
                .add(ModBlocks.STORAGE_ACCESSOR_BLOCK)
                .add(ModBlocks.STORAGE_PROCESSOR_BLOCK)
                .add(ModBlocks.WOOD_STORAGE_MEMORY_BLOCK)
                .add(ModBlocks.COPPER_STORAGE_MEMORY_BLOCK)
                .add(ModBlocks.IRON_STORAGE_MEMORY_BLOCK)
                .add(ModBlocks.GOLD_STORAGE_MEMORY_BLOCK)
                .add(ModBlocks.DIAMOND_STORAGE_MEMORY_BLOCK)
                .add(ModBlocks.NETHERITE_STORAGE_MEMORY_BLOCK)
                .add(ModBlocks.CONNECTION_CABLE)
                .add(ModBlocks.ITEM_EXPORTER)
                .add(ModBlocks.MEMORY_EXTRACTOR)
                .add(ModBlocks.QUICK_CRAFTING_TABLE)
                .add(ModBlocks.MEMORY_CONVERTER);
        TAGS.forEach((block, key) -> getOrCreateTagBuilder(key).add(block));
    }
}
