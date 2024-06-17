package com.imoonday.on1chest.screen;

import com.imoonday.on1chest.api.IScreenDataReceiver;
import com.imoonday.on1chest.blocks.entities.QuickCraftingTableBlockEntity;
import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.network.NetworkHandler;
import com.imoonday.on1chest.utils.CombinedItemStack;
import com.imoonday.on1chest.utils.CraftingRecipeTreeManager;
import com.imoonday.on1chest.utils.ItemStack2ObjectMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class QuickCraftingScreenHandler extends ScreenHandler implements IScreenDataReceiver {

    public final Inventory result = new SimpleInventory(1);
    private final PlayerEntity player;
    private final QuickCraftingTableBlockEntity entity;
    public final List<CraftingRecipeTreeManager.CraftResult> craftResults = new ArrayList<>();
    public CraftingRecipeTreeManager.CraftResult selectedResult;

    public QuickCraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    public QuickCraftingScreenHandler(int syncId, PlayerInventory playerInventory, QuickCraftingTableBlockEntity entity) {
        super(ModScreens.QUICK_CRAFTING_SCREEN_HANDLER, syncId);
        this.player = playerInventory.player;
        this.entity = entity;
        int i;
        int j;
        this.addSlot(new ResultSlot(result, 148, 35));
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public void updateResult(ItemStack stack, boolean remove, boolean max) {
        ItemStack cursorStack = this.getCursorStack();
        ItemStack currentStack = this.result.getStack(0);
        int originalCount = currentStack.getCount();
        if (!cursorStack.isEmpty()) {
            stack = cursorStack.copy();
            remove = false;
            max = false;
        } else {
            if (ItemStack.canCombine(stack, currentStack)) {
                stack.setCount(!isCraftedList(this.craftResults) ? 1 : MathHelper.clamp(currentStack.getCount() + (remove ? -1 : 1), 1, currentStack.getMaxCount()));
            } else if (remove) {
                stack = currentStack.copy();
                stack.decrement(1);
            }
        }
        if (stack.isEmpty()) {
            clearResults();
            this.result.removeStack(0);
            this.result.markDirty();
            updateToClient();
            updateResultsToClient();
            return;
        }
        if (!max) {
            clearResults();
            this.craftResults.addAll(this.entity.getCraftResults(stack));
            if (isCraftedList(this.craftResults)) {
                this.result.setStack(0, stack.copy());
            } else if (!ItemStack.canCombine(stack, this.result.getStack(0))) {
                this.result.setStack(0, stack.copyWithCount(1));
            } else {
                if (originalCount != stack.getCount()) {
                    ItemStack itemStack = stack.copyWithCount(originalCount);
                    List<CraftingRecipeTreeManager.CraftResult> lastResult = this.entity.getCraftResults(itemStack);
                    if (isCraftedList(lastResult)) {
                        clearResults();
                        this.craftResults.addAll(lastResult);
                        this.result.setStack(0, itemStack);
                    }
                } else {
                    this.result.setStack(0, stack.copyWithCount(1));
                }
            }
        } else if (remove) {
            clearResults();
            this.result.removeStack(0);
            updateResult(stack.copyWithCount(1), false, false);
        } else {
            List<CraftingRecipeTreeManager.CraftResult> testResults = this.entity.getCraftResults(stack.copyWithCount(1));
            if (!isCraftedList(testResults)) {
                clearResults();
                this.craftResults.addAll(testResults);
                this.result.setStack(0, stack.copyWithCount(1));
            } else {
                int left = 1;
                int right = stack.getMaxCount();
                while (left <= right) {
                    int mid = (left + right) / 2;
                    List<CraftingRecipeTreeManager.CraftResult> craftResults = this.entity.getCraftResults(stack.copyWithCount(mid));
                    if (isCraftedList(craftResults)) {
                        left = mid + 1;
                    } else {
                        right = mid - 1;
                    }
                }
                boolean success = false;
                if (right > 0) {
                    List<CraftingRecipeTreeManager.CraftResult> finalResult = this.entity.getCraftResults(stack.copyWithCount(right));
                    if (isCraftedList(finalResult)) {
                        clearResults();
                        this.craftResults.addAll(finalResult);
                        this.result.setStack(0, stack.copyWithCount(right));
                        success = true;
                    }
                }
                if (!success) {
                    updateResult(stack.copyWithCount(1), false, false);
                    return;
                }
            }
        }
        this.result.markDirty();
        updateToClient();
        updateResultsToClient();
    }

    public void updateResult(ItemStack stack) {
        ItemStack original = this.result.getStack(0).copy();
        List<CraftingRecipeTreeManager.CraftResult> craftResults = this.entity.getCraftResults(stack);
        if (!isCraftedList(craftResults)) {
            craftResults = this.entity.getCraftResults(original);
            stack = original;
        }
        clearResults();
        this.craftResults.addAll(craftResults);
        this.result.setStack(0, isCraftedList(craftResults) ? stack.copy() : stack.copyWithCount(1));
        this.result.markDirty();
        updateToClient();
        updateResultsToClient();
    }

    private void clearResults() {
        this.craftResults.clear();
        this.selectedResult = null;
    }

    public static boolean isCraftedList(List<CraftingRecipeTreeManager.CraftResult> results) {
        return results.stream().anyMatch(CraftingRecipeTreeManager.CraftResult::isCrafted);
    }

    private void updateResultsToClient() {
        NbtList list = new NbtList();
        this.craftResults.forEach(craftResult -> list.add(craftResult.toNbt()));
        NetworkHandler.sendToClient(player, "results", list);
    }

    public void updateResultsFromServer(NbtCompound nbtCompound) {
        if (nbtCompound.contains("results", NbtElement.LIST_TYPE)) {
            clearResults();
            NbtList list = nbtCompound.getList("results", NbtElement.COMPOUND_TYPE);
            for (NbtElement nbtElement : list) {
                if (nbtElement instanceof NbtCompound nbtCompound1) {
                    CraftingRecipeTreeManager.CraftResult craftResult = CraftingRecipeTreeManager.CraftResult.fromNbt(nbtCompound1);
                    if (!craftResult.isEmpty()) {
                        this.craftResults.add(craftResult);
                    }
                }
            }
            if (!this.craftResults.isEmpty()) {
                this.selectedResult = this.craftResults.get(0);
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
                if (this.selectedResult == null && isCraftedList(this.craftResults)) {
                    this.selectedResult = this.craftResults.get(0);
                }
            }
        }
        if (nbt.contains("Select", NbtElement.COMPOUND_TYPE)) {
            NbtCompound nbtCompound = nbt.getCompound("Select");
            CraftingRecipeTreeManager.CraftResult craftResult = CraftingRecipeTreeManager.CraftResult.fromNbt(nbtCompound);
            if (this.craftResults.contains(craftResult)) {
                this.selectedResult = craftResult;
            }
        }
        if (nbt.contains("Confirm", NbtElement.BYTE_TYPE)) {
            boolean refresh = nbt.getBoolean("Confirm");
            if (this.selectedResult != null) {
                if (this.selectedResult.isCrafted()) {
                    if (entity == null) {
                        return;
                    }
                    CraftingRecipeTreeManager.CraftResult craftResult = selectedResult;
                    if (craftResult == null) {
                        return;
                    }
                    if (this.entity.contains(ItemStack2ObjectMap.createIntegerMap(craftResult.getCost()))) {
                        this.player.getInventory().offerOrDrop(this.result.getStack(0).copy());
                        this.player.getInventory().markDirty();
                        for (ItemStack itemStack : craftResult.getCost()) {
                            entity.takeStack(new CombinedItemStack(itemStack), itemStack.getCount());
                        }
                        for (ItemStack itemStack : craftResult.getRemainder()) {
                            entity.insertOrDrop(itemStack);
                        }
                        entity.getInventory().markDirty();
                        if (refresh) {
                            updateResult(this.result.getStack(0));
                            if (this.selectedResult == null && isCraftedList(this.craftResults)) {
                                this.selectedResult = this.craftResults.get(0);
                            }
                        } else {
                            this.result.removeStack(0);
                            this.result.markDirty();
                            craftResults.clear();
                            updateResultsToClient();
                        }
                    }
                }
            }
        }
    }

    public static class ResultSlot extends Slot {

        public ResultSlot(Inventory inventory, int x, int y) {
            super(inventory, 0, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakePartial(PlayerEntity player) {
            return false;
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }
    }
}
