package com.imoonday.on1chest.client;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.client.renderer.GlassStorageMemoryBlockEntityRenderer;
import com.imoonday.on1chest.config.Config;
import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.screen.client.StorageAssessorScreen;
import com.imoonday.on1chest.utils.CombinedItemStack;
import com.imoonday.on1chest.utils.IScreenDataReceiver;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class OnlyNeedOneChestClient implements ClientModInitializer {

    public static final Identifier S2C = OnlyNeedOneChest.id("s2c");
    public static KeyBinding screenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.on1chest.setting_screen", GLFW.GLFW_KEY_N, "group.on1chest.storages"));
    private static CombinedItemStack selectedStack = null;

    @Override
    public void onInitializeClient() {
        Config.initConfig();
        ModScreens.registerClient();
        registerGlobalReceiver();
        registerCountDisplay();
        BlockEntityRendererFactories.register(ModBlocks.GLASS_STORAGE_MEMORY_BLOCK_ENTITY, GlassStorageMemoryBlockEntityRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GLASS_STORAGE_MEMORY_BLOCK, RenderLayer.getTranslucent());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (screenKey.wasPressed()) {
                client.setScreen(Config.createConfigScreen(client.currentScreen));
            }
        });
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
        ClientPlayNetworking.registerGlobalReceiver(S2C, (client, handler, buf, sender) -> {
            NbtCompound nbt = buf.readUnlimitedNbt();
            client.execute(() -> {
                if (client.currentScreen instanceof IScreenDataReceiver receiver) {
                    receiver.receive(nbt);
                }
            });
        });
    }


}
