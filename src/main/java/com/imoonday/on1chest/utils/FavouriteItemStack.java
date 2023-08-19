package com.imoonday.on1chest.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class FavouriteItemStack {

    private final Identifier id;
    private final NbtCompound nbt;

    public FavouriteItemStack(ItemStack stack) {
        this.id = Registries.ITEM.getId(stack.getItem());
        this.nbt = stack.getNbt();
    }

    public ItemStack getStack() {
        ItemStack stack = new ItemStack(Registries.ITEM.get(id));
        stack.setNbt(nbt);
        return stack;
    }

    public boolean equals(ItemStack stack) {
        return ItemStack.canCombine(stack, getStack());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavouriteItemStack that)) return false;
        return this.equals(that.getStack());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nbt);
    }
}
