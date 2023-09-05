package com.imoonday.on1chest.items;

import com.imoonday.on1chest.blocks.entities.StorageMemoryBlockEntity;
import com.imoonday.on1chest.init.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VanillaToWoodConversionModuleItem extends Item {
    public VanillaToWoodConversionModuleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isOf(Blocks.CHEST) && blockEntity instanceof ChestBlockEntity chestBlock) {
            if (world.isClient) {
                return ActionResult.SUCCESS;
            }
            Inventory inventory = new SimpleInventory(27);
            int max = Math.min(27, chestBlock.size());
            for (int i = 0; i < max; i++) {
                ItemStack stack = chestBlock.getStack(i);
                if (!stack.isEmpty()) {
                    inventory.setStack(i, stack.copyAndEmpty());
                }
            }
            world.setBlockState(pos, ModBlocks.WOOD_STORAGE_MEMORY_BLOCK.getDefaultState());
            PlayerEntity player = context.getPlayer();
            if (player != null && !player.isCreative()) {
                context.getStack().decrement(1);
            }
            if (world.getBlockEntity(pos) instanceof StorageMemoryBlockEntity memoryBlock) {
                return memoryBlock.copyFrom(inventory) ? ActionResult.SUCCESS : ActionResult.FAIL;
            }
        }
        return super.useOnBlock(context);
    }
}
