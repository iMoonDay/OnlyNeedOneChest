package com.imoonday.on1chest.api;

import net.minecraft.nbt.NbtCompound;

public interface IScreenDataReceiver {

    void receive(NbtCompound nbt);

    default void update() {

    }
}
