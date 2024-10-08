package com.imoonday.on1chest.filter;

import com.imoonday.on1chest.screen.widgets.SmallCheckboxWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ItemFilterInstance {

    private final ItemFilter filter;
    private boolean enabled;
    private boolean hide;
    @Nullable
    private List<String> data = null;

    public ItemFilterInstance(ItemFilter filter) {
        this(filter, false);
    }

    public ItemFilterInstance(ItemFilter filter, boolean enabled) {
        this(filter, enabled, false);
    }

    public ItemFilterInstance(ItemFilter filter, boolean enabled, boolean hide) {
        this.filter = filter;
        this.enabled = enabled;
        this.hide = hide;
        if (filter.hasExtraData()) {
            this.data = new ArrayList<>();
        }
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
    public List<String> getData() {
        if (filter.hasExtraData() && data == null) {
            data = new ArrayList<>();
        }
        return data;
    }

    public void setData(@Nullable List<String> data) {
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

    public ItemFilterInstance copy() {
        return new ItemFilterInstance(filter, enabled, hide);
    }

    public CheckboxWidget createCheckbox(TextRenderer textRenderer, int x, int y, BiConsumer<CheckboxWidget, Boolean> onPress) {
        Text name = this.filter.getDisplayName();
        CheckboxWidget widget = new CheckboxWidget(x, y, 24 + textRenderer.getWidth(name), 20, name, this.enabled) {
            @Override
            public void onPress() {
                super.onPress();
                onPress.accept(this, this.isChecked());
            }
        };
        widget.visible = !this.hide;
        return widget;
    }

    public SmallCheckboxWidget createSmallCheckbox(TextRenderer textRenderer, int x, int y, SmallCheckboxWidget.OnPress onPress) {
        Text name = this.filter.getDisplayName();
        SmallCheckboxWidget widget = new SmallCheckboxWidget(textRenderer, x, y, 16 + textRenderer.getWidth(name), name, this.enabled, onPress);
        widget.visible = !this.hide;
        return widget;
    }
}
