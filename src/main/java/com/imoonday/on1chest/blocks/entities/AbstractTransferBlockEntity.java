package com.imoonday.on1chest.blocks.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AbstractTransferBlockEntity extends BlockEntity {

    public float uniqueOffset;
    protected int cooldown;
    protected Item target;
    protected boolean matchMode;

    public AbstractTransferBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected static void tickUpdate(World world, BlockPos pos, BlockState state, AbstractTransferBlockEntity entity) {
        boolean updateListeners = false;
        if (entity.uniqueOffset == 0) {
            entity.uniqueOffset = world.random.nextFloat() * 360;
            updateListeners = true;
        }
        if (entity.target == Items.AIR) {
            entity.target = null;
            updateListeners = true;
        }
        if (updateListeners) {
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }
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

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("cooldown", cooldown);
        if (target != null && target != Items.AIR) {
            nbt.putString("target", Registries.ITEM.getId(target).toString());
        }
        nbt.putFloat("uniqueOffset", uniqueOffset);
        nbt.putBoolean("matchMode", matchMode);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("cooldown", NbtElement.INT_TYPE)) {
            this.cooldown = nbt.getInt("cooldown");
        }
        this.target = null;
        if (nbt.contains("target", NbtElement.STRING_TYPE)) {
            String id = nbt.getString("target");
            Identifier identifier = Identifier.tryParse(id);
            if (identifier != null) {
                Item item = Registries.ITEM.get(identifier);
                if (item != Items.AIR) {
                    this.target = item;
                }
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
