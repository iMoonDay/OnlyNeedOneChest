package com.imoonday.on1chest.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RecipeTree {

    private final Node root;

    public RecipeTree(ItemStack stack, RecipeManager recipeManager, DynamicRegistryManager registryManager) {
        this.root = new Node(stack, Ingredient.EMPTY, recipeManager, registryManager);
        Set<ItemStack> visited = ItemStackSet.create();
        visited.add(stack);
        buildTree(this.root, recipeManager, registryManager, visited);
    }

    private void buildTree(Node node, RecipeManager recipeManager, DynamicRegistryManager registryManager, Set<ItemStack> visited) {
        for (CraftingRecipe recipe : node.getRecipes()) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                ItemStack[] stacks = ingredient.getMatchingStacks();
                if (stacks.length > 0) {
                    ItemStack stack = stacks[0];
                    if (!visited.contains(stack)) {
                        Node child = new Node(stack, ingredient, recipeManager, registryManager);
                        node.addChild(child);
                        visited.add(stack);
                        buildTree(child, recipeManager, registryManager, visited);
                    }
                }
            }
        }
    }

    public Node getRoot() {
        return root;
    }

    public class Node {
        protected final ItemStack stack;
        protected final Ingredient ingredient;
        protected final List<CraftingRecipe> recipes;
        protected final List<Node> children;

        public Node(ItemStack stack, Ingredient ingredient, RecipeManager recipeManager, DynamicRegistryManager registryManager) {
            this(stack, ingredient, new ArrayList<>(), new ArrayList<>(), recipeManager, registryManager);
        }

        public Node(ItemStack stack, Ingredient ingredient, List<CraftingRecipe> recipes, List<Node> children, RecipeManager recipeManager, DynamicRegistryManager registryManager) {
            this.stack = stack;
            this.ingredient = ingredient;
            this.recipes = recipes;
            this.children = children;
            for (CraftingRecipe recipe : CraftingRecipeTreeManager.getOrCreate(recipeManager, registryManager).getRecipes()) {
                if (recipe != null && ItemStack.canCombine(this.getStack(), recipe.getOutput(registryManager))) {
                    this.recipes.add(recipe);
                }
            }
        }

        public ItemStack getStack() {
            return stack.copy();
        }

        public Ingredient getIngredient() {
            return ingredient;
        }

        public List<CraftingRecipe> getRecipes() {
            return Collections.unmodifiableList(recipes);
        }

        public List<Node> getChildren() {
            return Collections.unmodifiableList(children);
        }

        public void addChild(Node child) {
            this.children.add(child);
        }

        public boolean isEnd() {
            return this.children.isEmpty();
        }

        public boolean isRoot() {
            return this.ingredient.isEmpty() || this == root;
        }
    }
}
