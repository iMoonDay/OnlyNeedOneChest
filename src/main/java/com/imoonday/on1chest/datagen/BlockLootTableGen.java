package com.imoonday.on1chest.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.CopyNbtLootFunction;
import net.minecraft.loot.provider.nbt.ContextLootNbtProvider;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;

import static com.imoonday.on1chest.init.ModBlocks.*;

public class BlockLootTableGen extends FabricBlockLootTableProvider {

    public BlockLootTableGen(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generate() {
        this.addDrop(STORAGE_BLANK_BLOCK);
        this.addDrop(STORAGE_ACCESSOR_BLOCK);
        this.addDrop(STORAGE_PROCESSOR_BLOCK);
        this.addDrop(STORAGE_RECYCLE_BIN);
        this.addDrop(WOOD_STORAGE_MEMORY_BLOCK);
        this.addDrop(COPPER_STORAGE_MEMORY_BLOCK);
        this.addDrop(IRON_STORAGE_MEMORY_BLOCK);
        this.addDrop(GOLD_STORAGE_MEMORY_BLOCK);
        this.addDrop(DIAMOND_STORAGE_MEMORY_BLOCK);
        this.addDrop(NETHERITE_STORAGE_MEMORY_BLOCK);
        this.addDrop(OBSIDIAN_STORAGE_MEMORY_BLOCK);
        this.addDrop(COMPRESSED_STORAGE_MEMORY_BLOCK, LootTable.builder().pool(this.addSurvivesExplosionCondition(WIRELESS_CONNECTOR, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(COMPRESSED_STORAGE_MEMORY_BLOCK).apply(CopyNbtLootFunction.builder(ContextLootNbtProvider.BLOCK_ENTITY).withOperation("Level", "CompressionLevel"))))));
        this.addDrop(WIRELESS_CONNECTOR, LootTable.builder().pool(this.addSurvivesExplosionCondition(WIRELESS_CONNECTOR, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(WIRELESS_CONNECTOR).apply(CopyNbtLootFunction.builder(ContextLootNbtProvider.BLOCK_ENTITY).withOperation("network", "BlockEntityTag.network"))))));
        this.addDrop(ITEM_EXPORTER);
        this.addDrop(MEMORY_EXTRACTOR);
        this.addDrop(CONNECTION_CABLE);
        this.addDrop(RECIPE_PROCESSOR);
        this.addDrop(QUICK_CRAFTING_TABLE);
        this.addDrop(MEMORY_CONVERTER);
        this.addDropWithSilkTouch(GLASS_STORAGE_MEMORY_BLOCK);
        this.addDropWithSilkTouch(DISPLAY_STORAGE_MEMORY_BLOCK);
    }
}
