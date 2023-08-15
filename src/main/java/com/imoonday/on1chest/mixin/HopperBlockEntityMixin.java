package com.imoonday.on1chest.mixin;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.blocks.entities.StorageMemoryBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    @Inject(method = "getInventoryAt(Lnet/minecraft/world/World;DDD)Lnet/minecraft/inventory/Inventory;", at = @At("RETURN"), cancellable = true)
    private static void getInventoryAt(World world, double x, double y, double z, CallbackInfoReturnable<Inventory> cir) {
        if (cir.getReturnValue() == null) {
            BlockPos blockPos = BlockPos.ofFloored(x, y, z);
            BlockState blockState = world.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (world.getBlockEntity(blockPos) instanceof StorageMemoryBlockEntity entity && block instanceof StorageMemoryBlock) {
                cir.setReturnValue(entity);
            }
        }
    }
}
