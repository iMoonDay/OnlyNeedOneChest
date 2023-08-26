package com.imoonday.on1chest.screen.widgets;

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

    public ButtonIconWidget(int width, int height, Identifier texture, @Nullable Identifier hoveredTexture) {
        super(width, height, texture);
        this.hoveredTexture = hoveredTexture;
    }

    public ButtonIconWidget(int x, int y, int width, int height, Identifier texture, @Nullable Identifier hoveredTexture) {
        super(x, y, width, height, texture);
        this.hoveredTexture = hoveredTexture;
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
        if (this.hoveredTexture != null && this.isMouseOver(mouseX, mouseY)) {
            int i = this.getWidth();
            int j = this.getHeight();
            context.drawTexture(this.hoveredTexture, this.getX(), this.getY(), 0.0f, 0.0f, i, j, i, j);
        } else {
            super.renderButton(context, mouseX, mouseY, delta);
        }
    }
}
