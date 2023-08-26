package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.blocks.ItemExporterBlock;
import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.utils.ConnectBlock;
import com.imoonday.on1chest.utils.PositionPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ItemExporterBlockEntity extends BlockEntity {

    private int cooldown;
    private Item target;
    public float uniqueOffset;
    private boolean matchMode;

    public ItemExporterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_EXPORTER_BLOCK_ENTITY, pos, state);
    }

    public Item getTarget() {
        return target;
    }

    public boolean setTarget(Item target) {
        if (this.target == target) {
            return false;
        }
        this.target = target;
        return true;
    }

    public static void tick(World world, BlockPos pos, BlockState state, ItemExporterBlockEntity entity) {
        if (entity.uniqueOffset == 0) {
            entity.uniqueOffset = world.random.nextFloat() * 360;
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }
        if (--entity.cooldown <= 0) {
            Direction opposite = state.get(ItemExporterBlock.FACING).getOpposite();
            BlockPos offset = pos.offset(opposite);
            if (world.getBlockEntity(offset) instanceof Inventory inventory) {
                BlockState blockState = world.getBlockState(offset);
                if (blockState.getBlock() instanceof ChestBlock chestBlock) {
                    Inventory largeInv = ChestBlock.getInventory(chestBlock, blockState, world, offset, true);
                    if (largeInv != null) {
                        inventory = largeInv;
                    }
                }
                start:
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stack = inventory.getStack(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (entity.target != null && !stack.isOf(entity.target)) {
                        continue;
                    }
                    List<Inventory> inventories = ConnectBlock.getConnectedBlocks(world, pos, PositionPredicate.create(world, offset).add((world1, pos1) -> world1.getBlockEntity(pos1) instanceof StorageMemoryBlockEntity && Arrays.stream(Direction.values()).anyMatch(direction -> world1.getBlockEntity(pos1.offset(direction)) instanceof ItemExporterBlockEntity exporter && exporter.getCachedState().get(ItemExporterBlock.FACING) == direction && stack.getItem() == exporter.target))).stream().filter(pair -> pair.getLeft().getBlockState(pair.getRight()).getBlock() instanceof StorageMemoryBlock && pair.getLeft().getBlockState(pair.getRight()).get(StorageMemoryBlock.ACTIVATED) && pair.getLeft().getBlockEntity(pair.getRight()) instanceof StorageMemoryBlockEntity).map(pair -> (Inventory) pair.getLeft().getBlockEntity(pair.getRight())).toList();
                    if (inventories.isEmpty()) {
                        return;
                    }
                    for (Inventory validInv : inventories) {
                        for (int j = 0; j < validInv.size(); j++) {
                            ItemStack invStack = validInv.getStack(j);
                            if (ItemStack.canCombine(invStack, stack) && invStack.getCount() < invStack.getMaxCount()) {
                                if (invStack.getCount() + stack.getCount() <= invStack.getMaxCount()) {
                                    invStack.increment(stack.getCount());
                                    stack.setCount(0);
                                } else {
                                    int increased = invStack.getMaxCount() - invStack.getCount();
                                    invStack.increment(increased);
                                    stack.decrement(increased);
                                }
                                validInv.markDirty();
                                inventory.markDirty();
                                if (stack.isEmpty()) {
                                    break start;
                                }
                            }
                        }
                        if (entity.matchMode && !validInv.containsAny(stack1 -> ItemStack.areItemsEqual(stack, stack1))) {
                            continue;
                        }
                        if (!stack.isEmpty()) {
                            for (int j = 0; j < validInv.size(); j++) {
                                ItemStack invStack = validInv.getStack(j);
                                if (invStack.isEmpty()) {
                                    validInv.setStack(j, stack.split(stack.getMaxCount()));
                                    validInv.markDirty();
                                    inventory.markDirty();
                                }
                                if (stack.isEmpty()) {
                                    break start;
                                }
                            }
                        }
                    }
                }
                entity.cooldown = 5;
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("cooldown", cooldown);
        nbt.putString("target", Registries.ITEM.getId(target).toString());
        nbt.putFloat("uniqueOffset", uniqueOffset);
        nbt.putBoolean("matchMode", matchMode);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("cooldown", NbtElement.INT_TYPE)) {
            this.cooldown = nbt.getInt("cooldown");
        }
        if (nbt.contains("target", NbtElement.STRING_TYPE)) {
            String id = nbt.getString("target");
            Identifier identifier = Identifier.tryParse(id);
            if (identifier != null) {
                this.target = Registries.ITEM.get(identifier);
            }
        }
        if (nbt.contains("uniqueOffset", NbtElement.FLOAT_TYPE)) {
            this.uniqueOffset = nbt.getFloat("uniqueOffset");
        }
        if (nbt.contains("matchMode", NbtElement.BYTE_TYPE)) {
            this.matchMode = nbt.getBoolean("matchMode");
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

    public boolean isMatchMode() {
        return matchMode;
    }

    public void setMatchMode(boolean matchMode) {
        this.matchMode = matchMode;
    }
}
