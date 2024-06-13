package com.imoonday.on1chest.emi;

import com.imoonday.on1chest.screen.StorageProcessorScreenHandler;
import com.imoonday.on1chest.utils.CombinedItemStack;
import com.imoonday.on1chest.utils.IAutoCraftingHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;

import java.util.*;

public class EmiTransferHandler implements StandardRecipeHandler<StorageProcessorScreenHandler> {

    @Override
    public List<Slot> getInputSources(StorageProcessorScreenHandler handler) {
        return Collections.emptyList();
    }

    @Override
    public List<Slot> getCraftingSlots(StorageProcessorScreenHandler handler) {
        return Collections.emptyList();
    }

    @Override
    public EmiPlayerInventory getInventory(HandledScreen<StorageProcessorScreenHandler> screen) {
        List<EmiStack> stacks = new ArrayList<>();
        screen.getScreenHandler().slots.subList(1, screen.getScreenHandler().slots.size()).stream().map(Slot::getStack).map(EmiStack::of).forEach(stacks::add);
        screen.getScreenHandler().getStoredItems().forEach(s -> stacks.add(EmiStack.of(s.getStack(), s.getCount())));
        return new EmiPlayerInventory(stacks);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<StorageProcessorScreenHandler> context) {
        HandledScreen<StorageProcessorScreenHandler> screen = context.getScreen();
        handleRecipe(recipe, screen, false);
        MinecraftClient.getInstance().setScreen(screen);
        return true;
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<StorageProcessorScreenHandler> context, List<Widget> widgets, DrawContext draw) {
        RenderSystem.enableDepthTest();
        List<Integer> missing = handleRecipe(recipe, context.getScreen(), true);
        int i = 0;
        for (Widget w : widgets) {
            if (w instanceof SlotWidget sw) {
                int j = i++;
                EmiIngredient stack = sw.getStack();
                Bounds bounds = sw.getBounds();
                if (sw.getRecipe() == null && !stack.isEmpty()) {
                    if (missing.contains(j)) {
                        draw.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), 0x44FF0000);
                    }
                }
            }
        }
    }

    private static List<Integer> handleRecipe(EmiRecipe recipe, HandledScreen<StorageProcessorScreenHandler> screen, boolean simulate) {
        IAutoCraftingHandler term = screen.getScreenHandler();
        ItemStack[][] stacks = recipe.getInputs().stream().map(i ->
                i.getEmiStacks().stream().map(EmiStack::getItemStack).filter(s -> !s.isEmpty()).toArray(ItemStack[]::new)
        ).toArray(ItemStack[][]::new);

        int width = recipe.getDisplayWidth();
        List<Integer> missing = new ArrayList<>();
        Set<CombinedItemStack> stored = new HashSet<>(term.getStoredItems());
        Map<CombinedItemStack, Integer> consumed = new HashMap<>();
        Map<Integer, Integer> consumedInventory = new HashMap<>();
        {
            int i = 0;
            for (ItemStack[] list : stacks) {
                if (list.length > 0) {
                    boolean found = false;
                    for (ItemStack stack : list) {
                        if (stack != null) {
                            PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();
                            int slot = inventory.getSlotWithStack(stack);
                            if (slot != -1 && inventory.getStack(slot).getCount() - consumedInventory.getOrDefault(slot, 0) > 0) {
                                found = true;
                                consumedInventory.put(slot, consumedInventory.getOrDefault(slot, 0) + 1);
                                break;
                            }
                        }
                    }

                    if (!found) {
                        for (ItemStack stack : list) {
                            CombinedItemStack s = new CombinedItemStack(stack);
                            Optional<CombinedItemStack> optional = stored.stream().filter(s1 -> s1.canCombineWith(s)).findFirst();
                            if (optional.isPresent() && optional.get().getCount() - consumed.getOrDefault(s, 0) > 0) {
                                found = true;
                                consumed.put(s, consumed.getOrDefault(s, 0) + 1);
                                break;
                            }
                        }
                    }

                    if (!found) {
                        missing.add(width == 1 ? i * 3 : width == 2 ? ((i % 2) + i / 2 * 3) : i);
                    }
                }
                i++;
            }
        }

        if (!simulate) {
            NbtCompound compound = new NbtCompound();
            NbtList list = new NbtList();
            for (int i = 0; i < stacks.length; ++i) {
                if (stacks[i] != null) {
                    NbtCompound NbtCompound = new NbtCompound();
                    NbtCompound.putByte("s", (byte) (i));
                    int k = 0;
                    for (int j = 0; j < stacks[i].length && k < 9; j++) {
                        if (stacks[i][j] != null && !stacks[i][j].isEmpty()) {
                            CombinedItemStack s = new CombinedItemStack(stacks[i][j]);
                            if (stored.contains(s) || MinecraftClient.getInstance().player.getInventory().getSlotWithStack(stacks[i][j]) != -1) {
                                NbtCompound tag = new NbtCompound();
                                stacks[i][j].writeNbt(tag);
                                NbtCompound.put("i" + (k++), tag);
                            }
                        }
                    }
                    NbtCompound.putByte("l", (byte) Math.min(9, k));
                    list.add(NbtCompound);
                }
            }
            compound.put("i", list);
            compound.putBoolean("m", Screen.hasShiftDown());
            term.sendMessage(compound);
        }
        return missing;
    }
}
