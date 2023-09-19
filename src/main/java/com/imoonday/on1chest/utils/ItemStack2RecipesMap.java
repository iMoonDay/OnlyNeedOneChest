package com.imoonday.on1chest.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;

import java.util.HashSet;
import java.util.Set;

public class ItemStack2RecipesMap extends ItemStack2ObjectMap<Set<DefaultedList<Ingredient>>> {

    public ItemStack2RecipesMap(boolean compareCount) {
        super(compareCount);
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
