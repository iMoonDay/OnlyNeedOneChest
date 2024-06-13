package com.imoonday.on1chest.screen;

import com.imoonday.on1chest.blocks.entities.StorageAccessorBlockEntity;
import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.utils.CombinedItemStack;
import com.imoonday.on1chest.utils.IAutoCraftingHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
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

import java.util.List;
import java.util.Optional;

//@IPNPlayerSideOnly
public class StorageProcessorScreenHandler extends StorageAssessorScreenHandler implements IAutoCraftingHandler {

    private final RecipeInputInventory input = new CraftingInventory(this, 3, 3);
    private final CraftingResultInventory result = new CraftingResultInventory();
    protected ScreenHandlerContext context;
    private boolean continuousCrafting;
    private ItemStack[][] lastRecipe;

    public StorageProcessorScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY, null);
    }

    public StorageProcessorScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, StorageAccessorBlockEntity accessor) {
        super(ModScreens.STORAGE_PROCESSOR_SCREEN_HANDLER, syncId, playerInventory, accessor);
        this.context = context;
        this.addCraftingSlots(playerInventory);
        this.addPlayerInventorySlots(playerInventory, 137, 195);
        this.addStorageSlots(5, 18);
    }

    private void addCraftingSlots(PlayerInventory playerInventory) {
        this.addSlot(new CraftingResultSlot(playerInventory.player, this.input, this.result, 0, 135, 132));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(this.input, j + i * 3, 41 + j * 18, 114 + i * 18));
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
    protected ItemStack shiftClickItems(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            ItemStack itemStack = slotStack.copy();
            if (index == 0) {
                if (accessor == null) return ItemStack.EMPTY;

                if (!this.insertItem(slotStack, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickTransfer(slotStack, itemStack);

                if (slotStack.isEmpty()) {
                    slot.setStack(ItemStack.EMPTY);
                } else {
                    slot.markDirty();
                }

                if (slotStack.getCount() == itemStack.getCount()) {
                    return ItemStack.EMPTY;
                }

                slot.onTakeItem(player, slotStack);

                player.dropItem(slotStack, false);

                if (continuousCrafting) {
                    if (input.isEmpty() && lastRecipe != null) {
                        fillInputs(lastRecipe, true);
                        return ItemStack.EMPTY;
                    } else {
                        lastRecipe = new ItemStack[9][];
                        List<ItemStack> inputStacks = input.getInputStacks();
                        for (int i = 0; i < inputStacks.size(); i++) {
                            ItemStack inputStack = inputStacks.get(i);
                            if (!inputStack.isEmpty()) {
                                lastRecipe[i] = new ItemStack[]{inputStack.copyWithCount(1)};
                            }
                        }
                    }
                }

                return itemStack;
            } else if (index > 0 && index < 10) {
                if (accessor == null) return ItemStack.EMPTY;
                ItemStack stack = accessor.insertStack(itemStack);
                slot.setStack(stack);
                if (!player.getWorld().isClient) {
                    sendContentUpdates();
                }
            }
            slot.onTakeItem(player, slotStack);
        }
        return ItemStack.EMPTY;
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

    @Override
    public void receive(NbtCompound nbt) {
        super.receive(nbt);
        if (nbt.contains("c")) {
            continuousCrafting = nbt.getBoolean("c");
        }
        if (nbt.contains("i")) {
            for (ItemStack stack : input.getInputStacks()) {
                accessor.insertOrDrop(stack);
            }
            input.clear();
            ItemStack[][] stacks = new ItemStack[9][];
            NbtList list = nbt.getList("i", 10);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound compound = list.getCompound(i);
                byte slot = compound.getByte("s");
                byte l = compound.getByte("l");
                stacks[slot] = new ItemStack[l];
                for (int j = 0; j < l; j++) {
                    NbtCompound tag = compound.getCompound("i" + j);
                    stacks[slot][j] = ItemStack.fromNbt(tag);
                }
            }
            fillInputs(stacks, nbt.getBoolean("m"));
        }
    }

    public void fillInputs(ItemStack[][] stacks, boolean stacked) {
        if (stacked) {
            boolean[] full = new boolean[9];
            repeat:
            while (true) {
                boolean allFull = true;
                for (int i = 0; i < 9; i++) {
                    if (stacks[i] != null && stacks[i].length > 0 && !full[i]) {
                        allFull = false;
                        break;
                    }
                }
                if (allFull) {
                    break;
                }
                for (int i = 0; i < 9; i++) {
                    if (stacks[i] != null && stacks[i].length > 0 && !full[i]) {
                        ItemStack stack = ItemStack.EMPTY;
                        for (int j = 0; j < stacks[i].length; j++) {
                            ItemStack pulled = accessor.takeStack(stacks[i][j]);
                            if (!pulled.isEmpty()) {
                                stack = pulled;
                                break;
                            }
                        }
                        if (stack.isEmpty()) {
                            PlayerInventory inventory = player.getInventory();
                            for (int j = 0; j < stacks[i].length; j++) {
                                boolean br = false;
                                for (int k = 0; k < inventory.size(); k++) {
                                    if (ItemStack.canCombine(inventory.getStack(k), stacks[i][j])) {
                                        stack = inventory.removeStack(k, 1);
                                        br = true;
                                        break;
                                    }
                                }
                                if (br) {
                                    break;
                                }
                            }
                        }
                        if (!stack.isEmpty()) {
                            ItemStack inputStack = input.getStack(i);
                            if (ItemStack.canCombine(stack, inputStack)) {
                                inputStack.increment(stack.split(inputStack.getMaxCount() - inputStack.getCount()).getCount());
                                if (!stack.isEmpty()) {
                                    accessor.insertOrDrop(stack);
                                    full[i] = true;
                                }
                            } else {
                                accessor.insertOrDrop(inputStack);
                                input.setStack(i, stack);
                            }
                        } else {
                            break repeat;
                        }
                    }
                }
            }
            int maxCount = 0;
            int minCount = Integer.MAX_VALUE;
            for (int i = 0; i < 9; i++) {
                int count = input.getStack(i).getCount();
                if (count > 0) {
                    maxCount = Math.max(maxCount, count);
                    minCount = Math.min(minCount, count);
                }
            }
            if (maxCount != minCount) {
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = input.getStack(i);
                    int count = stack.getCount();
                    if (count > minCount) {
                        accessor.insertOrDrop(stack.split(count - minCount));
                    }
                }
            }
        } else {
            for (int i = 0; i < 9; i++) {
                if (stacks[i] != null) {
                    ItemStack stack = ItemStack.EMPTY;
                    for (int j = 0; j < stacks[i].length; j++) {
                        ItemStack pulled = accessor.takeStack(stacks[i][j]);
                        if (!pulled.isEmpty()) {
                            stack = pulled;
                            break;
                        }
                    }
                    if (stack.isEmpty()) {
                        PlayerInventory inventory = player.getInventory();
                        for (int j = 0; j < stacks[i].length; j++) {
                            boolean br = false;
                            for (int k = 0; k < inventory.size(); k++) {
                                if (ItemStack.canCombine(inventory.getStack(k), stacks[i][j])) {
                                    stack = inventory.removeStack(k, 1);
                                    br = true;
                                    break;
                                }
                            }
                            if (br) {
                                break;
                            }
                        }
                    }
                    if (!stack.isEmpty()) {
                        input.setStack(i, stack);
                    }
                }
            }
        }
    }

    @Override
    public List<CombinedItemStack> getStoredItems() {
        return itemList;
    }
}
