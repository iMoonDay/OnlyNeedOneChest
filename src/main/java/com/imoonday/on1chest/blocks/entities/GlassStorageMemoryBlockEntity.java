package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GlassStorageMemoryBlockEntity extends StorageMemoryBlockEntity {

    protected ItemStack displayItem = ItemStack.EMPTY;
    public float uniqueOffset;

    protected GlassStorageMemoryBlockEntity(BlockEntityType<? extends GlassStorageMemoryBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.uniqueOffset = Random.create().nextFloat() * (float) Math.PI * 2.0f;
    }

    public GlassStorageMemoryBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.GLASS_STORAGE_MEMORY_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, GlassStorageMemoryBlockEntity entity) {
        StorageMemoryBlockEntity.tick(world, pos, state, entity);
        if (!state.get(StorageMemoryBlock.ACTIVATED)) {
            entity.displayItem = ItemStack.EMPTY;
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
            return;
        }
        boolean noStack = entity.getItems().stream().noneMatch(stack -> ItemStack.canCombine(stack, entity.displayItem));
        if (world.getTime() % 30 == 0 || entity.displayItem.isEmpty() || noStack) {
            List<ItemStack> stacks = entity.getItems().stream().filter(stack -> !stack.isEmpty() && !ItemStack.canCombine(stack, entity.displayItem)).collect(Collectors.toList());
            if (stacks.isEmpty()) {
                if (noStack) {
                    entity.displayItem = ItemStack.EMPTY;
                }
            } else {
                Collections.shuffle(stacks);
                entity.displayItem = stacks.get(0).copy();
            }
        }
        world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
    }

    public ItemStack getDisplayItem() {
        return displayItem.copy();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("displayItem", displayItem.writeNbt(new NbtCompound()));
        nbt.putFloat("uniqueOffset", uniqueOffset);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("displayItem", NbtElement.COMPOUND_TYPE)) {
            this.displayItem = ItemStack.fromNbt(nbt.getCompound("displayItem"));
        }
        if (nbt.contains("uniqueOffset", NbtElement.FLOAT_TYPE)) {
            this.uniqueOffset = nbt.getFloat("uniqueOffset");
        }
    }
}
