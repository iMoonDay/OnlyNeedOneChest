package com.imoonday.on1chest.client.renderer;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.blocks.entities.DisplayStorageMemoryBlockEntity;
import com.imoonday.on1chest.config.Config;
import com.imoonday.on1chest.init.ModBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class DisplayStorageMemoryBlockEntityRenderer implements BlockEntityRenderer<DisplayStorageMemoryBlockEntity> {

    private final ItemRenderer itemRenderer;
    private final TextRenderer textRenderer;

    public DisplayStorageMemoryBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
        this.textRenderer = context.getTextRenderer();
    }

    @Override
    public void render(DisplayStorageMemoryBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = entity.getWorld();
        if (world == null) {
            return;
        }
        BlockState blockState = world.getBlockState(entity.getPos());
        if (!blockState.isOf(ModBlocks.DISPLAY_STORAGE_MEMORY_BLOCK)) {
            return;
        }
        if (!blockState.get(StorageMemoryBlock.ACTIVATED)) {
            return;
        }
        ItemStack stack = entity.getDisplayItem();
        if (stack.isEmpty()) {
            return;
        }
        Config config = Config.getInstance();
        boolean renderInCenter = config.isRenderDisplayItemInCenter();
        boolean renderCount = config.isRenderDisplayItemCount();

        int seed = Item.getRawId(stack.getItem()) + stack.getDamage();
        int count = stack.getCount();
        BakedModel bakedModel = this.itemRenderer.getModel(stack, world, null, seed);
        float scaleY = bakedModel.getTransformation().getTransformation(ModelTransformationMode.GROUND).scale.y();
        double y = (renderInCenter ? 0.3 : 1.15) - (renderCount && count > 1 ? 0.05 : 0.0) + config.getDisplayItemYOffset() + 0.25f * scaleY;

        matrices.push();
        matrices.translate(0.5, y, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((world.getTime() + tickDelta) * 4));
        this.itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, false, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, bakedModel);
        matrices.pop();

        if (!renderCount) return;
        if (count > 1) {
            String text = String.valueOf(count);
            if (renderInCenter) {
                MinecraftClient client = MinecraftClient.getInstance();
                float scale = 0.02f;
                float offsetX = -textRenderer.getWidth(text) / 2.0f + 0.5f;
                float offsetY = -textRenderer.fontHeight;
                if (!client.options.getForceUnicodeFont().getValue()) {
                    offsetY += 0.75f;
                }
                Vec3d offset = new Vec3d(0.5, 0, -0.001);
                double[][] offsets = {{0, 0}, {0, 1.005}, {1, 1.005}, {1.005, 0}};
                for (int i = 0; i < 4; i++) {
                    matrices.push();
                    matrices.translate(offset.x + offsets[i][0], 0, offset.z + offsets[i][1]);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90 * i));
                    matrices.scale(-scale, -scale, scale);
                    Matrix4f matrix = matrices.peek().getPositionMatrix();
                    textRenderer.draw(text, offsetX, offsetY, 0xffffff, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
                    matrices.pop();
                    offset = offset.rotateY((float) Math.PI / 2.0f);
                }
            } else {
                matrices.push();
                matrices.translate(0.5, y, 0.5);
                drawString(matrices, vertexConsumers, text, 0, 0.5, 0, 0xffffff, 0.02f, true, 0.0f, false);
                matrices.pop();
            }
        }
    }

    public void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, double x, double y, double z, int color, float size, boolean center, float offset, boolean visibleThroughObjects) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (!camera.isReady() || client.getEntityRenderDispatcher().gameOptions == null) {
            return;
        }
        matrices.push();
        matrices.translate(x, y + 0.07f, z);
        matrices.multiplyPositionMatrix(new Matrix4f().rotation(camera.getRotation()));
        matrices.scale(-size, -size, size);
        float g = center ? (float) (-textRenderer.getWidth(string)) / 2.0f : 0.0f;
        textRenderer.draw(string, g - (offset / size), 0.0f, color, false, matrices.peek().getPositionMatrix(), vertexConsumers, visibleThroughObjects ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        matrices.pop();
    }
}
