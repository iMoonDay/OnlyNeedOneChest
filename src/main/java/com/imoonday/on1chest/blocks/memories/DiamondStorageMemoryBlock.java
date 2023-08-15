package com.imoonday.on1chest.blocks.memories;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModItems;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

public class DiamondStorageMemoryBlock extends StorageMemoryBlock {
    public DiamondStorageMemoryBlock(Settings settings) {
        super(settings);
    }

    @Override
    public int getLevel() {
        return 4;
    }

    @Override
    public Map<Item, StorageMemoryBlock> getLevelUpEntries() {
        Map<Item, StorageMemoryBlock> map = new HashMap<>();
        map.put(ModItems.DIAMOND_TO_NETHERITE_EXPAND_MODULE, ModBlocks.NETHERITE_STORAGE_MEMORY_BLOCK);
        return map;
    }
}
