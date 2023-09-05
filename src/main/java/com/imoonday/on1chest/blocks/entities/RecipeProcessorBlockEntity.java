package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.items.RecipeRecordCardItem;
import com.imoonday.on1chest.screen.RecipeProcessorScreenHandler;
import com.imoonday.on1chest.utils.ImplementedInventory;
import com.imoonday.on1chest.utils.RecipeFilter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class RecipeProcessorBlockEntity extends BlockEntity implements ImplementedInventory, NamedScreenHandlerFactory, RecipeFilter {

    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
    private final Inventory cards = new SimpleInventory(4);
    private DefaultedList<ItemStack> results = DefaultedList.ofSize(4, ItemStack.EMPTY);

    public RecipeProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RECIPE_PROCESSOR_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, RecipeProcessorBlockEntity entity) {
        DefaultedList<ItemStack> lastResult = entity.results;
        entity.results = DefaultedList.ofSize(4, ItemStack.EMPTY);
        Inventory cards = entity.getCards();
        for (int i = 0; i < cards.size(); i++) {
            ItemStack card = cards.getStack(i);
            start:
            if (card.getItem() instanceof RecipeRecordCardItem cardItem) {
                MinecraftServer server = ((ServerWorld) world).getServer();
                Recipe<?> recipe = cardItem.getRecipe(server, card);
                if (recipe != null) {
                    ItemStack result = recipe.getOutput(server.getRegistryManager()).copy();
                    if (result != null && !result.isEmpty()) {
                        entity.results.set(i, result.copy());
                    }
                    if (!world.isReceivingRedstonePower(pos)) {
                        continue;
                    }
                    DefaultedList<Ingredient> ingredients = recipe.getIngredients();
                    DefaultedList<ItemStack> items = entity.getItems();
                    DefaultedList<ItemStack> temp = DefaultedList.ofSize(items.size(), ItemStack.EMPTY);
                    for (int j = 0; j < items.size(); j++) {
                        temp.set(j, items.get(j).copy());
                    }
                    for (Ingredient ingredient : ingredients) {
                        boolean found = false;
                        for (ItemStack stack : temp) {
                            if (ingredient.test(stack)) {
                                stack.decrement(1);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            break start;
                        }
                    }
                    if (result != null && !result.isEmpty()) {
                        for (ItemStack stack : temp) {
                            int count = stack.getCount();
                            int maxCount = stack.getMaxCount();
                            if (ItemStack.canCombine(stack, result) && count < maxCount) {
                                if (count + result.getCount() <= maxCount) {
                                    stack.increment(result.getCount());
                                    result.setCount(0);
                                } else {
                                    int increment = maxCount - count;
                                    stack.increment(increment);
                                    result.decrement(increment);
                                }
                            }
                            if (result.isEmpty()) {
                                break;
                            }
                        }
                        if (!result.isEmpty()) {
                            for (int j = 0; j < temp.size(); j++) {
                                ItemStack stack = temp.get(j);
                                if (stack.isEmpty()) {
                                    temp.set(j, result.split(result.getCount()));
                                    break;
                                }
                            }
                        }
                        if (result.isEmpty()) {
                            entity.inventory = temp;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < lastResult.size(); i++) {
            ItemStack last = lastResult.get(i);
            if (!ItemStack.canCombine(last, entity.getCards().getStack(i))) {
                world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
                break;
            }
        }
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    public DefaultedList<ItemStack> getResults() {
        return results;
    }

    public Inventory getCards() {
        return cards;
    }

    public void setCard(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.cards.size()) {
            return;
        }
        this.cards.setStack(slot, stack);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        NbtList list = new NbtList();
        Inventory cards = getCards();
        for (int i = 0; i < cards.size(); i++) {
            ItemStack card = cards.getStack(i);
            list.add(card.writeNbt(new NbtCompound()));
        }
        nbt.put("Cards", list);
        list = new NbtList();
        DefaultedList<ItemStack> results = getResults();
        for (ItemStack stack : results) {
            list.add(stack.writeNbt(new NbtCompound()));
        }
        nbt.put("Results", list);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        if (nbt.contains("Cards", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("Cards", NbtElement.COMPOUND_TYPE);
            int size = list.size();
            if (size <= 4) {
                for (int i = 0; i < size; i++) {
                    NbtElement nbtElement = list.get(i);
                    NbtCompound nbtCompound = (NbtCompound) nbtElement;
                    ItemStack itemStack = ItemStack.fromNbt(nbtCompound);
                    if (!itemStack.isEmpty()) {
                        this.setCard(i, itemStack);
                    }
                }
            }
        }
        Collections.fill(this.results, ItemStack.EMPTY);
        if (nbt.contains("Results", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("Results", NbtElement.COMPOUND_TYPE);
            int size = list.size();
            if (size <= 4) {
                for (int i = 0; i < size; i++) {
                    NbtElement nbtElement = list.get(i);
                    NbtCompound nbtCompound = (NbtCompound) nbtElement;
                    ItemStack itemStack = ItemStack.fromNbt(nbtCompound);
                    if (!itemStack.isEmpty()) {
                        this.results.set(i, itemStack);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public Text getDisplayName() {
        return this.getCachedState().getBlock().getName();
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new RecipeProcessorScreenHandler(syncId, playerInventory, this, this.getCards(), ScreenHandlerContext.create(world, pos));
    }


    @Override
    public boolean shouldFilter() {
        for (int i = 0; i < this.getCards().size(); i++) {
            ItemStack stack = this.getCards().getStack(i);
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean testIngredient(MinecraftServer server, ItemStack stack) {
        Inventory cards = this.getCards();
        for (int i = 0; i < cards.size(); i++) {
            ItemStack card = cards.getStack(i);
            if (!(card.getItem() instanceof RecipeRecordCardItem cardItem)) {
                continue;
            }
            Recipe<?> recipe = cardItem.getRecipe(server, card);
            if (recipe != null && recipe.getIngredients().stream().anyMatch(ingredient -> ingredient.test(stack))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean testOutput(MinecraftServer server, ItemStack stack) {
        if (this.testIngredient(server, stack)) {
            return false;
        }
        Inventory cards = this.getCards();
        for (int i = 0; i < cards.size(); i++) {
            ItemStack card = cards.getStack(i);
            if (!(card.getItem() instanceof RecipeRecordCardItem cardItem)) {
                continue;
            }
            Recipe<?> recipe = cardItem.getRecipe(server, card);
            if (recipe != null && recipe.getOutput(server.getRegistryManager()).getItem() == stack.getItem()) {
                return true;
            }
        }
        return false;
    }
}
