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

    protected final Map<Integer, Consumer<ButtonIconWidget>> actions = new HashMap<>();
    @Nullable
    protected final Identifier hoveredTexture;
    protected float textureU;
    protected float textureV;
    protected float hoveredTextureU;
    protected float hoveredTextureV;
    protected int textureWidth;
    protected int textureHeight;

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

    public ButtonIconWidget setTextureU(float textureU) {
        this.textureU = textureU;
        return this;
    }

    public float getTextureV() {
        return textureV;
    }

    public ButtonIconWidget setTextureV(float textureV) {
        this.textureV = textureV;
        return this;
    }

    public ButtonIconWidget setTextureOffset(float u, float v) {
        this.textureU = u;
        this.textureV = v;
        return this;
    }

    public float getHoveredTextureU() {
        return hoveredTextureU;
    }

    public ButtonIconWidget setHoveredTextureU(float hoveredTextureU) {
        this.hoveredTextureU = hoveredTextureU;
        return this;
    }

    public float getHoveredTextureV() {
        return hoveredTextureV;
    }

    public ButtonIconWidget setHoveredTextureV(float hoveredTextureV) {
        this.hoveredTextureV = hoveredTextureV;
        return this;
    }

    public ButtonIconWidget setHoveredTextureOffset(float u, float v) {
        this.hoveredTextureU = u;
        this.hoveredTextureV = v;
        return this;
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public ButtonIconWidget setTextureWidth(int textureWidth) {
        this.textureWidth = textureWidth;
        return this;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public ButtonIconWidget setTextureHeight(int textureHeight) {
        this.textureHeight = textureHeight;
        return this;
    }

    public ButtonIconWidget setTextureSize(int width, int height) {
        this.textureWidth = width;
        this.textureHeight = height;
        return this;
    }

    public ButtonIconWidget setTextureSize(int size) {
        this.setTextureSize(size, size);
        return this;
    }

    public ButtonIconWidget addClickAction(int button, Consumer<ButtonIconWidget> action) {
        this.actions.put(button, action);
        return this;
    }

    public Identifier getTexture() {
        return ((IconWidgetAccessor) this).texture();
    }

    public @Nullable Identifier getHoveredTexture() {
        return hoveredTexture;
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
        Identifier hoveredTexture = this.getHoveredTexture();
        if (hoveredTexture != null && this.isMouseOver(mouseX, mouseY)) {
            context.drawTexture(hoveredTexture, x, y, getHoveredTextureU(), getHoveredTextureV(), i, j, textureWidth, textureHeight);
        } else {
            context.drawTexture(this.getTexture(), x, y, getTextureU(), getTextureV(), i, j, textureWidth, textureHeight);
        }
    }
}
