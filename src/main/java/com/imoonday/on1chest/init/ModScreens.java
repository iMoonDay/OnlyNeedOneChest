package com.imoonday.on1chest.init;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.client.screen.StorageAssessorScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ModScreens {

    public static final ScreenHandlerType<StorageAssessorScreen.StorageAssessorScreenHandler> STORAGE_ASSESSOR_SCREEN_HANDLER = register("storage_assessor", new ScreenHandlerType<>(StorageAssessorScreen.StorageAssessorScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    public static void register() {

    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        HandledScreens.register(STORAGE_ASSESSOR_SCREEN_HANDLER, StorageAssessorScreen::new);
    }

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType<T> type) {
        return Registry.register(Registries.SCREEN_HANDLER, OnlyNeedOneChest.id(id), type);
    }
}
