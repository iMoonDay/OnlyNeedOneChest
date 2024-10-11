package com.imoonday.on1chest.filter;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class UnknownFilter implements ItemFilter {

    private final Identifier id;

    public UnknownFilter(Identifier id) {
        this.id = id;
    }

    @Override
    public String getTranslationKey() {
        return "filter.on1chest.unknown";
    }

    @Override
    public Text getDisplayName() {
        return UNKOWN_TEXT;
    }

    @Override
    public boolean alwaysTrue() {
        return true;
    }

    @Override
    public boolean test(ItemStack stack, @Nullable Object data) {
        return true;
    }

    @Override
    public Identifier getId() {
        return id;
    }
}
