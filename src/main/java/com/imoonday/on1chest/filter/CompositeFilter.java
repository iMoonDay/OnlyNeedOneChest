package com.imoonday.on1chest.filter;

import com.imoonday.on1chest.OnlyNeedOneChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class CompositeFilter implements ItemFilter {

    private final Identifier id;

    public CompositeFilter(Identifier id) {
        this.id = id;
    }

    public CompositeFilter(String name) {
        this(OnlyNeedOneChest.id(name));
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public String getTranslationKey() {
        return DEFAULT_PREFIX + "composite." + id.getPath();
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
    public boolean hasParent() {
        return false;
    }
}
