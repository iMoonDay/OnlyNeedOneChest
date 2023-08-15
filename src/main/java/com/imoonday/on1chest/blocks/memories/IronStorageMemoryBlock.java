package com.imoonday.on1chest.blocks.memories;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModItems;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

public class IronStorageMemoryBlock extends StorageMemoryBlock {
    public IronStorageMemoryBlock(Settings settings) {
        super(settings);
    }

    @Override
    public int getLevel() {
        return 2;
    }

    @Override
    public Map<Item, StorageMemoryBlock> getLevelUpEntries() {
        Map<Item, StorageMemoryBlock> map = new HashMap<>();
        map.put(ModItems.IRON_TO_GOLD_EXPAND_MODULE, ModBlocks.GOLD_STORAGE_MEMORY_BLOCK);
        return map;
    }
}
