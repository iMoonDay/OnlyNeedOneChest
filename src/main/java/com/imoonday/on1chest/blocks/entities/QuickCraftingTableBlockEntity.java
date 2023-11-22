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

import java.util.*;

public class QuickCraftingTableBlockEntity extends StorageAccessorBlockEntity {

    public QuickCraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUICK_CRAFTING_TABLE_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, QuickCraftingTableBlockEntity entity) {
        StorageAccessorBlockEntity.tick(world, pos, state, entity);
    }

    public CraftingRecipeTreeManager getManager() {
        if (world != null) {
            return CraftingRecipeTreeManager.getOrCreate(world);
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

    public List<CraftingRecipeTreeManager.CraftResult> getCraftResults(ItemStack stack, int maxSize) {
        Set<CraftingRecipeTreeManager.CraftResult> craftResults = new HashSet<>();
        Set<ItemStack> cost = ItemStackSet.create();
        Set<ItemStack> except = ItemStackSet.create();
        Set<Set<ItemStack>> excepted = new HashSet<>();
        List<Set<ItemStack>> powerSet = getPowerSet(cost);
        while (craftResults.size() < maxSize) {
            CraftingRecipeTreeManager.CraftResult craftResult = this.getManager().getCraftResult(inventory, stack, except);
            if (!except.isEmpty()) {
                excepted.add(except);
            }
            if (craftResult.isCrafted()) {
                craftResults.add(craftResult);
                int size = cost.size();
                cost.addAll(craftResult.getCost());
                if (size != cost.size()) {
                    powerSet = getPowerSet(cost);
                    powerSet.removeAll(excepted);
                    powerSet.sort(Comparator.comparingInt(Set::size));
                }
            }
            if (powerSet.isEmpty()) {
                if (craftResults.isEmpty()) {
                    craftResults.add(craftResult);
                }
                break;
            }
            except = powerSet.remove(0);
        }
        return new ArrayList<>(craftResults);
    }

    public static List<Set<ItemStack>> getPowerSet(Set<ItemStack> set) {
        List<Set<ItemStack>> result = new ArrayList<>();
        if (set == null || set.isEmpty()) {
            return result;
        }
        List<ItemStack> list = new ArrayList<>(set);
        int n = list.size();
        int max = 1 << n;
        for (int i = 1; i < max; i++) {
            Set<ItemStack> subSet = ItemStackSet.create();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    subSet.add(list.get(j));
                }
            }
            result.add(subSet);
        }
        return result;
    }
}
