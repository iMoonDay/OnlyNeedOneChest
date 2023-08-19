package com.imoonday.on1chest.screen.client;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.client.KeyBindings;
import com.imoonday.on1chest.config.ScreenConfig;
import com.imoonday.on1chest.mixin.CheckboxWidgetAccessor;
import com.imoonday.on1chest.mixin.ClickableWidgetAccessor;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import com.imoonday.on1chest.utils.*;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.IconButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StorageAssessorScreen extends HandledScreen<StorageAssessorScreenHandler> implements IScreenDataReceiver {

    protected TextFieldWidget searchBox;
    protected IconButtonWidget settingButton;
    protected IconButtonWidget sortButton;
    protected IconButtonWidget filtersButton;
    protected IconButtonWidget noSortButton;
    protected IconButtonWidget forceUpdateButton;
    protected IconButtonWidget themeButton;
    protected Map<IconButtonWidget, Function<ButtonWidget, Boolean>> rightClickButtonFunctions = new HashMap<>();
    protected final CheckboxWidget[] checkboxWidgets = new CheckboxWidget[ItemStackFilter.values().length];
    protected float scrollPosition;
    protected boolean scrolling;
    protected boolean refreshItemList;
    protected String searchLast = "";
    protected int slotIDUnderMouse = -1;
    private Comparator<CombinedItemStack> sortComp;
    private int buttonYOffset;
    protected boolean forceUpdate;

    public StorageAssessorScreen(StorageAssessorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 196;
        this.backgroundHeight = 222;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        this.playerInventoryTitleX = this.backgroundWidth - 10 - textRenderer.getWidth(title);

        this.searchBox = new TextFieldWidget(this.textRenderer, this.x + 82, this.y + 6, 86, 10, Text.empty());
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setEditableColor(0xFFFFFF);
        this.addDrawableChild(this.searchBox);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> this.searchBox.setText("")).dimensions(this.x + 175, this.y + 4, 14, 12).tooltip(Tooltip.of(Text.translatable("button.on1chest.clear"))).build());

        this.buttonYOffset = 2;

        this.settingButton = createIconButtonWidget("setting", buttonWidget -> {
            if (this.client != null) {
                this.client.setScreen(ScreenConfig.createConfigScreen(this));
            }
            return false;
        }, null, Text.literal("设置"));
        this.settingButton.visible = true;

        this.sortButton = createIconButtonWidget("sort", button -> {
            ScreenConfig.getInstance().setComparator(ScreenConfig.getInstance().getComparator().next());
            button.setTooltip(Tooltip.of(Text.translatable("sort.on1chest.tooltip", Text.translatable(ScreenConfig.getInstance().getComparator().translationKey))));
            return true;
        }, button -> {
            ScreenConfig.getInstance().setReversed(!ScreenConfig.getInstance().isReversed());
            return true;
        }, Text.translatable("sort.on1chest.tooltip", Text.translatable(ScreenConfig.getInstance().getComparator().translationKey)));

        this.filtersButton = createIconButtonWidget("filters", button -> {
            ScreenConfig.getInstance().setDisplayFilterWidgets(!ScreenConfig.getInstance().isDisplayFilterWidgets());
            button.setTooltip(Tooltip.of(Text.literal("过滤项：" + (ScreenConfig.getInstance().isDisplayFilterWidgets() ? "显示" : "隐藏"))));
            return false;
        }, null, Text.literal("过滤项：" + (ScreenConfig.getInstance().isDisplayFilterWidgets() ? "显示" : "隐藏")));

        this.noSortButton = createIconButtonWidget("nosort", button -> {
            ScreenConfig.getInstance().setNoSortWithShift(!ScreenConfig.getInstance().isNoSortWithShift());
            button.setTooltip(Tooltip.of(Text.literal("Shift暂停排序：" + (ScreenConfig.getInstance().isNoSortWithShift() ? "开启" : "关闭"))));
            return true;
        }, null, Text.literal("Shift暂停排序：" + (ScreenConfig.getInstance().isNoSortWithShift() ? "开启" : "关闭")));

        this.forceUpdateButton = createIconButtonWidget("force_update", button -> {
            ScreenConfig.getInstance().setUpdateOnInsert(!ScreenConfig.getInstance().isUpdateOnInsert());
            button.setTooltip(Tooltip.of(Text.literal("存入新物品时强制更新排序：" + (ScreenConfig.getInstance().isUpdateOnInsert() ? "开启" : "关闭"))));
            return ScreenConfig.getInstance().isUpdateOnInsert();
        }, null, Text.literal("存入新物品时强制更新排序：" + (ScreenConfig.getInstance().isUpdateOnInsert() ? "开启" : "关闭")));

        this.themeButton = createIconButtonWidget("theme", button -> {
            ScreenConfig.getInstance().setTheme(ScreenConfig.getInstance().getTheme().next());
            button.setTooltip(Tooltip.of(ScreenConfig.getInstance().getTheme().getLocalizeText()));
            return false;
        }, null, ScreenConfig.getInstance().getTheme().getLocalizeText());

        for (int i = 0; i < ItemStackFilter.values().length; i++) {
            this.checkboxWidgets[i] = createCheckboxWidget(i);
            this.addDrawableChild(this.checkboxWidgets[i]);
        }
    }

    public void onScreenConfigUpdate(ScreenConfig config) {
        boolean visible = config.isDisplayButtonWidgets();
        this.sortButton.visible = visible;
        this.filtersButton.visible = visible;
        this.noSortButton.visible = visible;
        Arrays.stream(this.checkboxWidgets).forEach(widget -> widget.visible = config.isDisplayFilterWidgets());
    }

    private IconButtonWidget createIconButtonWidget(String fileName, Function<ButtonWidget, Boolean> function, Function<ButtonWidget, Boolean> rightClickFunction, Text tooltip) {
        IconButtonWidget.Builder builder = IconButtonWidget.builder(Text.empty(), OnlyNeedOneChest.id("textures/button/" + fileName + ".png"), button -> {
            if (function.apply(button)) {
                this.refreshItemList = true;
            }
        });
        builder.iconSize(16, 16);
        builder.textureSize(16, 16);
        IconButtonWidget widget = builder.build();
        widget.setPosition(this.x - 16, this.y + this.buttonYOffset);
        this.buttonYOffset += 17;
        ((ClickableWidgetAccessor) widget).setHeight(16);
        widget.setWidth(16);
        widget.setAlpha(0);
        if (tooltip != null) {
            widget.setTooltip(Tooltip.of(tooltip));
        }
        widget.visible = ScreenConfig.getInstance().isDisplayButtonWidgets();
        if (rightClickFunction != null) {
            this.rightClickButtonFunctions.put(widget, rightClickFunction);
        }
        this.addDrawableChild(widget);
        return widget;
    }

    @SuppressWarnings("DataFlowIssue")
    @NotNull
    private CheckboxWidget createCheckboxWidget(int i) {
        ItemStackFilter filter = ItemStackFilter.values()[i];
        CheckboxWidget checkboxWidget = new CheckboxWidget(this.x + this.backgroundWidth + 1, this.y + 17 + 21 * i, 20, 20, Text.translatable(filter.getTranslationKey()), false) {
            @Override
            public void onPress() {
                super.onPress();
                ScreenConfig config = ScreenConfig.getInstance();
                if (config.getStackFilters().contains(filter)) {
                    config.removeStackFilter(filter);
                } else {
                    config.addStackFilter(filter);
                }
                refreshItemList = true;
            }
        };
        checkboxWidget.visible = ScreenConfig.getInstance().isDisplayFilterWidgets();
        ((CheckboxWidgetAccessor) checkboxWidget).setChecked(ScreenConfig.getInstance().getStackFilters().contains(filter));
        return checkboxWidget;
    }

    protected void updateSearch() {
        String searchString = searchBox.getText();
        if (refreshItemList || !searchLast.equals(searchString) || forceUpdate) {
            handler.itemListClientSorted.clear();
            handler.itemListClient.stream().filter(stack -> stack != null && stack.getStack() != null && StorageAssessorScreenHandler.checkTextFilter(stack.getStack(), searchString)).forEach(this::addStackToClientList);
            handler.itemListClientSorted.sort(handler.noSort && !forceUpdate ? sortComp : ScreenConfig.getInstance().getComparator().createComparator(ScreenConfig.getInstance().getFavouriteStacks().stream().map(FavouriteItemStack::getStack).collect(Collectors.toSet()), ScreenConfig.getInstance().isReversed()));
            handler.itemListClientSorted.removeIf(stack -> !ScreenConfig.getInstance().getStackFilters().stream().allMatch(filter -> filter.getPredicate().test(stack.getStack())));
            if (!searchLast.equals(searchString)) {
                handler.scrollItems(0);
                this.scrollPosition = 0;
                NbtCompound nbt = new NbtCompound();
                nbt.putString("s", searchString);
                handler.sendMessage(nbt);
                onUpdateSearch(searchString);
            } else {
                handler.scrollItems(this.scrollPosition);
            }
            this.refreshItemList = false;
            this.searchLast = searchString;
            this.forceUpdate = false;
        }
    }

    protected void onUpdateSearch(String text) {

    }

    private void addStackToClientList(CombinedItemStack stack) {
        handler.itemListClientSorted.add(stack);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
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
        i = this.x + 175 + 1;
        j = this.y + 18;
        int k = j + getScrollBarHeight();
        context.drawTexture(getTexture(), i, j + (int) ((float) (k - j - 21) * this.scrollPosition), 232, 0, 12, 15);
    }

    public int getScrollBarHeight() {
        return 112;
    }

    protected Identifier getTexture() {
        return ScreenConfig.getInstance().getTheme().getId("assessor");
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        int row = this.handler.getRow(this.scrollPosition);
        String string = this.searchBox.getText();
        this.init(client, width, height);
        this.searchBox.setText(string);
        this.scrollPosition = this.handler.getScrollPosition(row);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.searchBox.isMouseOver(mouseX, mouseY)) {
            this.setFocused(null);
        }
        if (button == 0 && this.isClickInScrollbar(mouseX, mouseY)) {
            this.scrolling = true;
            return true;
        }
        if (button == 1) {
            boolean clicked = false;
            for (Map.Entry<IconButtonWidget, Function<ButtonWidget, Boolean>> entry : this.rightClickButtonFunctions.entrySet()) {
                if (entry.getKey().isMouseOver(mouseX, mouseY)) {
                    clicked = true;
                    if (entry.getValue().apply(entry.getKey())) {
                        refreshItemList = true;
                    }
                }
            }
            if (clicked) {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
        }
        if (slotIDUnderMouse > -1) {
            switch (button) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
                    if (isKeyPressed(KeyBindings.markItemStackKey) && this.handler.getSlotByID(slotIDUnderMouse).stack != null) {
                        ItemStack stack = this.handler.getSlotByID(slotIDUnderMouse).stack.getActualStack(1);
                        if (ScreenConfig.getInstance().getFavouriteStacks().stream().noneMatch(stack1 -> stack1.equals(stack))) {
                            ScreenConfig.getInstance().addFavouriteStack(stack);
                            refreshItemList = true;
                        }
                        return true;
                    }
                    if (!this.handler.getCursorStack().isEmpty()) {
                        storageSlotClick(null, isKeyPressed(KeyBindings.takeAllStacksKey) ? InteractHandler.SlotAction.TAKE_ALL : InteractHandler.SlotAction.LEFT_CLICK, false);
                    } else if (this.handler.getSlotByID(slotIDUnderMouse).stack != null && this.handler.getSlotByID(slotIDUnderMouse).stack.getCount() > 0) {
                        storageSlotClick(this.handler.getSlotByID(slotIDUnderMouse).stack, hasShiftDown() ? InteractHandler.SlotAction.QUICK_MOVE : (isKeyPressed(KeyBindings.takeAllStacksKey) ? InteractHandler.SlotAction.TAKE_ALL : InteractHandler.SlotAction.LEFT_CLICK), false);
                        return true;
                    }
                }
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                    if (isKeyPressed(KeyBindings.markItemStackKey) && this.handler.getSlotByID(slotIDUnderMouse).stack != null) {
                        ScreenConfig.getInstance().removeFavouriteStack(this.handler.getSlotByID(slotIDUnderMouse).stack.getActualStack(1));
                        refreshItemList = true;
                        return true;
                    }
                    if (this.handler.getSlotByID(slotIDUnderMouse).stack != null && this.handler.getSlotByID(slotIDUnderMouse).stack.getCount() > 0) {
                        storageSlotClick(this.handler.getSlotByID(slotIDUnderMouse).stack, InteractHandler.SlotAction.RIGHT_CLICK, hasShiftDown());
                    }
                    return true;
                }
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> {
                    if (this.handler.getCursorStack().isEmpty() && this.handler.getSlotByID(slotIDUnderMouse).stack != null) {
                        storageSlotClick(this.handler.getSlotByID(slotIDUnderMouse).stack, InteractHandler.SlotAction.COPY, false);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected static boolean isKeyPressed(KeyBinding key) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), KeyBindingHelper.getBoundKeyOf(key).getCode());
    }

    protected void storageSlotClick(CombinedItemStack slotStack, InteractHandler.SlotAction act, boolean mod) {
        this.handler.manager.sendInteract(slotStack, act, mod);
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
        this.scrollPosition = this.handler.getScrollPosition(this.scrollPosition, amount);
        this.handler.scrollItems(this.scrollPosition);
        return true;
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int i = this.x;
        int j = this.y;
        int k = i + 175 + 1;
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
            this.handler.scrollItems(this.scrollPosition);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        if (ScreenConfig.getInstance().isUpdateOnInsert() && handler.itemList.stream().anyMatch(stack -> handler.itemListClient.stream().noneMatch(stack1 -> stack1.canCombineWith(stack)))) {
            forceUpdate = true;
        }
        if (ScreenConfig.getInstance().isNoSortWithShift() && hasShiftDown() && !forceUpdate) {
            if (!handler.noSort) {
                List<CombinedItemStack> list = handler.itemListClientSorted;
                Object2IntMap<CombinedItemStack> map = new Object2IntOpenHashMap<>();
                map.defaultReturnValue(Integer.MAX_VALUE);
                for (int m = 0; m < list.size(); m++) {
                    map.put(list.get(m), m);
                }
                sortComp = Comparator.comparing(map::getInt);
                handler.noSort = true;
            }
        } else if (handler.noSort || forceUpdate) {
            sortComp = null;
            handler.noSort = false;
            refreshItemList = true;
            handler.itemListClient = new ArrayList<>(handler.itemList);
        }

        if (this.handler.getCursorStack().isEmpty() && slotIDUnderMouse != -1) {
            StorageAssessorScreenHandler.StorageSlot slot = handler.storageSlotList.get(slotIDUnderMouse);
            if (slot.stack != null) {
                if (slot.stack.isEmpty()) {
                    MutableText text = Text.literal("等待刷新");
                    long time = Util.getMeasuringTimeMs() % 4000 / 1000;
                    for (long i = 0; i < time; i++) {
                        text.append(".");
                    }
                    context.drawTooltip(textRenderer, text, mouseX, mouseY);
                } else {
                    context.drawItemTooltip(textRenderer, slot.stack.getActualStack(), mouseX, mouseY);
                }
            }
        } else {
            this.drawMouseoverTooltip(context, mouseX, mouseY);
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);
        context.getMatrices().push();
        slotIDUnderMouse = drawSlots(context, mouseX, mouseY);
        context.getMatrices().pop();
    }

    protected int drawSlots(DrawContext context, int mouseX, int mouseY) {
        int slotHover = -1;
        for (int i = 0; i < this.handler.storageSlotList.size(); i++) {
            if (drawSlot(context, this.handler.storageSlotList.get(i), mouseX, mouseY)) {
                slotHover = i;
            }
        }
        return slotHover;
    }

    protected boolean drawSlot(DrawContext context, StorageAssessorScreenHandler.StorageSlot slot, int mouseX, int mouseY) {

        int i = slot.x, j = slot.y;
        boolean isHovered = mouseX >= this.x + i - 1 && mouseY >= this.y + j - 1 && mouseX < this.x + i + 17 && mouseY < this.y + j + 17;
        boolean isVanilla = ScreenConfig.getInstance().getTheme() == Theme.VANILLA;

        if (slot.stack != null) {
            ItemStack stack = slot.stack.getStack().copy().split(1);

            if (ScreenConfig.getInstance().getFavouriteStacks().stream().anyMatch(stack1 -> stack1.equals(stack))) {
                if (isVanilla) {
                    context.drawBorder(i - 1, j - 1, 18, 18, Color.YELLOW.getRGB());
                } else {
                    context.fill(i, j + 1, i + 3, j + 2, Color.YELLOW.getRGB());
                    context.fill(i + 1, j, i + 2, j + 3, Color.YELLOW.getRGB());
                }
            }

            context.drawItem(stack, i, j);
            context.drawItemInSlot(textRenderer, stack, i, j);

            long count = slot.stack.getCount();
            Color color = null;
            if (count <= 0) {
                color = Color.RED;
            } else if (isHovered) {
                color = Color.GREEN;
            }
            drawStackSize(context, textRenderer, count, i, j, color);
        }

        if (isHovered) {
            if (isVanilla) {
                drawSlotHighlight(context, i, j, 0);
            } else if (slot.stack != null && this.handler.getCursorStack().isEmpty()) {
                context.drawBorder(i - 1, j - 1, 18, 18, Color.GREEN.getRGB());
            }
            return true;
        }
        return false;
    }

    private void drawStackSize(DrawContext context, TextRenderer renderer, long size, int x, int y, @Nullable Color color) {
        float scaleFactor = 0.6f;
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        NumberFormat instance = hasShiftDown() ? NumberFormat.getNumberInstance() : NumberFormat.getCompactNumberInstance(Locale.ROOT, NumberFormat.Style.SHORT);
        String stackSize = instance.format(size);
        context.getMatrices().push();
        context.getMatrices().scale(scaleFactor, scaleFactor, scaleFactor);
        context.getMatrices().translate(0, 0, 450);
        float inverseScaleFactor = 1.0f / scaleFactor;
        int X = (int) (((float) x + 0 + 16.0f - renderer.getWidth(stackSize) * scaleFactor) * inverseScaleFactor);
        int Y = (int) (((float) y + 0 + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
        int i = color != null ? color.getRGB() : 16777215;
        context.drawText(renderer, stackSize, X, Y, i, true);
        context.getMatrices().pop();
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void handledScreenTick() {
        updateSearch();
        searchBox.tick();
    }

    @Override
    public void receive(NbtCompound nbt) {
        handler.receiveClientNBTPacket(nbt);
        refreshItemList = true;
    }
}
