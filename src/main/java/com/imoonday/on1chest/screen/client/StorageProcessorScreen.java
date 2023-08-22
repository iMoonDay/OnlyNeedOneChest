package com.imoonday.on1chest.screen.client;

import com.imoonday.on1chest.config.Config;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class StorageProcessorScreen extends StorageAssessorScreen {

    public StorageProcessorScreen(StorageAssessorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 256;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.playerInventoryTitleX = this.backgroundWidth - 26 - textRenderer.getWidth(title);
    }

    @Override
    public int getScrollBarHeight() {
        return 94;
    }

    @Override
    protected Identifier getTexture() {
        return Config.getInstance().getTheme().getId("processor");
    }
}
