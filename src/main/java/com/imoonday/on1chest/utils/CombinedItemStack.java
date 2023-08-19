package com.imoonday.on1chest.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class CombinedItemStack {

    private final ItemStack stack;
    private long count;
    private int hash;

    public CombinedItemStack(ItemStack stack) {
        this(stack, stack.getCount());
    }

    public CombinedItemStack(ItemStack stack, long count) {
        this.stack = stack.copyWithCount(1);
        this.count = count;
    }

    public ItemStack getStack() {
        return stack;
    }

    public long getCount() {
        return count;
    }

    public boolean isEmpty() {
        return count <= 0 || stack.isEmpty();
    }

    public ItemStack getActualStack(long count) {
        ItemStack itemStack = stack.copy();
        itemStack.setCount((int) count);
        return itemStack;
    }

    public ItemStack getActualStack() {
        return getActualStack(count);
    }

    public void writeToNBT(NbtCompound nbt) {
        writeToNBT(nbt, getCount());
    }

    public void writeToNBT(NbtCompound nbt, long count) {
        nbt.putLong("Count", count);
        nbt.put("ItemStack", stack.writeNbt(new NbtCompound()));
        nbt.getCompound("ItemStack").remove("Count");
    }

    @Nullable
    public static CombinedItemStack readFromNBT(NbtCompound nbt) {
        ItemStack itemStack = ItemStack.fromNbt(nbt);
        nbt.getCompound("ItemStack").putByte("Count", (byte) 1);
        CombinedItemStack stack = new CombinedItemStack(!itemStack.isEmpty() ? itemStack : ItemStack.fromNbt(nbt.getCompound("ItemStack")), !itemStack.isEmpty() ? itemStack.getCount() : nbt.getLong("Count"));
        return !stack.stack.isEmpty() ? stack : null;
    }

    public String getDisplayName() {
        return stack.getName().getString();
    }

    public void increment(long count) {
        this.count += count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public int getMaxCount() {
        return this.stack.getMaxCount();
    }

    public boolean canCombineWith(ItemStack stack) {
        return ItemStack.canCombine(stack, this.stack);
    }

    public boolean canCombineWith(CombinedItemStack stack) {
        return canCombineWith(stack.getStack());
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((stack == null) ? 0 : stack.getItem().hashCode());
            result = prime * result + ((stack == null || stack.getNbt() == null) ? 0 : stack.getNbt().hashCode());
            hash = result;
            return result;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CombinedItemStack other = (CombinedItemStack) obj;
        return stack == null ? other.stack == null : ItemStack.canCombine(stack, other.stack);
    }

    public boolean equals(CombinedItemStack other) {
        if (this == other) return true;
        if (other == null) return false;
        if (count != other.count) return false;
        return stack == null ? other.stack == null : ItemStack.canCombine(stack, other.stack);
    }

    @Override
    public String toString() {
        return "CombinedItemStack{" +
                "stack=" + stack +
                ", count=" + count +
                '}';
    }
}
