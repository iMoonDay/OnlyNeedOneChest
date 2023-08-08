package com.imoonday.on1chest.client.screen;

import com.imoonday.on1chest.ChineseUtils;
import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.blocks.StorageAccessorBlock;
import com.imoonday.on1chest.init.ModScreens;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class StorageAssessorScreen extends HandledScreen<StorageAssessorScreen.StorageAssessorScreenHandler> {

    private static final Identifier TEXTURE = OnlyNeedOneChest.id("textures/gui/assessor.png");
    private static final SimpleInventory INVENTORY = new SimpleInventory(54) {
        @Override
        public int getMaxCountPerStack() {
            return Integer.MAX_VALUE;
        }
    };
    private static final DefaultedList<ItemStack> ITEM_LIST = DefaultedList.of();
    private static float scrollPosition = 0;
    private static SortComparator comparator = SortComparator.ID;
    private static boolean reversed = false;
    private static String nameFilter;
    private static final Set<FilterPredicate> STACK_FILTERS = new HashSet<>();
    private static final Set<ItemStack> FAVOURITE_STACKS = new HashSet<>();
    private boolean scrolling;
    private TextFieldWidget searchBox;
    private ButtonWidget sortWidget;

    public StorageAssessorScreen(StorageAssessorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 214;
        this.backgroundHeight = 222;
        this.playerInventoryTitleX += 19;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
        this.titleX += 19;
        scrollPosition = 0;
    }

    @Override
    protected void init() {
        super.init();
        this.searchBox = new TextFieldWidget(this.textRenderer, this.x + 100, this.y + 6, 86, 10, Text.empty());
        this.searchBox.setChangedListener(s -> {
            nameFilter = s;
            this.handler.updateItems();
        });
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setEditableColor(0xFFFFFF);
        this.addDrawableChild(this.searchBox);

        this.sortWidget = ButtonWidget.builder(Text.translatable("sort.on1chest.title"), button -> {
            comparator = comparator.next();
            button.setTooltip(Tooltip.of(Text.translatable("sort.on1chest.tooltip", Text.translatable(comparator.translationKey))));
            this.handler.updateItems();
        }).dimensions(this.x + 5, this.y + 19, 19, 13).tooltip(Tooltip.of(Text.translatable("sort.on1chest.tooltip", Text.translatable(comparator.translationKey)))).build();
        this.addDrawableChild(this.sortWidget);

        for (int i = 0; i < FilterPredicate.values().length; i++) {
            CheckboxWidget checkboxWidget = createCheckboxWidget(i);
            this.addDrawableChild(checkboxWidget);
        }
    }

    @NotNull
    private CheckboxWidget createCheckboxWidget(int i) {
        FilterPredicate filter = FilterPredicate.values()[i];
        return new CheckboxWidget(StorageAssessorScreen.this.x + 215, StorageAssessorScreen.this.y + 17 + 21 * i, 20, 20, Text.translatable(filter.translationKey), STACK_FILTERS.contains(filter)) {
            @Override
            public void onPress() {
                super.onPress();
                if (this.isChecked()) {
                    STACK_FILTERS.add(filter);
                } else {
                    STACK_FILTERS.remove(filter);
                }
                StorageAssessorScreen.this.handler.updateItems();
            }
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        String string = this.searchBox.getText();
        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
                this.handler.updateItems();
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
        context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        i = this.x + 175 + 19;
        j = this.y + 18;
        int k = j + 112;
        context.drawTexture(TEXTURE, i, j + (int) ((float) (k - j - 21) * scrollPosition), 232, 0, 12, 15);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        int i = this.handler.getRow(scrollPosition);
        String string = this.searchBox.getText();
        this.init(client, width, height);
        this.searchBox.setText(string);
        if (!this.searchBox.getText().isEmpty()) {
            this.handler.updateItems();
        }
        scrollPosition = this.handler.getScrollPosition(i);
        this.handler.updateItems();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.searchBox.isMouseOver(mouseX, mouseY) && !this.sortWidget.isMouseOver(mouseX, mouseY)) {
            this.setFocused(null);
        }
        if (button == 1 && this.sortWidget.isMouseOver(mouseX, mouseY)) {
            reversed = !reversed;
            this.handler.updateItems();
        }
        if (button == 0) {
            if (this.isClickInScrollbar(mouseX, mouseY)) {
                this.scrolling = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        scrollPosition = this.handler.getScrollPosition(scrollPosition, amount);
        this.handler.updateItems();
        return true;
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int i = this.x;
        int j = this.y;
        int k = i + 175 + 19;
        int l = j + 18;
        int m = k + 14;
        int n = l + 112;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            int i = this.y + 18;
            int j = i + 112;
            scrollPosition = ((float) mouseY - (float) i - 7.5f) / ((float) (j - i) - 15.0f);
            scrollPosition = MathHelper.clamp(scrollPosition, 0.0f, 1.0f);
            this.handler.updateItems();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.handler.slots.stream().takeWhile(slot -> slot.id < INVENTORY.size()).filter(slot -> FAVOURITE_STACKS.stream().anyMatch(stack -> ItemStack.canCombine(stack, slot.getStack()))).forEach(slot -> context.drawBorder(this.x + slot.x - 1, this.y + slot.y - 1, 18, 18, Color.YELLOW.getRGB()));
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    public static List<ItemStack> getDisplayItemStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        ITEM_LIST.stream().filter(stack -> !stack.isEmpty() && isFilterPassed(stack, nameFilter) && STACK_FILTERS.stream().allMatch(filter -> filter.predicate.test(stack))).sorted(comparator.getComparator(reversed).thenComparing(ItemStack::isEmpty)).forEach(stacks::add);
        return stacks;
    }

    public static boolean isFilterPassed(ItemStack stack, String filter) {
        if (filter == null || filter.isEmpty()) return true;
        List<String> stringList = new ArrayList<>();
        stringList.add(stack.getTranslationKey());
        stringList.add(stack.getItem().getTranslationKey());
        if (!filter.startsWith("#")) {
            stringList.add(stack.getName().getString());
            stringList.add(stack.getItem().getName().getString());
            stringList.add(ChineseUtils.toPinyin(stack.getName().getString()));
            stringList.add(ChineseUtils.toFirstChar(stack.getName().getString()));
        }
        return stringList.stream().anyMatch(s -> s.toLowerCase().contains(filter.replaceFirst("#", "").toLowerCase()));
    }

    public static class StorageAssessorScreenHandler extends ScreenHandler {

        private final ScreenHandlerContext context;

        public StorageAssessorScreenHandler(int syncId, PlayerInventory playerInventory) {
            this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
        }

        public StorageAssessorScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
            super(ModScreens.STORAGE_ASSESSOR_SCREEN_HANDLER, syncId);
            this.context = context;
            updateItemList();
            int k;
            int j;
            for (j = 0; j < 6; ++j) {
                for (k = 0; k < 9; ++k) {
                    this.addSlot(new MemorySlot(INVENTORY, k + j * 9, 27 + k * 18, 18 + j * 18));
                }
            }
            for (j = 0; j < 3; ++j) {
                for (k = 0; k < 9; ++k) {
                    this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 27 + k * 18, 103 + j * 18 + 36 + 1));
                }
            }
            for (j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j, 27 + j * 18, 161 + 36 + 1));
            }
            this.scrollItems(0.0f);
        }

        @Override
        public void onClosed(PlayerEntity player) {
            super.onClosed(player);
            if (!ITEM_LIST.isEmpty()) {
                ITEM_LIST.clear();
            }
            nameFilter = null;
        }

        protected int getOverflowRows() {
            int size = 0;
            for (ItemStack stack : getDisplayItemStacks()) {
                if (!stack.isEmpty()) {
                    size++;
                }
            }
            return MathHelper.ceilDiv(size, 9) - 6;
        }

        protected int getRow(float scroll) {
            return Math.max((int) ((double) (scroll * (float) this.getOverflowRows()) + 0.5), 0);
        }

        protected float getScrollPosition(int row) {
            return MathHelper.clamp((float) row / (float) this.getOverflowRows(), 0.0f, 1.0f);
        }

        protected float getScrollPosition(float current, double amount) {
            if (getDisplayItemStacks().size() <= INVENTORY.size()) {
                return 0.0f;
            }
            return MathHelper.clamp(current - (float) (amount / (double) this.getOverflowRows()), 0.0f, 1.0f);
        }

        public void updateItems() {
            scrollItems(scrollPosition);
        }

        public void scrollItems(float position) {
            int i = this.getRow(position);
            List<ItemStack> stacks = getDisplayItemStacks();
            for (int j = 0; j < 6; ++j) {
                for (int k = 0; k < 9; ++k) {
                    int l = k + (j + i) * 9;
                    if (l >= 0 && l < stacks.size()) {
                        ItemStack stack = stacks.get(l);
                        INVENTORY.setStack(k + j * 9, stack);
                        continue;
                    }
                    INVENTORY.setStack(k + j * 9, ItemStack.EMPTY);
                }
            }
        }

        @SuppressWarnings("ConstantValue")
        @Override
        public ItemStack quickMove(PlayerEntity player, int invSlot) {
            if (player.getWorld().isClient) {
                updateItemList();
                return ItemStack.EMPTY;
            }
            ItemStack newStack = ItemStack.EMPTY;
            Slot slot = this.slots.get(invSlot);
            if (slot != null && slot.hasStack()) {
                ItemStack originalStack = slot.getStack().copy();
                if (originalStack.getCount() > originalStack.getMaxCount() && invSlot < INVENTORY.size()) {
                    originalStack.setCount(originalStack.getMaxCount());
                }
                newStack = originalStack.copy();
                if (invSlot < INVENTORY.size()) {
                    if (this.insertItem(originalStack, INVENTORY.size(), this.slots.size(), true)) {
                        this.removeStack(newStack, Math.min(newStack.getCount(), newStack.getMaxCount()));
                    } else {
                        return ItemStack.EMPTY;
                    }
                } else if (this.canInsert(originalStack)) {
                    this.addStack(originalStack);
                } else {
                    return ItemStack.EMPTY;
                }

                int count = slot.getStack().getCount();
                if (originalStack.isEmpty() && count <= slot.getStack().getMaxCount()) {
                    slot.setStack(ItemStack.EMPTY);
                } else {
                    newStack = originalStack;
                    slot.setStack(newStack);
                    slot.markDirty();
                }
                updateItemList();
            }

            return newStack;
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return INVENTORY.canPlayerUse(player);
        }

        public void updateItemList() {
            context.run((world, pos) -> {
                if (world.getBlockState(pos).getBlock() instanceof StorageAccessorBlock block) {
                    if (!ITEM_LIST.isEmpty()) {
                        ITEM_LIST.clear();
                    }
                    DefaultedList<ItemStack> inventory = block.createItemList(world, pos);
                    ITEM_LIST.addAll(inventory);
                }
                this.updateItems();
            });
        }

        @Override
        public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
            if (slotIndex >= INVENTORY.size() || slotIndex < 0 || !(this.slots.get(slotIndex) instanceof MemorySlot slot)) {
                super.onSlotClick(slotIndex, button, actionType, player);
                return;
            }
            ClickType clickType = button == 0 ? ClickType.LEFT : ClickType.RIGHT;
            switch (actionType) {
                case QUICK_MOVE -> {
                    if (slot.canTakeItems(player)) {
                        this.quickMove(player, slotIndex);
                        slot.markDirty();
                        return;
                    }
                }
                case PICKUP -> {
                    ItemStack slotStack = slot.getStack();
                    if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_ALT)) {
                        if (clickType == ClickType.LEFT) {
                            FAVOURITE_STACKS.add(slotStack.copyWithCount(1));
                        } else {
                            FAVOURITE_STACKS.removeIf(stack -> ItemStack.canCombine(slotStack, stack));
                        }
                    } else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL)) {
                        int times = slotStack.getCount() / slotStack.getMaxCount() + (slotStack.getCount() % slotStack.getMaxCount() == 0 ? 0 : 1);
                        int maxTimes = player.getInventory().size();
                        if (times > maxTimes) {
                            times = maxTimes;
                        }
                        IntStream.range(0, times).forEach(i -> this.slots.stream().filter(slot1 -> ItemStack.canCombine(slotStack, slot1.getStack())).findFirst().ifPresent(slot1 -> this.quickMove(player, slot1.id)));
                    } else {
                        ItemStack cursorStack = this.getCursorStack();
                        player.onPickupSlotClick(cursorStack, slot.getStack(), clickType);
                        if (!(this.handleSlotClick(player, clickType, slot, slotStack, cursorStack))) {
                            if (slotStack.isEmpty()) {
                                if (!cursorStack.isEmpty()) {
                                    int o = clickType == ClickType.LEFT ? cursorStack.getCount() : 1;
                                    this.setCursorStack(slot.insertStack(cursorStack, o));
                                }
                            } else if (slot.canTakeItems(player)) {
                                if (cursorStack.isEmpty()) {
                                    int count = clickType == ClickType.LEFT ? Math.min(slotStack.getCount(), slotStack.getMaxCount()) : 1;
                                    Optional<ItemStack> optional = slot.tryTakeStackRange(count, Integer.MAX_VALUE, player);
                                    optional.ifPresent(stack -> {
                                        this.setCursorStack(stack);
                                        slot.onTakeItem(player, stack);
                                    });
                                } else {
                                    if (clickType == ClickType.LEFT) {
                                        if (slot.canInsert(cursorStack)) {
                                            this.setCursorStack(slot.insertStack(cursorStack));
                                        } else if (ItemStack.canCombine(slotStack, cursorStack)) {
                                            Optional<ItemStack> optional2 = slot.tryTakeStackRange(slotStack.getCount(), cursorStack.getMaxCount() - cursorStack.getCount(), player);
                                            optional2.ifPresent(stack -> {
                                                cursorStack.increment(stack.getCount());
                                                slot.onTakeItem(player, stack);
                                            });
                                        }
                                    } else {
                                        if (ItemStack.canCombine(slotStack, cursorStack)) {
                                            Optional<ItemStack> optional2 = slot.tryTakeStackRange(1, cursorStack.getMaxCount() - cursorStack.getCount(), player);
                                            optional2.ifPresent(stack -> {
                                                cursorStack.increment(stack.getCount());
                                                slot.onTakeItem(player, stack);
                                            });
                                        } else if (slot.canInsert(cursorStack)) {
                                            this.setCursorStack(slot.insertStack(cursorStack, 1));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    slot.markDirty();
                    return;
                }
                case PICKUP_ALL -> {
                    return;
                }
                case SWAP -> {
                    ItemStack slotStack = slot.getStack();
                    PlayerInventory playerInventory = player.getInventory();
                    ItemStack inventoryStack = playerInventory.getStack(button);
                    if (inventoryStack.isEmpty() && slotStack.isEmpty()) {
                        return;
                    }
                    if (!inventoryStack.isEmpty()) {
                        slot.insertStack(inventoryStack);
                        slot.markDirty();
                    }
                    if (!slot.canTakeItems(player)) {
                        return;
                    }
                    int count = Math.min(slotStack.getCount(), slotStack.getMaxCount());
                    ItemStack swapStack = slotStack.copyWithCount(count);
                    playerInventory.setStack(button, swapStack);
                    slot.onTake(count);
                    slot.setStack(ItemStack.EMPTY);
                    slot.onTakeItem(player, swapStack);
                    return;
                }
            }
            super.onSlotClick(slotIndex, button, actionType, player);
        }

        private boolean handleSlotClick(PlayerEntity player, ClickType clickType, Slot slot, ItemStack stack, ItemStack cursorStack) {
            FeatureSet featureSet = player.getWorld().getEnabledFeatures();
            if (cursorStack.isItemEnabled(featureSet) && cursorStack.onStackClicked(slot, clickType, player)) {
                return true;
            }
            return stack.isItemEnabled(featureSet) && stack.onClicked(cursorStack, slot, clickType, player, this.getCursorStackReference());
        }

        private StackReference getCursorStackReference() {
            return new StackReference() {

                @Override
                public ItemStack get() {
                    return StorageAssessorScreenHandler.this.getCursorStack();
                }

                @Override
                public boolean set(ItemStack stack) {
                    StorageAssessorScreenHandler.this.setCursorStack(stack);
                    return true;
                }
            };
        }

        class MemorySlot extends Slot {

            public MemorySlot(Inventory inventory, int index, int x, int y) {
                super(inventory, index, x, y);
            }

            @Override
            public boolean canInsert(ItemStack stack) {
                return isInMemorySlot() ? StorageAssessorScreenHandler.this.canInsert(stack) : super.canInsert(stack);
            }

            @Override
            public ItemStack insertStack(ItemStack stack, int count) {
                return isInMemorySlot() ? addStack(stack, count) : super.insertStack(stack, count);
            }

            @Override
            public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
                return player.getWorld().isClient && isInMemorySlot() ? Optional.empty() : super.tryTakeStackRange(min, max, player);
            }

            @Override
            public ItemStack takeStack(int amount) {
                int maxCount = this.getStack().getMaxCount();
                if (amount > maxCount) {
                    amount = maxCount;
                }
                return super.takeStack(amount);
            }

            @Override
            public void setStack(ItemStack stack) {
                if (isInMemorySlot()) {
                    insertStack(stack);
                    return;
                }
                super.setStack(stack);
            }

            @Override
            public void onTake(int amount) {
                super.onTake(amount);
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                if (!player.getWorld().isClient && isInMemorySlot()) {
                    removeStack(stack);
                }
                super.onTakeItem(player, stack);
            }

            @Override
            public void markDirty() {
                super.markDirty();
                updateItemList();
            }

            private boolean isInMemorySlot() {
                return this.id >= 0 && this.id <= INVENTORY.size() - 1;
            }

        }

        public boolean canInsert(ItemStack stack) {
            return context.get((world, pos) -> {
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof StorageAccessorBlock block) {
                    DefaultedList<ItemStack> stacks = block.createItemList(world, pos);
                    int count = stack.getCount();
                    if (stacks.stream().anyMatch(stack1 -> ItemStack.canCombine(stack, stack1))) {
                        for (ItemStack itemStack : stacks) {
                            if (ItemStack.canCombine(stack, itemStack)) {
                                if (itemStack.getCount() + count <= itemStack.getMaxCount()) {
                                    return true;
                                }
                                count -= itemStack.getMaxCount() - itemStack.getCount();
                            }
                        }
                    }
                    if (count > 0) {
                        return stacks.stream().anyMatch(ItemStack::isEmpty);
                    }
                }
                return false;
            }, false);
        }

        public void removeStack(ItemStack stack) {
            removeStack(stack, stack.getCount());
        }

        public void removeStack(ItemStack stack, int removeCount) {
            context.run((world, pos) -> {
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof StorageAccessorBlock block) {
                    int count = removeCount;
                    for (Inventory inventory : block.getAllInventories(world, pos)) {
                        if (count <= 0) {
                            break;
                        }
                        for (int i = 0; i < inventory.size(); ++i) {
                            if (count <= 0) {
                                break;
                            }
                            ItemStack itemStack = inventory.getStack(i);
                            int maxCount = Math.min(count, stack.getMaxCount());
                            if (ItemStack.canCombine(itemStack, stack)) {
                                int stackCount = itemStack.getCount();
                                if (stackCount >= maxCount) {
                                    inventory.removeStack(i, maxCount);
                                    count -= maxCount;
                                } else {
                                    inventory.removeStack(i);
                                    count -= stackCount;
                                }
                            }
                        }
                        inventory.markDirty();
                    }
                    updateItemList();
                }
            });
        }

        public ItemStack addStack(ItemStack stack) {
            return addStack(stack, stack.getCount());
        }

        public ItemStack addStack(ItemStack stack, int count) {
            context.run((world, pos) -> {
                if (world.getBlockState(pos).getBlock() instanceof StorageAccessorBlock block) {
                    List<Inventory> inventories = block.getAllInventories(world, pos);
                    boolean cannotCombine = inventories.stream().noneMatch(inventory1 -> inventory1.containsAny(stack2 -> ItemStack.canCombine(stack2, stack) && stack2.getCount() + stack.getCount() <= stack.getMaxCount()));
                    int remainingCount = count;
                    for (Inventory inventory : inventories) {
                        if (remainingCount <= 0) {
                            break;
                        }
                        for (int i = 0; i < inventory.size(); i++) {
                            if (remainingCount == 0) {
                                break;
                            }
                            ItemStack stack1 = inventory.getStack(i);
                            int maxCount = Math.min(remainingCount, stack.getMaxCount());
                            if (cannotCombine && stack1.isEmpty()) {
                                inventory.setStack(i, stack.copyWithCount(maxCount));
                                remainingCount -= maxCount;
                            } else if (ItemStack.canCombine(stack1, stack)) {
                                if (maxCount + stack1.getCount() <= stack1.getMaxCount()) {
                                    stack1.increment(maxCount);
                                    remainingCount -= maxCount;
                                } else {
                                    int amount = stack1.getMaxCount() - stack1.getCount();
                                    stack1.increment(amount);
                                    remainingCount -= amount;
                                }
                            }
                        }
                        inventory.markDirty();
                    }
                    stack.decrement(count - remainingCount);
                    updateItemList();
                }
            });
            return stack;
        }
    }

    public enum SortComparator {
        ID("sort.on1chest.raw_id", stack -> Registries.ITEM.getRawId(stack.getItem())),
        NAME("sort.on1chest.name", stack -> stack.getName().getString()),
        COUNT("sort.on1chest.count", ItemStack::getCount),
        MAX_COUNT("sort.on1chest.max_count", ItemStack::getMaxCount),
        DAMAGE("sort.on1chest.damage", ItemStack::getDamage),
        RARITY("sort.on1chest.rarity", ItemStack::getRarity);

        public final String translationKey;
        private final Comparator<ItemStack> comparator;

        <T extends Comparable<? super T>> SortComparator(String translationKey, Function<ItemStack, ? extends T> comparator) {
            this.translationKey = translationKey;
            this.comparator = Comparator.comparing(comparator).thenComparing(stack -> stack.getName().getString()).thenComparing(ItemStack::getCount).thenComparing(ItemStack::getDamage);
        }

        public Comparator<ItemStack> getComparator(boolean reversed) {
            return Comparator.<ItemStack, Boolean>comparing(stack -> FAVOURITE_STACKS.stream().anyMatch(stack1 -> ItemStack.canCombine(stack, stack1))).reversed().thenComparing(reversed ? comparator.reversed() : comparator);
        }

        public SortComparator next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }

    public enum FilterPredicate {
        IS_ENCHANTED("filter.on1chest.is_enchanted", ItemStack::hasEnchantments),
        HAS_NBT("filter.on1chest.has_nbt", ItemStack::hasNbt),
        IS_DAMAGED("filter.on1chest.is_damaged", ItemStack::isDamaged),
        IS_FOOD("filter.on1chest.is_food", ItemStack::isFood);

        public final String translationKey;
        public final Predicate<ItemStack> predicate;

        FilterPredicate(String translationKey, Predicate<ItemStack> predicate) {
            this.translationKey = translationKey;
            this.predicate = predicate;
        }
    }
}
