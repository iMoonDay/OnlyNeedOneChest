package com.imoonday.on1chest.blocks.memories;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;

public class GoldStorageMemoryBlock extends StorageMemoryBlock {
    public GoldStorageMemoryBlock(Settings settings) {
        super(settings);
    }

    @Override
    public int getLevel() {
        return 3;
    }
}
