package com.imoonday.on1chest.blocks.memories;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModItems;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

public class CopperStorageMemoryBlock extends StorageMemoryBlock {
    public CopperStorageMemoryBlock(Settings settings) {
        super(settings);
    }

    @Override
    public int getLevel() {
        return 1;
    }

    @Override
    public Map<Item, StorageMemoryBlock> getLevelUpEntries() {
        Map<Item, StorageMemoryBlock> map = new HashMap<>();
        map.put(ModItems.COPPER_TO_IRON_EXPAND_MODULE, ModBlocks.IRON_STORAGE_MEMORY_BLOCK);
        return map;
    }
}
