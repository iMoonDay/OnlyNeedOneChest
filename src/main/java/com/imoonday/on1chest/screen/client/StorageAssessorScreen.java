package com.imoonday.on1chest.screen.client;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.api.IScreenDataReceiver;
import com.imoonday.on1chest.client.OnlyNeedOneChestClient;
import com.imoonday.on1chest.config.Config;
import com.imoonday.on1chest.config.ConfigScreenHandler;
import com.imoonday.on1chest.filter.ItemFilter;
import com.imoonday.on1chest.filter.ItemFilterList;
import com.imoonday.on1chest.filter.ItemFilterSettings;
import com.imoonday.on1chest.filter.ItemFilterWrapper;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import com.imoonday.on1chest.screen.widgets.ButtonIconWidget;
import com.imoonday.on1chest.screen.widgets.OnOffButtonIconWidget;
import com.imoonday.on1chest.screen.widgets.SmallCheckboxWidget;
import com.imoonday.on1chest.utils.CombinedItemStack;
import com.imoonday.on1chest.utils.MapUtils;
import com.imoonday.on1chest.utils.SlotAction;
import com.imoonday.on1chest.utils.Theme;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.function.Function;

public class StorageAssessorScreen extends HandledScreen<StorageAssessorScreenHandler> implements IScreenDataReceiver {

    public static final Identifier ARROWS_TEXTURE = OnlyNeedOneChest.id("textures/button/arrows.png");
    public static final Identifier HIDE_TEXTURE = OnlyNeedOneChest.id("textures/button/hidden.png");
    protected TextFieldWidget searchBox;
    protected ButtonIconWidget settingButton;
    protected ButtonIconWidget sortButton;
    protected ButtonIconWidget filtersButton;
    protected ButtonIconWidget noSortButton;
    protected ButtonIconWidget forceUpdateButton;
    protected ButtonIconWidget themeButton;
    protected ButtonIconWidget filteringLogicButton;
    protected ButtonIconWidget moveUpButton;
    protected ButtonIconWidget moveDownButton;
    protected OnOffButtonIconWidget hideButton;
    protected final LinkedHashMap<ItemFilterSettings, SmallCheckboxWidget> filterWidgets = new LinkedHashMap<>();
    protected ItemFilterSettings hoveredSettings = null;
    protected float scrollPosition;
    protected boolean scrolling;
    protected boolean refreshItemList;
    protected String searchLast = "";
    protected int selectedSlot = -1;
    protected boolean forceUpdate;
    private Comparator<CombinedItemStack> sortComp;
    private int buttonYOffset;
    protected boolean drawTitle = true;
    protected int filterYOffset;

