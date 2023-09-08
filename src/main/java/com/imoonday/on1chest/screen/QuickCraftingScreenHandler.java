package com.imoonday.on1chest.screen;

import com.imoonday.on1chest.blocks.entities.QuickCraftingTableBlockEntity;
import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.utils.CombinedItemStack;
import com.imoonday.on1chest.utils.CraftingRecipeTreeManager;
import com.imoonday.on1chest.utils.IScreenDataReceiver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class QuickCraftingScreenHandler extends ScreenHandler implements IScreenDataReceiver {

    protected final Inventory result = new SimpleInventory(1);
    private final QuickCraftingTableBlockEntity entity;
    private final List<CraftingRecipeTreeManager.CraftResult> craftResults = new ArrayList<>();

    public QuickCraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    public QuickCraftingScreenHandler(int syncId, PlayerInventory playerInventory, QuickCraftingTableBlockEntity entity) {
        super(ModScreens.QUICK_CRAFTING_SCREEN_HANDLER, syncId);
        this.entity = entity;
        int i;
        int j;
        this.addSlot(new ResultSlot(result, 148, 123));
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 158 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 216));
        }
    }

    public void updateResult(ItemStack stack, boolean remove, boolean max) {
        ItemStack currentStack = this.result.getStack(0);
        if (ItemStack.canCombine(stack, currentStack)) {
            stack.setCount(MathHelper.clamp(currentStack.getCount() + (remove ? -1 : 1), 1, currentStack.getMaxCount()));
        } else if (remove) {
            return;
        }
        if (!max) {
            this.craftResults.clear();
            this.craftResults.addAll(this.entity.getCraftResults(stack, 10));
            if (!this.craftResults.isEmpty()) {
                this.result.setStack(0, stack.copy());
            } else if (!ItemStack.canCombine(stack, this.result.getStack(0))) {
                this.result.setStack(0, Items.BARRIER.getDefaultStack());
            }
        } else if (remove) {
            this.craftResults.clear();
            this.result.removeStack(0);
        } else {
            int maxCount = stack.getMaxCount();
            List<CraftingRecipeTreeManager.CraftResult> craftResults = new ArrayList<>();
            while (craftResults.isEmpty() && maxCount > 0) {
                craftResults = this.entity.getCraftResults(stack.copyWithCount(maxCount--), 10);
            }
            if (!craftResults.isEmpty()) {
                this.craftResults.clear();
                this.craftResults.addAll(craftResults);
                this.result.setStack(0, stack.copyWithCount(maxCount + 1));
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < 28) {
                if (!this.insertItem(originalStack, 28, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 1, 27, false)) {
                return ItemStack.EMPTY;
            }

            if (slot.inventory == result) {
                slot.onTakeItem(player, newStack);
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void receive(NbtCompound nbt) {
        if (nbt.contains("Craft", NbtElement.COMPOUND_TYPE)) {
            NbtCompound nbtCompound = nbt.getCompound("Craft");
            ItemStack itemStack = ItemStack.fromNbt(nbtCompound);
            if (!itemStack.isEmpty()) {
                boolean remove = nbt.contains("Button", NbtElement.INT_TYPE) && nbt.getInt("Button") == 1;
                boolean max = nbt.contains("Shift", NbtElement.BYTE_TYPE) && nbt.getBoolean("Shift");
                this.updateResult(itemStack, remove, max);
            }
        }
    }

    public class ResultSlot extends Slot {

        public ResultSlot(Inventory inventory, int x, int y) {
            super(inventory, 0, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeItems(PlayerEntity player) {
            return !this.getStack().isOf(Items.BARRIER);
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            super.onTakeItem(player, stack);
            List<CraftingRecipeTreeManager.CraftResult> list = QuickCraftingScreenHandler.this.craftResults;
            if (list.isEmpty()) {
                return;
            }
            CraftingRecipeTreeManager.CraftResult craftResult = list.get(0);
            for (ItemStack itemStack : craftResult.getCost()) {
                QuickCraftingScreenHandler.this.entity.takeStack(new CombinedItemStack(itemStack), itemStack.getCount());
            }
            for (ItemStack itemStack : craftResult.getRemainder()) {
                QuickCraftingScreenHandler.this.entity.insertOrDrop(itemStack);
            }
            QuickCraftingScreenHandler.this.craftResults.clear();
        }
    }
}
