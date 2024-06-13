package com.imoonday.on1chest.screen.client;

import com.imoonday.on1chest.config.Config;
import com.imoonday.on1chest.network.NetworkHandler;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import com.imoonday.on1chest.screen.widgets.SmallCheckboxWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

//@IPNPlayerSideOnly
//@IPNGuiHint(button = IPNButton.CONTINUOUS_CRAFTING, horizontalOffset = 59, bottom = -24)
//@IPNGuiHint(button = IPNButton.SORT, horizontalOffset = -12, bottom = 12)
public class StorageProcessorScreen extends StorageAssessorScreen {

    public static boolean continuousCrafting;
    private SmallCheckboxWidget continuousCraftingWidget;

    public StorageProcessorScreen(StorageAssessorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 256;
        this.drawTitle = false;
    }

    @Override
    protected void init() {
        super.init();
        updateContinuousCrafting();
        this.continuousCraftingWidget = new SmallCheckboxWidget(this.textRenderer, x + backgroundWidth / 2, height / 2 + 26, 13, Text.translatable("screen.on1chest.button.continuous_crafting"), continuousCrafting, false, (widget, checked) -> {
            continuousCrafting = checked;
            updateContinuousCrafting();
        });
        this.continuousCraftingWidget.setTooltip(Tooltip.of(Text.translatable("screen.on1chest.button.continuous_crafting")));
        this.addDrawableChild(this.continuousCraftingWidget);
    }

    public static void updateContinuousCrafting() {
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("c", continuousCrafting);
        NetworkHandler.sendToServer(nbt);
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
