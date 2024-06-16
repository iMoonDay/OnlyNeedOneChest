package com.imoonday.on1chest.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Deprecated
public class RecipeTree {

    private static final ItemStack2ObjectMap<RecipeTree> CACHE = new ItemStack2ObjectMap<>(true);
    private final RecipeManager recipeManager;
    private final DynamicRegistryManager registryManager;
    private final RootNode root;

    private RecipeTree(ItemStack stack, RecipeManager recipeManager, DynamicRegistryManager registryManager) {
        this.recipeManager = recipeManager;
        this.registryManager = registryManager;
        this.root = new RootNode(stack);
    }

    @NotNull
    public static RecipeTree getOrCreate(Item item, RecipeManager recipeManager, DynamicRegistryManager registryManager) {
        return CACHE.computeIfAbsent(new ItemStack(item), (ItemStack stack) -> new RecipeTree(stack, recipeManager, registryManager));
    }

    @NotNull
    public static RecipeTree getOrCreate(ItemStack stack, RecipeManager recipeManager, DynamicRegistryManager registryManager) {
        return CACHE.computeIfAbsent(stack, (ItemStack stack1) -> new RecipeTree(stack1, recipeManager, registryManager));
    }

    @Nullable
    public static RecipeTree getOrNull(ItemStack stack) {
        return CACHE.get(stack);
    }


    public RootNode getRoot() {
        return root;
    }

    public void print() {
        print(root, "|");
    }

    private void print(Node<?, ?, ?> node, String indent) {
        boolean printed = false;
        if (node instanceof ItemNode) {
            System.out.println(indent + node.toString().replace("物品: ", ""));
            printed = true;
        }
        String newIndent = indent;
        if (printed) {
            newIndent += "-";
        }
        for (Node<?, ?, ?> child : node.getChildren()) {
            print(child, newIndent);
        }
    }

    public abstract static class Node<P extends Node<?, ?, ?>, O, N extends Node<?, ?, ?>> {
        @Nullable
        protected final P parent;
        protected final O obj;
        protected int count = 1;
        protected final Set<N> children;

        protected Node(@Nullable P parent, O obj) {
            this.parent = parent;
            this.obj = obj;
            this.children = this.generateChildren();
        }

        protected abstract Set<N> generateChildren();

        public O get() {
            return obj;
        }

        public Set<N> getChildren() {
            return children;
        }

        protected static <T extends Node<?, ?, ?>> void addChild(Set<T> set, T node) {
            for (T obj : set) {
                if (obj.equals(node)) {
                    obj.count++;
                    return;
                }
            }
            set.add(node);
        }

        public int getCount() {
            int count = this.count;
            Node<?, ?, ?> parent = this.parent;
            while (parent != null) {
                count *= parent.count;
                parent = parent.parent;
            }
            return count;
        }

        @Nullable
        public P getParent() {
            return parent;
        }

        public boolean isRoot() {
            return parent == null;
        }

        public boolean isLeaf() {
            return false;
        }

