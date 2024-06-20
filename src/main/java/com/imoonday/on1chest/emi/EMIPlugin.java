package com.imoonday.on1chest.emi;

import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModRecipes;
import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.screen.client.StorageAssessorScreen;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

public class EMIPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(ModBlocks.STORAGE_PROCESSOR_BLOCK));
        registry.addRecipeHandler(ModScreens.STORAGE_PROCESSOR_SCREEN_HANDLER, new EmiTransferHandler());
        registry.addGenericStackProvider((scr, x, y) -> {
            if (scr instanceof StorageAssessorScreen screen) {
                Slot slot = screen.getSelectedSlot();
                if (slot != null) return new EmiStackInteraction(EmiStack.of(slot.getStack()), null, false);
            }
            return EmiStackInteraction.EMPTY;
        });
        EmiStack stack = EmiStack.of(ModBlocks.COMPRESSED_STORAGE_MEMORY_BLOCK);
        Identifier id = Registries.RECIPE_SERIALIZER.getId(ModRecipes.STORAGE_MEMORY_COMPRESSION);
        for (int i = 2; i < 10; i++) {
            List<EmiIngredient> input = Collections.nCopies(i, stack);
            ItemStack outputStack = new ItemStack(ModBlocks.COMPRESSED_STORAGE_MEMORY_BLOCK);
            outputStack.getOrCreateNbt().putInt("CompressionLevel", i - 1);
            EmiStack output = EmiStack.of(outputStack);
            registry.addRecipe(new EmiCraftingRecipe(input, output, id));
        }
    }
}
