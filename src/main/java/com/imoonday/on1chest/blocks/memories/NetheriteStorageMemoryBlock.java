package com.imoonday.on1chest.blocks.memories;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;

public class DiamondStorageMemoryBlock extends StorageMemoryBlock {
    public DiamondStorageMemoryBlock(Settings settings) {
        super(settings);
    }

    @Override
    public int getLevel() {
        return 4;
    }
}
