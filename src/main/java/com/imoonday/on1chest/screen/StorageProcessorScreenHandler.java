package com.imoonday.on1chest.screen;

import com.imoonday.on1chest.api.IAutoCraftingHandler;
import com.imoonday.on1chest.blocks.entities.StorageAccessorBlockEntity;
import com.imoonday.on1chest.blocks.entities.StorageMemoryBlockEntity;
import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.network.NetworkHandler;
import com.imoonday.on1chest.utils.CombinedItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
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

import java.util.*;

public class StorageProcessorScreenHandler extends StorageAssessorScreenHandler implements IAutoCraftingHandler {

    private final RecipeInputInventory input = new CraftingInventory(this, 3, 3);
    private final CraftingResultInventory result = new CraftingResultInventory();
    protected ScreenHandlerContext context;
    private boolean continuousCrafting;
    private boolean searching;

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

                ItemStack[][] lastRecipe = null;
                if (continuousCrafting) {
                    lastRecipe = new ItemStack[9][];
                    List<ItemStack> inputStacks = input.getInputStacks();
                    for (int i = 0; i < inputStacks.size(); i++) {
                        ItemStack inputStack = inputStacks.get(i);
                        if (!inputStack.isEmpty()) {
                            lastRecipe[i] = new ItemStack[]{inputStack.copyWithCount(1)};
                        }
                    }
                }

                slot.onTakeItem(player, slotStack);

                player.dropItem(slotStack, false);

                if (continuousCrafting && input.isEmpty() && lastRecipe != null) {
                    fillInputs(lastRecipe, true);
                    return ItemStack.EMPTY;
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
        if (world.isClient) return;
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
        if (nbt.contains("w")) {
            if (accessor != null && accessor.size() > (StorageMemoryBlockEntity.MAX_LEVEL + 1) * 27 * 10) {
                NetworkHandler.sendToClient(player, "w", NbtByte.ONE);
            }
        }
    }

    public void fillInputs(ItemStack[][] stacks, boolean stacked) {
        if (searching) return;
        searching = true;
        NetworkHandler.sendToClient(player, "s", NbtByte.ONE);
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
        if (stacked) {
            List<ItemStack> inputStacks = input.getInputStacks();
            List<CombinedItemStack> inputs = inputStacks.stream().map(CombinedItemStack::new).toList();
            Map<CombinedItemStack, Integer> requires = new HashMap<>(inputs.size());
            Map<CombinedItemStack, List<Integer>> stackIndices = new HashMap<>(inputs.size());
            for (int i = 0; i < inputs.size(); i++) {
                CombinedItemStack stack = inputs.get(i);
                ItemStack inputStack = inputStacks.get(i);
                int requiredCount = inputStack.getMaxCount() - inputStack.getCount();
                requires.put(stack, requires.getOrDefault(stack, 0) + requiredCount);
                stackIndices.computeIfAbsent(stack, c -> new ArrayList<>(inputs.size())).add(i);
            }
            for (Map.Entry<CombinedItemStack, Integer> entry : requires.entrySet()) {
                CombinedItemStack stack = entry.getKey();
                if (entry.getValue() > 0) {
                    CombinedItemStack pulled = accessor.takeStack(stack, entry.getValue());
                    if (pulled == null) continue;
                    ItemStack pulledStack = pulled.getActualStack();
                    if (!pulledStack.isEmpty()) {
                        List<Integer> indices = stackIndices.get(stack);
                        int size = indices.size();
                        int amount = pulledStack.getCount() / size;
                        for (int i : indices) {
                            ItemStack inputStack = input.getInputStacks().get(i);
                            inputStack.increment(pulledStack.split(amount).getCount());
                        }
                        if (pulledStack.getCount() > 0) {
                            accessor.insertOrDrop(pulledStack);
                        }
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
        searching = false;
        NetworkHandler.sendToClient(player, "s", NbtByte.ZERO);
    }

    public boolean isSearching() {
        return searching;
    }

    @Override
    public List<CombinedItemStack> getStoredItems() {
        return itemList;
    }
}
