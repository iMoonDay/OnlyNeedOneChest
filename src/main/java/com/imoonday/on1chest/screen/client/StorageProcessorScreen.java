package com.imoonday.on1chest.screen.client;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.config.Config;
import com.imoonday.on1chest.network.NetworkHandler;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import com.imoonday.on1chest.screen.widgets.SmallCheckboxWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class StorageProcessorScreen extends StorageAssessorScreen {

    public static final Identifier WARN_ID = OnlyNeedOneChest.id("textures/button/warn.png");
    public static boolean continuousCrafting;
    private SmallCheckboxWidget continuousCraftingWidget;
    private boolean searching;
    private boolean warn;

    public StorageProcessorScreen(StorageAssessorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 256;
        this.drawTitle = false;
    }

    @Override
    protected void init() {
        super.init();
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("c", continuousCrafting);
        nbt.putBoolean("w", true);
        NetworkHandler.sendToServer(nbt);
        this.continuousCraftingWidget = new SmallCheckboxWidget(this.textRenderer, width / 2, height / 2 + 26, 13, Text.translatable("screen.on1chest.button.continuous_crafting"), continuousCrafting, true, (widget, checked) -> {
            continuousCrafting = checked;
            updateContinuousCrafting();
        });
        this.continuousCraftingWidget.setUseTooltipInsteadOfMessage(true);
        this.addDrawableChild(this.continuousCraftingWidget);
    }

    public static void updateContinuousCrafting() {
        NetworkHandler.sendToServer("c", NbtByte.of(continuousCrafting));
    }

    @Override
    public int getScrollBarHeight() {
        return 94;
    }

    @Override
    protected Identifier getTexture() {
        return Config.getInstance().getTheme().getId("processor");
    }

    @Override
    public void receive(NbtCompound nbt) {
        super.receive(nbt);
        if (nbt.contains("s", NbtElement.BYTE_TYPE)) {
            this.searching = nbt.getBoolean("s");
        }
        if (nbt.contains("w", NbtElement.BYTE_TYPE)) {
            this.warn = nbt.getBoolean("w");
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (searching) {
            context.drawText(this.textRenderer, Text.translatable("screen.on1chest.searching").append("...".substring(0, (int) (System.currentTimeMillis() / 500 % 4))), width / 2, height / 2 - 14, 0x000000, false);
        }
        if (warn) {
            int x = this.x + backgroundWidth - 22;
            int y = this.y + backgroundHeight / 2 - 18;
            int width = 16;
            int height = 16;
            context.drawTexture(WARN_ID, x, y, 0, 0, width, height, width, height);
            if (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height) {
                context.drawTooltip(this.textRenderer, Text.translatable("screen.on1chest.too_many_memory_blocks").formatted(Formatting.RED), mouseX, mouseY);
            }
        }
    }
}
