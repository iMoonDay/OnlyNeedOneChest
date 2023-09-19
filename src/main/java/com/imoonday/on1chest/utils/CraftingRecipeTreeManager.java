package com.imoonday.on1chest.utils;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class CraftingRecipeTreeManager {

    private static CraftingRecipeTreeManager MANAGER;
    private final RecipeManager recipeManager;
    private final DynamicRegistryManager registryManager;
    private List<CraftingRecipe> recipes;
    private ItemStack2RecipesMap cache;
    public ItemStack2ObjectMap<RecipeTree> recipeTreeMap;

    private CraftingRecipeTreeManager(RecipeManager recipeManager, DynamicRegistryManager registryManager) {
        this.recipeManager = recipeManager;
        this.registryManager = registryManager;
        loadRecipes();
    }

    public static CraftingRecipeTreeManager getOrCreate(RecipeManager recipeManager, DynamicRegistryManager registryManager) {
        if (MANAGER == null) {
            MANAGER = new CraftingRecipeTreeManager(recipeManager, registryManager);
        }
        return MANAGER;
    }

    public static CraftingRecipeTreeManager getOrCreate(World world) {
        MANAGER = getOrCreate(world.getRecipeManager(), world.getRegistryManager());
        return MANAGER;
    }

    private void loadRecipes() {
        this.recipes = new ArrayList<>(recipeManager.listAllOfType(RecipeType.CRAFTING));
        this.cache = new ItemStack2RecipesMap(false);
        this.recipeTreeMap = new ItemStack2ObjectMap<>(true);
        for (CraftingRecipe recipe : this.recipes) {
            ItemStack stack = recipe.getOutput(registryManager);
            if (stack.isEmpty()) {
                continue;
            }
            this.cache.putOrAdd(stack.copyWithCount(1), recipe.getIngredients());
            this.recipeTreeMap.putIfAbsent(stack.copy(), new RecipeTree(stack.copy(), recipeManager, registryManager));
        }
    }

    public List<CraftingRecipe> getRecipe(ItemStack stack) {
        return recipes.stream().filter(recipe -> recipe.getOutput(registryManager).hasNbt() ? ItemStack.canCombine(stack, recipe.getOutput(registryManager)) : stack.isOf(recipe.getOutput(registryManager).getItem())).toList();
    }

    public List<CraftingRecipe> getRecipes() {
        return new ArrayList<>(recipes);
    }

    public ItemStack2RecipesMap getCache() {
        return cache;
    }

    public void reload() {
        loadRecipes();
    }

    public CraftResult getCraftResult(Inventory inventory, ItemStack result, Collection<ItemStack> except) {
        Set<ItemStack> exceptStacks = ItemStackSet.create();
        exceptStacks.addAll(except);
        return getCraftResult(getStacks(inventory, exceptStacks), result, result, ItemStackSet.create(), 20);
    }

    private CraftResult getCraftResult(Set<ItemStack> itemStacks, ItemStack source, ItemStack result, Set<ItemStack> except, int depth) {
        CraftResult craftResult = new CraftResult();
        if (depth-- <= 0) {
            System.out.println("递归溢出");
            return craftResult;
        }
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
                int count = getOutputCount(recipe);
                CraftResult tempResult = new CraftResult();
                Map<Ingredient, Integer> missing = new HashMap<>();
                for (Ingredient ingredient : ingredients) {
                    if (ingredient.isEmpty()) {
                        continue;
                    }
                    boolean found = false;
                    Set<ItemStack> cost = tempResult.getCost();
                    if (ingredient.getMatchingStacks().length > 1 && !cost.isEmpty()) {
                        for (ItemStack tempStack : itemStacks) {
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
                                    CraftResult.increment(itemStacks, remainder);
                                }
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        for (ItemStack itemStack : itemStacks) {
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
                                    CraftResult.increment(itemStacks, remainder);
                                }
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        putOrAdd(ingredient, missing);
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
                            if (source == result) {
                                except.clear();
                            }
                            if (except.contains(itemStack)) {
                                continue;
                            }
                            if (ItemStack.canCombine(itemStack, source) || ItemStack.canCombine(itemStack, result)) {
                                continue;
                            }
                            except.add(itemStack);
                            CraftResult childCraftResult = getCraftResult(itemStacks, source, itemStack.copyWithCount(costCount), except, depth);
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
                        continue;
                    }
                }
                craftResult.addAll(tempResult);
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
                copy.decrement(getOutputCount(firstRecipe));
                boolean areEqual = areEqual(itemStacks, lastStacks, missingList, lastMissing);
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

    private static <T> void putOrAdd(T key, Map<T, Integer> map) {
        map.put(key, !map.containsKey(key) ? 1 : map.get(key) + 1);
    }

    private static Set<ItemStack> copyItemStackSet(Set<ItemStack> itemStacks) {
        Set<ItemStack> stacks = ItemStackSet.create();
        itemStacks.stream().map(ItemStack::copy).forEach(stacks::add);
        return stacks;
    }

    public int getOutputCount(CraftingRecipe recipe) {
        return recipe.getOutput(registryManager).getCount();
    }

    private boolean areEqual(Set<ItemStack> itemStacks, Set<ItemStack> otherStacks, List<Map<Ingredient, Integer>> missingList, List<Map<Ingredient, Integer>> otherList) {
        boolean areEqual;
        if (otherList.size() != missingList.size()) {
            areEqual = false;
        } else {
            List<Map<Ingredient, Integer>> list1 = new ArrayList<>(otherList);
            List<Map<Ingredient, Integer>> list2 = new ArrayList<>(missingList);
            list1.sort(Comparator.comparing(Map::toString));
            list2.sort(Comparator.comparing(Map::toString));
            areEqual = list1.equals(list2) && otherStacks.size() == itemStacks.size() && otherStacks.stream().allMatch(stack -> itemStacks.stream().anyMatch(stack1 -> ItemStack.areEqual(stack, stack1)));
        }
        return areEqual;
    }

    public static Set<ItemStack> getStacks(Inventory inventory, Set<ItemStack> except) {
        Set<ItemStack> itemStacks = ItemStackSet.create();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i).copy();
            if (except != null && except.contains(itemStack)) {
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

        public static CraftResult fail(List<Map<Ingredient, Integer>> missing) {
            CraftResult craftResult = new CraftResult();
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

        public NbtCompound toNbt() {
            NbtCompound nbtCompound = new NbtCompound();
            NbtList cost = new NbtList();
            this.cost.forEach(stack -> cost.add(stack.writeNbt(new NbtCompound())));
            NbtList remainder = new NbtList();
            this.remainder.forEach(stack -> remainder.add(stack.writeNbt(new NbtCompound())));
            NbtList missing = new NbtList();
            for (Map<Ingredient, Integer> map : this.missing) {
                NbtList ingredients = new NbtList();
                for (Map.Entry<Ingredient, Integer> entry : map.entrySet()) {
                    Ingredient ingredient = entry.getKey();
                    Integer count = entry.getValue();
                    NbtList stacks = new NbtList();
                    for (ItemStack stack : ingredient.getMatchingStacks()) {
                        stacks.add(stack.writeNbt(new NbtCompound()));
                    }
                    NbtCompound nbtCompound1 = new NbtCompound();
                    nbtCompound1.put("stacks", stacks);
                    nbtCompound1.putInt("count", count);
                    ingredients.add(nbtCompound1);
                }
                missing.add(ingredients);
            }
            nbtCompound.put("cost", cost);
            nbtCompound.put("remainder", remainder);
            nbtCompound.put("missing", missing);
            return nbtCompound;
        }

        public static CraftResult fromNbt(NbtCompound nbtCompound) {
            CraftResult craftResult = new CraftResult();
            if (nbtCompound.contains("cost", NbtElement.LIST_TYPE)) {
                craftResult.addCosts(nbtCompound.getList("cost", NbtElement.COMPOUND_TYPE).stream().filter(element -> element instanceof NbtCompound).map(element -> ((NbtCompound) element)).map(ItemStack::fromNbt).filter(stack -> stack != null && !stack.isEmpty()).collect(Collectors.toSet()));
            }
            if (nbtCompound.contains("remainder", NbtElement.LIST_TYPE)) {
                craftResult.addRemainders(nbtCompound.getList("remainder", NbtElement.COMPOUND_TYPE).stream().filter(element -> element instanceof NbtCompound).map(element -> ((NbtCompound) element)).map(ItemStack::fromNbt).filter(stack -> stack != null && !stack.isEmpty()).collect(Collectors.toSet()));
            }
            if (nbtCompound.contains("missing", NbtElement.LIST_TYPE)) {
                Set<Map<Ingredient, Integer>> missing = new HashSet<>();
                NbtList list = nbtCompound.getList("missing", NbtElement.LIST_TYPE);
                for (NbtElement nbtElement : list) {
                    if (nbtElement instanceof NbtList list1) {
                        Map<Ingredient, Integer> ingredients = new HashMap<>();
                        for (NbtElement element : list1) {
                            if (element instanceof NbtCompound compound) {
                                if (compound.contains("count", NbtElement.INT_TYPE) && compound.contains("stacks", NbtElement.LIST_TYPE)) {
                                    int count = compound.getInt("count");
                                    NbtList list2 = compound.getList("stacks", NbtElement.COMPOUND_TYPE);
                                    Set<ItemStack> itemStacks = ItemStackSet.create();
                                    for (NbtElement element1 : list2) {
                                        if (element1 instanceof NbtCompound stack) {
                                            ItemStack itemStack = ItemStack.fromNbt(stack);
                                            if (!itemStack.isEmpty()) {
                                                itemStacks.add(itemStack);
                                            }
                                        }
                                    }
                                    Ingredient ingredient = Ingredient.ofStacks(itemStacks.stream());
                                    if (!ingredient.isEmpty()) {
                                        ingredients.put(ingredient, count);
                                    }
                                }
                            }
                        }
                        missing.add(ingredients);
                    }
                }
                craftResult.missing.addAll(missing);
            }
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CraftResult that)) return false;
            return Objects.equals(cost, that.cost) && Objects.equals(remainder, that.remainder) && Objects.equals(missing, that.missing);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cost, remainder, missing);
        }
    }
}
