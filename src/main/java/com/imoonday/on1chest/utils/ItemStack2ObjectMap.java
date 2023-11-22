package com.imoonday.on1chest.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public class ItemStack2ObjectMap<T> extends Object2ObjectLinkedOpenCustomHashMap<ItemStack, T> {

    protected final boolean compareCount;

    public ItemStack2ObjectMap(boolean compareCount) {
        super((createStrategy(compareCount)));
        this.compareCount = compareCount;
    }

    public static ItemStack2ObjectMap<Integer> createIntegerMap(Collection<ItemStack> collection) {
        ItemStack2ObjectMap<Integer> map = new ItemStack2ObjectMap<>(false);
        for (ItemStack itemStack : collection) {
            map.merge(itemStack.copyWithCount(1), itemStack.getCount(), Integer::sum);
        }
        return map;
    }

    public <M> ItemStack2ObjectMap<M> map(BiFunction<ItemStack, T, M> valueFunction, BiFunction<ItemStack, M, M> remappingFunction) {
        return this.map((stack, t) -> stack, valueFunction, remappingFunction);
    }

    public <M> ItemStack2ObjectMap<M> map(BiFunction<ItemStack, T, ItemStack> keyFunction, BiFunction<ItemStack, T, M> valueFunction, BiFunction<ItemStack, M, M> conflictFunction) {
        ItemStack2ObjectMap<M> newMap = new ItemStack2ObjectMap<>(this.compareCount);
        for (Map.Entry<ItemStack, T> entry : this.entrySet()) {
            ItemStack stack = entry.getKey().copy();
            T t = entry.getValue();
            if (newMap.containsKey(stack)) {
                newMap.put(stack, conflictFunction.apply(stack, newMap.get(stack)));
            } else {
                newMap.put(keyFunction.apply(stack, t), valueFunction.apply(stack, t));
            }
        }
        return newMap;
    }

    public boolean removeIf(BiFunction<ItemStack, T, Boolean> predicate) {
        boolean removed = false;
        Set<ItemStack> removeSet = ItemStackSet.create();
        for (Map.Entry<ItemStack, T> entry : this.entrySet()) {
            if (predicate.apply(entry.getKey(), entry.getValue())) {
                removeSet.add(entry.getKey());
                removed = true;
            }
        }
        for (ItemStack itemStack : removeSet) {
            this.remove(itemStack);
        }
        return removed;
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
