package com.imoonday.on1chest.recipes;

import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModRecipes;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

public class StorageMemoryCompressionRecipe extends SpecialCraftingRecipe {

    public StorageMemoryCompressionRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        List<ItemStack> stacks = inventory.getInputStacks();
        int count = 0;
        for (ItemStack stack : stacks) {
            if (stack.isOf(ModBlocks.COMPRESSED_STORAGE_MEMORY_BLOCK.asItem())) {
                count++;
            } else if (!stack.isEmpty()) {
                return false;
            }
        }
        return count >= 2;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        List<ItemStack> stacks = inventory.getInputStacks();
        int level = 0;
        for (ItemStack stack : stacks) {
            if (stack.isOf(ModBlocks.COMPRESSED_STORAGE_MEMORY_BLOCK.asItem())) {
                NbtCompound nbt = stack.getNbt();
                if (nbt != null && nbt.contains("CompressionLevel")) {
                    int compressionLevel = nbt.getInt("CompressionLevel");
                    level += compressionLevel + 1;
                } else {
                    level++;
                }
            } else if (!stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        level--;
        if (level <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(ModBlocks.COMPRESSED_STORAGE_MEMORY_BLOCK);
        stack.getOrCreateNbt().putInt("CompressionLevel", level);
        return stack;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.STORAGE_MEMORY_COMPRESSION;
    }
}
