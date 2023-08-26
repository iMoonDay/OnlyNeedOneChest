package com.imoonday.on1chest.screen.client;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.network.NetworkHandler;
import com.imoonday.on1chest.screen.WirelessNetworkScreenHandler;
import com.imoonday.on1chest.screen.widgets.ButtonIconWidget;
import com.imoonday.on1chest.utils.IScreenDataReceiver;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class WirelessNetworkScreen extends HandledScreen<WirelessNetworkScreenHandler> implements IScreenDataReceiver {

    private static final Identifier TEXTURE = OnlyNeedOneChest.id("textures/gui/wireless_network.png");
    public static final Identifier UPLOAD = OnlyNeedOneChest.id("textures/button/upload.png");
    public static final Identifier UPLOAD_HOVERED = OnlyNeedOneChest.id("textures/button/upload_on.png");
    public static final Identifier CLEAR = OnlyNeedOneChest.id("textures/button/clear.png");
    public static final Identifier CLEAR_HOVERED = OnlyNeedOneChest.id("textures/button/clear_on.png");

    private TextFieldWidget text;

    public WirelessNetworkScreen(WirelessNetworkScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 127;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

        text = new TextFieldWidget(textRenderer, this.x + 45, this.y + 20, 86, 10, Text.empty());
        text.setDrawsBackground(false);
        this.addDrawableChild(text);

        this.addDrawableChild(new ButtonIconWidget(this.x + 134, this.y + 19, 12, 12, UPLOAD, UPLOAD_HOVERED).addClickAction(0, button -> {
            String s = text.getText();
            if (s == null) {
                s = "";
            }
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putString("network", s);
            NetworkHandler.sendToServer(nbtCompound);
        }));

        this.addDrawableChild(new ButtonIconWidget(this.x + 30, this.y + 19, 12, 12, CLEAR, CLEAR_HOVERED).addClickAction(0, button -> {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putString("network", "");
            nbtCompound.putBoolean("update", true);
            NetworkHandler.sendToServer(nbtCompound);
        }));

        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putBoolean("update", true);
        NetworkHandler.sendToServer(nbtCompound);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void receive(NbtCompound nbt) {
        if (nbt.contains("network", NbtElement.STRING_TYPE)) {
            text.setText(nbt.getString("network"));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.text.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(null);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.text.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.text.isFocused() && this.text.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
