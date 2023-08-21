package com.imoonday.on1chest.utils;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

public enum ItemStackFilter {

    ENCHANTED("filter.on1chest.enchanted", stack -> stack.hasEnchantments() || stack.isOf(Items.ENCHANTED_BOOK)),
    HAS_NBT("filter.on1chest.has_nbt", ItemStack::hasNbt),
    DAMAGED("filter.on1chest.damaged", ItemStack::isDamaged),
    FOOD("filter.on1chest.food", ItemStack::isFood),
    MOD("filter.on1chest.mod", stack -> !Registries.ITEM.getId(stack.getItem()).getNamespace().equals(Identifier.DEFAULT_NAMESPACE)),
    INGREDIENT("filter.on1chest.ingredient", stack -> {
        ItemGroup itemGroup = Registries.ITEM_GROUP.get(ItemGroups.INGREDIENTS);
        return itemGroup != null && itemGroup.getDisplayStacks().stream().anyMatch(stack1 -> ItemStack.areItemsEqual(stack, stack1));
    }),
    TOOL("filter.on1chest.tool", stack -> {
        ItemGroup itemGroup = Registries.ITEM_GROUP.get(ItemGroups.TOOLS);
        return itemGroup != null && itemGroup.getDisplayStacks().stream().anyMatch(stack1 -> ItemStack.areItemsEqual(stack, stack1));
    }),
    COMBAT("filter.on1chest.combat", stack -> {
        ItemGroup itemGroup = Registries.ITEM_GROUP.get(ItemGroups.COMBAT);
        return itemGroup != null && itemGroup.getDisplayStacks().stream().anyMatch(stack1 -> ItemStack.areItemsEqual(stack, stack1));
    }),
    REDSTONE("filter.on1chest.redstone", stack -> {
        ItemGroup itemGroup = Registries.ITEM_GROUP.get(ItemGroups.REDSTONE);
        return itemGroup != null && itemGroup.getDisplayStacks().stream().anyMatch(stack1 -> ItemStack.areItemsEqual(stack, stack1));
    }),
    BLOCK("filter.on1chest.block", stack -> Registries.BLOCK.containsId(Registries.ITEM.getId(stack.getItem())));


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
