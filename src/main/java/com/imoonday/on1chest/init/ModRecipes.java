package com.imoonday.on1chest.init;

import com.imoonday.on1chest.recipes.StorageMemoryCompressionRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;

public class ModRecipes {

    public static final RecipeSerializer<StorageMemoryCompressionRecipe> STORAGE_MEMORY_COMPRESSION = RecipeSerializer.register("storage_memory_compression", new SpecialRecipeSerializer<>(StorageMemoryCompressionRecipe::new));

    public static void register() {

    }
}
