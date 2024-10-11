package com.imoonday.on1chest.filter;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

public interface ItemFilter extends Serializable {

    String DEFAULT_PREFIX = "filter.on1chest.";
    Text NO_DATA_TEXT = Text.translatable("filter.on1chest.no_data");
    Text UNKOWN_TEXT = Text.translatable("filter.on1chest.unknown");

    Identifier getId();

    default String getTranslationKey() {
        return DEFAULT_PREFIX + getId().getPath();
    }

    default Text getDisplayName() {
        return Text.translatable(getTranslationKey());
    }

    default Text getDisplayName(boolean hidden) {
        Text name = getDisplayName();
        return hidden ? name.copy().append(" ").append(Text.translatable("filter.on1chest.hidden")).formatted(Formatting.GRAY) : name;
    }

    default Text getTooltip(ItemFilterSettings settings) {
        return Text.empty();
    }

    default Text getDataTooltip() {
        return Text.empty();
    }

    default Identifier getParent() {
        return null;
    }

    default boolean hasParent() {
        return getParent() != null;
    }

    default boolean hasExtraData() {
        return false;
    }

    default Supplier<?> getDefaultData() {
        return () -> null;
    }

    default Text getDataDisplayName() {
        return Text.translatable(getTranslationKey() + ".data");
    }

    default boolean hiddenByDefault() {
        return false;
    }

    default boolean alwaysTrue() {
        return false;
    }

    boolean test(ItemStack stack, @Nullable Object data);

    @SafeVarargs
    static boolean checkItemType(ItemStack stack, Class<? extends Item>... classes) {
        Item item = stack.getItem();
        return Arrays.stream(classes).anyMatch(clazz -> clazz.isInstance(item));
    }

    @SafeVarargs
    static boolean checkBlockType(ItemStack stack, Class<? extends Block>... classes) {
        return stack.getItem() instanceof BlockItem item && Arrays.stream(classes).anyMatch(clazz -> clazz.isInstance(item.getBlock()));
    }

    static boolean checkGroup(ItemStack stack, RegistryKey<ItemGroup> groupKey) {
        ItemGroup itemGroup = Registries.ITEM_GROUP.get(groupKey);
        return itemGroup != null && itemGroup.getDisplayStacks().stream().anyMatch(itemStack -> ItemStack.areItemsEqual(stack, itemStack));
    }

    static Optional<Block> getBlock(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item ? Optional.of(item.getBlock()) : Optional.empty();
    }

    static <T> Optional<T> getItem(ItemStack stack, Class<T> clazz) {
        Item item = stack.getItem();
        return clazz.isInstance(item) ? Optional.of(clazz.cast(item)) : Optional.empty();
    }

    enum DisplayType {
        DISPLAY, ALL, HIDE;

        public Text getDisplayName() {
            return Text.translatable("filter.on1chest.display_type." + name().toLowerCase());
        }

        public DisplayType next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public boolean isDisplay() {
            return this == DISPLAY || this == ALL;
        }

        public boolean isDisplay(boolean hide) {
            return this == ALL || this == DISPLAY && !hide;
        }

        public boolean isHide() {
            return this == HIDE;
        }
    }

    enum FilteringLogic {
        AND, OR;

        public Text getDisplayName() {
            return Text.translatable("filter.on1chest.filtering_logic." + name().toLowerCase());
        }

        public FilteringLogic next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public boolean isAnd() {
            return this == AND;
        }

        public boolean isOr() {
            return this == OR;
        }
    }
}