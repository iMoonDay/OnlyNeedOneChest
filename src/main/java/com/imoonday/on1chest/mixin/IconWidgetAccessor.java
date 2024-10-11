package com.imoonday.on1chest.mixin;

import net.minecraft.client.gui.widget.IconWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IconWidget.class)
public interface IconWidgetAccessor {

    @Accessor("texture")
    Identifier texture();
}
