package com.imoonday.on1chest.screen;

import com.imoonday.on1chest.init.ModScreens;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.Optional;

public class StorageProcessorScreenHandler extends StorageAssessorScreenHandler {

    public static final int RESULT_ID = 0;
    private final RecipeInputInventory input = new CraftingInventory(this, 3, 3);
    private final CraftingResultInventory result = new CraftingResultInventory();

    public StorageProcessorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public StorageProcessorScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModScreens.STORAGE_PROCESSOR_SCREEN_HANDLER, syncId, playerInventory, context, 27, 18, 5);
        this.addCraftingSlots(playerInventory);
        this.addInventorySlots();
        this.addPlayerInventorySlots(playerInventory, 27, 137, 27, 195);
        this.scrollItems(0.0f);
    }

    private void addCraftingSlots(PlayerInventory playerInventory) {
        this.addSlot(new CraftingResultSlot(playerInventory.player, this.input, this.result, 0, 144, 132));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(this.input, j + i * 3, 50 + j * 18, 114 + i * 18));
            }
        }
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        this.input.provideRecipeInputs(finder);
    }

    @Override
    public void clearCraftingSlots() {
        this.input.clear();
        this.result.clear();
    }

    @Override
    public boolean matches(Recipe<? super RecipeInputInventory> recipe) {
        return recipe.matches(this.input, this.player.getWorld());
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.input));
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.result && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return 0;
    }

    @Override
    public int getCraftingWidth() {
        return this.input.getWidth();
    }

    @Override
    public int getCraftingHeight() {
        return this.input.getHeight();
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

    @SuppressWarnings("ConstantValue")
    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        if (player.getWorld().isClient) {
            return ItemStack.EMPTY;
        }
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack().copy();
            newStack = originalStack.copy();
            if (invSlot >= inventoryStartIndex && invSlot < playerInventoryStartIndex) {
                if (this.insertItem(originalStack, playerInventoryStartIndex, playerInventoryStartIndex + 36, true)) {
                    this.removeStack(newStack, slot);
                } else {
                    return ItemStack.EMPTY;
                }
            } else if ((invSlot >= playerInventoryStartIndex) && this.canInsert(originalStack)) {
                this.addStack(originalStack, slot);
            } else if (invSlot == RESULT_ID) {
                this.context.run((world, pos) -> originalStack.getItem().onCraft(originalStack, world, player));
                if (!this.insertItem(originalStack, playerInventoryStartIndex, playerInventoryStartIndex + 36, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(originalStack, newStack);
            } else if (invSlot > RESULT_ID && invSlot < inventoryStartIndex) {
                if (!insertItem(originalStack, playerInventoryStartIndex, playerInventoryStartIndex + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            int count = slot.getStack().getCount();
            if (originalStack.isEmpty() && count <= slot.getStack().getMaxCount()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
                if (originalStack.getCount() == newStack.getCount()) {
                    return ItemStack.EMPTY;
                }
                newStack = originalStack;
                slot.setStack(newStack);
                slot.markDirty();
            }

            slot.onTakeItem(player, originalStack);
            if (invSlot == RESULT_ID) {
                player.dropItem(originalStack, false);
            }

            updateItemList();
        }

        return newStack;
    }

    protected static void updateResult(ScreenHandler handler, World world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory) {
        ItemStack itemStack2;
        CraftingRecipe craftingRecipe;
        if (world.isClient) {
            return;
        }
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
        ItemStack itemStack = ItemStack.EMPTY;
        Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInventory, world);
        if (optional.isPresent() && resultInventory.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe = optional.get()) && (itemStack2 = craftingRecipe.craft(craftingInventory, world.getRegistryManager())).isItemEnabled(world.getEnabledFeatures())) {
            itemStack = itemStack2;
        }
        resultInventory.setStack(0, itemStack);
        handler.setPreviousTrackedSlot(0, itemStack);
        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, itemStack));
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        this.context.run((world, pos) -> StorageProcessorScreenHandler.updateResult(this, world, this.player, this.input, this.result));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
