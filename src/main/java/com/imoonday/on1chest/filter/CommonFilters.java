package com.imoonday.on1chest.filter;

import com.imoonday.on1chest.OnlyNeedOneChest;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockView;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public enum CommonFilters implements ItemFilter {
    ENCHANTED("enchanted", stack -> stack.hasEnchantments() || ItemFilter.checkItemType(stack, EnchantedBookItem.class)),
    ENCHANTED_ENCHANTED_BOOK("enchanted_book", stack -> ItemFilter.checkItemType(stack, EnchantedBookItem.class), ENCHANTED),
    ENCHANTED_NOT_ENCHANTED_BOOK("not_enchanted_book", stack -> stack.hasEnchantments() && !ItemFilter.checkItemType(stack, EnchantedBookItem.class), ENCHANTED),
    HAS_NBT("has_nbt", ItemStack::hasNbt),
    HAS_NBT_HAS_CUSTOM_NAME("has_custom_name", ItemStack::hasCustomName, HAS_NBT),
    STACKABLE("stackable", ItemStack::isStackable),
    UNSTACKABLE("unstackable", stack -> !stack.isStackable()),
    DAMAGED("damaged", ItemStack::isDamaged),
    DAMAGED_SLIGHTLY_DAMAGED("slightly_damaged", stack -> stack.isDamaged() && stack.getDamage() < stack.getMaxDamage() * 0.25, DAMAGED),
    DAMAGED_25_PERCENT_DAMAGED("25_percent_damaged", stack -> stack.isDamaged() && stack.getDamage() >= stack.getMaxDamage() * 0.25 && stack.getDamage() < stack.getMaxDamage() * 0.5, DAMAGED),
    DAMAGED_50_PERCENT_DAMAGED("50_percent_damaged", stack -> stack.isDamaged() && stack.getDamage() >= stack.getMaxDamage() * 0.5 && stack.getDamage() < stack.getMaxDamage() * 0.75, DAMAGED),
    DAMAGED_75_PERCENT_DAMAGED("75_percent_damaged", stack -> stack.isDamaged() && stack.getDamage() >= stack.getMaxDamage() * 0.75 && stack.getDamage() < stack.getMaxDamage() - 1, DAMAGED),
    DAMAGED_FULL_DAMAGED("full_damaged", stack -> stack.isDamaged() && stack.getDamage() >= stack.getMaxDamage() - 1, DAMAGED),
    FOOD_AND_DRINKS("food_and_drinks", ItemStack::isFood),
    FOOD_AND_DRINKS_STEW("stew", stack -> ItemFilter.checkItemType(stack, StewItem.class), FOOD_AND_DRINKS),
    FOOD_AND_DRINKS_POTION("potion", stack -> ItemFilter.checkItemType(stack, PotionItem.class), FOOD_AND_DRINKS),
    MOD("mod", stack -> !Registries.ITEM.getId(stack.getItem()).getNamespace().equals(Identifier.DEFAULT_NAMESPACE)),
    INGREDIENT("ingredient", stack -> ItemFilter.checkGroup(stack, ItemGroups.INGREDIENTS)),
    INGREDIENT_DYE("dye", stack -> ItemFilter.checkItemType(stack, DyeItem.class), INGREDIENT),
    INGREDIENT_SMITHING_TEMPLATE("smithing_template", stack -> ItemFilter.checkItemType(stack, SmithingTemplateItem.class), INGREDIENT),
    TOOLS_AND_UTILITIES("tools_and_utilities", stack -> ItemFilter.checkGroup(stack, ItemGroups.TOOLS) || ItemFilter.checkItemType(stack, MiningToolItem.class)),
    TOOLS_AND_UTILITIES_PICKAXE("pickaxe", stack -> ItemFilter.checkItemType(stack, PickaxeItem.class), TOOLS_AND_UTILITIES),
    TOOLS_AND_UTILITIES_AXE("axe", stack -> ItemFilter.checkItemType(stack, AxeItem.class), TOOLS_AND_UTILITIES),
    TOOLS_AND_UTILITIES_SHOVEL("shovel", stack -> ItemFilter.checkItemType(stack, ShovelItem.class), TOOLS_AND_UTILITIES),
    TOOLS_AND_UTILITIES_HOE("hoe", stack -> ItemFilter.checkItemType(stack, HoeItem.class), TOOLS_AND_UTILITIES),
    TOOLS_AND_UTILITIES_VEHICLE("vehicle", stack -> ItemFilter.checkItemType(stack, BoatItem.class, MinecartItem.class), TOOLS_AND_UTILITIES),
    TOOLS_AND_UTILITIES_MISIC_DISC("music_disc", stack -> ItemFilter.checkItemType(stack, MusicDiscItem.class), TOOLS_AND_UTILITIES),
    COMBAT("combat", stack -> ItemFilter.checkItemType(stack, SwordItem.class, TridentItem.class, RangedWeaponItem.class, ShieldItem.class) || ItemFilter.checkGroup(stack, ItemGroups.COMBAT)),
    COMBAT_MELEE("melee", stack -> ItemFilter.checkItemType(stack, SwordItem.class, TridentItem.class), COMBAT),
    COMBAT_RANGED("ranged", stack -> ItemFilter.checkItemType(stack, TridentItem.class, RangedWeaponItem.class), COMBAT),
    COMBAT_ARROW("arrow", stack -> ItemFilter.checkItemType(stack, ArrowItem.class), COMBAT),
    COMBAT_ARMOR("armor", stack -> ItemFilter.checkItemType(stack, ArmorItem.class, ShieldItem.class, HorseArmorItem.class), COMBAT),
    COMBAT_HELMET("helmet", stack -> ItemFilter.getItem(stack, ArmorItem.class).map(armorItem -> armorItem.getType() == ArmorItem.Type.HELMET).orElse(false), COMBAT),
    COMBAT_CHESTPLATE("chestplate", stack -> ItemFilter.getItem(stack, ArmorItem.class).map(armorItem -> armorItem.getType() == ArmorItem.Type.CHESTPLATE).orElse(false), COMBAT),
    COMBAT_LEGGINGS("leggings", stack -> ItemFilter.getItem(stack, ArmorItem.class).map(armorItem -> armorItem.getType() == ArmorItem.Type.LEGGINGS).orElse(false), COMBAT),
    COMBAT_BOOTS("boots", stack -> ItemFilter.getItem(stack, ArmorItem.class).map(armorItem -> armorItem.getType() == ArmorItem.Type.BOOTS).orElse(false), COMBAT),
    UPGRADABLE_TOOL("upgradable_tool", stack -> ItemFilter.checkItemType(stack, ToolItem.class)),
    UPGRADABLE_TOOL_WOOD("wood", stack -> ItemFilter.getItem(stack, ToolItem.class).map(toolItem -> toolItem.getMaterial() == ToolMaterials.WOOD).orElse(false), UPGRADABLE_TOOL),
    UPGRADABLE_TOOL_STONE("stone", stack -> ItemFilter.getItem(stack, ToolItem.class).map(toolItem -> toolItem.getMaterial() == ToolMaterials.STONE).orElse(false), UPGRADABLE_TOOL),
    UPGRADABLE_TOOL_IRON("iron", stack -> ItemFilter.getItem(stack, ToolItem.class).map(toolItem -> toolItem.getMaterial() == ToolMaterials.IRON).orElse(false), UPGRADABLE_TOOL),
    UPGRADABLE_TOOL_GOLD("gold", stack -> ItemFilter.getItem(stack, ToolItem.class).map(toolItem -> toolItem.getMaterial() == ToolMaterials.GOLD).orElse(false), UPGRADABLE_TOOL),
    UPGRADABLE_TOOL_DIAMOND("diamond", stack -> ItemFilter.getItem(stack, ToolItem.class).map(toolItem -> toolItem.getMaterial() == ToolMaterials.DIAMOND).orElse(false), UPGRADABLE_TOOL),
    UPGRADABLE_TOOL_NETHERITE("netherite", stack -> ItemFilter.getItem(stack, ToolItem.class).map(toolItem -> toolItem.getMaterial() == ToolMaterials.NETHERITE).orElse(false), UPGRADABLE_TOOL),
    UPGRADABLE_TOOL_MOD_MATERIAL("mod_material", stack -> ItemFilter.getItem(stack, ToolItem.class).map(toolItem -> !(toolItem.getMaterial() instanceof ToolMaterials)).orElse(false), UPGRADABLE_TOOL),
    UPGRADABLE_ARMOR("upgradable_armor", stack -> ItemFilter.checkItemType(stack, ArmorItem.class)),
    UPGRADABLE_ARMOR_LEATHER("leather", stack -> ItemFilter.getItem(stack, ArmorItem.class).map(armorItem -> armorItem.getMaterial() == ArmorMaterials.LEATHER).orElse(false), UPGRADABLE_ARMOR),
    UPGRADABLE_ARMOR_CHAIN("chain", stack -> ItemFilter.getItem(stack, ArmorItem.class).map(armorItem -> armorItem.getMaterial() == ArmorMaterials.CHAIN).orElse(false), UPGRADABLE_ARMOR),
    UPGRADABLE_ARMOR_IRON("iron", stack -> ItemFilter.getItem(stack, ArmorItem.class).map(armorItem -> armorItem.getMaterial() == ArmorMaterials.IRON).orElse(false), UPGRADABLE_ARMOR),
    UPGRADABLE_ARMOR_GOLD("gold", stack -> ItemFilter.getItem(stack, ArmorItem.class).map(armorItem -> armorItem.getMaterial() == ArmorMaterials.GOLD).orElse(false), UPGRADABLE_ARMOR),
    UPGRADABLE_ARMOR_DIAMOND("diamond", stack -> ItemFilter.getItem(stack, ArmorItem.class).map(armorItem -> armorItem.getMaterial() == ArmorMaterials.DIAMOND).orElse(false), UPGRADABLE_ARMOR),
    UPGRADABLE_ARMOR_NETHERITE("netherite", stack -> ItemFilter.getItem(stack, ArmorItem.class).map(armorItem -> armorItem.getMaterial() == ArmorMaterials.NETHERITE).orElse(false), UPGRADABLE_ARMOR),
    UPGRADABLE_ARMOR_TURTLE("turtle", stack -> ItemFilter.getItem(stack, ArmorItem.class).map(armorItem -> armorItem.getMaterial() == ArmorMaterials.TURTLE).orElse(false), UPGRADABLE_ARMOR),
    UPGRADABLE_ARMOR_MOD_MATERIAL("mod_material", stack -> ItemFilter.getItem(stack, ArmorItem.class).map(armorItem -> !(armorItem.getMaterial() instanceof ArmorMaterials)).orElse(false), UPGRADABLE_ARMOR),
    REDSTONE("redstone", stack -> ItemFilter.checkGroup(stack, ItemGroups.REDSTONE)),
    BLOCK("block", stack -> Registries.BLOCK.containsId(Registries.ITEM.getId(stack.getItem())) || ItemFilter.checkItemType(stack, BlockItem.class)),
    BLOCK_FULL_CUBE("full_cube", stack -> ItemFilter.getBlock(stack).map(block -> block.getDefaultState().isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)).orElse(false), BLOCK),
    BLOCK_NOT_FULL_CUBE("not_full_cube", stack -> ItemFilter.getBlock(stack).map(block -> !block.getDefaultState().isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)).orElse(false), BLOCK),
    BLOCK_STAIRS("stairs", stack -> ItemFilter.checkBlockType(stack, StairsBlock.class), BLOCK),
    BLOCK_SLAB("slab", stack -> ItemFilter.checkBlockType(stack, SlabBlock.class), BLOCK),
    BLOCK_FENCES_AND_WALLS("fences_and_walls", stack -> ItemFilter.checkBlockType(stack, FenceBlock.class, WallBlock.class), BLOCK),
    BLOCK_LIGHT_SOURCE("light_source", stack -> ItemFilter.getBlock(stack).map(block -> block.getDefaultState().getLuminance() > 0).orElse(false), BLOCK),
    BLOCK_PLANT("plant", stack -> ItemFilter.checkBlockType(stack, PlantBlock.class), BLOCK);

    private final Identifier id;
    private final Predicate<ItemStack> predicate;
    private final Identifier parent;
    private final Set<CommonFilters> children = new HashSet<>();

    CommonFilters(String id, Predicate<ItemStack> predicate) {
        this(id, predicate, null);
    }

    CommonFilters(String id, Predicate<ItemStack> predicate, CommonFilters parent) {
        if (parent != null) {
            id = parent.getId().getPath() + "." + id;
            parent.addChild(this);
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
    public boolean test(ItemStack stack, @Nullable Object data) {
        return predicate.test(stack) || children.stream().anyMatch(child -> child.test(stack, null));
    }

    @Override
    public Identifier getParent() {
        return parent;
    }

    private void addChild(CommonFilters child) {
        if (hasParent()) {
            throw new IllegalStateException("Cannot add child to a child filter");
        }
        children.add(child);
    }

    public List<CommonFilters> getChildren() {
        return List.copyOf(children);
    }
}