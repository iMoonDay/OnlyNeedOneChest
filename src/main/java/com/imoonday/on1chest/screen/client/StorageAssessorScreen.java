package com.imoonday.on1chest.screen.client;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.config.ScreenConfig;
import com.imoonday.on1chest.mixin.CheckboxWidgetAccessor;
import com.imoonday.on1chest.network.NetworkHandler;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import com.imoonday.on1chest.utils.IScreenDataReceiver;
import com.imoonday.on1chest.utils.ItemStackFilter;
import com.imoonday.on1chest.utils.SortComparator;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

public class StorageAssessorScreen extends HandledScreen<StorageAssessorScreenHandler> implements IScreenDataReceiver {

    private static final Identifier TEXTURE = OnlyNeedOneChest.id("textures/gui/assessor.png");
    protected TextFieldWidget searchBox;
    protected ButtonWidget sortWidget;
    protected final CheckboxWidget[] checkboxWidgets = new CheckboxWidget[ItemStackFilter.values().length];
    protected float scrollPosition;
    protected boolean scrolling;
    protected int favouriteCount;

    public StorageAssessorScreen(StorageAssessorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 214;
        this.backgroundHeight = 222;
        this.playerInventoryTitleX += 19;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
        this.titleX += 19;
    }

    @Override
    protected void init() {
        super.init();

        this.searchBox = new TextFieldWidget(this.textRenderer, this.x + 100, this.y + 6, 86, 10, Text.empty());
        this.searchBox.setChangedListener(s -> NetworkHandler.sendToServer(nbt -> nbt.putString("nameFilter", s)));
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setEditableColor(0xFFFFFF);
        this.addDrawableChild(this.searchBox);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> {
            this.searchBox.setText("");
            NetworkHandler.sendToServer(nbt -> nbt.putString("nameFilter", ""));
        }).dimensions(this.x + 193, this.y + 4, 14, 12).tooltip(Tooltip.of(Text.translatable("button.on1chest.clear"))).build());

        this.sortWidget = ButtonWidget.builder(Text.translatable("sort.on1chest.title"), button -> NetworkHandler.sendToServer(nbt -> nbt.putBoolean("comparator", true))).dimensions(this.x + 5, this.y + 19, 19, 13).tooltip(Tooltip.of(Text.translatable("sort.on1chest.tooltip", Text.translatable(SortComparator.ID.translationKey)))).build();
        this.addDrawableChild(this.sortWidget);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("O"), button -> {
            if (this.client != null) {
                this.client.setScreen(ScreenConfig.createConfigScreen(this));
            }
        }).dimensions(this.x + 5, this.y + 6, 10, 10).build());

        for (int i = 0; i < ItemStackFilter.values().length; i++) {
            this.checkboxWidgets[i] = createCheckboxWidget(i);
            this.addDrawableChild(this.checkboxWidgets[i]);
        }

        NetworkHandler.sendToServer(nbtCompound -> nbtCompound.putBoolean("init", true));
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        this.sortWidget.visible = ScreenConfig.getInstance().displaySortWidget;
        Arrays.stream(this.checkboxWidgets).forEach(widget -> widget.visible = ScreenConfig.getInstance().displayCheckBoxes);
    }

    @NotNull
    private CheckboxWidget createCheckboxWidget(int i) {
        ItemStackFilter filter = ItemStackFilter.values()[i];
        return new CheckboxWidget(StorageAssessorScreen.this.x + 215, StorageAssessorScreen.this.y + 17 + 21 * i, 20, 20, Text.translatable(filter.getTranslationKey()), false) {
            @Override
            public void onPress() {
                super.onPress();
                NetworkHandler.sendToServer(nbtCompound -> nbtCompound.putInt(this.isChecked() ? "addStackFilter" : "removeStackFilter", i));
            }
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        String string = this.searchBox.getText();
        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
                NetworkHandler.sendToServer(null);
            }
            return true;
        }
        if (this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(getTexture(), i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        i = this.x + 175 + 19;
        j = this.y + 18;
        int k = j + getScrollBarHeight();
        context.drawTexture(getTexture(), i, j + (int) ((float) (k - j - 21) * this.scrollPosition), 232, 0, 12, 15);
    }

    public int getScrollBarHeight() {
        return 112;
    }

    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        int originHeight = this.height;
        String string = this.searchBox.getText();
        this.init(client, width, height);
        this.searchBox.setText(string);
        this.scrollPosition *= (float) height / originHeight;
        this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f);
        NetworkHandler.sendToServer(nbtCompound -> nbtCompound.putFloat("scrollPosition", this.scrollPosition));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.searchBox.isMouseOver(mouseX, mouseY) && !this.sortWidget.isMouseOver(mouseX, mouseY)) {
            this.setFocused(null);
        }
        if (button == 1 && this.sortWidget.isMouseOver(mouseX, mouseY)) {
            NetworkHandler.sendToServer(nbtCompound -> nbtCompound.putBoolean("reversed", true));
        }
        if (button == 0) {
            if (this.isClickInScrollbar(mouseX, mouseY)) {
                this.scrolling = true;
                return true;
            }
        }
        NetworkHandler.sendToServer(nbtCompound -> {
            nbtCompound.putBoolean("Alt", isKeyPressed(ScreenConfig.getInstance().markItemStackKey));
            nbtCompound.putBoolean("Ctrl", isKeyPressed(ScreenConfig.getInstance().takeAllStacksKey));
        });
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected static boolean isKeyPressed(KeyBinding keyBinding) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), KeyBindingHelper.getBoundKeyOf(keyBinding).getCode());
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.scrolling = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        NetworkHandler.sendToServer(nbtCompound -> {
            NbtCompound nbtCompound1 = new NbtCompound();
            nbtCompound1.putFloat("scrollPosition", this.scrollPosition);
            nbtCompound1.putDouble("amount", amount);
            nbtCompound.put("getScrollPosition", nbtCompound1);
        });
        return true;
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int i = this.x;
        int j = this.y;
        int k = i + 175 + 19;
        int l = j + 18;
        int m = k + 14;
        int n = l + getScrollBarHeight();
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            int i = this.y + 18;
            int j = i + getScrollBarHeight();
            this.scrollPosition = ((float) mouseY - (float) i - 7.5f) / ((float) (j - i) - 15.0f);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f);
            NetworkHandler.sendToServer(null);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.handler.slots.stream().filter(slot -> slot.inventory == this.handler.inventory && slot.id - this.handler.getCraftingSlotCount() < favouriteCount).forEach(slot -> context.drawBorder(this.x + slot.x - 1, this.y + slot.y - 1, 18, 18, Color.YELLOW.getRGB()));
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public void receive(NbtCompound nbt) {
        if (nbt.contains("comparator", NbtElement.INT_TYPE)) {
            int index = nbt.getInt("comparator");
            if (index >= 0 && index < SortComparator.values().length) {
                this.sortWidget.setTooltip(Tooltip.of(Text.translatable("sort.on1chest.tooltip", Text.translatable(SortComparator.values()[index].translationKey))));
            }
        }

        if (nbt.contains("updateItems", NbtElement.INT_TYPE)) {
            this.favouriteCount = nbt.getInt("updateItems");
            NetworkHandler.sendToServer(nbtCompound -> nbtCompound.putFloat("scrollPosition", this.scrollPosition));
        }

        if (nbt.contains("scrollPosition", NbtElement.FLOAT_TYPE)) {
            this.scrollPosition = nbt.getFloat("scrollPosition");
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f);
        }

        if (nbt.contains("favouriteCount", NbtElement.INT_TYPE)) {
            this.favouriteCount = nbt.getInt("favouriteCount");
        }

        if (nbt.contains("stackFilters", NbtElement.INT_ARRAY_TYPE)) {
            int[] ints = nbt.getIntArray("stackFilters");
            for (int i = 0; i < this.checkboxWidgets.length; i++) {
                CheckboxWidget widget = this.checkboxWidgets[i];
                int index = i;
                ((CheckboxWidgetAccessor) widget).setChecked(Arrays.stream(ints).anyMatch(value -> value == index));
            }
        }

        if (nbt.contains("nameFilter", NbtElement.STRING_TYPE)) {
            this.searchBox.setText(nbt.getString("nameFilter"));
        }
    }
}
