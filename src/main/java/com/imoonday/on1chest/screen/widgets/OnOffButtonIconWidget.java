package com.imoonday.on1chest.screen.widgets;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class OnOffButtonIconWidget extends ButtonIconWidget {

    protected final Identifier textureOn;
    protected final Identifier hoveredTextureOn;
    protected float textureOnU;
    protected float textureOnV;
    protected float hoveredTextureOnU;
    protected float hoveredTextureOnV;
    protected boolean activated = false;

    public OnOffButtonIconWidget(int width, int height, Identifier texture) {
        this(width, height, texture, texture, texture, texture);
    }

    public OnOffButtonIconWidget(int width, int height, Identifier textureOff, Identifier textureOn, @Nullable Identifier hoveredTextureOff, Identifier hoveredTextureOn) {
        super(width, height, textureOff, hoveredTextureOff);
        this.textureOn = textureOn;
        this.hoveredTextureOn = hoveredTextureOn;
    }

    public OnOffButtonIconWidget(int x, int y, int width, int height, Identifier textureOff, Identifier textureOn, @Nullable Identifier hoveredTextureOff, Identifier hoveredTextureOn) {
        super(x, y, width, height, textureOff, hoveredTextureOff);
        this.textureOn = textureOn;
        this.hoveredTextureOn = hoveredTextureOn;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isActivated() {
        return activated;
    }

    @Override
    public Identifier getTexture() {
        return isActivated() ? textureOn : super.getTexture();
    }

    @Override
    public Identifier getHoveredTexture() {
        return isActivated() ? hoveredTextureOn : super.getHoveredTexture();
    }

    @Override
    public float getTextureU() {
        return isActivated() ? textureOnU : super.getTextureU();
    }

    @Override
    public float getTextureV() {
        return isActivated() ? textureOnV : super.getTextureV();
    }

    @Override
    public float getHoveredTextureU() {
        return isActivated() ? hoveredTextureOnU : super.getHoveredTextureU();
    }

    @Override
    public float getHoveredTextureV() {
        return isActivated() ? hoveredTextureOnV : super.getHoveredTextureV();
    }

    public OnOffButtonIconWidget setTextureOnU(float u) {
        this.textureOnU = u;
        return this;
    }

    public OnOffButtonIconWidget setTextureOnV(float v) {
        this.textureOnV = v;
        return this;
    }

    public OnOffButtonIconWidget setTextureOnOffset(float u, float v) {
        this.textureOnU = u;
        this.textureOnV = v;
        return this;
    }

    public OnOffButtonIconWidget setHoveredTextureOnU(float u) {
        this.hoveredTextureOnU = u;
        return this;
    }

    public OnOffButtonIconWidget setHoveredTextureOnV(float v) {
        this.hoveredTextureOnV = v;
        return this;
    }

    public OnOffButtonIconWidget setHoveredTextureOnOffset(float u, float v) {
        this.hoveredTextureOnU = u;
        this.hoveredTextureOnV = v;
        return this;
    }

    @Override
    public OnOffButtonIconWidget setTextureU(float textureU) {
        super.setTextureU(textureU);
        return this;
    }

    @Override
    public OnOffButtonIconWidget setTextureV(float textureV) {
        super.setTextureV(textureV);
        return this;
    }

    @Override
    public OnOffButtonIconWidget setTextureOffset(float u, float v) {
        super.setTextureOffset(u, v);
        return this;
    }

    @Override
    public OnOffButtonIconWidget setHoveredTextureU(float hoveredTextureU) {
        super.setHoveredTextureU(hoveredTextureU);
        return this;
    }

    @Override
    public OnOffButtonIconWidget setHoveredTextureV(float hoveredTextureV) {
        super.setHoveredTextureV(hoveredTextureV);
        return this;

    }

    @Override
    public OnOffButtonIconWidget setHoveredTextureOffset(float u, float v) {
        super.setHoveredTextureOffset(u, v);
        return this;
    }

    @Override
    public OnOffButtonIconWidget setTextureWidth(int textureWidth) {
        super.setTextureWidth(textureWidth);
        return this;
    }

    @Override
    public OnOffButtonIconWidget setTextureHeight(int textureHeight) {
        super.setTextureHeight(textureHeight);
        return this;
    }

    @Override
    public OnOffButtonIconWidget setTextureSize(int width, int height) {
        super.setTextureSize(width, height);
        return this;
    }

    @Override
    public OnOffButtonIconWidget setTextureSize(int size) {
        super.setTextureSize(size);
        return this;
    }

    @Override
    public OnOffButtonIconWidget addClickAction(int button, Consumer<ButtonIconWidget> action) {
        super.addClickAction(button, action);
        return this;
    }
}
