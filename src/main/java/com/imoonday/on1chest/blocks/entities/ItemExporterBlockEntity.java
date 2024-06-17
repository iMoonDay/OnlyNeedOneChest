package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.api.ConnectBlock;
import com.imoonday.on1chest.api.RecipeFilter;
import com.imoonday.on1chest.blocks.ItemExporterBlock;
import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.utils.PositionPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

public class ItemExporterBlockEntity extends TransferBlockEntity {

    public ItemExporterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_EXPORTER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, TransferBlockEntity entity) {
        tickUpdate(world, pos, state, entity);
        if (--entity.cooldown <= 0) {
            Direction opposite = state.get(ItemExporterBlock.FACING).getOpposite();
            BlockPos offset = pos.offset(opposite);
            BlockEntity blockEntity = world.getBlockEntity(offset);
            if (blockEntity instanceof Inventory inventory) {
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
                    if (stack.isEmpty()) continue;
                    Item target;
                    boolean tested = false;
                    if (entity.matchMode && blockEntity instanceof RecipeFilter filter && filter.shouldFilter()) {
                        if (!filter.testOutput(((ServerWorld) world).getServer(), stack)) {
                            continue;
                        }
                        tested = true;
                    } else if ((target = entity.target) != null && !stack.isOf(target)) {
                        continue;
                    }
                    List<Inventory> inventories = ConnectBlock.getConnectedBlocks(world, pos, PositionPredicate.create(world, offset).add((world1, pos1) -> world1.getBlockEntity(pos1) instanceof StorageMemoryBlockEntity && Arrays.stream(Direction.values()).anyMatch(direction -> world1.getBlockEntity(pos1.offset(direction)) instanceof TransferBlockEntity exporter && exporter.getCachedState().get(ItemExporterBlock.FACING) == direction && stack.getItem() == exporter.target))).stream().filter(pair -> pair.getLeft().getBlockState(pair.getRight()).getBlock() instanceof StorageMemoryBlock && pair.getLeft().getBlockState(pair.getRight()).get(StorageMemoryBlock.ACTIVATED) && pair.getLeft().getBlockEntity(pair.getRight()) instanceof StorageMemoryBlockEntity).map(pair -> (Inventory) pair.getLeft().getBlockEntity(pair.getRight())).toList();
                    if (inventories.isEmpty()) return;
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
                        if (!tested && entity.matchMode && !validInv.containsAny(stack1 -> ItemStack.areItemsEqual(stack, stack1))) {
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

}
