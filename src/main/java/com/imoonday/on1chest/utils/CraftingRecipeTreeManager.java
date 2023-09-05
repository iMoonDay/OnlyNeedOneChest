package com.imoonday.on1chest.utils;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

public class CraftingRecipeTreeManager {

    private final MinecraftServer server;
    private List<CraftingRecipe> recipes;

    public CraftingRecipeTreeManager(MinecraftServer server) {
        this.server = server;
        loadRecipes();
    }

    private void loadRecipes() {
        recipes = new ArrayList<>(server.getRecipeManager().listAllOfType(RecipeType.CRAFTING));
    }

    public List<CraftingRecipe> getRecipe(ItemStack stack) {
        return recipes.stream().filter(recipe -> recipe.getOutput(server.getRegistryManager()).hasNbt() ? ItemStack.canCombine(stack, recipe.getOutput(server.getRegistryManager())) : stack.isOf(recipe.getOutput(server.getRegistryManager()).getItem())).toList();
    }

    public void reload() {
        this.recipes.clear();
        loadRecipes();
    }

    public CraftResult getCraftResult(Inventory inventory, ItemStack result, Collection<ItemStack> except) {
        Set<ItemStack> exceptStacks = ItemStackSet.create();
        exceptStacks.addAll(except);
        return getCraftResult(getStacks(inventory, exceptStacks), result, result, new HashMap<>());
    }

