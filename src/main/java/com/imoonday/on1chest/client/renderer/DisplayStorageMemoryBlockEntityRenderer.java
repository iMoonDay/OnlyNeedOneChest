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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class DisplayStorageMemoryBlockEntityRenderer implements BlockEntityRenderer<DisplayStorageMemoryBlockEntity> {

    private final ItemRenderer itemRenderer;

    public DisplayStorageMemoryBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
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

        double y = config.isRenderDisplayItemInCenter() ? 0.4 : 1.15;
        matrices.push();
        matrices.translate(0.5, y, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((world.getTime() + tickDelta) * 4));
        itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, world, 0);
        matrices.pop();

        if (!config.isRenderDisplayItemCount()) return;
        int count = stack.getCount();
        if (count > 1) {
            matrices.push();
            matrices.translate(0.5, y, 0.5);
            drawString(matrices, vertexConsumers, String.valueOf(count), 0, 0.5, 0, 0xffffff, 0.02f, true, 0.0f, false);
            matrices.pop();
        }
    }

    public static void drawString(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String string, double x, double y, double z, int color, float size, boolean center, float offset, boolean visibleThroughObjects) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        Camera camera = minecraftClient.gameRenderer.getCamera();
        if (!camera.isReady() || minecraftClient.getEntityRenderDispatcher().gameOptions == null) {
            return;
        }
        TextRenderer textRenderer = minecraftClient.textRenderer;
        matrices.push();
        matrices.translate(x, y + 0.07f, z);
        matrices.multiplyPositionMatrix(new Matrix4f().rotation(camera.getRotation()));
        matrices.scale(-size, -size, size);
        float g = center ? (float) (-textRenderer.getWidth(string)) / 2.0f : 0.0f;
        float backgroundOpacity = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f);
        int j = (int) (backgroundOpacity * 255.0f) << 24;
        textRenderer.draw(string, g - (offset / size), 0.0f, color, false, matrices.peek().getPositionMatrix(), vertexConsumers, visibleThroughObjects ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, j, 0xF000F0);
        matrices.pop();
    }
}
