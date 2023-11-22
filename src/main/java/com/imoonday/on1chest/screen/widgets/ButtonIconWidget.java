package com.imoonday.on1chest.screen.widgets;

import com.imoonday.on1chest.mixin.IconWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.IconWidget;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ButtonIconWidget extends IconWidget {

    private final Map<Integer, Consumer<ButtonIconWidget>> actions = new HashMap<>();
    @Nullable
    private final Identifier hoveredTexture;
    private float textureU = 0.0f;
    private float textureV = 0.0f;
    private float hoveredTextureU = 0.0f;
    private float hoveredTextureV = 0.0f;
    private int textureWidth;
    private int textureHeight;

    public ButtonIconWidget(int width, int height, Identifier texture, @Nullable Identifier hoveredTexture) {
        super(width, height, texture);
        this.hoveredTexture = hoveredTexture;
        this.textureWidth = width;
        this.textureHeight = height;
    }

    public ButtonIconWidget(int x, int y, int width, int height, Identifier texture, @Nullable Identifier hoveredTexture) {
        super(x, y, width, height, texture);
        this.hoveredTexture = hoveredTexture;
        this.textureWidth = width;
        this.textureHeight = height;
    }

    public float getTextureU() {
        return textureU;
    }

    public void setTextureU(float textureU) {
        this.textureU = textureU;
    }

    public float getTextureV() {
        return textureV;
    }

    public void setTextureV(float textureV) {
        this.textureV = textureV;
    }

    public float getHoveredTextureU() {
        return hoveredTextureU;
    }

    public void setHoveredTextureU(float hoveredTextureU) {
        this.hoveredTextureU = hoveredTextureU;
    }

    public float getHoveredTextureV() {
        return hoveredTextureV;
    }

    public void setHoveredTextureV(float hoveredTextureV) {
        this.hoveredTextureV = hoveredTextureV;
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public void setTextureWidth(int textureWidth) {
        this.textureWidth = textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public void setTextureHeight(int textureHeight) {
        this.textureHeight = textureHeight;
    }

    public ButtonIconWidget addClickAction(int button, Consumer<ButtonIconWidget> action) {
        this.actions.put(button, action);
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) {
            return false;
        }
        if (this.clicked(mouseX, mouseY)) {
            Consumer<ButtonIconWidget> action = this.actions.get(button);
            if (action != null) {
                this.playDownSound(MinecraftClient.getInstance().getSoundManager());
                action.accept(this);
                return true;
            }
        }
        return false;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        int i = this.getWidth();
        int j = this.getHeight();
        int x = this.getX();
        int y = this.getY();
        int textureWidth = getTextureWidth();
        int textureHeight = getTextureHeight();
        if (this.hoveredTexture != null && this.isMouseOver(mouseX, mouseY)) {
            context.drawTexture(this.hoveredTexture, x, y, getHoveredTextureU(), getHoveredTextureV(), i, j, textureWidth, textureHeight);
        } else {
            context.drawTexture(((IconWidgetAccessor) this).getTexture(), x, y, getTextureU(), getTextureV(), i, j, textureWidth, textureHeight);
        }
    }
}
