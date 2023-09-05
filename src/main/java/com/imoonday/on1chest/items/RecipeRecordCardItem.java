package com.imoonday.on1chest.items;

import com.imoonday.on1chest.client.gui.tooltip.RecipeTooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RecipeRecordCardItem extends Item {
    public RecipeRecordCardItem(Settings settings) {
        super(settings);
    }

    @Nullable
    public Recipe<?> getRecipe(MinecraftServer server, ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("Recipe", NbtElement.STRING_TYPE)) {
            return null;
        }
        String recipe = nbt.getString("Recipe");
        Identifier identifier = Identifier.tryParse(recipe);
        if (identifier == null) {
            return null;
        }
        return server.getRecipeManager().get(identifier).orElse(null);
    }

    public void setRecipe(ItemStack stack, @Nullable Recipe<?> recipe) {
        if (recipe == null) {
            stack.getOrCreateNbt().remove("Recipe");
            return;
        }
        stack.getOrCreateNbt().putString("Recipe", recipe.getId().toString());
    }

    public DefaultedList<ItemStack> getItems(ItemStack stack) {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(9, ItemStack.EMPTY);
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("Items", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
            for (NbtElement nbtElement : list) {
                if (nbtElement instanceof NbtCompound nbtCompound && nbtCompound.contains("Slot", NbtElement.BYTE_TYPE) && nbtCompound.contains("Item", NbtElement.COMPOUND_TYPE)) {
                    byte slot = nbtCompound.getByte("Slot");
                    NbtCompound item = nbtCompound.getCompound("Item");
                    ItemStack itemStack = ItemStack.fromNbt(item);
                    if (itemStack != null && !itemStack.isEmpty()) {
                        itemStacks.set(slot, itemStack);
                    }
                }
            }
        }
        return itemStacks;
    }

    public void setItems(ItemStack stack, RecipeInputInventory ingredients) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.remove("Items");
        NbtList list = new NbtList();
        for (int i = 0; i < ingredients.getInputStacks().size(); i++) {
            ItemStack ingredient = ingredients.getInputStacks().get(i);
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putByte("Slot", (byte) i);
            nbtCompound.put("Item", ingredient.writeNbt(new NbtCompound()));
            list.add(nbtCompound);
        }
        nbt.put("Items", list);
    }

    public ItemStack getResult(ItemStack stack) {
        ItemStack result = ItemStack.EMPTY;
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("Result", NbtElement.COMPOUND_TYPE)) {
            result = ItemStack.fromNbt(nbt.getCompound("Result"));
        }
        return result;
    }

    public void setResult(ItemStack stack, ItemStack result) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.put("Result", result.writeNbt(new NbtCompound()));
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Optional.of(new RecipeTooltipComponent.RecipeTooltipData(this.getItems(stack), this.getResult(stack)));
    }
}
