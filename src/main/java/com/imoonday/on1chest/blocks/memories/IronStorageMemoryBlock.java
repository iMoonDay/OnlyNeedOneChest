package com.imoonday.on1chest.blocks.memories;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;

public class CopperStorageMemoryBlock extends StorageMemoryBlock {
    public CopperStorageMemoryBlock(Settings settings) {
        super(settings);
    }

    @Override
    public int getLevel() {
        return 1;
    }
}
