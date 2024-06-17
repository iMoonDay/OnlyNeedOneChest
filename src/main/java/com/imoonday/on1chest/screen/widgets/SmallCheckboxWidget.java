package com.imoonday.on1chest.screen.widgets;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class SmallCheckboxWidget extends PressableWidget {

    private static final Identifier TEXTURE = OnlyNeedOneChest.id("textures/button/small_checkbox.png");
    private static final int TEXTURE_SIZE = 13;
    private boolean checked;
    private final TextRenderer textRenderer;
    private final boolean showMessage;
    private OnPress onPress;

    public SmallCheckboxWidget(TextRenderer textRenderer, int x, int y, int width, Text message, boolean checked, OnPress onPress) {
        this(textRenderer, x, y, width, message, checked, true, onPress);
    }

    public SmallCheckboxWidget(TextRenderer textRenderer, int x, int y, int width, Text message, boolean checked, boolean showMessage, OnPress onPress) {
        super(x, y, width, TEXTURE_SIZE, message);
        this.textRenderer = textRenderer;
        this.showMessage = showMessage;
        this.checked = checked;
        this.onPress = onPress;
    }

    public void setOnPress(OnPress onPress) {
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        this.checked = !this.checked;
        if (this.onPress != null) {
            this.onPress.onPress(this, this.checked);
        }
    }

    public boolean isChecked() {
        return this.checked;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, this.getNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                builder.put(NarrationPart.USAGE, Text.translatable("narration.checkbox.usage.focused"));
            } else {
                builder.put(NarrationPart.USAGE, Text.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.enableDepthTest();
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        context.drawTexture(TEXTURE, this.getX(), this.getY(), 0f, 0f, TEXTURE_SIZE, TEXTURE_SIZE, 32, 16);
        if (this.isChecked()) {
            context.drawTexture(TEXTURE, this.getX(), this.getY(), 16, 0f, TEXTURE_SIZE, TEXTURE_SIZE, 32, 16);
        }
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.showMessage) {
            Text message = this.getMessage();
            TextColor textColor = message.getStyle().getColor();
            drawScrollableText(context, textRenderer, message, this.getX() + TEXTURE_SIZE + 3, this.getY(), this.getX() + this.getWidth(), this.getY() + TEXTURE_SIZE, textColor != null ? textColor.getRgb() : 0xE0E0E0 | MathHelper.ceil(this.alpha * 255.0f) << 24);
        }
    }

    public interface OnPress {
        void onPress(SmallCheckboxWidget widget, boolean checked);
    }
}
