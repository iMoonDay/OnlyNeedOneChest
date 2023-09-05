package com.imoonday.on1chest.client.gui.tooltip;

import com.imoonday.on1chest.OnlyNeedOneChest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.joml.Matrix4f;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class RecipeTooltipComponent implements TooltipComponent {

    public static final Identifier TEXTURE = OnlyNeedOneChest.id("textures/gui/recipe_tooltip.png");
    private final DefaultedList<ItemStack> inventory;
    private final ItemStack result;

    public RecipeTooltipComponent(RecipeTooltipData tooltipData) {
        DefaultedList<ItemStack> inventory = tooltipData.inventory;
        if (inventory.size() != 9) {
            throw new IllegalStateException();
        }
        this.inventory = inventory;
        this.result = tooltipData.result();
    }

    @Override
    public int getHeight() {
        return 54;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        int width = 116;
        if (!result.isEmpty()) {
            width += Math.max(textRenderer.getWidth(result.getName()) / 2 - 13, 0);
        }
        return width;
    }

    @Override
    public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, VertexConsumerProvider.Immediate vertexConsumers) {
        if (result.isEmpty()) {
            return;
        }
        MutableText mutableText = Text.empty().append(result.getName()).formatted(result.getRarity().formatting);
        if (result.hasCustomName()) {
            mutableText.formatted(Formatting.ITALIC);
        }
        TextColor textColor = mutableText.getStyle().getColor();
        int color = textColor != null ? textColor.getRgb() : Color.WHITE.getRGB();
        textRenderer.draw(mutableText, x + 95 + 9 - (float) textRenderer.getWidth(result.getName()) / 2, y + 2, color, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        context.drawTexture(TEXTURE, x, y, 0, 0, 116, 54);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                ItemStack stack = inventory.get(j * 3 + i);
                context.drawItem(stack, x + i * 18 + 1, y + j * 18 + 1);
                context.drawItemInSlot(textRenderer, stack, x + i * 18 + 1, y + j * 18 + 1);
            }
        }
        context.drawItem(result, x + 95, y + 19);
        context.drawItemInSlot(textRenderer, result, x + 95, y + 19);
    }

    public record RecipeTooltipData(DefaultedList<ItemStack> inventory, ItemStack result) implements TooltipData {

    }
}
