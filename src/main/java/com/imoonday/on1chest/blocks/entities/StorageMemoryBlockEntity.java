package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.api.ImplementedInventory;
import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class StorageMemoryBlockEntity extends BlockEntity implements ImplementedInventory {

    public static final int MAX_LEVEL = 999;
    protected int level = 0;
    private DefaultedList<ItemStack> inventory;

    public StorageMemoryBlockEntity(BlockEntityType<? extends StorageMemoryBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inventory = createInventory();
    }

    public StorageMemoryBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.STORAGE_MEMORY_BLOCK_ENTITY, pos, state);
    }

    public StorageMemoryBlockEntity(BlockPos pos, BlockState state, int level) {
        this(pos, state);
        this.level = level;
        updateInventory();
    }

    public static void tick(World world, BlockPos pos, BlockState state, StorageMemoryBlockEntity entity) {
        if (world.isClient) return;
        StorageMemoryBlock.UsedCapacity usedCapacity = entity.getUsedCapacity();
        StorageMemoryBlock.UsedCapacity usedCapacity1 = state.get(StorageMemoryBlock.USED_CAPACITY);
        if (usedCapacity != usedCapacity1) {
            world.setBlockState(pos, state.with(StorageMemoryBlock.USED_CAPACITY, usedCapacity), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    public boolean copyFrom(Inventory inventory) {
        int size = inventory.size();
        if (size != this.inventory.size()) {
            return false;
        }
        if (IntStream.range(0, size).mapToObj(inventory::getStack).allMatch(ItemStack::isEmpty)) {
            return true;
        }
        this.inventory.clear();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                this.inventory.set(i, stack.copy());
            }
        }
        return true;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        writeNbt(nbt, inventory);
        nbt.putInt("Level", level);
    }

    public static NbtCompound writeNbt(NbtCompound nbt, DefaultedList<ItemStack> stacks) {
        return writeNbt(nbt, stacks, true);
    }

    public static NbtCompound writeNbt(NbtCompound nbt, DefaultedList<ItemStack> stacks, boolean setIfEmpty) {
        NbtList nbtList = new NbtList();
        for (int i = 0; i < stacks.size(); ++i) {
            ItemStack itemStack = stacks.get(i);
            if (itemStack.isEmpty()) continue;
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putInt("Slot", i);
            itemStack.writeNbt(nbtCompound);
            nbtList.add(nbtCompound);
        }
        if (!nbtList.isEmpty() || setIfEmpty) {
            nbt.put("Items", nbtList);
        }
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("Level", NbtElement.INT_TYPE)) {
            this.level = nbt.getInt("Level");
        }
        updateInventory();
        readNbt(nbt, inventory);
    }

    public static void readNbt(NbtCompound nbt, DefaultedList<ItemStack> stacks) {
        NbtList nbtList = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            int j = nbtCompound.getInt("Slot");
            if (j < 0 || j >= stacks.size()) continue;
            stacks.set(j, ItemStack.fromNbt(nbtCompound));
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

    private DefaultedList<ItemStack> createInventory() {
        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(getStorageSize(), ItemStack.EMPTY);
        if (this.world != null && this.inventory != null && !this.world.isClient) {
            for (int i = 0; i < this.inventory.size(); i++) {
                inventory.set(i, this.inventory.get(i));
            }
        }
        return inventory;
    }

    public int getOccupiedSize() {
        return (int) getItems().stream().filter(stack -> !stack.isEmpty()).count();
    }

    public int getStorageSize() {
        return 27 * (level + 1);
    }

    public void updateLevel(int level) {
        this.level = MathHelper.clamp(level, 0, MAX_LEVEL);
        updateInventory();
    }

    public int getLevel() {
        return level;
    }

    private void updateInventory() {
        this.inventory = createInventory();
    }

    public StorageMemoryBlock.UsedCapacity getUsedCapacity() {
        int occupied = getOccupiedSize();
        int size = getStorageSize();
        if (occupied <= 0) {
            return StorageMemoryBlock.UsedCapacity.ZERO;
        } else if (occupied < size / 2) {
            return StorageMemoryBlock.UsedCapacity.LOW;
        } else if (occupied < size) {
            return StorageMemoryBlock.UsedCapacity.HIGH;
        } else {
            return StorageMemoryBlock.UsedCapacity.FULL;
        }
    }
}
