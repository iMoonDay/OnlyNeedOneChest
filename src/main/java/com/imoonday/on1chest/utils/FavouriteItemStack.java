package com.imoonday.on1chest.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class FavouriteItemStack {

    public static final String IGNORED_NBT = "*";
    private final Identifier id;
    @Nullable
    private final String nbt;

    public FavouriteItemStack(ItemStack stack, boolean checkNBT) {
        this.id = Registries.ITEM.getId(stack.getItem());
        NbtCompound nbt = stack.getNbt();
        this.nbt = checkNBT ? Optional.ofNullable(nbt).map(NbtElement::asString).orElse(null) : IGNORED_NBT;
    }

    private ItemStack getStack() {
        ItemStack stack = new ItemStack(Registries.ITEM.get(id));
        if (nbt != null && !nbt.equals(IGNORED_NBT)) {
            try {
                stack.setNbt(StringNbtReader.parse(nbt));
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }
        return stack;
    }

    @Override
    public String toString() {
        return nbt == null ? id.toString() : id.toString() + nbt;
    }

    @SuppressWarnings("ConstantValue")
    @Nullable
    public static FavouriteItemStack fromString(String s) {
        if (s.endsWith(IGNORED_NBT)) {
            String id = s.split("\\*", 2)[0];
            Identifier identifier = Identifier.tryParse(id);
            if (identifier == null) {
                identifier = Identifier.tryParse(Identifier.DEFAULT_NAMESPACE + ":" + id);
            }
            if (identifier == null) {
                return null;
            }
            Item item = Registries.ITEM.get(identifier);
            if (item != null) {
                return new FavouriteItemStack(new ItemStack(item), false);
            }

        }
        String[] split = s.split("\\{", 2);
        if (split.length > 0) {
            Identifier identifier = Identifier.tryParse(split[0]);
            if (identifier == null) {
                identifier = Identifier.tryParse(Identifier.DEFAULT_NAMESPACE + ":" + split[0]);
            }
            if (identifier == null) {
                return null;
            }
            NbtCompound nbt = null;
            if (split.length > 1) {
                try {
                    nbt = StringNbtReader.parse("{" + split[1]);
                } catch (CommandSyntaxException ignored) {

                }
            }
            Item item = Registries.ITEM.get(identifier);
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                if (nbt != null) {
                    stack.setNbt(nbt);
                }
                return new FavouriteItemStack(stack, true);
            }
        }
        return null;
    }

    public boolean equals(ItemStack stack) {
        return IGNORED_NBT.equals(nbt) ? ItemStack.areItemsEqual(stack, getStack()) : ItemStack.canCombine(stack, getStack());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavouriteItemStack that)) return false;
        return this.equals(that.getStack());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nbt);
    }
}
