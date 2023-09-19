package com.imoonday.on1chest.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class ItemStack2ObjectMap<T> extends Object2ObjectLinkedOpenCustomHashMap<ItemStack, T> {

    protected final boolean compareCount;

    public ItemStack2ObjectMap(boolean compareCount) {
        super((createStrategy(compareCount)));
        this.compareCount = compareCount;
    }

    public static int getHashCode(@Nullable ItemStack stack) {
        if (stack != null) {
            NbtCompound nbtCompound = stack.getNbt();
            int i = 31 + stack.getItem().hashCode();
            return 31 * i + (nbtCompound == null ? 0 : nbtCompound.hashCode());
        }
        return 0;
    }

    private static Strategy<ItemStack> createStrategy(boolean checkCount) {
        return new Strategy<>() {

            @Override
            public int hashCode(@Nullable ItemStack itemStack) {
                return ItemStack2ObjectMap.getHashCode(itemStack);
            }

            @Override
            public boolean equals(@Nullable ItemStack itemStack, @Nullable ItemStack itemStack2) {
                return itemStack == itemStack2 || itemStack != null && itemStack2 != null && itemStack.isEmpty() == itemStack2.isEmpty() && (checkCount ? ItemStack.areEqual(itemStack, itemStack2) : ItemStack.canCombine(itemStack, itemStack2));
            }
        };
    }
}
