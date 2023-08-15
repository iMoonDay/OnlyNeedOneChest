package com.imoonday.on1chest.screen.client;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class StorageProcessorScreen extends StorageAssessorScreen {

    private static final Identifier TEXTURE = OnlyNeedOneChest.id("textures/gui/processor.png");

    public StorageProcessorScreen(StorageAssessorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 214;
        this.backgroundHeight = 256;
    }

    @Override
    public int getScrollBarHeight() {
        return 94;
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }
}
