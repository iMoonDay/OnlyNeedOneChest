package com.imoonday.on1chest.mixin;

import net.minecraft.client.gui.widget.CheckboxWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CheckboxWidget.class)
public interface CheckboxWidgetAccessor {

    @Accessor("checked")
    void setChecked(boolean checked);
}
