package com.imoonday.on1chest.init;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import com.imoonday.on1chest.screen.StorageProcessorScreenHandler;
import com.imoonday.on1chest.screen.client.StorageAssessorScreen;
import com.imoonday.on1chest.screen.client.StorageProcessorScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ModScreens {

    public static final ScreenHandlerType<StorageAssessorScreenHandler> STORAGE_ASSESSOR_SCREEN_HANDLER = register("storage_assessor", new ScreenHandlerType<>(StorageAssessorScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
    public static final ScreenHandlerType<StorageProcessorScreenHandler> STORAGE_PROCESSOR_SCREEN_HANDLER = register("storage_processor", new ScreenHandlerType<>(StorageProcessorScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    public static void register() {

    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        HandledScreens.register(STORAGE_ASSESSOR_SCREEN_HANDLER, StorageAssessorScreen::new);
        HandledScreens.register(STORAGE_PROCESSOR_SCREEN_HANDLER, StorageProcessorScreen::new);
    }

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType<T> type) {
        return Registry.register(Registries.SCREEN_HANDLER, OnlyNeedOneChest.id(id), type);
    }
}
