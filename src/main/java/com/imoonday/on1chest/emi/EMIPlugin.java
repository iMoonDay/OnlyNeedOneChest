package com.imoonday.on1chest.emi;

import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.screen.client.StorageAssessorScreen;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import net.minecraft.screen.slot.Slot;

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
    }
}
