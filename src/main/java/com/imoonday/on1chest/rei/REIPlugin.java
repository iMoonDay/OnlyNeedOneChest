package com.imoonday.on1chest.rei;

import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModRecipes;
import com.imoonday.on1chest.screen.client.StorageAssessorScreen;
import dev.architectury.event.CompoundEventResult;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomShapelessDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

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

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        EntryIngredient ingredient = EntryIngredient.of(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(ModBlocks.COMPRESSED_STORAGE_MEMORY_BLOCK)));
        Identifier id = Registries.RECIPE_SERIALIZER.getId(ModRecipes.STORAGE_MEMORY_COMPRESSION);
        for (int i = 2; i < 10; i++) {
            List<EntryIngredient> input = Collections.nCopies(i, ingredient);
            ItemStack outputStack = new ItemStack(ModBlocks.COMPRESSED_STORAGE_MEMORY_BLOCK);
            outputStack.getOrCreateNbt().putInt("CompressionLevel", i - 1);
            EntryIngredient output = EntryIngredient.of(EntryStack.of(VanillaEntryTypes.ITEM, outputStack));
            registry.add(new DefaultCustomShapelessDisplay(id, null, input, Collections.singletonList(output)));
        }
    }
}
