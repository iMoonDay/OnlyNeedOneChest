package com.imoonday.on1chest.blocks.memories;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModItems;
import net.minecraft.item.Item;

import java.util.Map;

public class WoodStorageMemoryBlock extends StorageMemoryBlock {

    public WoodStorageMemoryBlock(Settings settings) {
        super(settings);
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public Map<Item, StorageMemoryBlock> getLevelUpEntries() {
        return Map.of(ModItems.WOOD_TO_COPPER_EXPAND_MODULE, ModBlocks.COPPER_STORAGE_MEMORY_BLOCK);
    }
}
