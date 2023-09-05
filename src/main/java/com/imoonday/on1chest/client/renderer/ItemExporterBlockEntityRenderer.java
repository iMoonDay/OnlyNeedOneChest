package com.imoonday.on1chest.client.renderer;

import com.imoonday.on1chest.blocks.ItemExporterBlock;
import com.imoonday.on1chest.blocks.entities.AbstractTransferBlockEntity;
import com.imoonday.on1chest.config.Config;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class ItemExporterBlockEntityRenderer implements BlockEntityRenderer<AbstractTransferBlockEntity> {

    private final ItemRenderer itemRenderer;

    public ItemExporterBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(AbstractTransferBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!Config.getInstance().isRenderTargetItem()) {
            return;
        }
        Item target = entity.getTarget();
        if (target == null) {
            return;
        }
        World world = entity.getWorld();
        if (world == null) {
            return;
        }
        matrices.push();
        Direction direction = entity.getCachedState().get(ItemExporterBlock.FACING);
        Direction opposite = direction.getOpposite();
        boolean hasUp = entity.getCachedState().get(ItemExporterBlock.UP);
        if (direction.getHorizontal() == -1) {
            double angle = -((world.getTime() + tickDelta) / 8.0 + entity.uniqueOffset) / 2;
            double radius = 0.35;
            double offsetX = opposite.getOffsetX() * radius + Math.cos(angle + Math.PI / 2) * radius;
            double offsetZ = opposite.getOffsetZ() * radius + Math.sin(angle + Math.PI / 2) * radius;
            boolean hasDown = entity.getCachedState().get(ItemExporterBlock.DOWN);
            if (direction == Direction.UP && !hasUp || direction == Direction.DOWN && !hasDown) {
                offsetX = 0;
                offsetZ = 0;
            }
            matrices.translate(0.5 + offsetX, direction == Direction.UP ? 0.75 : 0.05, 0.5 + offsetZ);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) -Math.toDegrees(angle)));
        } else {
            double angle = ((world.getTime() + tickDelta) / 8.0 + entity.uniqueOffset) / 2;
            double offsetX = direction.getOffsetX() * 0.3;
            double offsetZ = direction.getOffsetZ() * 0.3;
            if (!hasUp) {
                offsetX = 0;
                offsetZ = 0;
            }
            matrices.translate(0.5 + offsetX, 0.75, 0.5 + offsetZ);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) -Math.toDegrees(angle)));
        }
        matrices.scale(0.75f, 0.75f, 0.75f);
        itemRenderer.renderItem(target.getDefaultStack(), ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, world, 0);
        matrices.pop();
    }
}
