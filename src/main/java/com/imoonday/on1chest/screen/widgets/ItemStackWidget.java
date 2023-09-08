package com.imoonday.on1chest.screen.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.awt.*;

public class ItemStackWidget extends ClickableWidget {

    private final TextRenderer textRenderer;
    private ItemStack stack;

    public ItemStackWidget(TextRenderer textRenderer, int x, int y, ItemStack stack) {
        super(x, y, 16, 16, Text.empty());
        this.textRenderer = textRenderer;
        this.stack = stack.copy();
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();
        context.drawItem(stack, this.getX(), this.getY());
        context.getMatrices().pop();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (isMouseOver(mouseX, mouseY)) {
            context.drawBorder(this.getX() - 1, this.getY() - 1, this.getWidth() + 2, this.getHeight() + 2, Color.WHITE.getRGB());
            context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
        }
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
}
