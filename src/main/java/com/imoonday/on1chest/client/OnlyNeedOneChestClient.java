package com.imoonday.on1chest.client;

import com.imoonday.on1chest.api.IScreenDataReceiver;
import com.imoonday.on1chest.client.renderer.GlassStorageMemoryBlockEntityRenderer;
import com.imoonday.on1chest.client.renderer.ItemExporterBlockEntityRenderer;
import com.imoonday.on1chest.client.renderer.MemoryExtractorBlockEntityRenderer;
import com.imoonday.on1chest.client.renderer.RecipeProcessorBlockEntityRenderer;
import com.imoonday.on1chest.config.Config;
import com.imoonday.on1chest.filter.ItemFilterManager;
import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.network.NetworkHandler;
import com.imoonday.on1chest.screen.client.StorageAssessorScreen;
import com.imoonday.on1chest.utils.CombinedItemStack;
import com.imoonday.on1chest.utils.CraftingRecipeTreeManager;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class OnlyNeedOneChestClient implements ClientModInitializer {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static boolean clothConfig = FabricLoader.getInstance().isModLoaded("cloth-config");
    private static CombinedItemStack selectedStack = null;

    @Override
    public void onInitializeClient() {
        ItemFilterManager.initFilters();
        Config.initConfig();
        ModScreens.registerClient();
        registerGlobalReceiver();
        registerCountDisplay();
        registerRenderers();
        KeyBindings.registerKeys();
        registerRecipeTreeManagerEvents();
        registerCommands();
    }

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("on1chest")
                                        .then(literal("load").executes(context -> {
                                                  Config.load();
                                                  context.getSource().sendFeedback(Text.translatable("message.on1chest.loaded"));
                                                  return 1;
                                              })
                                        )
                                        .then(literal("settings").executes(context -> {
                                            if (clothConfig) {
                                                return 1;
                                            } else {
                                                context.getSource().sendFeedback(Text.translatable("message.on1chest.no_cloth_config"));
                                                File file = Config.getFile();
                                                if (file != null) {
                                                    if (!file.exists()) {
                                                        Config.save();
                                                    }
                                                    Util.getOperatingSystem().open(file.toPath().toUri());
                                                    return 1;
                                                }
                                                return 0;
                                            }
                                        }))
            );
        });
    }

    private static void registerRecipeTreeManagerEvents() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.world != null) {
                try {
                    CraftingRecipeTreeManager manager = CraftingRecipeTreeManager.getOrCreate(client.world);
                    if (manager != null) {
                        LOGGER.info("Client recipe loaded successfully");
                    }
                } catch (Throwable t) {
                    LOGGER.error("Failed to load client recipe", t);
                }
            }
        });
    }

    private static void registerRenderers() {
        BlockEntityRendererFactories.register(ModBlockEntities.GLASS_STORAGE_MEMORY_BLOCK_ENTITY, GlassStorageMemoryBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.ITEM_EXPORTER_BLOCK_ENTITY, ItemExporterBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.MEMORY_EXTRACTOR_BLOCK_ENTITY, MemoryExtractorBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.RECIPE_PROCESSOR_BLOCK_ENTITY, RecipeProcessorBlockEntityRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GLASS_STORAGE_MEMORY_BLOCK, RenderLayer.getTranslucent());
    }

    @Environment(EnvType.CLIENT)
    private static void registerCountDisplay() {
        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, list) -> {
            if (!Config.getInstance().isDisplayCountBeforeName()) {
                return;
            }
            if (selectedStack == null) {
                return;
            }
            if (!(MinecraftClient.getInstance().currentScreen instanceof StorageAssessorScreen)) {
                selectedStack = null;
                return;
            }
            if (selectedStack.canCombineWith(itemStack)) {
                Text text = list.get(0);
                if (text != null) {
                    list.set(0, Text.literal(selectedStack.getCount() + " ").append(text).setStyle(text.getStyle()));
                }
            }
        });
    }

    public static void setSelectedStack(@Nullable CombinedItemStack selectedStack) {
        OnlyNeedOneChestClient.selectedStack = selectedStack;
    }

    @Environment(EnvType.CLIENT)
    private void registerGlobalReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.S2C, (client, handler, buf, sender) -> {
            NbtCompound nbt = buf.readUnlimitedNbt();
            client.execute(() -> {
                if (client.currentScreen instanceof IScreenDataReceiver receiver) {
                    receiver.receive(nbt);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.UPDATE_RECIPE, (client, handler, buf, sender) -> {
            client.execute(() -> {
                if (client.world != null) {
                    CraftingRecipeTreeManager.getOrCreate(client.world).reload();
                    LOGGER.info("Client recipe reloaded successfully");
                }
                if (client.currentScreen instanceof IScreenDataReceiver receiver) {
                    receiver.update();
                    LOGGER.info("Server-side recipe synchronization completed");
                }
            });
        });

    }


}
