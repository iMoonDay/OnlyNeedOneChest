package com.imoonday.on1chest.blocks.memories;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

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
        map.put(Items.DIAMOND_BLOCK, ModBlocks.DIAMOND_STORAGE_MEMORY_BLOCK);
        map.put(Items.OBSIDIAN, ModBlocks.OBSIDIAN_STORAGE_MEMORY_BLOCK);
        return map;
    }
}
