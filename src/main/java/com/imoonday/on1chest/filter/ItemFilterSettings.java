package com.imoonday.on1chest.filter;

import com.imoonday.on1chest.screen.widgets.SmallCheckboxWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;

public class ItemFilterSettings {

    private final ItemFilter filter;
    private boolean enabled;
    private boolean hide;
    @Nullable
    private Object data;

    public ItemFilterSettings(ItemFilter filter) {
        this(filter, false);
    }

    public ItemFilterSettings(ItemFilter filter, boolean enabled) {
        this(filter, enabled, false);
    }

    public ItemFilterSettings(ItemFilter filter, boolean enabled, boolean hide) {
        this.filter = filter;
        this.enabled = enabled;
        this.hide = hide;
        if (filter.hasExtraData()) {
            this.data = filter.getDefaultData().get();
        }
    }

    public ItemFilterSettings(ItemFilter filter, boolean enabled, boolean hide, @Nullable Object data) {
        this.filter = filter;
        this.enabled = enabled;
        this.hide = hide;
        this.data = data;
    }

    public ItemFilter getFilter() {
        return filter;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    @Nullable
    public Object getData() {
        if (filter.hasExtraData()) {
            if (data == null) {
                this.data = filter.getDefaultData().get();
            } else {
                Object defaultData = filter.getDefaultData().get();
                if (this.data.getClass() != defaultData.getClass()) {
                    this.data = defaultData;
                }
            }
        }
        return data;
    }

    public void setData(@Nullable Object data) {
        this.data = data;
    }

    public boolean is(ItemFilter filter) {
        return this.filter.getId().equals(filter.getId());
    }

    public boolean is(Identifier id) {
        return this.filter.getId().equals(id);
    }

    public boolean test(ItemStack stack) {
        return filter.test(stack, this.getData());
    }

    public ItemFilterSettings copy() {
        return new ItemFilterSettings(filter, enabled, hide, ObjectUtils.cloneIfPossible(data));
    }

    public SmallCheckboxWidget createCheckbox(TextRenderer textRenderer, int x, int y, SmallCheckboxWidget.OnPress onPress) {
        Text name = this.filter.getDisplayName(this.hide);
        SmallCheckboxWidget widget = new SmallCheckboxWidget(textRenderer, x, y, name, this.enabled, onPress);
        widget.visible = !this.hide;
        Text tooltip = filter.getTooltip(this);
        if (!tooltip.getString().isEmpty()) {
            widget.setTooltip(Tooltip.of(tooltip));
        }
        return widget;
    }
}