    private CraftResult getCraftResult(Set<ItemStack> itemStacks, ItemStack source, ItemStack result, Map<ItemStack, ItemStack> except) {
        CraftResult craftResult = CraftResult.empty();
        ItemStack copy = result.copy();
        List<CraftingRecipe> recipes = this.getRecipe(copy);
        List<Map<Ingredient, Integer>> lastMissing = new ArrayList<>();
        Set<ItemStack> lastStacks = ItemStackSet.create();
        CraftingRecipe firstRecipe = null;
        List<Map<Ingredient, Integer>> missingList = new ArrayList<>();
        while (!copy.isEmpty()) {
            boolean anyMatch = false;
            for (CraftingRecipe recipe : recipes) {
                if (firstRecipe == null) {
                    firstRecipe = recipe;
                }
                DefaultedList<Ingredient> ingredients = recipe.getIngredients();
                int count = recipe.getOutput(server.getRegistryManager()).getCount();
                CraftResult tempResult = CraftResult.empty();
                Set<ItemStack> tempStacks = ItemStackSet.create();
                Map<Ingredient, Integer> missing = new HashMap<>();
                for (ItemStack itemStack : itemStacks) {
                    tempStacks.add(itemStack.copy());
                }
                for (Ingredient ingredient : ingredients) {
                    if (ingredient.isEmpty()) {
                        continue;
                    }
                    boolean found = false;
                    Set<ItemStack> cost = tempResult.getCost();
                    if (ingredient.getMatchingStacks().length > 1 && !cost.isEmpty()) {
                        for (ItemStack tempStack : tempStacks) {
                            if (!cost.contains(tempStack)) {
                                continue;
                            }
                            if (tempStack.isEmpty()) {
                                continue;
                            }
                            if (ItemStack.canCombine(tempStack, source)) {
                                continue;
                            }
                            if (ingredient.test(tempStack)) {
                                ItemStack remainder = tempStack.getRecipeRemainder().copy();
                                tempResult.addCost(tempStack.split(1));
                                if (!remainder.isEmpty()) {
                                    tempResult.addRemainder(remainder);
                                    CraftResult.increment(tempStacks, remainder);
                                }
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        for (ItemStack itemStack : tempStacks) {
                            if (itemStack.isEmpty()) {
                                continue;
                            }
                            if (ItemStack.canCombine(itemStack, source)) {
                                continue;
                            }
                            if (ingredient.test(itemStack)) {
                                ItemStack remainder = itemStack.getRecipeRemainder().copy();
                                tempResult.addCost(itemStack.split(1));
                                if (!remainder.isEmpty()) {
                                    tempResult.addRemainder(remainder);
                                    CraftResult.increment(tempStacks, remainder);
                                }
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        if (!missing.containsKey(ingredient)) {
                            missing.put(ingredient, 1);
                        } else {
                            missing.put(ingredient, missing.get(ingredient) + 1);
                        }
                    }
                }
                if (!missing.isEmpty()) {
                    boolean allMatch = true;
                    for (Iterator<Map.Entry<Ingredient, Integer>> iterator = missing.entrySet().iterator(); iterator.hasNext(); ) {
                        Map.Entry<Ingredient, Integer> entry = iterator.next();
                        Ingredient ingredient = entry.getKey();
                        int costCount = entry.getValue();
                        boolean found = false;
                        for (ItemStack itemStack : ingredient.getMatchingStacks()) {
                            if (except.entrySet().stream().anyMatch(entry1 -> ItemStack.canCombine(entry1.getValue(), result) && ItemStack.canCombine(entry1.getKey(), itemStack))) {
                                continue;
                            }
                            if (ItemStack.canCombine(itemStack, source)) {
                                continue;
                            }
                            boolean exist = false;
                            for (Map.Entry<ItemStack, ItemStack> stackEntry : except.entrySet()) {
                                if (ItemStack.canCombine(result, stackEntry.getKey()) && ItemStack.canCombine(itemStack, stackEntry.getValue())) {
                                    exist = true;
                                    break;
                                }
                            }
                            if (!exist) {
                                except.put(result, itemStack);
                            }
                            CraftResult childCraftResult = getCraftResult(tempStacks, source, itemStack.copyWithCount(costCount), except);
                            if (childCraftResult.isCrafted()) {
                                tempResult.addCosts(childCraftResult.getCost());
                                tempResult.addRemainders(childCraftResult.getRemainder());
                                iterator.remove();
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            allMatch = false;
                        }
                    }
                    if (!allMatch) {
                        missingList.add(missing);
                        itemStacks.clear();
                        itemStacks.addAll(tempStacks);
                        continue;
                    }
                }
                craftResult.addAll(tempResult);
                itemStacks.clear();
                itemStacks.addAll(tempStacks);
                int remainder = count - copy.getCount();
                if (remainder > 0) {
                    craftResult.addRemainder(copy.copyWithCount(remainder));
                }
                copy.decrement(count);
                anyMatch = true;
                if (copy.isEmpty()) {
                    break;
                }
            }
            if (!anyMatch) {
                if (firstRecipe == null) {
                    return CraftResult.fail(missingList);
                }
                copy.decrement(firstRecipe.getOutput(server.getRegistryManager()).getCount());
                boolean areEqual;
                if (lastMissing.size() != missingList.size()) {
                    areEqual = false;
                } else {
                    List<Map<Ingredient, Integer>> list1 = new ArrayList<>(lastMissing);
                    List<Map<Ingredient, Integer>> list2 = new ArrayList<>(missingList);
                    list1.sort(Comparator.comparing(Map::toString));
                    list2.sort(Comparator.comparing(Map::toString));
                    areEqual = list1.equals(list2);
                }
                if (areEqual) {
                    if (lastStacks.size() != itemStacks.size()) {
                        areEqual = false;
                    } else {
                        areEqual = lastStacks.stream().allMatch(stack -> itemStacks.stream().anyMatch(stack1 -> ItemStack.areEqual(stack, stack1)));
                    }
                }
                if (!copy.isEmpty() && !areEqual) {
                    lastMissing = new ArrayList<>(missingList);
                    lastStacks = ItemStackSet.create();
                    lastStacks.addAll(itemStacks);
                    continue;
                }
                return CraftResult.fail(missingList);
            }
        }
        return craftResult;
    }

    public static Set<ItemStack> getStacks(Inventory inventory, Set<ItemStack> except) {
        Set<ItemStack> itemStacks = ItemStackSet.create();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i).copy();
            if (except.contains(itemStack)) {
                continue;
            }
            if (!itemStacks.contains(itemStack)) {
                itemStacks.add(itemStack);
            } else {
                List<ItemStack> list = new ArrayList<>(itemStacks);
                itemStacks.remove(itemStack);
                int count = itemStack.getCount() + list.stream().filter(stack -> ItemStack.canCombine(stack, itemStack)).toList().get(0).getCount();
                itemStacks.add(itemStack.copyWithCount(count));
            }
        }
        return itemStacks;
    }

    public static class CraftResult {

        private final Set<ItemStack> cost = ItemStackSet.create();
        private final Set<ItemStack> remainder = ItemStackSet.create();
        private final Set<Map<Ingredient, Integer>> missing = new HashSet<>();

        public CraftResult(ItemStack cost, ItemStack remainder) {
            if (cost != null) {
                this.cost.add(cost);
            }
            if (remainder != null) {
                this.remainder.add(remainder);
            }
        }

        public static CraftResult empty() {
            return new CraftResult(null, null);
        }

        public static CraftResult fail(List<Map<Ingredient, Integer>> missing) {
            CraftResult craftResult = CraftResult.empty();
            List<Map<Ingredient, Integer>> list = new ArrayList<>(missing);
            Set<Map<Ingredient, Integer>> result = new HashSet<>();
            HashMap<Map<Ingredient, Integer>, Integer> count = new HashMap<>();
            for (Map<Ingredient, Integer> map : list) {
                if (count.containsKey(map)) {
                    count.put(map, count.get(map) + 1);
                } else {
                    count.put(map, 1);
                    result.add(map);
                }
            }
            for (Map<Ingredient, Integer> m : result) {
                int times = count.get(m);
                if (times > 1) {
                    m.replaceAll((k, v) -> v * times);
                }
            }
            craftResult.missing.addAll(result);
            return craftResult;
        }

        public Set<ItemStack> getCost() {
            return cost;
        }

        public Set<ItemStack> getRemainder() {
            return remainder;
        }

        public Set<Map<Ingredient, Integer>> getMissing() {
            return missing;
        }

        private static void increment(Set<ItemStack> itemStacks, ItemStack itemStack) {
            if (!itemStacks.contains(itemStack)) {
                itemStacks.add(itemStack);
            } else {
                List<ItemStack> list = new ArrayList<>(itemStacks);
                itemStacks.remove(itemStack);
                int count = itemStack.getCount() + list.stream().filter(stack -> ItemStack.canCombine(stack, itemStack)).toList().get(0).getCount();
                itemStacks.add(itemStack.copyWithCount(count));
            }
        }

        private static void decrement(Set<ItemStack> itemStacks, ItemStack itemStack) {
            if (itemStacks.contains(itemStack)) {
                List<ItemStack> list = new ArrayList<>(itemStacks);
                itemStacks.remove(itemStack);
                int count = list.stream().filter(stack -> ItemStack.canCombine(stack, itemStack)).toList().get(0).getCount() - itemStack.getCount();
                if (count > 0) {
                    itemStacks.add(itemStack.copyWithCount(count));
                }
            }
        }

        public void addRemainder(ItemStack stack) {
            increment(this.remainder, stack);
        }

        public void addCost(ItemStack stack) {
            increment(this.cost, stack);
        }

        public void addRemainders(Collection<ItemStack> itemStacks) {
            itemStacks.forEach(stack -> increment(this.remainder, stack));
        }

        public void addCosts(Collection<ItemStack> itemStacks) {
            itemStacks.forEach(stack -> increment(this.cost, stack));
        }

        public void addAll(CraftResult craftResult) {
            craftResult.getCost().forEach(this::addCost);
            craftResult.getRemainder().forEach(this::addRemainder);
        }

        public boolean isEmpty() {
            return cost.isEmpty() && remainder.isEmpty() && missing.isEmpty();
        }

        public boolean isCrafted() {
            return (!this.cost.isEmpty() || !this.remainder.isEmpty()) && this.missing.isEmpty();
        }

        public boolean isMissing() {
            return !this.missing.isEmpty();
        }
    }
}