package com.imoonday.on1chest.init;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import com.imoonday.on1chest.screen.StorageProcessorScreenHandler;
import com.imoonday.on1chest.screen.StorageRecycleBinScreenHandler;
import com.imoonday.on1chest.screen.WirelessNetworkScreenHandler;
import com.imoonday.on1chest.screen.client.StorageAssessorScreen;
import com.imoonday.on1chest.screen.client.StorageProcessorScreen;
import com.imoonday.on1chest.screen.client.StorageRecycleBinScreen;
import com.imoonday.on1chest.screen.client.WirelessNetworkScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ModScreens {

    public static final ScreenHandlerType<StorageAssessorScreenHandler> STORAGE_ASSESSOR_SCREEN_HANDLER = register("storage_assessor", StorageAssessorScreenHandler::new);
    public static final ScreenHandlerType<StorageProcessorScreenHandler> STORAGE_PROCESSOR_SCREEN_HANDLER = register("storage_processor", StorageProcessorScreenHandler::new);
    public static final ScreenHandlerType<StorageRecycleBinScreenHandler> STORAGE_RECYCLE_BIN_SCREEN_HANDLER = registerExtended("recycle_bin", StorageRecycleBinScreenHandler::new);
    public static final ScreenHandlerType<WirelessNetworkScreenHandler> WIRELESS_NETWORK_SCREEN_HANDLER = register("wireless_network", WirelessNetworkScreenHandler::new);

    public static void register() {

    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        HandledScreens.register(STORAGE_ASSESSOR_SCREEN_HANDLER, StorageAssessorScreen::new);
        HandledScreens.register(STORAGE_PROCESSOR_SCREEN_HANDLER, StorageProcessorScreen::new);
        HandledScreens.register(STORAGE_RECYCLE_BIN_SCREEN_HANDLER, StorageRecycleBinScreen::new);
        HandledScreens.register(WIRELESS_NETWORK_SCREEN_HANDLER, WirelessNetworkScreen::new);
    }

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType.Factory<T> factory) {
        return Registry.register(Registries.SCREEN_HANDLER, OnlyNeedOneChest.id(id), new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
    }

    private static <T extends ScreenHandler> ExtendedScreenHandlerType<T> registerExtended(String id, ExtendedScreenHandlerType.ExtendedFactory<T> factory) {
        return Registry.register(Registries.SCREEN_HANDLER, OnlyNeedOneChest.id(id), new ExtendedScreenHandlerType<>(factory));
    }

}
