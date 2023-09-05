package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.blocks.MemoryExtractorBlock;
import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.utils.ConnectBlock;
import com.imoonday.on1chest.utils.PositionPredicate;
import com.imoonday.on1chest.utils.RecipeFilter;
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

public class MemoryExtractorBlockEntity extends AbstractTransferBlockEntity {

    public MemoryExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MEMORY_EXTRACTOR_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, MemoryExtractorBlockEntity entity) {
        tickUpdate(world, pos, state, entity);
        if (--entity.cooldown <= 0) {
            Direction opposite = state.get(MemoryExtractorBlock.FACING).getOpposite();
            BlockPos offset = pos.offset(opposite);
            List<Inventory> inventories = ConnectBlock.getConnectedBlocks(world, pos, PositionPredicate.create(world, offset).add((world1, pos1) -> world1.getBlockEntity(pos1) instanceof StorageMemoryBlockEntity && Arrays.stream(Direction.values()).anyMatch(direction -> world1.getBlockEntity(pos1.offset(direction)) instanceof MemoryExtractorBlockEntity extractor && extractor.getCachedState().get(MemoryExtractorBlock.FACING) == direction && entity.target == extractor.target))).stream().filter(pair -> pair.getLeft().getBlockState(pair.getRight()).getBlock() instanceof StorageMemoryBlock && pair.getLeft().getBlockState(pair.getRight()).get(StorageMemoryBlock.ACTIVATED) && pair.getLeft().getBlockEntity(pair.getRight()) instanceof StorageMemoryBlockEntity).map(pair -> (Inventory) pair.getLeft().getBlockEntity(pair.getRight())).toList();
            if (inventories.isEmpty()) {
                return;
            }
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
                for (Inventory validInv : inventories) {
                    for (int i = 0; i < validInv.size(); i++) {
                        ItemStack stack = validInv.getStack(i);
                        if (stack.isEmpty()) {
                            continue;
                        }
                        Item target;
                        boolean tested = false;
                        if (entity.matchMode && blockEntity instanceof RecipeFilter filter && filter.shouldFilter()) {
                            if (!filter.testIngredient(((ServerWorld) world).getServer(), stack)) {
                                continue;
                            }
                            tested = true;
                        } else if ((target = entity.target) != null && !stack.isOf(target)) {
                            continue;
                        }
                        for (int j = 0; j < inventory.size(); j++) {
                            ItemStack invStack = inventory.getStack(j);
                            if (ItemStack.canCombine(invStack, stack) && invStack.getCount() < invStack.getMaxCount()) {
                                if (invStack.getCount() + stack.getCount() <= invStack.getMaxCount()) {
                                    invStack.increment(stack.getCount());
                                    stack.setCount(0);
                                } else {
                                    int decreased = invStack.getMaxCount() - invStack.getCount();
                                    invStack.increment(decreased);
                                    stack.decrement(decreased);
                                }
                                inventory.markDirty();
                                validInv.markDirty();
                                if (stack.isEmpty()) {
                                    break start;
                                }
                            }
                        }
                        if (!tested && entity.matchMode && !inventory.containsAny(stack1 -> ItemStack.areItemsEqual(stack, stack1))) {
                            continue;
                        }
                        if (!stack.isEmpty()) {
                            for (int j = 0; j < inventory.size(); j++) {
                                ItemStack invStack = inventory.getStack(j);
                                if (invStack.isEmpty()) {
                                    inventory.setStack(j, stack.split(stack.getMaxCount()));
                                    inventory.markDirty();
                                    validInv.markDirty();
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
