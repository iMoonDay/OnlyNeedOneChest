package com.imoonday.on1chest.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface StorageAccessorEvent {

    Event<OnRemove> REMOVE = EventFactory.createArrayBacked(OnRemove.class, (listeners) -> (inventory, slot) -> {
        for (OnRemove listener : listeners) {
            if (!listener.canRemove(inventory, slot)) {
                return false;
            }
        }
        return true;
    });

    Event<OnInsert> INSERT = EventFactory.createArrayBacked(OnInsert.class, (listeners) -> (inventory, slot, stack) -> {
        for (OnInsert listener : listeners) {
            if (!listener.canInsert(inventory, slot, stack)) {
                return false;
            }
        }
        return true;
    });

    interface OnRemove {

        boolean canRemove(Inventory inventory, int slot);
    }

    interface OnInsert {

        boolean canInsert(Inventory inventory, int slot, ItemStack stack);
    }
}
