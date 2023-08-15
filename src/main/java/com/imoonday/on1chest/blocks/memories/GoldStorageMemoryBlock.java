package com.imoonday.on1chest.blocks.memories;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModItems;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

public class GoldStorageMemoryBlock extends StorageMemoryBlock {
    public GoldStorageMemoryBlock(Settings settings) {
        super(settings);
    }

    @Override
    public int getLevel() {
        return 3;
    }

    @Override
    public Map<Item, StorageMemoryBlock> getLevelUpEntries() {
        Map<Item, StorageMemoryBlock> map = new HashMap<>();
        map.put(ModItems.GOLD_TO_DIAMOND_EXPAND_MODULE, ModBlocks.DIAMOND_STORAGE_MEMORY_BLOCK);
        map.put(ModItems.GOLD_TO_OBSIDIAN_EXPAND_MODULE, ModBlocks.OBSIDIAN_STORAGE_MEMORY_BLOCK);
        return map;
    }
}
