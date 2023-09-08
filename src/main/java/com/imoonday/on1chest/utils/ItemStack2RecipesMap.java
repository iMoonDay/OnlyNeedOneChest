package com.imoonday.on1chest.utils;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ItemStack2RecipesMap extends Object2ObjectLinkedOpenCustomHashMap<ItemStack, Set<DefaultedList<Ingredient>>> {
    private static final Hash.Strategy<? super ItemStack> HASH_STRATEGY = new Hash.Strategy<ItemStack>() {

        @Override
        public int hashCode(@Nullable ItemStack itemStack) {
            return getHashCode(itemStack);
        }

        @Override
        public boolean equals(@Nullable ItemStack itemStack, @Nullable ItemStack itemStack2) {
            return itemStack == itemStack2 || itemStack != null && itemStack2 != null && itemStack.isEmpty() == itemStack2.isEmpty() && ItemStack.areEqual(itemStack, itemStack2);
        }
    };

    public ItemStack2RecipesMap() {
        super(HASH_STRATEGY);
    }

    static int getHashCode(@Nullable ItemStack stack) {
        if (stack != null) {
            NbtCompound nbtCompound = stack.getNbt();
            int i = 31 + stack.getItem().hashCode();
            return 31 * i + (nbtCompound == null ? 0 : nbtCompound.hashCode());
        }
        return 0;
    }

    public void putOrAdd(ItemStack stack, DefaultedList<Ingredient> ingredients) {
        if (this.containsKey(stack)) {
            this.get(stack).add(ingredients);
        } else {
            Set<DefaultedList<Ingredient>> list = new HashSet<>();
            list.add(ingredients);
            this.put(stack, list);
        }
    }
}
