package com.imoonday.on1chest.filter;

import com.imoonday.on1chest.OnlyNeedOneChest;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record ItemGroupFilter(RegistryKey<ItemGroup> groupKey, Text displayName) implements ItemFilter {

    @Override
    public Identifier getId() {
        return OnlyNeedOneChest.id("item_group." + groupKey.getValue().getPath());
    }

    @Override
    public String getTranslationKey() {
        return displayName.getContent() instanceof TranslatableTextContent content ? content.getKey() : "filter.on1chest.unknown";
    }

    @Override
    public Text getDisplayName() {
        return displayName;
    }

    @Override
    public Identifier getParent() {
        return OnlyNeedOneChest.id("item_group");
    }

    @Override
    public boolean test(ItemStack stack, @Nullable Object data) {
        return ItemFilter.checkGroup(stack, groupKey());
    }
}
