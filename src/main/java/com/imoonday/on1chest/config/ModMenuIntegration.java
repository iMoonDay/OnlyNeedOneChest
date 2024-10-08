package com.imoonday.on1chest.config;

import com.imoonday.on1chest.client.OnlyNeedOneChestClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> OnlyNeedOneChestClient.clothConfig ? ConfigScreenHandler.createConfigScreen(parent) : null;
    }
}
