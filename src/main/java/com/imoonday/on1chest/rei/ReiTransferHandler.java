package com.imoonday.on1chest.rei;

import com.imoonday.on1chest.api.IAutoCraftingHandler;
import com.imoonday.on1chest.utils.CombinedItemStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.*;

public class ReiTransferHandler implements TransferHandler {
    private final CategoryIdentifier<?> crafting = CategoryIdentifier.of("minecraft", "plugins/crafting");

    @Override
    public Result handle(Context context) {
        if (context.getMenu() instanceof IAutoCraftingHandler handler) {
            if (!context.getDisplay().getCategoryIdentifier().equals(crafting) || context.getMinecraft().currentScreen == context.getContainerScreen()) {
                return Result.createNotApplicable();
            }
            Display recipe = context.getDisplay();
            ItemStack[][] stacks = recipe.getInputEntries().stream().map(l ->
                    l.stream().filter(es -> es.getDefinition().getValueType() == ItemStack.class).
                            map(EntryStack::getValue).filter(Objects::nonNull).toArray(ItemStack[]::new)
            ).toArray(ItemStack[][]::new);
            List<Integer> missing = new ArrayList<>();
            int width = recipe instanceof SimpleGridMenuDisplay ? ((SimpleGridMenuDisplay) recipe).getWidth() : Integer.MAX_VALUE;
            Set<CombinedItemStack> stored = new HashSet<>(handler.getStoredItems());
            Map<CombinedItemStack, Integer> consumed = new HashMap<>();
            Map<Integer, Integer> consumedInventory = new HashMap<>();
            {
                int i = 0;
                for (ItemStack[] list : stacks) {
                    if (list.length > 0) {
                        boolean found = false;
                        for (ItemStack stack : list) {
                            if (stack != null) {
                                PlayerInventory inventory = context.getMinecraft().player.getInventory();
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
            if (context.isActuallyCrafting() && missing.isEmpty()) {
                NbtCompound compound = new NbtCompound();
                NbtList list = new NbtList();
                for (int i = 0; i < stacks.length; ++i) {
                    if (stacks[i] != null) {
                        NbtCompound NbtCompound = new NbtCompound();
                        NbtCompound.putByte("s", (byte) (width == 1 ? i * 3 : width == 2 ? ((i % 2) + i / 2 * 3) : i));
                        int k = 0;
                        for (int j = 0; j < stacks[i].length && k < 9; j++) {
                            if (stacks[i][j] != null && !stacks[i][j].isEmpty()) {
                                CombinedItemStack s = new CombinedItemStack(stacks[i][j]);
                                if (stored.contains(s) || context.getMinecraft().player.getInventory().getSlotWithStack(stacks[i][j]) != -1) {
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
                compound.putBoolean("m", context.isStackedCrafting());
                handler.sendMessage(compound);
            }
            if (!missing.isEmpty()) {
                return Result.createSuccessful().color(0x67aaaa00).blocksFurtherHandling(false).
                        renderer((gr, mouseX, mouseY, delta, widgets, bounds, d) -> {
                            int i = 0;
                            for (Widget widget : widgets) {
                                if (widget instanceof Slot && ((Slot) widget).getNoticeMark() == Slot.INPUT) {
                                    if (missing.contains(i++)) {
                                        gr.getMatrices().push();
                                        gr.getMatrices().translate(0, 0, 400);
                                        Rectangle innerBounds = ((Slot) widget).getInnerBounds();
                                        gr.fill(innerBounds.x, innerBounds.y, innerBounds.getMaxX(), innerBounds.getMaxY(), 0x40ff0000);
                                        gr.getMatrices().pop();
                                    }
                                }
                            }
                        });
            }
            return Result.createSuccessful().blocksFurtherHandling();
        }
        return Result.createNotApplicable();
    }
}
