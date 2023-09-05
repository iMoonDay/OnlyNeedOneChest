package com.imoonday.on1chest.screen;

import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.items.RecipeRecordCardItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Optional;

//@IPNPlayerSideOnly
public class RecipeProcessorScreenHandler extends AbstractRecipeScreenHandler<RecipeInputInventory> {

    public final int cardIndex;
    public final int cardsStartIndex;
    public final int cardsEndIndex;
    public final int inventoryStartIndex;
    public final int inventoryEndIndex;
    public final int resultIndex;
    public final int ingredientsStartIndex;
    public final int ingredientsEndIndex;
    public final int playerInventoryStartIndex;
    public final int playerInventoryEndIndex;
    protected final PlayerEntity player;
    protected final Inventory cards;
    protected final RecipeInputInventory ingredients = new RecipeInventory();
    protected final CraftingResultInventory result = new CraftingResultInventory();
    protected ScreenHandlerContext context;

    public RecipeProcessorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(27), new SimpleInventory(4), ScreenHandlerContext.EMPTY);
    }

    public RecipeProcessorScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, Inventory cards, ScreenHandlerContext context) {
        super(ModScreens.RECIPE_PROCESSOR_SCREEN_HANDLER, syncId);
        this.player = playerInventory.player;
        this.cards = cards;
        this.context = context;
        checkSize(inventory, 27);
        checkSize(cards, 4);
        this.cardIndex = this.slots.size();
        addCardSlot(cards);
        this.cardsStartIndex = this.slots.size();
        addCardSlots(cards);
        this.cardsEndIndex = this.slots.size() - 1;
        this.inventoryStartIndex = this.slots.size();
        addInventorySlots(inventory);
        this.inventoryEndIndex = this.slots.size() - 1;
        this.resultIndex = this.slots.size();
        addResultSlot();
        this.ingredientsStartIndex = this.slots.size();
        addIngredientSlots();
        this.ingredientsEndIndex = this.slots.size() - 1;
        this.playerInventoryStartIndex = this.slots.size();
        addPlayerInventorySlots(playerInventory);
        this.playerInventoryEndIndex = this.slots.size() - 1;
        updateIngredients();
    }

    private void addPlayerInventorySlots(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 148 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 206));
        }
    }

    private void addIngredientSlots() {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new IngredientSlot(this.ingredients, j + i * 3, 44 + j * 18, 83 + i * 18));
            }
        }
    }

    private void addResultSlot() {
        this.addSlot(new ResultSlot(result, 0, 138, 101));
    }

    private void addCardSlot(Inventory cards) {
        this.addSlot(new CardSlot(cards, 0, 15, 101));
    }

    private void addCardSlots(Inventory cards) {
        for (int i = 0; i < 3; i++) {
            this.addSlot(new CardSlot(cards, i + 1, 178, 18 + i * 18));
        }
    }

    private void addInventorySlots(Inventory inventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }
    }

    private void updateIngredients() {
        if (player.getWorld().isClient) {
            return;
        }
        ItemStack stack = this.cards.getStack(0);
        if (stack.getItem() instanceof RecipeRecordCardItem cardItem) {
            DefaultedList<ItemStack> items = cardItem.getItems(stack);
            for (int i = 0; i < items.size(); i++) {
                ItemStack itemStack = items.get(i);
                this.ingredients.setStack(i, itemStack);
            }
        } else {
            this.ingredients.clear();
        }
        onContentChanged(this.ingredients);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < playerInventoryStartIndex) {
                if (!this.insertItem(originalStack, playerInventoryStartIndex, playerInventoryEndIndex + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, resultIndex, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        if (slot instanceof CardSlot cardSlot) {
            return cardSlot.canInsert(stack);
        }
        return slot.inventory != this.result && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    public boolean canInsertIntoSlot(Slot slot) {
        return slot.inventory != this.result && super.canInsertIntoSlot(slot);
    }

    protected static void updateResult(RecipeProcessorScreenHandler handler, World world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory) {
        ItemStack itemStack2;
        CraftingRecipe craftingRecipe = null;
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
        ItemStack itemStack = ItemStack.EMPTY;
        Optional<CraftingRecipe> optional = serverWorld.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInventory, world);
        if (optional.isPresent() && resultInventory.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe = optional.get()) && (itemStack2 = craftingRecipe.craft(craftingInventory, world.getRegistryManager())).isItemEnabled(world.getEnabledFeatures())) {
            itemStack = itemStack2;
        }
        ItemStack stack = handler.cards.getStack(0);
        if (stack.getItem() instanceof RecipeRecordCardItem cardItem) {
            cardItem.setRecipe(stack, craftingRecipe);
            cardItem.setItems(stack, handler.ingredients);
            cardItem.setResult(stack, itemStack);
        }
        resultInventory.setStack(0, itemStack);
        handler.setPreviousTrackedSlot(handler.resultIndex, itemStack);
        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), handler.resultIndex, itemStack));
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        this.context.run((world, pos) -> updateResult(this, world, this.player, this.ingredients, this.result));
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        this.ingredients.provideRecipeInputs(finder);
    }

    @Override
    public void clearCraftingSlots() {
        this.ingredients.clear();
        this.result.clear();
    }

    @Override
    public boolean matches(Recipe<? super RecipeInputInventory> recipe) {
        return recipe.matches(this.ingredients, this.player.getWorld());
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return resultIndex;
    }

    @Override
    public int getCraftingWidth() {
        return this.ingredients.getWidth();
    }

    @Override
    public int getCraftingHeight() {
        return this.ingredients.getHeight();
    }

    @Override
    public int getCraftingSlotCount() {
        return 10;
    }

    @Override
    public RecipeBookCategory getCategory() {
        return RecipeBookCategory.CRAFTING;
    }

    @Override
    public boolean canInsertIntoSlot(int index) {
        return index != this.getCraftingResultSlotIndex();
    }

    private static class ResultSlot extends Slot {

        public ResultSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canTakePartial(PlayerEntity player) {
            return false;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }

        @Override
        public ItemStack takeStack(int amount) {
            return ItemStack.EMPTY;
        }
    }

    private class CardSlot extends Slot {

        public CardSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.getItem() instanceof RecipeRecordCardItem;
        }

        @Override
        public void markDirty() {
            super.markDirty();
            updateIngredients();
        }
    }

    private class IngredientSlot extends Slot {

        public IngredientSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return !cards.getStack(0).isEmpty();
        }
    }

    private class RecipeInventory extends CraftingInventory {
        public RecipeInventory() {
            super(RecipeProcessorScreenHandler.this, 3, 3);
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }
    }
}
