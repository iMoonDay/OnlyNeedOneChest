package com.imoonday.on1chest.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class MultiInventory implements Inventory {

    private final List<InsertionPredicate> insertionPredicates = new ArrayList<>();
    private final List<RemovalPredicate> removalPredicates = new ArrayList<>();
    private final List<Inventory> inventories = new ArrayList<>();
    private final List<ItemStack[]> dupDetector = new ArrayList<>();
    @Nullable
    private List<MultiInventoryChangedListener> listeners;
    private int[] invSizes = new int[0];
    private int invSize;
    private boolean using;

    public MultiInventory() {

    }

    public MultiInventory(List<Inventory> inventories) {
        inventories.forEach(this::add);
        this.refresh();
    }

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
        return getFromSlot(slot, Inventory::getStack, ItemStack.EMPTY, false);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return getFromSlot(slot, (inventory, i) -> {
            if (!canRemove(inventory, i)) return ItemStack.EMPTY;
            return inventory.removeStack(i, amount);
        }, ItemStack.EMPTY, true);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return getFromSlot(slot, (inventory, i) -> {
            if (!canRemove(inventory, i)) return ItemStack.EMPTY;
            return inventory.removeStack(i);
        }, ItemStack.EMPTY, true);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        runFromSlot(slot, (inventory, i) -> {
            if (!inventory.getStack(i).isEmpty() && !canRemove(inventory, i)) return;
            if (!canInsert(inventory, i, stack)) return;
            inventory.setStack(i, stack);
        }, true);
    }

    @Override
    public void markDirty() {
        inventories.forEach(Inventory::markDirty);
        if (this.listeners != null) {
            for (MultiInventoryChangedListener listener : this.listeners) {
                listener.onInventoryChanged(this);
            }
        }
    }

    public void addListener(MultiInventoryChangedListener listener) {
        if (this.listeners == null) {
            this.listeners = new ArrayList<>();
        }
        this.listeners.add(listener);
    }

    public void removeListener(MultiInventoryChangedListener listener) {
        if (this.listeners != null) {
            this.listeners.remove(listener);
        }
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
        return getFromSlot(slot, (inventory, i) -> inventory.isValid(i, stack), false, false);
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return getFromSlot(slot, (inventory, i) -> {
            if (!canRemove(inventory, i)) return false;
            return inventory.canTransferTo(hopperInventory, i, stack);
        }, false, false);
    }

    @Override
    public int count(Item item) {
        return inventories.stream().mapToInt(inventory -> inventory.count(item)).sum();
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
            if (!canInsert(inventory, s, stack)) return stack;
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
        }, stack, true);
    }

    public ItemStack insertItem(ItemStack stack) {
        a:
        for (Inventory inventory : this.inventories) {
            ItemStack itemStack;
            int i = 0;
            if (stack.isStackable()) {
                while (!stack.isEmpty() && i < inventory.size()) {
                    if (!canInsert(inventory, i, stack)) {
                        ++i;
                        continue;
                    }
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
                    if (!canInsert(inventory, i, stack)) {
                        ++i;
                        continue;
                    }
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
        markDirty();
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

    private <T> T getFromSlot(int slot, BiFunction<Inventory, Integer, T> function, T defaultValue, boolean markDirty) {
        if (using || slot >= invSize) {
            return defaultValue;
        }
        using = true;
        for (int i = 0; i < invSizes.length; i++) {
            if (slot >= invSizes[i]) {
                slot -= invSizes[i];
            } else {
                T valid = function.apply(inventories.get(i), slot);
                if (markDirty) {
                    markDirty();
                }
                using = false;
                return valid;
            }
        }
        using = false;
        return defaultValue;
    }

    private void runFromSlot(int slot, BiConsumer<Inventory, Integer> consumer, boolean markDirty) {
        if (using || slot >= invSize) {
            return;
        }
        using = true;
        for (int i = 0; i < invSizes.length; i++) {
            if (slot >= invSizes[i]) {
                slot -= invSizes[i];
            } else {
                consumer.accept(inventories.get(i), slot);
                if (markDirty) {
                    markDirty();
                }
                using = false;
                return;
            }
        }
        using = false;
    }

    public void addInsertionPredicate(InsertionPredicate insertionPredicate) {
        insertionPredicates.add(insertionPredicate);
    }

    public boolean removeInsertionPredicate(InsertionPredicate insertionPredicate) {
        return insertionPredicates.remove(insertionPredicate);
    }

    public List<InsertionPredicate> getInsertionPredicates() {
        return List.copyOf(insertionPredicates);
    }

    public void addRemovalPredicate(RemovalPredicate removalPredicate) {
        removalPredicates.add(removalPredicate);
    }

    public boolean removeRemovalPredicate(RemovalPredicate removalPredicate) {
        return removalPredicates.remove(removalPredicate);
    }

    public List<RemovalPredicate> getRemovalPredicates() {
        return List.copyOf(removalPredicates);
    }

    public boolean canRemove(Inventory inventory, int slot) {
        for (RemovalPredicate predicate : removalPredicates) {
            if (!predicate.canRemove(inventory, slot)) {
                return false;
            }
        }
        return true;
    }

    public boolean canInsert(Inventory inventory, int slot, ItemStack stack) {
        if (!inventory.isValid(slot, stack)) return false;
        for (InsertionPredicate predicate : insertionPredicates) {
            if (!predicate.canInsert(inventory, slot, stack)) {
                return false;
            }
        }
        return true;
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

    public interface MultiInventoryChangedListener {
        void onInventoryChanged(MultiInventory inventory);
    }

    public interface RemovalPredicate {

        boolean canRemove(Inventory inventory, int slot);
    }

    public interface InsertionPredicate {

        boolean canInsert(Inventory inventory, int slot, ItemStack stack);
    }
}
