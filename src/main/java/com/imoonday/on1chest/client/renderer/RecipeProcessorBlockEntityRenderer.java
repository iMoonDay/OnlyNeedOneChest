package com.imoonday.on1chest.client.renderer;

import com.imoonday.on1chest.blocks.entities.RecipeProcessorBlockEntity;
import com.imoonday.on1chest.config.Config;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

import java.util.List;

public class RecipeProcessorBlockEntityRenderer implements BlockEntityRenderer<RecipeProcessorBlockEntity> {

    private final ItemRenderer itemRenderer;

    public RecipeProcessorBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(RecipeProcessorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!Config.getInstance().isRenderTargetItem()) {
            return;
        }
        World world = entity.getWorld();
        if (world == null) {
            return;
        }
        List<ItemStack> stacks = entity.getResults().stream().filter(stack -> !stack.isEmpty()).toList();
        if (stacks.isEmpty()) {
            return;
        }
        int index = (int) ((world.getTime() / 30) % stacks.size());
        ItemStack target = stacks.get(index);
        matrices.push();
        matrices.translate(0.5, 1.25, 0.5);
        double angle = (world.getTime() + tickDelta) / 16;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) Math.toDegrees(angle)));
        int lightAbove = WorldRenderer.getLightmapCoordinates(world, entity.getPos().up());
        itemRenderer.renderItem(target, ModelTransformationMode.GROUND, lightAbove, overlay, matrices, vertexConsumers, world, 0);
        matrices.pop();
    }
}
