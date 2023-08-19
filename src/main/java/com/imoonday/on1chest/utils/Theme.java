package com.imoonday.on1chest.utils;

import com.imoonday.on1chest.OnlyNeedOneChest;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public enum Theme {
    VANILLA("", "theme.on1chest.vanilla"),
    NO_SLOT("_no_slot", "theme.on1chest.noSlot"),
    DARK("_dark", "theme.on1chest.dark"),
    BLACK("_black", "theme.on1chest.black");

    private final String suffix;
    private final String translationKey;

    Theme(String suffix, String translationKey) {
        this.suffix = suffix;
        this.translationKey = translationKey;
    }

    public Identifier getId(String fileName) {
        return OnlyNeedOneChest.id("textures/gui/" + fileName + this.suffix + ".png");
    }

    public Text getLocalizeText() {
        return Text.translatable(translationKey);
    }

    public Theme next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
