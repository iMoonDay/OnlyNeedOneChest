package com.imoonday.on1chest.client.renderer;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.blocks.entities.GlassStorageMemoryBlockEntity;
import com.imoonday.on1chest.config.Config;
import com.imoonday.on1chest.init.ModBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

import java.util.List;

@Environment(EnvType.CLIENT)
public class GlassStorageMemoryBlockEntityRenderer implements BlockEntityRenderer<GlassStorageMemoryBlockEntity> {

    private final ItemRenderer itemRenderer;

    public GlassStorageMemoryBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(GlassStorageMemoryBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = entity.getWorld();
        if (world == null) {
            return;
        }
        BlockState blockState = world.getBlockState(entity.getPos());
        if (!blockState.isOf(ModBlocks.GLASS_STORAGE_MEMORY_BLOCK)) {
            return;
        }
        if (!blockState.get(StorageMemoryBlock.ACTIVATED)) {
            return;
        }
        Config config = Config.getInstance();
        if (config.isRandomMode()) {
            ItemStack stack = entity.getDisplayItem();
            if (stack.isEmpty()) {
                return;
            }
            matrices.push();
            double offset = Math.sin((world.getTime() + tickDelta) / 8.0 + entity.uniqueOffset) / 8.0;
            matrices.translate(0.5, 0.4 + config.getItemYOffset() + offset, 0.5);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((world.getTime() + tickDelta) * 4));
            itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, world, 0);
            matrices.pop();
            return;
        }

        List<ItemStack> items = entity.getItems().stream().filter(stack -> !stack.isEmpty()).toList();
        int itemCount = items.size();
        if (itemCount == 0) {
            return;
        }
        //自动间距
        boolean autoSpacing = config.isAutoSpacing();
        //缩放比例
        float size = config.getScale();
        //间隔系数
        double interval = config.getInterval() * (autoSpacing ? size : 1.0f);
        //旋转速度
        float rotationSpeed = config.getRotationSpeed();
        //旋转角度(-1为自动旋转)
        float rotationDegrees = config.getRotationDegrees();

        int a = autoSpacing ? itemCount : minCube(itemCount);
        double scale = 1 / Math.cbrt(a);
        int faceCount = (int) Math.ceil(Math.cbrt(a));
        double[][][][] positions = new double[faceCount][faceCount][faceCount][3];
        float[][][] speeds = new float[faceCount][faceCount][faceCount];
        for (int x = 0; x < faceCount; x++) {
            for (int y = 0; y < faceCount; y++) {
                for (int z = 0; z < faceCount; z++) {
                    positions[x][y][z][0] = 0.5 - interval * scale * (faceCount - 1) / 2 + interval * scale * x;
                    positions[x][y][z][1] = 0.5 - interval * scale * (faceCount - 1) / 2 + interval * scale * y;
                    positions[x][y][z][2] = 0.5 - interval * scale * (faceCount - 1) / 2 + interval * scale * z;
                    speeds[x][y][z] = rotationSpeed;
                }
            }
        }
        for (int x = 0; x < faceCount; x++) {
            for (int y = 0; y < faceCount; y++) {
                for (int z = 0; z < faceCount; z++) {
                    int index = x * faceCount * faceCount + y * faceCount + z;
                    if (index < 0 || index >= itemCount) {
                        break;
                    }
                    matrices.push();
                    matrices.translate(positions[x][y][z][0], positions[x][y][z][1] + config.getItemYOffset(), positions[x][y][z][2]);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDegrees >= 0 ? rotationDegrees : speeds[x][y][z] * (world.getTime() + tickDelta)));
                    matrices.scale((float) scale * size, (float) scale * size, (float) scale * size);
                    itemRenderer.renderItem(items.get(index), ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, world, 0);
                    matrices.pop();
                }
            }
        }
    }

    public static int minCube(int n) {
        assert n > 0;
        int i = 1;
        while (i * i * i < n) {
            i++;
        }
        return i * i * i;
    }

}
