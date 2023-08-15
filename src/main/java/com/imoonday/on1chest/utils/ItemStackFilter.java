package com.imoonday.on1chest.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.function.Predicate;

public enum ItemStackFilter {

    IS_ENCHANTED("filter.on1chest.is_enchanted", stack -> stack.hasEnchantments() || stack.isOf(Items.ENCHANTED_BOOK)),
    HAS_NBT("filter.on1chest.has_nbt", ItemStack::hasNbt),
    IS_DAMAGED("filter.on1chest.is_damaged", ItemStack::isDamaged),
    IS_FOOD("filter.on1chest.is_food", ItemStack::isFood);

    private final String translationKey;
    private final Predicate<ItemStack> predicate;

    ItemStackFilter(String translationKey, Predicate<ItemStack> predicate) {
        this.translationKey = translationKey;
        this.predicate = predicate;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public Predicate<ItemStack> getPredicate() {
        return predicate;
    }
}
