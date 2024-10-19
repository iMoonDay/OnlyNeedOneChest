package com.imoonday.on1chest.api;

import net.minecraft.item.ItemStack;

/**
 * Inventory interface with priority when inserting items in MultiInventory.
 */
public interface PrioritizedInventory extends ImplementedInventory {

    default boolean isPrioritizedFor(ItemStack stack) {
        return true;
    }

    default int getPriorityFor(ItemStack stack) {
        return 0;
    }
}
