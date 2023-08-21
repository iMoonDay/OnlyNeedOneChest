package com.imoonday.on1chest.utils;

import net.minecraft.registry.Registries;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;

public enum SortComparator {

    ID("sort.on1chest.raw_id", stack -> Registries.ITEM.getRawId(stack.getStack().getItem())),
    NAME("sort.on1chest.name", stack -> stack.getStack().getName().getString()),
    MOD("sort.on1chest.mod", stack -> Registries.ITEM.getId(stack.getStack().getItem()).getNamespace()),
    COUNT("sort.on1chest.count", CombinedItemStack::getCount),
    DAMAGE("sort.on1chest.damage", itemStack -> itemStack.getStack().getDamage()),
    RARITY("sort.on1chest.rarity", itemStack -> itemStack.getStack().getRarity());

    public final String translationKey;
    private final Comparator<CombinedItemStack> comparator;

    <T extends Comparable<? super T>> SortComparator(String translationKey, Function<CombinedItemStack, ? extends T> comparator) {
        this.translationKey = translationKey;

        this.comparator = Comparator.comparing(comparator).thenComparing(stack -> {
            String name = stack.getStack().getName().getString();
            try {
                name = ChineseUtils.toPinyin(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return name;
        }).thenComparingLong(CombinedItemStack::getCount).thenComparingInt(value -> value.getStack().getDamage());
    }

    public Comparator<CombinedItemStack> createComparator(Collection<FavouriteItemStack> favouriteStacks, boolean reversed) {
        return Comparator.<CombinedItemStack, Boolean>comparing(stack -> favouriteStacks.stream().anyMatch(favouriteItemStack -> favouriteItemStack.equals(stack.getStack()))).reversed().thenComparing(reversed ? comparator.reversed() : comparator);
    }

    public SortComparator next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
