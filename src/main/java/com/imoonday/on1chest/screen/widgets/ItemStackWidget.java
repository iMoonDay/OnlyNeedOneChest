package com.imoonday.on1chest.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class ItemStackWidget extends ClickableWidget {

    private final TextRenderer textRenderer;
    private ItemStack stack;
    private boolean displayCount;

    public ItemStackWidget(TextRenderer textRenderer, int x, int y, ItemStack stack, boolean displayCount) {
        super(x, y, 16, 16, Text.empty());
        this.textRenderer = textRenderer;
        this.stack = stack.copy();
        this.displayCount = displayCount;
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawItem(stack, this.getX(), this.getY());
        if (displayCount) {
            drawStackCount(context, textRenderer, stack.getCount(), this.getX(), this.getY(), null, false);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (isMouseOver(mouseX, mouseY)) {
            context.drawBorder(this.getX() - 1, this.getY() - 1, this.getWidth() + 2, this.getHeight() + 2, Color.WHITE.getRGB());
            context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
        }
    }

    private void drawStackCount(DrawContext context, TextRenderer renderer, int count, int x, int y, @Nullable Color color, boolean shortStyle) {
        float scaleFactor = 0.6f;
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        NumberFormat instance = !shortStyle ? NumberFormat.getNumberInstance() : NumberFormat.getCompactNumberInstance(Locale.ROOT, NumberFormat.Style.SHORT);
        String stackSize = instance.format(count);
        context.getMatrices().push();
        context.getMatrices().scale(scaleFactor, scaleFactor, scaleFactor);
        context.getMatrices().translate(0, 0, 450);
        float inverseScaleFactor = 1.0f / scaleFactor;
        int X = (int) (((float) x + 0 + 16.0f - renderer.getWidth(stackSize) * scaleFactor) * inverseScaleFactor);
        int Y = (int) (((float) y + 0 + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
        int i = color != null ? color.getRGB() : 16777215;
        context.drawText(renderer, stackSize, X, Y, i, true);
        context.getMatrices().pop();
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    public ItemStack getStack() {
        return stack.copy();
    }

    public void setStack(ItemStack stack) {
        this.stack = stack.copy();
    }

    public void setCount(int count) {
        this.stack.setCount(count);
    }

    public long getCount() {
        return stack.getCount();
    }

    public boolean isDisplayCount() {
        return displayCount;
    }

    public void setDisplayCount(boolean displayCount) {
        this.displayCount = displayCount;
    }
}
