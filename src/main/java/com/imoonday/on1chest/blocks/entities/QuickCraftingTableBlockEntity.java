package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.screen.QuickCraftingScreenHandler;
import com.imoonday.on1chest.utils.CraftingRecipeTreeManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuickCraftingTableBlockEntity extends StorageAccessorBlockEntity {

    public QuickCraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUICK_CRAFTING_TABLE_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, QuickCraftingTableBlockEntity entity) {
        StorageAccessorBlockEntity.tick(world, pos, state, entity);
    }

    public CraftingRecipeTreeManager getManager() {
        if (world != null) {
            return CraftingRecipeTreeManager.get(world);
        }
        return null;
    }

    @Override
    public Text getDisplayName() {
        return this.getCachedState().getBlock().getName();
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new QuickCraftingScreenHandler(syncId, playerInventory, this);
    }

    public CraftingRecipeTreeManager.CraftResult getCraftResult(ItemStack stack) {
        return this.getManager().getCraftResult(inventory, stack, new HashSet<>());
    }

    public List<CraftingRecipeTreeManager.CraftResult> getCraftResults(ItemStack stack, int limit) {
        int i = 0;
        List<CraftingRecipeTreeManager.CraftResult> craftResults = new ArrayList<>();
        Set<ItemStack> except = ItemStackSet.create();
        while (i++ < limit) {
            CraftingRecipeTreeManager.CraftResult craftResult = this.getManager().getCraftResult(inventory, stack, except);
            if (craftResult.isCrafted()) {
                except.addAll(craftResult.getCost());
                craftResults.add(craftResult);
                continue;
            }
            break;
        }
        return craftResults;
    }

}
