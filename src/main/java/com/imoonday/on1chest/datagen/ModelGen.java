package com.imoonday.on1chest.datagen;

import com.imoonday.on1chest.blocks.StorageMemoryBlock;
import com.imoonday.on1chest.blocks.WirelessConnectorBlock;
import com.imoonday.on1chest.init.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.client.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;

import static com.imoonday.on1chest.init.ModBlocks.*;

public class ModelGen extends FabricModelProvider {

    public ModelGen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        generator.registerSimpleCubeAll(STORAGE_BLANK_BLOCK);
        generator.registerSimpleCubeAll(RECIPE_PROCESSOR);
        registerCubeColumn(generator, STORAGE_ACCESSOR_BLOCK, TextureMap.getSubId(STORAGE_ACCESSOR_BLOCK, "_end"));
        registerCubeColumn(generator, STORAGE_PROCESSOR_BLOCK, TextureMap.getSubId(STORAGE_PROCESSOR_BLOCK, "_end"));
        registerCubeColumn(generator, QUICK_CRAFTING_TABLE, TextureMap.getSubId(QUICK_CRAFTING_TABLE, "_end"));
        registerCooker(generator, STORAGE_RECYCLE_BIN);
        registerMemoryBlock(generator, WOOD_STORAGE_MEMORY_BLOCK, TextureMap.getId(STORAGE_BLANK_BLOCK));
        registerMemoryBlock(generator, COPPER_STORAGE_MEMORY_BLOCK, TextureMap.getSubId(COPPER_STORAGE_MEMORY_BLOCK, "_end"));
        registerMemoryBlock(generator, IRON_STORAGE_MEMORY_BLOCK, TextureMap.getSubId(IRON_STORAGE_MEMORY_BLOCK, "_end"));
        registerMemoryBlock(generator, GOLD_STORAGE_MEMORY_BLOCK, TextureMap.getSubId(GOLD_STORAGE_MEMORY_BLOCK, "_end"));
        registerMemoryBlock(generator, DIAMOND_STORAGE_MEMORY_BLOCK, TextureMap.getSubId(DIAMOND_STORAGE_MEMORY_BLOCK, "_end"));
        registerMemoryBlock(generator, NETHERITE_STORAGE_MEMORY_BLOCK, TextureMap.getSubId(NETHERITE_STORAGE_MEMORY_BLOCK, "_end"));
        registerMemoryBlock(generator, OBSIDIAN_STORAGE_MEMORY_BLOCK, TextureMap.getSubId(OBSIDIAN_STORAGE_MEMORY_BLOCK, "_end"));
        registerMemoryBlock(generator, GLASS_STORAGE_MEMORY_BLOCK, TextureMap.getSubId(GLASS_STORAGE_MEMORY_BLOCK, "_end"));
        registerMemoryBlock(generator, DISPLAY_STORAGE_MEMORY_BLOCK, TextureMap.getSubId(DISPLAY_STORAGE_MEMORY_BLOCK, "_end"));
        registerMemoryBlock(generator, COMPRESSED_STORAGE_MEMORY_BLOCK, TextureMap.getSubId(COMPRESSED_STORAGE_MEMORY_BLOCK, "_end"));
        registerParentedItemModel(generator, ITEM_EXPORTER);
        registerParentedItemModel(generator, MEMORY_EXTRACTOR);
        registerParentedItemModel(generator, MEMORY_CONVERTER);
        registerWirelessConnector(generator);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.BASIC_REMOTE_ACCESSOR, Models.GENERATED);
        itemModelGenerator.register(ModItems.ADVANCED_REMOTE_ACCESSOR, Models.GENERATED);
        itemModelGenerator.register(ModItems.VANILLA_TO_WOOD_CONVERSION_MODULE, Models.GENERATED);
        itemModelGenerator.register(ModItems.WOOD_TO_COPPER_EXPAND_MODULE, Models.GENERATED);
        itemModelGenerator.register(ModItems.COPPER_TO_IRON_EXPAND_MODULE, Models.GENERATED);
        itemModelGenerator.register(ModItems.IRON_TO_GOLD_EXPAND_MODULE, Models.GENERATED);
        itemModelGenerator.register(ModItems.GOLD_TO_DIAMOND_EXPAND_MODULE, Models.GENERATED);
        itemModelGenerator.register(ModItems.DIAMOND_TO_NETHERITE_EXPAND_MODULE, Models.GENERATED);
        itemModelGenerator.register(ModItems.GOLD_TO_OBSIDIAN_EXPAND_MODULE, Models.GENERATED);
        itemModelGenerator.register(ModItems.COMPRESSION_UPGRADE_MODULE, Models.GENERATED);
        itemModelGenerator.register(ModItems.RECIPE_RECORD_CARD, Models.GENERATED);
    }

    private void registerCubeColumn(BlockStateModelGenerator generator, Block block, Identifier endId) {
        generator.registerSingleton(block, block1 -> TexturedModel.CUBE_COLUMN.get(block).textures(textureMap -> {
            textureMap.put(TextureKey.SIDE, TextureMap.getId(block));
            textureMap.put(TextureKey.END, endId);
        }));
    }

    private void registerMemoryBlock(BlockStateModelGenerator generator, Block block, Identifier endId) {
        Map<StorageMemoryBlock.UsedCapacity, Identifier> identifiers = new HashMap<>();
        for (StorageMemoryBlock.UsedCapacity capacity : StorageMemoryBlock.UsedCapacity.values()) {
            Identifier identifier = TextureMap.getSubId(block, "_" + capacity.asString());
            identifiers.put(capacity, Models.CUBE_COLUMN.upload(identifier, TextureMap.sideEnd(identifier, endId), generator.modelCollector));
        }
        Identifier identifier = TextureMap.getSubId(block, "_closed");
        Identifier close = Models.CUBE_COLUMN.upload(identifier, TextureMap.sideEnd(identifier, endId), generator.modelCollector);
        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block, BlockStateVariant.create()).coordinate(BlockStateVariantMap.create(StorageMemoryBlock.USED_CAPACITY, StorageMemoryBlock.ACTIVATED).register((usedCapacity, activated) -> BlockStateVariant.create().put(VariantSettings.MODEL, activated ? identifiers.get(usedCapacity) : close))));
        generator.registerParentedItemModel(block, ModelIds.getBlockSubModelId(block, "_zero"));
    }

    public final void registerCooker(BlockStateModelGenerator generator, Block block) {
        TextureMap textureMap = new TextureMap().put(TextureKey.TOP, TextureMap.getSubId(Blocks.FURNACE, "_top")).put(TextureKey.SIDE, TextureMap.getSubId(Blocks.FURNACE, "_side")).put(TextureKey.FRONT, TextureMap.getSubId(block, "_front"));
        TextureMap textureMap2 = new TextureMap().put(TextureKey.SIDE, TextureMap.getSubId(Blocks.FURNACE, "_top")).put(TextureKey.TOP, TextureMap.getSubId(Blocks.FURNACE, "_top")).put(TextureKey.BOTTOM, TextureMap.getSubId(Blocks.FURNACE, "_top")).put(TextureKey.FRONT, TextureMap.getSubId(block, "_front_vertical"));
        TextureMap textureMap3 = new TextureMap().put(TextureKey.TOP, TextureMap.getSubId(Blocks.FURNACE, "_top")).put(TextureKey.SIDE, TextureMap.getSubId(Blocks.FURNACE, "_side")).put(TextureKey.FRONT, TextureMap.getSubId(block, "_front_on"));
        TextureMap textureMap4 = new TextureMap().put(TextureKey.SIDE, TextureMap.getSubId(Blocks.FURNACE, "_top")).put(TextureKey.TOP, TextureMap.getSubId(Blocks.FURNACE, "_top")).put(TextureKey.BOTTOM, TextureMap.getSubId(Blocks.FURNACE, "_top")).put(TextureKey.FRONT, TextureMap.getSubId(block, "_front_vertical_on"));
        Identifier identifier = Models.ORIENTABLE.upload(block, textureMap, generator.modelCollector);
        Identifier identifier2 = Models.ORIENTABLE_WITH_BOTTOM.upload(TextureMap.getSubId(block, "_vertical"), textureMap2, generator.modelCollector);
        Identifier identifier3 = Models.ORIENTABLE.upload(TextureMap.getSubId(block, "_on"), textureMap3, generator.modelCollector);
        Identifier identifier4 = Models.ORIENTABLE_WITH_BOTTOM.upload(TextureMap.getSubId(block, "_vertical_on"), textureMap4, generator.modelCollector);
        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block).coordinate(BlockStateVariantMap.create(Properties.LIT, Properties.FACING).register((lit, direction) -> {
            BlockStateVariant variant = switch (direction) {
                case DOWN -> BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R90);
                case UP -> BlockStateVariant.create().put(VariantSettings.X, VariantSettings.Rotation.R270);
                case NORTH -> BlockStateVariant.create();
                case SOUTH -> BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R180);
                case WEST -> BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R270);
                case EAST -> BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R90);
            };
            variant = direction.getAxis() == Direction.Axis.Y ? variant.put(VariantSettings.MODEL, lit ? identifier4 : identifier2) : variant.put(VariantSettings.MODEL, lit ? identifier3 : identifier);
            return variant;
        })));
    }

    private static void registerParentedItemModel(BlockStateModelGenerator generator, Block block) {
        generator.registerParentedItemModel(block, ModelIds.getBlockModelId(block));
    }

    private void registerWirelessConnector(BlockStateModelGenerator generator) {
        Block block = WIRELESS_CONNECTOR;
        Identifier on = ModelIds.getBlockSubModelId(block, "_on");
        Identifier off = ModelIds.getBlockModelId(block);
        Identifier connected = ModelIds.getBlockSubModelId(block, "_connected");
        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block, BlockStateVariant.create().put(VariantSettings.MODEL, ModelIds.getBlockModelId(block))).coordinate(generator.createUpDefaultFacingVariantMap()).coordinate(BlockStateVariantMap.create(WirelessConnectorBlock.STATUS).register(WirelessConnectorBlock.ConnectionStatus.OFF, BlockStateVariant.create().put(VariantSettings.MODEL, off)).register(WirelessConnectorBlock.ConnectionStatus.ON, BlockStateVariant.create().put(VariantSettings.MODEL, on)).register(WirelessConnectorBlock.ConnectionStatus.CONNECTED, BlockStateVariant.create().put(VariantSettings.MODEL, connected))));
        generator.registerParentedItemModel(block, off);
    }
}
