package com.imoonday.on1chest.utils;

import net.minecraft.nbt.NbtCompound;

import java.util.List;

public interface IAutoCraftingHandler {

    void sendMessage(NbtCompound compound);

    List<CombinedItemStack> getStoredItems();
}
