package com.imoonday.on1chest.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class MultiInventory implements Inventory {

    private final List<Inventory> inventories = new ArrayList<>();
    private final List<ItemStack[]> dupDetector = new ArrayList<>();
    private int[] invSizes = new int[0];
    private int invSize;
    private boolean using;

    @Override
    public int size() {
        return invSize;
    }

    @Override
    public boolean isEmpty() {
        return inventories.stream().allMatch(Inventory::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return getFromSlot(slot, Inventory::getStack, ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return getFromSlot(slot, (inventory, i) -> inventory.removeStack(i, amount), ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return getFromSlot(slot, Inventory::removeStack, ItemStack.EMPTY);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        runFromSlot(slot, (inventory, i) -> inventory.setStack(i, stack));
    }

    @Override
    public void markDirty() {
        inventories.forEach(Inventory::markDirty);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onOpen(PlayerEntity player) {
        inventories.forEach(inventory -> inventory.onOpen(player));
    }

    @Override
    public void onClose(PlayerEntity player) {
        inventories.forEach(inventory -> inventory.onClose(player));
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return getFromSlot(slot, (inventory, i) -> inventory.isValid(i, stack), false);
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return getFromSlot(slot, (inventory, i) -> inventory.canTransferTo(hopperInventory, i, stack), false);
    }

    @Override
    public int count(Item item) {
        return (int) inventories.stream().map(inventory -> inventory.count(item)).count();
    }

    @Override
    public boolean containsAny(Set<Item> items) {
        return inventories.stream().anyMatch(inventory -> inventory.containsAny(items));
    }

    @Override
    public boolean containsAny(Predicate<ItemStack> predicate) {
        return inventories.stream().anyMatch(inventory -> inventory.containsAny(predicate));
    }

    public ItemStack insertItem(int slot, ItemStack stack) {
        return getFromSlot(slot, (inventory, s) -> {
            ItemStack itemStack = inventory.getStack(s);
            if (itemStack.isEmpty()) {
                inventory.setStack(s, stack.split(Math.min(stack.getCount(), stack.getMaxCount())));
                inventory.markDirty();
            } else {
                int count = itemStack.getCount();
                if (ItemStack.canCombine(stack, itemStack)) {
                    if (count + stack.getCount() <= itemStack.getMaxCount()) {
                        itemStack.increment(stack.getCount());
                        stack.setCount(0);
                    } else {
                        int value = itemStack.getMaxCount() - itemStack.getCount();
                        itemStack.setCount(itemStack.getMaxCount());
                        stack.decrement(value);
                    }
                    inventory.markDirty();
                }
            }
            return stack;
        }, stack);
    }

    public ItemStack insertItem(ItemStack stack) {
        a:
        for (Inventory inventory : this.inventories) {
            ItemStack itemStack;
            int i = 0;
            if (stack.isStackable()) {
                while (!stack.isEmpty() && i < inventory.size()) {
                    itemStack = inventory.getStack(i);
                    if (!itemStack.isEmpty() && ItemStack.canCombine(stack, itemStack)) {
                        int j = itemStack.getCount() + stack.getCount();
                        if (j <= stack.getMaxCount()) {
                            stack.setCount(0);
                            itemStack.setCount(j);
                            inventory.markDirty();
                        } else if (itemStack.getCount() < stack.getMaxCount()) {
                            stack.decrement(stack.getMaxCount() - itemStack.getCount());
                            itemStack.setCount(stack.getMaxCount());
                            inventory.markDirty();
                        }
                    }
                    ++i;
                }
            }
            if (!stack.isEmpty()) {
                i = 0;
                while (i < inventory.size()) {
                    itemStack = inventory.getStack(i);
                    if (itemStack.isEmpty()) {
                        if (stack.getCount() > itemStack.getMaxCount()) {
                            inventory.setStack(i, stack.split(itemStack.getMaxCount()));
                        } else {
                            inventory.setStack(i, stack.split(stack.getCount()));
                        }
                        inventory.markDirty();
                        break a;
                    }
                    ++i;
                }
            }
        }
        return stack;
    }

    public void refresh() {
        dupDetector.clear();
        if (invSizes.length != inventories.size()) {
            invSizes = new int[inventories.size()];
        }
        invSize = 0;
        for (int i = 0; i < invSizes.length; i++) {
            Inventory inv = inventories.get(i);
            if (inv == null) {
                invSizes[i] = 0;
            } else {
                int s = inv.size();
                invSizes[i] = s;
                invSize += s;
            }
        }
    }

    public void add(Inventory inventory) {
        if (checkInv(inventory)) {
            inventories.add(inventory);
        }
    }

    private boolean checkInv(Inventory inv) {
        int invDupScanSize = 100;
        int len = Math.min(invDupScanSize, inv.size());
        if (len == 0) return true;
        ItemStack[] stacks = new ItemStack[len];
        for (int i = 0; i < len; i++) {
            stacks[i] = inv.getStack(i);
        }

        for (ItemStack[] itemStacks : dupDetector) {
            int l = Math.min(len, itemStacks.length);
            for (int i = 0; i < l; i++) {
                ItemStack stack = itemStacks[i];
                if (!stack.isEmpty() && stack == stacks[i]) {
                    return false;
                }
            }
        }
        dupDetector.add(stacks);
        return true;
    }

    @Override
    public void clear() {
        invSize = 0;
        inventories.clear();
        dupDetector.clear();
    }

    private <T> T getFromSlot(int slot, BiFunction<Inventory, Integer, T> function, T defaultValue) {
        if (using || slot >= invSize) {
            return defaultValue;
        }
        using = true;
        for (int i = 0; i < invSizes.length; i++) {
            if (slot >= invSizes[i]) {
                slot -= invSizes[i];
            } else {
                T valid = function.apply(inventories.get(i), slot);
                using = false;
                return valid;
            }
        }
        using = false;
        return defaultValue;
    }

    private void runFromSlot(int slot, BiConsumer<Inventory, Integer> consumer) {
        if (using || slot >= invSize) {
            return;
        }
        using = true;
        for (int i = 0; i < invSizes.length; i++) {
            if (slot >= invSizes[i]) {
                slot -= invSizes[i];
            } else {
                consumer.accept(inventories.get(i), slot);
                using = false;
                return;
            }
        }
        using = false;
    }

    @Override
    public String toString() {
        return "MultiInventory{" +
                "inventories=" + inventories +
                ", dupDetector=" + dupDetector +
                ", invSizes=" + Arrays.toString(invSizes) +
                ", invSize=" + invSize +
                ", using=" + using +
                '}';
    }
}
