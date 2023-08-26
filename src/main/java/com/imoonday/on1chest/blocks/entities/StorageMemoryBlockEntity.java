package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.utils.ImplementedInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class StorageMemoryBlockEntity extends BlockEntity implements ImplementedInventory {

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
        if (world.isClient) {
            return;
        }
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

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("Level", level);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("Level", NbtElement.INT_TYPE)) {
            this.level = nbt.getInt("Level");
        }
        updateInventory();
        Inventories.readNbt(nbt, inventory);
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

    public void updateLevel(StorageMemoryBlock block) {
        this.level = block.getLevel();
        if (this.level < 0) {
            this.level = 0;
        }
        updateInventory();
    }

    public int getLevel() {
        return level;
    }

    private void updateInventory() {
        this.inventory = createInventory();
    }

    @Override
    public int getMaxCountPerStack() {
        return Integer.MAX_VALUE;
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
