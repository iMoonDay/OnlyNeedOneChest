package com.imoonday.on1chest.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.List;

public interface ItemFilter extends Serializable {

    String DEFAULT_PREFIX = "filter.on1chest.";

    Identifier getId();

    default String getTranslationKey() {
        return DEFAULT_PREFIX + getId().getPath();
    }

    default Text getDisplayName() {
        return Text.translatable(getTranslationKey());
    }

    default Text getTooltip(ItemFilterInstance filterInstance) {
        return Text.empty();
    }

    default Text getDataTooltip() {
        return Text.empty();
    }

    default Identifier getParent() {
        return null;
    }

    default boolean hasParent() {
        return getParent() != null;
    }

    default boolean hasExtraData() {
        return false;
    }

    default Text getDataDisplayName() {
        return Text.translatable(getTranslationKey() + ".data");
    }

    default boolean hiddenByDefault() {
        return false;
    }

    boolean test(ItemStack stack, @Nullable List<String> data);
}
