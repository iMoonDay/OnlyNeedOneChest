package com.imoonday.on1chest.client;

import com.imoonday.on1chest.init.ModScreens;
import net.fabricmc.api.ClientModInitializer;

public class OnlyNeedOneChestClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        ModScreens.registerClient();
    }
}