        protected boolean isValidNode() {
            return this.isLeaf() || !this.children.isEmpty();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node<?, ?, ?> node)) return false;
            return Objects.equals(obj, node.obj);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(obj);
        }
    }

    public class ItemNode extends Node<IngredientNode, ItemStack, RecipeNode> {

        public ItemNode(IngredientNode parent, ItemStack stack) {
            super(parent, stack);
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        protected Set<RecipeNode> generateChildren() {
            Set<RecipeNode> set = new LinkedHashSet<>();
            recipeManager.listAllOfType(RecipeType.CRAFTING)
                    .stream()
                    .filter(recipe -> filter(recipe) && ItemStack.canCombine(recipe.getOutput(registryManager), this.get()))
                    .map(recipe -> new RecipeNode(this, recipe))
                    .filter(Node::isValidNode)
                    .forEach(node -> addChild(set, node));
            return set;
        }

        @Override
        public int getCount() {
            if (parent == null) return get().getCount();
            try {
                assert parent.parent != null;
                assert parent.parent.parent != null;
                int requiredCount = parent.parent.parent.getCount();
                int resultCount = parent.parent.get().getOutput(registryManager).getCount();
                return (int) Math.ceil((double) requiredCount / resultCount) * parent.count;
            } catch (Throwable ignored) {
                System.out.println("计算数量失败 (迭代错误)");
                return super.getCount();
            }
        }

        private boolean filter(CraftingRecipe recipe) {
            if (recipe.isEmpty()) return false;
            Node<?, ?, ?> node = this.parent;
            while (node != null) {
                if (node instanceof RecipeNode recipeNode && recipe.getId().equals(recipeNode.get().getId())) {
                    return false;
                }
                node = node.parent;
            }
            return true;
        }

        public Set<ItemNode> getChildItemNodes() {
            Set<ItemNode> set = new LinkedHashSet<>();
            for (RecipeNode recipeNode : children) {
                for (IngredientNode ingredientNode : recipeNode.children) {
                    set.addAll(ingredientNode.children);
                }
            }
            return set;
        }

        @Override
        public boolean equals(Object o) {
            boolean equals = super.equals(o);
            if (!equals && o instanceof ItemNode node) {
                equals = ItemStack.canCombine(this.get(), node.get());
            }
            return equals;
        }

        @Override
        public String toString() {
            return "物品: " + this.get().getName().getString() + " x" + this.getCount();
        }
    }

    public class RootNode extends ItemNode {

        public RootNode(ItemStack stack) {
            super(null, stack);
        }
    }

    public class RecipeNode extends Node<ItemNode, CraftingRecipe, IngredientNode> {

        public RecipeNode(ItemNode parent, CraftingRecipe recipe) {
            super(parent, recipe);
        }

        @Override
        protected Set<IngredientNode> generateChildren() {
            Set<IngredientNode> set = new LinkedHashSet<>();
            this.get().getIngredients()
                    .stream()
                    .filter(this::filter)
                    .map(ingredient -> new IngredientNode(this, ingredient))
                    .filter(Node::isValidNode)
                    .forEach(ingredient -> addChild(set, ingredient));
            return set;
        }

        private boolean filter(Ingredient ingredient) {
            if (ingredient.isEmpty()) return false;
            Node<?, ?, ?> node = this.parent;
            while (node != null) {
                if (node instanceof IngredientNode ingredientNode && ingredient.getMatchingItemIds().equals(ingredientNode.get().getMatchingItemIds())) {
                    return false;
                }
                node = node.parent;
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            boolean equals = super.equals(o);
            if (!equals && o instanceof RecipeNode node) {
                equals = this.get().getId().equals(node.get().getId());
            }
            return equals;
        }

        @Override
        public String toString() {
            return "配方: " + this.get().getId().toString();
        }
    }

    public class IngredientNode extends Node<RecipeNode, Ingredient, ItemNode> {

        public IngredientNode(RecipeNode parent, Ingredient ingredient) {
            super(parent, ingredient);
        }

        @Override
        protected Set<ItemNode> generateChildren() {
            Set<ItemNode> set = new LinkedHashSet<>();
            Arrays.stream(this.get().getMatchingStacks())
                    .filter(this::filter)
                    .map(stack -> new ItemNode(this, stack))
                    .filter(Node::isValidNode)
                    .forEach(item -> addChild(set, item));
            return set;
        }

        private boolean filter(ItemStack stack) {
            if (stack.isEmpty()) return false;
            Node<?, ?, ?> node = this.parent;
            while (node != null) {
                if (node instanceof ItemNode itemNode && ItemStack.canCombine(stack, itemNode.get())) {
                    return false;
                }
                node = node.parent;
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            boolean equals = super.equals(o);
            if (!equals && o instanceof IngredientNode node) {
                equals = this.get().getMatchingItemIds().equals(node.get().getMatchingItemIds());
            }
            return equals;
        }

        @Override
        public String toString() {
            return "材料: " + Arrays.stream(this.get().getMatchingStacks()).map(ItemStack::getName).map(Text::getString).collect(Collectors.joining(" / "));
        }
    }
}