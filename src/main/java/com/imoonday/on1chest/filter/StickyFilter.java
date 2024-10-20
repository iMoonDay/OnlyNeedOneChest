package com.imoonday.on1chest.filter;

import net.minecraft.text.Text;

public enum StickyFilter {
    BOTH,
    TEXT_FILTER,
    BUTTON_FILTERS,
    OFF;

    private final String translationKey = "filter.on1chest.sticky_filter." + this.name().toLowerCase();

    public Text getDisplayName() {
        return Text.translatable(translationKey);
    }

    public boolean isStickyText() {
        return this == BOTH || this == TEXT_FILTER;
    }

    public boolean isStickyButtons() {
        return this == BOTH || this == BUTTON_FILTERS;
    }
}
