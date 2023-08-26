package com.imoonday.on1chest.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;

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
        this.addDrop(ITEM_EXPORTER);
        this.addDrop(MEMORY_EXTRACTOR);
        this.addDrop(WIRELESS_CONNECTOR);
        this.addDrop(CONNECTION_CABLE);
        this.addDropWithSilkTouch(GLASS_STORAGE_MEMORY_BLOCK);
    }
}
