package com.imoonday.on1chest.client.renderer;

import com.imoonday.on1chest.blocks.entities.GlassStorageMemoryBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class GlassStorageMemoryBlockEntityRenderer implements BlockEntityRenderer<GlassStorageMemoryBlockEntity> {

    public GlassStorageMemoryBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(GlassStorageMemoryBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = entity.getWorld();
        if (world == null) {
            return;
        }
        ItemStack stack = entity.getDisplayItem();
        if (stack.isEmpty()) {
            return;
        }
        matrices.push();
        double offset = Math.sin((world.getTime() + tickDelta) / 8.0) / 8.0;
        matrices.translate(0.5, 0.4 + offset, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((world.getTime() + tickDelta) * 4));
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, world, 0);
        matrices.pop();
    }
}
