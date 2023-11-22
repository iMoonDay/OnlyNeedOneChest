package com.imoonday.on1chest.client.gui.tooltip;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

public class IngredientTooltip implements TooltipComponent, TooltipData {

    private final Ingredient ingredient;

    public IngredientTooltip(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public int getHeight() {
        int length = ingredient.getMatchingStacks().length;
        return (length / 8 + (length % 8 == 0 ? 0 : 1)) * 18 + 2;
    }

    @Override
    public int getWidth(TextRenderer font) {
        int length = ingredient.getMatchingStacks().length;
        return Math.min(length * 18, 144);
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        int index = 0;
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            int offsetX = index % 8 * 18;
            int offsetY = index / 8 * 18;
            context.drawItem(stack, x + offsetX, y + offsetY);
            context.drawItemInSlot(textRenderer, stack, x + offsetX, y + offsetY);
            index++;
        }
    }
}