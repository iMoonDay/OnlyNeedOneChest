package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.utils.CraftingRecipeTreeManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class QuickCraftingTableBlockEntity extends BlockEntity {

    private CraftingRecipeTreeManager recorder;

    public QuickCraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUICK_CRAFTING_TABLE_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, QuickCraftingTableBlockEntity entity) {
        if (entity.recorder == null && world instanceof ServerWorld serverWorld) {
            entity.recorder = new CraftingRecipeTreeManager(serverWorld.getServer());
        }
        if (entity.recorder != null && world.getTime() % 20 == 0) {
            entity.recorder.reload();
        }
    }

    public CraftingRecipeTreeManager getRecorder() {
        return recorder;
    }
}
