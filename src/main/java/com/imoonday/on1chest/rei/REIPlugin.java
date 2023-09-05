package com.imoonday.on1chest.rei;

import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.screen.client.StorageAssessorScreen;
import dev.architectury.event.CompoundEventResult;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.screen.slot.Slot;

public class REIPlugin implements REIClientPlugin {

    @Override
    public void registerTransferHandlers(TransferHandlerRegistry recipeHelper) {
        recipeHelper.register(new ReiTransferHandler());
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(ModBlocks.STORAGE_PROCESSOR_BLOCK));
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerFocusedStack((scr, point) -> {
            if (scr instanceof StorageAssessorScreen screen) {
                Slot slot = screen.getSelectedSlot();
                if (slot != null) {
                    return CompoundEventResult.interruptTrue(EntryStack.of(VanillaEntryTypes.ITEM, slot.getStack()));
                }
            }
            return CompoundEventResult.pass();
        });
    }
}
