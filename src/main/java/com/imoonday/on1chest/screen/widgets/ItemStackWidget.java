package com.imoonday.on1chest.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
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
    private boolean drawStackCount;
    private boolean drawSlotHighlight;
    private boolean drawBorder;
    private boolean drawItemTooltip;
    private TooltipRenderer tooltipRenderer;

    public ItemStackWidget(TextRenderer textRenderer, int x, int y, ItemStack stack, boolean drawStackCount) {
        super(x, y, 16, 16, Text.empty());
        this.textRenderer = textRenderer;
        this.stack = stack.copy();
        this.drawStackCount = drawStackCount;
        this.drawItemTooltip = true;
        this.tooltipRenderer = (context, mouseX, mouseY, delta) -> context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawItem(stack, this.getX(), this.getY());
        if (drawStackCount) {
            drawStackCount(context, textRenderer, stack.getCount(), this.getX(), this.getY(), null, false);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (isMouseOver(mouseX, mouseY)) {
            if (drawSlotHighlight) {
                HandledScreen.drawSlotHighlight(context, this.getX(), this.getY(), 0);
            }
            if (drawBorder) {
                context.drawBorder(this.getX() - 1, this.getY() - 1, this.getWidth() + 2, this.getHeight() + 2, Color.WHITE.getRGB());
            }
            if (drawItemTooltip) {
                this.tooltipRenderer.draw(context, mouseX, mouseY, delta);
            }
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

    public boolean isDrawStackCount() {
        return drawStackCount;
    }

    public void setDrawStackCount(boolean drawStackCount) {
        this.drawStackCount = drawStackCount;
    }

    public boolean isDrawBorder() {
        return drawBorder;
    }

    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
    }

    public boolean isDrawItemTooltip() {
        return drawItemTooltip;
    }

    public void setDrawItemTooltip(boolean drawItemTooltip) {
        this.drawItemTooltip = drawItemTooltip;
    }

    public boolean isDrawSlotHighlight() {
        return drawSlotHighlight;
    }

    public void setDrawSlotHighlight(boolean drawSlotHighlight) {
        this.drawSlotHighlight = drawSlotHighlight;
    }

    public TooltipRenderer getTooltipRenderer() {
        return tooltipRenderer;
    }

    public void setTooltipRenderer(TooltipRenderer tooltipRenderer) {
        this.tooltipRenderer = tooltipRenderer;
    }

    public interface TooltipRenderer {
        void draw(DrawContext context, int mouseX, int mouseY, float delta);
    }
}