    public StorageAssessorScreen(StorageAssessorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 196;
        this.backgroundHeight = 222;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        this.searchBox = new TextFieldWidget(this.textRenderer, this.x + 82, this.y + 6, 86, 10, Text.empty());
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setEditableColor(0xFFFFFF);
        this.addDrawableChild(this.searchBox);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> this.searchBox.setText("")).dimensions(this.x + 175, this.y + 4, 14, 12).tooltip(Tooltip.of(Text.translatable("button.on1chest.clear"))).build());

        this.buttonYOffset = 2;

        this.settingButton = createIconButtonWidget("setting", button -> {
            if (OnlyNeedOneChestClient.clothConfig) {
                if (this.client != null) {
                    this.client.setScreen(ConfigScreenHandler.createConfigScreen(this));
                }
            } else {
                File file = Config.getFile();
                if (file != null) {
                    if (!file.exists()) {
                        Config.save();
                    }
                    Util.getOperatingSystem().open(file.toPath().toUri());
                }
            }
            return false;
        }, null, OnlyNeedOneChestClient.clothConfig ? Text.translatable("screen.on1chest.button.setting") : Text.translatable("screen.on1chest.button.open_config_file"));

        this.sortButton = createIconButtonWidget("sort", button -> {
            Config.getInstance().setComparator(Config.getInstance().getComparator().next());
            if (Config.getInstance().getComparator().ordinal() == 0) {
                Config.getInstance().setReversed(!Config.getInstance().isReversed());
            }
            button.setTooltip(Tooltip.of(Text.translatable("sort.on1chest.tooltip", Text.translatable(Config.getInstance().getComparator().translationKey), Text.translatable("sort.on1chest.order." + (Config.getInstance().isReversed() ? "reverse" : "positive")))));
            return true;
        }, null, Text.translatable("sort.on1chest.tooltip", Text.translatable(Config.getInstance().getComparator().translationKey), Text.translatable("sort.on1chest.order." + (Config.getInstance().isReversed() ? "reverse" : "positive"))));

        this.filtersButton = createIconButtonWidget("filters", button -> {
            Config config = Config.getInstance();
            config.setDisplayFilterWidgets(config.getDisplayFilterWidgets().next());
            button.setTooltip(Tooltip.of(Text.translatable("screen.on1chest.button.filter", config.getDisplayFilterWidgets().getDisplayName())));
            updateFilters(config);
            return false;
        }, null, Text.translatable("screen.on1chest.button.filter", Config.getInstance().getDisplayFilterWidgets().getDisplayName()));

        this.noSortButton = createIconButtonWidget("nosort", button -> {
            Config.getInstance().setNoSortWithShift(!Config.getInstance().isNoSortWithShift());
            button.setTooltip(Tooltip.of(Text.translatable("screen.on1chest.button.noSortWithShift", Text.translatable("screen.on1chest.button." + (Config.getInstance().isNoSortWithShift() ? "open" : "close")))));
            return true;
        }, null, Text.translatable("screen.on1chest.button.noSortWithShift", Text.translatable("screen.on1chest.button." + (Config.getInstance().isNoSortWithShift() ? "open" : "close"))));

        this.forceUpdateButton = createIconButtonWidget("force_update", button -> {
            Config.getInstance().setUpdateOnInsert(!Config.getInstance().isUpdateOnInsert());
            button.setTooltip(Tooltip.of(Text.translatable("screen.on1chest.button.updateOnInsert", Text.translatable("screen.on1chest.button." + (Config.getInstance().isUpdateOnInsert() ? "open" : "close")))));
            return Config.getInstance().isUpdateOnInsert();
        }, null, Text.translatable("screen.on1chest.button.updateOnInsert", Text.translatable("screen.on1chest.button." + (Config.getInstance().isUpdateOnInsert() ? "open" : "close"))));

        this.themeButton = createIconButtonWidget("theme", button -> {
            Config.getInstance().setTheme(Config.getInstance().getTheme().next());
            button.setTooltip(Tooltip.of(Config.getInstance().getTheme().getLocalizeText()));
            return false;
        }, null, Config.getInstance().getTheme().getLocalizeText());

        this.filteringLogicButton = createIconButtonWidget("filtering_logic", button -> {
            Config config = Config.getInstance();
            config.setFilteringLogic(config.getFilteringLogic().next());
            button.setTooltip(Tooltip.of(Text.translatable("screen.on1chest.button.filteringLogic", config.getFilteringLogic().getDisplayName())));
            refreshItemList = true;
            return false;
        }, null, Text.translatable("screen.on1chest.button.filteringLogic", Config.getInstance().getFilteringLogic().getDisplayName()));

        List<ItemFilterWrapper> itemFilterList = Config.getInstance().getItemFilterList();
        for (ItemFilterWrapper data : itemFilterList) {
            ItemFilterSettings settings = data.getMainFilter();
            SmallCheckboxWidget checkboxWidget = createCheckboxWidget(settings);
            this.filterWidgets.put(settings, checkboxWidget);
            this.addDrawableChild(checkboxWidget);
            boolean enabled = settings.isEnabled();
            for (ItemFilterSettings subFilter : data.getSubFilters()) {
                SmallCheckboxWidget widget = createCheckboxWidget(subFilter);
                if (!enabled) {
                    widget.visible = false;
                }
                this.filterWidgets.put(subFilter, widget);
                this.addDrawableChild(widget);
            }
        }

        this.hideButton = new OnOffButtonIconWidget(9, 7, HIDE_TEXTURE)
                .setTextureSize(32, 32)
                .setTextureU(16)
                .setHoveredTextureOnV(16)
                .setHoveredTextureOffset(16, 16)
                .addClickAction(0, button -> {
                    if (hoveredSettings != null) {
                        hoveredSettings.setHide(!hoveredSettings.isHide());
                        Config.saveAndUpdate();
                    }
                });
        this.hideButton.setX(this.width - 25);
        this.hideButton.visible = false;
        this.addDrawableChild(this.hideButton);

        this.moveUpButton = new ButtonIconWidget(this.hideButton.getX() + this.hideButton.getWidth() + 3, 0, 9, 6, ARROWS_TEXTURE, ARROWS_TEXTURE)
                .setTextureSize(32, 32)
                .setHoveredTextureU(16)
                .addClickAction(0, button -> {
                    if (hoveredSettings != null) {
                        Config config = Config.getInstance();
                        ItemFilterList list = config.getItemFilters();
                        if (list.moveForward(hoveredSettings)) {
                            sortFilters(config);
                        }
                    }
                });
        this.moveUpButton.visible = false;
        this.addDrawableChild(this.moveUpButton);

        this.moveDownButton = new ButtonIconWidget(this.moveUpButton.getX(), 0, 9, 6, ARROWS_TEXTURE, ARROWS_TEXTURE)
                .setTextureSize(32, 32)
                .setTextureV(16)
                .setHoveredTextureOffset(16, 16)
                .addClickAction(0, button -> {
                    if (hoveredSettings != null) {
                        Config config = Config.getInstance();
                        ItemFilterList list = config.getItemFilters();
                        if (list.moveBackward(hoveredSettings)) {
                            sortFilters(config);
                        }
                    }
                });
        this.moveDownButton.visible = false;
        this.addDrawableChild(this.moveDownButton);

        updateFilters(Config.getInstance());
    }

    protected void updateFilterButtons() {
        boolean isHovering = hoveredSettings != null;
        if (isHovering) {
            SmallCheckboxWidget widget = this.filterWidgets.get(hoveredSettings);
            if (widget != null) {
                int y = widget.getY();
                this.moveUpButton.setY(y);
                this.moveDownButton.setY(y + 7);
                this.hideButton.setY(y + (widget.getHeight() - this.hideButton.getHeight()) / 2);
            }
        }
        this.moveUpButton.visible = isHovering;
        this.moveDownButton.visible = isHovering;
        this.hideButton.visible = isHovering;
    }

    public void onScreenConfigUpdate(Config config) {
        boolean visible = config.isDisplayButtonWidgets();
        this.sortButton.visible = visible;
        this.filtersButton.visible = visible;
        this.noSortButton.visible = visible;
        updateFilters(config);
        refreshItemList = true;
    }

    public void updateFilters(Config config) {
        if (!isFilterWidgetsOutOfBounds() && filterYOffset != 0) {
            filterYOffset = 0;
        }
        int y = getFilterWidgetStartY();
        int widgetHeight = getFilterWidgetHeight();
        for (Map.Entry<ItemFilterSettings, SmallCheckboxWidget> entry : this.filterWidgets.entrySet()) {
            ItemFilterSettings settings = entry.getKey();
            SmallCheckboxWidget widget = entry.getValue();
            ItemFilter filter = settings.getFilter();
            Identifier parent = filter.getParent();
            boolean parentEnabled = !filter.hasParent() || this.filterWidgets.entrySet().stream().anyMatch(e -> e.getKey().is(parent) && e.getKey().isEnabled() && e.getValue().visible);
            widget.visible = config.getDisplayFilterWidgets().isDisplay(settings.isHide()) && parentEnabled;
            widget.setMessage(filter.getDisplayName(settings.isHide()));
            widget.recalculateWidth();
            if (widget.visible) {
                widget.setY(y + filterYOffset);
                y += widgetHeight;
            }
        }
    }

    protected int getFilterWidgetStartY() {
        return Math.max((this.height - this.backgroundHeight) / 2, 5);
    }

    protected List<SmallCheckboxWidget> getVisibleFilterWidgets() {
        return this.filterWidgets.values().stream().sorted(Comparator.comparingInt(ClickableWidget::getY)).filter(widget -> widget.visible).toList();
    }

    public boolean isFilterWidgetsOutOfBounds() {
        return isFilterWidgetsTooHigh() || isFilterWidgetsTooLow();
    }

    public boolean isFilterWidgetsTooHigh() {
        List<SmallCheckboxWidget> list = getVisibleFilterWidgets();
        return !list.isEmpty() && list.get(0).getY() < getFilterWidgetStartY();
    }

    public boolean isFilterWidgetsTooLow() {
        List<SmallCheckboxWidget> list = getVisibleFilterWidgets();
        if (list.isEmpty()) return false;
        ClickableWidget widget = list.get(list.size() - 1);
        return widget.getY() + widget.getHeight() > this.height - getFilterWidgetStartY();
    }

    private ButtonIconWidget createIconButtonWidget(String fileName, Function<ButtonIconWidget, Boolean> function, Function<ButtonIconWidget, Boolean> rightClickFunction, Text tooltip) {
        ButtonIconWidget widget = new ButtonIconWidget(this.x - 16, this.y + this.buttonYOffset, 16, 16, OnlyNeedOneChest.id("textures/button/" + fileName + ".png"), null);
        if (function != null) {
            widget.addClickAction(0, button -> {
                if (function.apply(button)) {
                    this.refreshItemList = true;
                }
            });
        }
        this.buttonYOffset += 17;
        if (tooltip != null) {
            widget.setTooltip(Tooltip.of(tooltip));
        }
        widget.visible = Config.getInstance().isDisplayButtonWidgets();
        if (rightClickFunction != null) {
            widget.addClickAction(1, button -> {
                if (rightClickFunction.apply(button)) {
                    refreshItemList = true;
                }
            });
        }
        this.addDrawableChild(widget);
        return widget;
    }

    private SmallCheckboxWidget createCheckboxWidget(ItemFilterSettings settings) {
        ItemFilter filter = settings.getFilter();
        SmallCheckboxWidget widget = settings.createCheckbox(textRenderer, this.x + this.backgroundWidth + 1 + (filter.hasParent() ? 7 : 0), 0, (checkbox, enabled) -> {
            Config.getInstance().setItemFilterEnabled(filter, enabled);
            refreshItemList = true;
        });
        if (Config.getInstance().getDisplayFilterWidgets().isHide()) {
            widget.visible = false;
        }
        return widget;
    }

    protected void sortFilters(Config config) {
        List<ItemFilterSettings> sortedFilters = config.getItemFilters().getSortedFilters();
        MapUtils.sortMapByList(this.filterWidgets, sortedFilters);
        Config.saveAndUpdate();
    }

    protected void updateSearch() {
        String searchString = searchBox.getText();
        if (refreshItemList || !searchLast.equals(searchString) || forceUpdate) {
            handler.itemListClientSorted.clear();
            handler.itemListClient.stream().filter(stack -> stack != null && stack.getStack() != null && StorageAssessorScreenHandler.checkTextFilter(stack.getStack(), searchString)).forEach(this::addStackToClientList);
            handler.itemListClientSorted.sort(handler.noSort && !forceUpdate ? sortComp : Config.getInstance().getComparator().createComparator(Config.getInstance().getFavouriteStacks(), Config.getInstance().isReversed()));
            handler.itemListClientSorted.removeIf(stack -> !Config.getInstance().getItemFilters().test(stack.getStack()));
            if (!searchLast.equals(searchString)) {
                handler.scrollItems(0);
                this.scrollPosition = 0;
            } else {
                handler.scrollItems(this.scrollPosition);
            }
            this.refreshItemList = false;
            this.searchLast = searchString;
            this.forceUpdate = false;
        }
    }

    public Slot getSelectedSlot() {
        Slot slot = this.focusedSlot;
        if (slot != null) return slot;
        if (selectedSlot > -1 && handler.getSlotByID(selectedSlot).stack != null) {
            fakeSelectedSlot.inventory.setStack(0, handler.getSlotByID(selectedSlot).stack.getStack());
            return fakeSelectedSlot;
        }
        return null;
    }

    @Override
    public void close() {
        super.close();
        OnlyNeedOneChestClient.setSelectedStack(null);
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
        if (this.client != null && selectedSlot > -1) {
            CombinedItemStack stack = this.handler.getSlotByID(selectedSlot).stack;
            if (this.client.options.dropKey.matchesKey(keyCode, scanCode) && stack != null) {
                storageSlotClick(stack, SlotAction.THROWN, hasControlDown());
                return true;
            }
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
        return Config.getInstance().getTheme().getId("assessor");
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        int row = this.handler.getRow(this.scrollPosition);
        String string = this.searchBox.getText();
        this.init(client, width, height);
        this.searchBox.setText(string);
        this.scrollPosition = this.handler.getScrollPosition(row);
        updateFilterButtons();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.searchBox.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(null);
        }
        Config config = Config.getInstance();
        if (this.searchBox.isMouseOver(mouseX, mouseY) && this.searchBox.active && this.searchBox.visible && button == 1 && config.isResetWithRightClick()) {
            if (client != null) {
                this.searchBox.playDownSound(client.getSoundManager());
            }
            this.searchBox.setText("");
            this.setFocused(this.searchBox);
        }
        if (button == 0 && this.isClickInScrollbar(mouseX, mouseY)) {
            this.scrolling = true;
            return true;
        }
        if (selectedSlot > -1) {
            switch (button) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
                    if (isKeyPressed(config.getMarkItemStackKey()) && this.handler.getSlotByID(selectedSlot).stack != null) {
                        ItemStack stack = this.handler.getSlotByID(selectedSlot).stack.getActualStack(1);
                        if (config.getFavouriteStacks().stream().noneMatch(stack1 -> stack1.equals(stack))) {
                            config.addFavouriteStack(stack);
                            refreshItemList = true;
                        }
                        return true;
                    }
                    if (!this.handler.getCursorStack().isEmpty()) {
                        storageSlotClick(null, isKeyPressed(config.getTakeAllStacksKey()) ? SlotAction.TAKE_ALL : SlotAction.LEFT_CLICK, false);
                    } else if (this.handler.getSlotByID(selectedSlot).stack != null && this.handler.getSlotByID(selectedSlot).stack.getCount() > 0) {
                        storageSlotClick(this.handler.getSlotByID(selectedSlot).stack, hasShiftDown() ? SlotAction.QUICK_MOVE : (isKeyPressed(config.getTakeAllStacksKey()) ? SlotAction.TAKE_ALL : SlotAction.LEFT_CLICK), false);
                        return true;
                    }
                }
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                    if (isKeyPressed(config.getMarkItemStackKey()) && this.handler.getSlotByID(selectedSlot).stack != null) {
                        config.removeFavouriteStack(this.handler.getSlotByID(selectedSlot).stack.getActualStack(1));
                        refreshItemList = true;
                        return true;
                    }
                    if (this.handler.getSlotByID(selectedSlot).stack != null && this.handler.getSlotByID(selectedSlot).stack.getCount() > 0) {
                        storageSlotClick(this.handler.getSlotByID(selectedSlot).stack, SlotAction.RIGHT_CLICK, hasShiftDown());
                    }
                    return true;
                }
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> {
                    if (this.handler.getSlotByID(selectedSlot).stack != null && this.handler.getCursorStack().isEmpty()) {
                        storageSlotClick(this.handler.getSlotByID(selectedSlot).stack, SlotAction.COPY, false);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected static boolean isKeyPressed(InputUtil.Key key) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), key.getCode());
    }

    protected void storageSlotClick(CombinedItemStack slotStack, SlotAction act, boolean mod) {
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
        Config config = Config.getInstance();
        if (config.getDisplayFilterWidgets().isDisplay() && mouseX > this.x + this.backgroundWidth && isFilterWidgetsOutOfBounds()) {
            int step = getFilterWidgetHeight();
            filterYOffset += amount > 0 ? step : -step;
            int min = this.height - getFilterWidgetsHeight() - getFilterWidgetStartY() * 2;
            filterYOffset = MathHelper.clamp(filterYOffset, min, 0);
            updateFilters(config);
            return true;
        }
        if (!config.isScrollOutside() && this.selectedSlot <= -1 && !this.isClickInScrollbar(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, amount);
        }
        this.scrollPosition = this.handler.getScrollPosition(this.scrollPosition, amount);
        this.handler.scrollItems(this.scrollPosition);
        return true;
    }

    public int getFilterWidgetHeight() {
        return 15;
    }

    public int getFilterWidgetsHeight() {
        return getVisibleFilterWidgets().size() * getFilterWidgetHeight();
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

        if (Config.getInstance().isUpdateOnInsert() && handler.itemList.stream().anyMatch(stack -> handler.itemListClient.stream().noneMatch(stack1 -> stack1.canCombineWith(stack)))) {
            forceUpdate = true;
        }
        if (Config.getInstance().isNoSortWithShift() && hasShiftDown() && !forceUpdate) {
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

        OnlyNeedOneChestClient.setSelectedStack(selectedSlot != -1 && handler.storageSlotList.get(selectedSlot) != null ? handler.storageSlotList.get(selectedSlot).stack : null);

        if (this.handler.getCursorStack().isEmpty() && selectedSlot != -1) {
            StorageAssessorScreenHandler.StorageSlot slot = handler.storageSlotList.get(selectedSlot);
            if (slot.stack != null) {
                if (slot.stack.isEmpty()) {
                    MutableText text = Text.translatable("screen.on1chest.slot.waitingForRefresh");
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

        updateHoveredSettings(mouseX, mouseY);
    }

    protected void updateHoveredSettings(int mouseX, int mouseY) {
        ItemFilterSettings lastSettings = this.hoveredSettings;
        this.hoveredSettings = null;
        for (Map.Entry<ItemFilterSettings, SmallCheckboxWidget> entry : this.filterWidgets.entrySet()) {
            SmallCheckboxWidget widget = entry.getValue();
            if (!widget.visible) continue;
            if (mouseY >= widget.getY() && mouseY <= widget.getY() + widget.getHeight() && mouseX >= widget.getX()) {
                this.hoveredSettings = entry.getKey();
                break;
            }
        }
        if (lastSettings != this.hoveredSettings) {
            updateFilterButtons();
        }
        if (this.hoveredSettings != null) {
            this.hideButton.setActivated(!hoveredSettings.isHide());
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);
        if (drawTitle) {
            context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0x404040, false);
        }
        context.getMatrices().push();
        selectedSlot = drawSlots(context, mouseX, mouseY);
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
        boolean isVanilla = Config.getInstance().getTheme() == Theme.VANILLA;

        if (slot.stack != null) {
            ItemStack stack = slot.stack.getStack().copy().split(1);

            context.drawItem(stack, i, j);
            context.drawItemInSlot(textRenderer, stack, i, j);

            if (Config.getInstance().getFavouriteStacks().stream().anyMatch(stack1 -> stack1.equals(stack))) {
                int color = Config.getInstance().getFavouriteColor();
                if (isVanilla) {
                    context.drawBorder(i - 1, j - 1, 18, 18, color);
                } else {
                    context.fill(i, j + 1, i + 3, j + 2, color);
                    context.fill(i + 1, j, i + 2, j + 3, color);
                }
            }

            long count = slot.stack.getCount();
            Integer color = null;
            if (count <= 0) {
                color = Color.RED.getRGB();
            } else if (isHovered && !isVanilla && this.handler.getCursorStack().isEmpty()) {
                color = Config.getInstance().getSelectedColor();
            }
            drawStackSize(context, textRenderer, count, i, j, color);
        }

        if (isHovered) {
            if (isVanilla) {
                drawSlotHighlight(context, i, j, 0);
            } else if (slot.stack != null && this.handler.getCursorStack().isEmpty()) {
                context.drawBorder(i - 1, j - 1, 18, 18, Config.getInstance().getSelectedColor());
            }
            return true;
        }
        return false;
    }

    private void drawStackSize(DrawContext context, TextRenderer renderer, long size, int x, int y, Integer color) {
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
        int i = color != null ? color : 16777215;
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

    private FakeSlot fakeSelectedSlot = new FakeSlot();

    private static class FakeSlot extends Slot {

        private static final Inventory DUMMY = new SimpleInventory(1);

        public FakeSlot() {
            super(DUMMY, 0, java.lang.Integer.MIN_VALUE, java.lang.Integer.MIN_VALUE);
        }

        @Override
        public boolean canTakePartial(PlayerEntity player) {
            return false;
        }

        @Override
        public void setStack(ItemStack stack) {
            super.setStack(stack);
        }

        @Override
        public ItemStack takeStack(int amount) {
            return ItemStack.EMPTY;
        }
    }
}
