package com.imoonday.on1chest.filter;

import com.imoonday.on1chest.OnlyNeedOneChest;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockView;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public enum ItemFilters implements ItemFilter {
    ENCHANTED("enchanted", stack -> stack.hasEnchantments() || checkType(stack, EnchantedBookItem.class)),
    ENCHANTED_ENCHANTED_BOOK("enchanted_book", stack -> checkType(stack, EnchantedBookItem.class), ENCHANTED),
    ENCHANTED_NOT_ENCHANTED_BOOK("not_enchanted_book", stack -> !checkType(stack, EnchantedBookItem.class), ENCHANTED),
    HAS_NBT("has_nbt", ItemStack::hasNbt),
    DAMAGED("damaged", ItemStack::isDamaged),
    FOOD("food_and_drinks", ItemStack::isFood),
    FOOD_STEW("stew", stack -> checkType(stack, StewItem.class), FOOD),
    FOOD_POTION("potion", stack -> checkType(stack, PotionItem.class), FOOD),
    MOD("mod", stack -> !Registries.ITEM.getId(stack.getItem()).getNamespace().equals(Identifier.DEFAULT_NAMESPACE)),
    INGREDIENT("ingredient", stack -> checkGroup(stack, ItemGroups.INGREDIENTS)),
    INGREDIENT_DYE("dye", stack -> checkType(stack, DyeItem.class), INGREDIENT),
    INGREDIENT_SMITHING_TEMPLATE("smithing_template", stack -> checkType(stack, SmithingTemplateItem.class), INGREDIENT),
    TOOL("tools_and_utilities", stack -> checkGroup(stack, ItemGroups.TOOLS)),
    TOOL_PICKAXE("pickaxe", stack -> checkType(stack, PickaxeItem.class), TOOL),
    TOOL_AXE("axe", stack -> checkType(stack, AxeItem.class), TOOL),
    TOOL_SHOVEL("shovel", stack -> checkType(stack, ShovelItem.class), TOOL),
    TOOL_HOE("hoe", stack -> checkType(stack, HoeItem.class), TOOL),
    TOOL_VEHICLE("vehicle", stack -> checkType(stack, BoatItem.class, MinecartItem.class), TOOL),
    TOOL_MISIC_DISC("music_disc", stack -> checkType(stack, MusicDiscItem.class), TOOL),
    COMBAT("combat", stack -> checkType(stack, SwordItem.class, TridentItem.class, RangedWeaponItem.class, ShieldItem.class) || checkGroup(stack, ItemGroups.COMBAT)),
    COMBAT_MELEE("melee", stack -> checkType(stack, SwordItem.class, TridentItem.class), COMBAT),
    COMBAT_RANGED("ranged", stack -> checkType(stack, TridentItem.class, RangedWeaponItem.class), COMBAT),
    COMBAT_ARMOR("armor", stack -> checkType(stack, ArmorItem.class, ShieldItem.class, HorseArmorItem.class), COMBAT),
    COMBAT_ARROW("arrow", stack -> checkType(stack, ArrowItem.class), COMBAT),
    REDSTONE("redstone", stack -> checkGroup(stack, ItemGroups.REDSTONE)),
    BLOCK("block", stack -> Registries.BLOCK.containsId(Registries.ITEM.getId(stack.getItem()))),
    BLOCK_FULL_CUBE("full_cube", stack -> checkType(stack, BlockItem.class) && ((BlockItem) stack.getItem()).getBlock().getDefaultState().isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN), BLOCK),
    BLOCK_NOT_FULL_CUBE("not_full_cube", stack -> checkType(stack, BlockItem.class) && !((BlockItem) stack.getItem()).getBlock().getDefaultState().isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN), BLOCK),
    REGULAR_EXPRESSION("regular_expression") {
        @Override
        public boolean hasExtraData() {
            return true;
        }

        @Override
        public Text getTooltip(ItemFilterInstance filterInstance) {
            List<String> data = filterInstance.getData();
            return data == null || data.isEmpty() ? Text.empty() : Text.of(String.join("\n", data));
        }

        @Override
        public boolean hiddenByDefault() {
            return true;
        }

        @Override
        public boolean test(ItemStack stack, @Nullable List<String> data) {
            if (data == null || data.isEmpty()) return true;
            List<String> strings = List.of(
                    Registries.ITEM.getId(stack.getItem()).toString(),
                    stack.getName().getString()
            );
            for (String str : data) {
                try {
                    if (strings.stream().noneMatch(s -> s.matches(str))) {
                        return false;
                    }
                } catch (PatternSyntaxException ignored) {

                }
            }
            return true;
        }
    },
    CUSTOM("custom") {

        private static final Text DATA_TOOLTIP = Text.translatable("filter.on1chest.custom.data.tooltip");

        @Override
        public boolean hasExtraData() {
            return true;
        }

        @Override
        public Text getTooltip(ItemFilterInstance filterInstance) {
            List<String> data = filterInstance.getData();
            if (data == null || data.isEmpty()) return Text.empty();
            String validIds = data.stream().map(Identifier::tryParse)
                                  .filter(Objects::nonNull)
                                  .map(Registries.ITEM::getOrEmpty)
                                  .filter(Optional::isPresent)
                                  .map(Optional::get)
                                  .filter(item -> item != Items.AIR)
                                  .map(item -> item.getName().getString())
                                  .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                                      int size = list.size();
                                      if (size > 9) {
                                          return String.join("\n", list.subList(0, 9)) + "\n...(+" + (size - 9) + ")";
                                      } else {
                                          return String.join("\n", list);
                                      }
                                  }));
            return Text.literal(validIds);
        }

        @Override
        public Text getDataTooltip() {
            return DATA_TOOLTIP;
        }

        @Override
        public boolean hiddenByDefault() {
            return true;
        }

        @Override
        public boolean test(ItemStack stack, @Nullable List<String> data) {
            Identifier itemId = Registries.ITEM.getId(stack.getItem());
            return data == null || data.stream().map(Identifier::tryParse)
                                       .filter(Objects::nonNull)
                                       .anyMatch(itemId::equals);
        }
    };

    private final Identifier id;
    private final Predicate<ItemStack> predicate;
    private final Identifier parent;

    ItemFilters(String id) {
        this(id, null);
    }

    ItemFilters(String id, Predicate<ItemStack> predicate) {
        this(id, predicate, null);
    }

    ItemFilters(String id, Predicate<ItemStack> predicate, ItemFilter parent) {
        if (parent != null) {
            id = parent.getId().getPath() + "." + id;
        }
        this.id = OnlyNeedOneChest.id(id);
        this.predicate = predicate;
        if (parent != null) {
            this.parent = parent.getId();
        } else {
            this.parent = null;
        }
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public boolean test(ItemStack stack, @Nullable List<String> data) {
        return predicate == null || predicate.test(stack);
    }

    @Override
    public Identifier getParent() {
        return parent;
    }

    @SafeVarargs
    public static boolean checkType(ItemStack stack, Class<? extends ItemConvertible>... classes) {
        Item item = stack.getItem();
        return Arrays.stream(classes).anyMatch(clazz -> clazz.isInstance(item));
    }

    public static boolean checkGroup(ItemStack stack, RegistryKey<ItemGroup> groupKey) {
        ItemGroup itemGroup = Registries.ITEM_GROUP.get(groupKey);
        return itemGroup != null && itemGroup.getDisplayStacks().stream().anyMatch(stack1 -> ItemStack.areItemsEqual(stack, stack1));
    }
}