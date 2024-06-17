package com.imoonday.on1chest.api;

import net.minecraft.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

public interface ConnectInventoryProvider {

    @Nullable
    Inventory getInventory();
}
