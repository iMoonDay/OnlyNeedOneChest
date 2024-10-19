package com.imoonday.on1chest.blocks.memories;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.blocks.entities.StorageMemoryBlockEntity;
import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static com.imoonday.on1chest.blocks.entities.StorageMemoryBlockEntity.MAX_LEVEL;

public class CompressedStorageMemoryBlock extends StorageMemoryBlock {

    public CompressedStorageMemoryBlock(Settings settings) {
        super(settings);
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        NbtCompound nbt = itemStack.getNbt();
        if (nbt == null || !nbt.contains("CompressionLevel")) return;
        int level = nbt.getInt("CompressionLevel");
        if (level <= 0) return;
        updateLevel(world, pos, level);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        int level = 1;
        NbtCompound blockEntityTag = stack.getSubNbt("BlockEntityTag");
        if (blockEntityTag != null && blockEntityTag.contains("Level")) {
            level = blockEntityTag.getInt("Level") + 1;
            if (level > MAX_LEVEL + 1) {
                blockEntityTag.putInt("Level", MAX_LEVEL);
            }
        } else {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null && nbt.contains("CompressionLevel")) {
                level = nbt.getInt("CompressionLevel") + 1;
                if (level > MAX_LEVEL + 1) {
                    nbt.putInt("CompressionLevel", MAX_LEVEL);
                }
            }
        }
        if (level > MAX_LEVEL + 1) {
            level = MAX_LEVEL + 1;
        }
        tooltip.add(Text.translatable("block.on1chest.compressed_storage_memory_block.tooltip.1", level, MAX_LEVEL + 1).formatted(Formatting.GRAY));
        if (level == 1) {
            tooltip.add(Text.translatable("block.on1chest.compressed_storage_memory_block.tooltip.2").formatted(Formatting.GREEN));
        }
    }

    @Override
    public Map<Item, StorageMemoryBlock> getLevelUpEntries() {
        return Map.of(ModItems.COMPRESSION_UPGRADE_MODULE, ModBlocks.COMPRESSED_STORAGE_MEMORY_BLOCK);
    }

    @Override
    public boolean levelUp(World world, BlockPos pos, BlockState oldState, BlockState newState) {
        if (!(world.getBlockEntity(pos) instanceof StorageMemoryBlockEntity entity)) return false;
        int level = entity.getLevel();
        if (level >= MAX_LEVEL) return false;
        entity.updateLevel(level + 1);
        entity.markDirty();
        return true;
    }
}
