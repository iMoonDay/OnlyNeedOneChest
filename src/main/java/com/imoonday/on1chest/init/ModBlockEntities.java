package com.imoonday.on1chest.init;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.blocks.entities.*;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities {
    public static final BlockEntityType<StorageMemoryBlockEntity> STORAGE_MEMORY_BLOCK_ENTITY = register("storage_memory_block", StorageMemoryBlockEntity::new, ModBlocks.WOOD_STORAGE_MEMORY_BLOCK, ModBlocks.COPPER_STORAGE_MEMORY_BLOCK, ModBlocks.IRON_STORAGE_MEMORY_BLOCK, ModBlocks.GOLD_STORAGE_MEMORY_BLOCK, ModBlocks.DIAMOND_STORAGE_MEMORY_BLOCK, ModBlocks.NETHERITE_STORAGE_MEMORY_BLOCK, ModBlocks.OBSIDIAN_STORAGE_MEMORY_BLOCK);
    public static final BlockEntityType<GlassStorageMemoryBlockEntity> GLASS_STORAGE_MEMORY_BLOCK_ENTITY = register("glass_storage_memory_block", GlassStorageMemoryBlockEntity::new, ModBlocks.GLASS_STORAGE_MEMORY_BLOCK);
    public static final BlockEntityType<StorageAccessorBlockEntity> STORAGE_ACCESSOR_BLOCK_ENTITY = register("storage_accessor_block", StorageAccessorBlockEntity::new, ModBlocks.STORAGE_ACCESSOR_BLOCK);
    public static final BlockEntityType<StorageProcessorBlockEntity> STORAGE_PROCESSOR_BLOCK_ENTITY = register("storage_processor_block", StorageProcessorBlockEntity::new, ModBlocks.STORAGE_PROCESSOR_BLOCK);
    public static final BlockEntityType<StorageRecycleBinBlockEntity> STORAGE_RECYCLE_BIN_BLOCK_ENTITY = register("storage_recycle_bin", StorageRecycleBinBlockEntity::new, ModBlocks.STORAGE_RECYCLE_BIN);
    public static final BlockEntityType<AbstractTransferBlockEntity> ITEM_EXPORTER_BLOCK_ENTITY = register("item_exporter", ItemExporterBlockEntity::new, ModBlocks.ITEM_EXPORTER);
    public static final BlockEntityType<MemoryExtractorBlockEntity> MEMORY_EXTRACTOR_BLOCK_ENTITY = register("memory_extractor", MemoryExtractorBlockEntity::new, ModBlocks.MEMORY_EXTRACTOR);
    public static final BlockEntityType<WirelessConnectorBlockEntity> WIRELESS_CONNECTOR_BLOCK_ENTITY = register("wireless_connector", WirelessConnectorBlockEntity::new, ModBlocks.WIRELESS_CONNECTOR);
    public static final BlockEntityType<RecipeProcessorBlockEntity> RECIPE_PROCESSOR_BLOCK_ENTITY = register("recipe_processor", RecipeProcessorBlockEntity::new, ModBlocks.RECIPE_PROCESSOR);
    public static final BlockEntityType<QuickCraftingTableBlockEntity> QUICK_CRAFTING_TABLE_BLOCK_ENTITY = register("quick_crafting_table", QuickCraftingTableBlockEntity::new, ModBlocks.QUICK_CRAFTING_TABLE);

    public static void register() {

    }

    public static <T extends BlockEntity> BlockEntityType<T> register(String id, FabricBlockEntityTypeBuilder.Factory<T> factory, Block... blocks) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, OnlyNeedOneChest.id(id), FabricBlockEntityTypeBuilder.create(factory, blocks).build());
    }
}
