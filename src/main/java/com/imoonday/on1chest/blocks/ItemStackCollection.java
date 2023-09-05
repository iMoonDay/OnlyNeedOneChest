package com.imoonday.on1chest.blocks;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ItemStackCollection {

    public final boolean isSet;
    private final List<ItemStack> list = new ArrayList<>();

    private ItemStackCollection(boolean isSet) {
        this.isSet = isSet;
    }

    public ItemStackCollection(boolean isSet, @NotNull Collection<ItemStack> itemStacks) {
        this.isSet = isSet;
        this.list.addAll(itemStacks);
    }

    public static ItemStackCollection createList() {
        return new ItemStackCollection(false);
    }

    public static ItemStackCollection createSet() {
        return new ItemStackCollection(true);
    }

    public boolean isSet() {
        return isSet;
    }

    public int size() {
        return list.size();
    }

    public ItemStack get(int index) {
        return list.get(index);
    }

    public boolean add(ItemStack stack) {
        if (isSet && contains(stack)) {
            return false;
        }
        return list.add(stack);
    }

    public boolean remove(ItemStack stack) {
        return list.removeIf(stack1 -> ItemStack.canCombine(stack, stack1));
    }

    public boolean contains(ItemStack stack) {
        return list.stream().anyMatch(stack1 -> ItemStack.canCombine(stack, stack1));
    }

    public List<ItemStack> get(ItemStack stack) {
        return list.stream().filter(stack1 -> ItemStack.canCombine(stack, stack1)).collect(Collectors.toList());
    }

    public List<ItemStack> get() {
        return list;
    }
}
