package com.imoonday.on1chest.filter;

import com.imoonday.on1chest.OnlyNeedOneChest;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemGroupFilter implements ItemFilter {

    private final RegistryKey<ItemGroup> groupKey;

    public ItemGroupFilter(RegistryKey<ItemGroup> groupKey) {
        this.groupKey = groupKey;
    }

    @Override
    public Identifier getId() {
        return OnlyNeedOneChest.id(getTranslationKey());
    }

    @Override
    public String getTranslationKey() {
        return "item_group." + groupKey.getValue().toTranslationKey();
    }

    @Override
    public boolean test(ItemStack stack, @Nullable List<String> data) {
        return ItemFilters.checkGroup(stack, getGroupKey());
    }

    public RegistryKey<ItemGroup> getGroupKey() {
        return groupKey;
    }
}
