package com.imoonday.on1chest.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;

public enum SortComparator {

    ID("sort.on1chest.raw_id", stack -> Registries.ITEM.getRawId(stack.getItem())),
    NAME("sort.on1chest.name", stack -> stack.getName().getString()),
    COUNT("sort.on1chest.count", ItemStack::getCount),
    MAX_COUNT("sort.on1chest.max_count", ItemStack::getMaxCount),
    DAMAGE("sort.on1chest.damage", ItemStack::getDamage),
    RARITY("sort.on1chest.rarity", ItemStack::getRarity);

    public final String translationKey;
    private final Comparator<ItemStack> comparator;

    <T extends Comparable<? super T>> SortComparator(String translationKey, Function<ItemStack, ? extends T> comparator) {
        this.translationKey = translationKey;

        this.comparator = Comparator.comparing(comparator).thenComparing(stack -> {
            String name = stack.getName().getString();
            try {
                name = ChineseUtils.toPinyin(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return name;
        }).thenComparingInt(ItemStack::getCount).thenComparingInt(ItemStack::getDamage);
    }

    public Comparator<ItemStack> getComparator(Collection<ItemStack> favouriteStacks, boolean reversed) {
        return Comparator.<ItemStack, Boolean>comparing(stack -> favouriteStacks.stream().anyMatch(stack1 -> ItemStack.canCombine(stack, stack1))).reversed().thenComparing(reversed ? comparator.reversed() : comparator);
    }

    public SortComparator next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
