package com.imoonday.on1chest.screen.client;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.client.gui.tooltip.IngredientTooltip;
import com.imoonday.on1chest.network.NetworkHandler;
import com.imoonday.on1chest.screen.QuickCraftingScreenHandler;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import com.imoonday.on1chest.screen.widgets.ButtonIconWidget;
import com.imoonday.on1chest.screen.widgets.ItemStackWidget;
import com.imoonday.on1chest.utils.CraftingRecipeTreeManager;
import com.imoonday.on1chest.utils.IScreenDataReceiver;
import com.imoonday.on1chest.utils.ItemStack2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuickCraftingScreen extends HandledScreen<QuickCraftingScreenHandler> implements IScreenDataReceiver {

    private static final Identifier TEXTURE = OnlyNeedOneChest.id("textures/gui/quick_crafting.png");
    private static final Identifier RECIPE_BOOK = new Identifier("textures/gui/recipe_book.png");
    public static final Identifier CRAFT_ID = OnlyNeedOneChest.id("textures/button/craft.png");
    public static final Identifier CRAFT_ON_ID = OnlyNeedOneChest.id("textures/button/craft_on.png");
    private static final Text SEARCH_HINT_TEXT = Text.translatable("gui.recipebook.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
    private static final int RECIPE_WIDTH = 147;
    private static final int RECIPE_HEIGHT = 166;
    private final Map<CraftingRecipeTreeManager.CraftResult, Integer> resultOffsetMap = new HashMap<>();
    private final Map<Integer, Boolean> resultIndexMap = new HashMap<>();
    private final Boolean[] overflow = new Boolean[]{false, false, false};
    private final ItemStackWidget[][] displayResults = new ItemStackWidget[6][3];
    private ItemStackWidget[][] displayStacks = new ItemStackWidget[5][4];
    private TextFieldWidget searchField;
    private ToggleButtonWidget nextPageButton;
    private ToggleButtonWidget prevPageButton;
    private ButtonIconWidget increaseButton;
    private ButtonIconWidget decreaseButton;
    private ButtonIconWidget craftButton;
    private int currentPage;
    private int pageCount;
    private boolean calculating;
    private int calculateTime;
    private int resultOffset;

    public QuickCraftingScreen(QuickCraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = this.x;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        int i1 = (this.width - 147) / 2 - 86;
        context.drawTexture(RECIPE_BOOK, i1, j, 1, 1, RECIPE_WIDTH, 166);
        drawCraftMode(context);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawPage(context);
        renderDisplayStacks(context, mouseX, mouseY, delta);
        drawCraftResults(context, mouseX, mouseY, delta);
        drawCalculating(context);
        drawCraftButtonTooltip(context, mouseX, mouseY);
        drawCalculateTime(context);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private void drawCalculateTime(DrawContext context) {
        if (!calculating && calculateTime > 0) {
            String text = "本次耗时约:%ss".formatted(String.valueOf(calculateTime / 20.0f));
            context.drawText(textRenderer, text, this.x + this.backgroundWidth - textRenderer.getWidth(text) - 6, this.y + 6, Color.GRAY.getRGB(), false);
        }
    }

    private void drawCraftMode(DrawContext context) {
        if (!this.craftButton.visible && !QuickCraftingScreenHandler.isCraftedList(this.handler.craftResults) && !this.handler.result.isEmpty()) {
            context.drawTexture(TEXTURE, this.x + 115, this.y + 32, 176, 0, 28, 21);
        }
    }

    private void drawCraftButtonTooltip(DrawContext context, int mouseX, int mouseY) {
        if (this.craftButton.isHovered() && this.handler.selectedResult != null) {
            Set<ItemStack> remainder = this.handler.selectedResult.getRemainder();
            if (!remainder.isEmpty()) {
                context.drawTooltip(textRenderer, List.of(Text.literal("剩余材料：").formatted(Formatting.GREEN)), Optional.of(new IngredientTooltip(Ingredient.ofStacks(remainder.stream()))), mouseX, mouseY);
            } else if (QuickCraftingScreenHandler.isCraftedList(this.handler.craftResults)) {
                context.drawTooltip(textRenderer, Text.literal("无剩余材料").formatted(Formatting.YELLOW), mouseX, mouseY);
            }
        }
    }

    private void drawCalculating(DrawContext context) {
        if (calculating && calculateTime > 10) {
            context.drawText(textRenderer, "计算中(%ds)%s".formatted(calculateTime / 20, ".".repeat(calculateTime % 40 / 10)), this.x + 8, this.y + 18, Color.WHITE.getRGB(), true);
        }
    }

    private void drawCraftResults(DrawContext context, int mouseX, int mouseY, float delta) {
        updateDisplayResults();
        for (ItemStackWidget[] widgets : this.displayResults) {
            for (ItemStackWidget widget : widgets) {
                if (widget != null) {
                    widget.render(context, mouseX, mouseY, delta);
                }
            }
        }
        List<Map.Entry<Integer, Boolean>> sorted = new ArrayList<>(this.resultIndexMap.entrySet());
        sorted.sort(Comparator.comparingInt(Map.Entry::getKey));
        int y = this.y + 17;
        for (Map.Entry<Integer, Boolean> entry : sorted) {
            context.drawText(textRenderer, String.valueOf(entry.getKey()), this.x + 3, y, entry.getValue() ? Color.GREEN.getRGB() : Color.WHITE.getRGB(), true);
            y += 18;
        }
        if (!calculating) {
            for (int i = 0; i < this.overflow.length; i++) {
                boolean overflow = this.overflow[i];
                if (overflow) {
                    context.fill(this.x + 114, this.y + i * 18 + 17, this.x + 115, this.y + i * 18 + 33, Color.GREEN.getRGB());
                }
            }
        }
    }

    private void renderDisplayStacks(DrawContext context, int mouseX, int mouseY, float delta) {
        Arrays.stream(this.displayStacks).flatMap(Arrays::stream).filter(Objects::nonNull).forEach(widget -> widget.render(context, mouseX, mouseY, delta));
    }

    private void drawPage(DrawContext context) {
        if (this.pageCount > 1) {
            String string = this.currentPage + 1 + "/" + this.pageCount;
            int i = this.textRenderer.getWidth(string);
            context.drawText(this.textRenderer, string, x - i / 2 + 73 - RECIPE_WIDTH, y + 141, -1, false);
        }
    }

    @Override
    protected void init() {
        super.init();

        this.x = 177 + (width - backgroundWidth - 200) / 2;

        this.reset();
        this.addDrawableChild(this.searchField);
        this.nextPageButton = new ToggleButtonWidget(this.x + 93 - RECIPE_WIDTH, this.y + 137, 12, 17, false);
        this.nextPageButton.setTextureUV(1, 208, 13, 18, RECIPE_BOOK);
        this.addDrawableChild(this.nextPageButton);
        this.prevPageButton = new ToggleButtonWidget(this.x + 38 - RECIPE_WIDTH, this.y + 137, 12, 17, true);
        this.prevPageButton.setTextureUV(1, 208, 13, 18, RECIPE_BOOK);
        this.prevPageButton.visible = false;
        this.addDrawableChild(this.prevPageButton);
        this.craftButton = new ButtonIconWidget(this.x + 123, this.y + 37, 12, 12, CRAFT_ID, CRAFT_ON_ID);
        this.craftButton.addClickAction(0, widget -> {
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putBoolean("Confirm", hasShiftDown());
            NetworkHandler.sendToServer(nbtCompound);
        });
        this.craftButton.visible = false;
        this.addDrawableChild(this.craftButton);
        this.increaseButton = new ButtonIconWidget(this.x + 147, this.y + 16, 17, 11, TEXTURE, TEXTURE);
        this.increaseButton.addClickAction(0, widget -> onClickStack(0, this.handler.result.getStack(0).copyWithCount(1)));
        this.increaseButton.setTextureWidth(256);
        this.increaseButton.setTextureHeight(256);
        this.increaseButton.setTextureV(190);
        this.increaseButton.setHoveredTextureU(18);
        this.increaseButton.setHoveredTextureV(190);
        this.increaseButton.visible = false;
        this.addDrawableChild(this.increaseButton);
        this.decreaseButton = new ButtonIconWidget(this.x + 147, this.y + 59, 17, 11, TEXTURE, TEXTURE);
        this.decreaseButton.addClickAction(0, widget -> onClickStack(1, this.handler.result.getStack(0).copyWithCount(1)));
        this.decreaseButton.setTextureWidth(256);
        this.decreaseButton.setTextureHeight(256);
        this.decreaseButton.setTextureV(176);
        this.decreaseButton.setHoveredTextureU(18);
        this.decreaseButton.setHoveredTextureV(176);
        this.decreaseButton.visible = false;
        this.addDrawableChild(this.decreaseButton);
        updateDisplayStacks();
    }

    private void updateDisplayStacks() {
        int row = 1;
        int column = 1;
        boolean empty = Arrays.stream(this.displayStacks).allMatch(Objects::isNull);
        this.displayStacks = new ItemStackWidget[5][4];
        List<ItemStack> stacks = this.getDisplayStacks();
        for (ItemStack stack : stacks) {
            ItemStackWidget widget = new ItemStackWidget(this.textRenderer, this.getLeftX() + 22 * column, this.y + 20 + 22 * row, stack, false);
            widget.setDrawBorder(true);
            this.displayStacks[column - 1][row - 1] = widget;
            if (empty) {
                this.addDrawableChild(widget);
            }
            column++;
            if (column > 5) {
                column = 1;
                row++;
                if (row > 4) {
                    break;
                }
            }
        }
    }

    public void updateDisplayResults() {
        int startX = this.x + 8;
        int x = startX;
        int y = this.y + 17;
        int index = 0;
        List<CraftingRecipeTreeManager.CraftResult> results = this.handler.craftResults;
        int size = this.handler.craftResults.size() - 3;
        if (this.resultOffset > size) {
            this.resultOffset = size;
        }
        if (this.resultOffset < 0) {
            this.resultOffset = 0;
        }
        int offset = resultOffset;
        for (ItemStackWidget[] displayResult : this.displayResults) {
            Arrays.fill(displayResult, null);
        }
        this.resultIndexMap.clear();
        Arrays.fill(this.overflow, false);
        for (int i = 0; i < results.size(); i++) {
            CraftingRecipeTreeManager.CraftResult craftResult = results.get(i);
            if (offset > 0) {
                offset--;
                continue;
            }
            Set<ItemStack> stacks = craftResult.getCost();
            List<ItemStack> sorted;
            ItemStack2ObjectMap<Ingredient> ingredientMap = new ItemStack2ObjectMap<>(false);
            if (stacks.isEmpty() || craftResult.isMissing()) {
                Map<Ingredient, Integer> map = craftResult.getMissing().iterator().next();
                for (Map.Entry<Ingredient, Integer> entry : map.entrySet()) {
                    ItemStack itemStack = entry.getKey().getMatchingStacks()[(int) (Util.getMeasuringTimeMs() / 1000 % entry.getKey().getMatchingStacks().length)].copyWithCount(entry.getValue());
                    ingredientMap.put(itemStack, entry.getKey());
                }
                sorted = ingredientMap.entrySet().stream().sorted(Comparator.comparing(entry -> entry.getValue().getMatchingStacks()[0].getNbt() != null ? entry.getValue().getMatchingStacks()[0].getName().getString() + " " + entry.getValue().getMatchingStacks()[0].getNbt() : entry.getValue().getMatchingStacks()[0].getName().getString())).map(Map.Entry::getKey).toList();
            } else {
                sorted = stacks.stream().sorted(Comparator.comparing(stack -> stack.getNbt() != null ? stack.getName().getString() + " " + stack.getNbt() : stack.getName().getString())).toList();
            }
            int count = 0;
            int renderOffset = this.resultOffsetMap.getOrDefault(craftResult, 0);
            for (ItemStack itemStack : sorted) {
                if (renderOffset > 0) {
                    renderOffset--;
                    continue;
                }
                ItemStackWidget widget = createResultWidget(itemStack, ingredientMap.getOrDefault(itemStack, null), x, y);
                this.displayResults[count][index] = widget;
                x += 18;
                if (++count >= 6) {
                    break;
                }
            }
            if (results.size() > 1) {
                this.resultIndexMap.put(i, craftResult == this.handler.selectedResult);
            }
            this.overflow[index] = sorted.size() > 6;
            x = startX;
            y += 18;
            if (++index >= 3) {
                break;
            }
        }
    }

    private ItemStackWidget createResultWidget(ItemStack itemStack, Ingredient ingredient, int x, int y) {
        ItemStackWidget widget = new ItemStackWidget(textRenderer, x, y, itemStack, true);
        widget.setDrawBorder(false);
        widget.setDrawSlotHighlight(true);
        widget.setTooltipRenderer((context, mouseX, mouseY, delta) -> {
            if (hasShiftDown()) {
                List<CraftingRecipe> recipes = null;
                boolean missingRecipe = this.client != null && this.client.world != null && (recipes = CraftingRecipeTreeManager.getOrCreate(this.client.world).getRecipe(widget.getStack())).isEmpty();
                if (missingRecipe) {
                    context.drawTooltip(textRenderer, Text.literal("无合成配方").formatted(Formatting.RED), mouseX, mouseY);
                } else if (recipes != null) {
                    context.drawTooltip(textRenderer, Text.literal("左击尝试合成").formatted(Formatting.GREEN), mouseX, mouseY);
                }
            } else {
                if (this.client != null) {
                    context.drawTooltip(textRenderer, Screen.getTooltipFromItem(this.client, widget.getStack()), ingredient == null || ingredient.getMatchingStacks().length == 1 ? widget.getStack().getTooltipData() : Optional.of(new IngredientTooltip(ingredient)), mouseX, mouseY);
                }
            }
        });
        return widget;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.searchField.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(null);
        }
        if (isClickInsideRecipeBook(mouseX, mouseY, false) && !this.handler.getCursorStack().isEmpty()) {
            onClickStack(button, this.handler.getCursorStack());
            return true;
        }
        for (ItemStackWidget[] displayStack : this.displayStacks) {
            for (ItemStackWidget widget : displayStack) {
                if (widget != null && widget.isMouseOver(mouseX, mouseY)) {
                    onClickStack(button, widget.getStack());
                    return true;
                }
            }
        }
        if (this.nextPageButton.mouseClicked(mouseX, mouseY, button)) {
            if (hasShiftDown()) {
                this.currentPage += 10;
            } else {
                ++this.currentPage;
            }
            if (this.currentPage >= this.pageCount) {
                this.currentPage = this.pageCount - 1;
            }
            this.updateDisplayStacks();
            return true;
        }
        if (this.prevPageButton.mouseClicked(mouseX, mouseY, button)) {
            if (hasShiftDown()) {
                this.currentPage -= 10;
            } else {
                --this.currentPage;
            }
            if (this.currentPage < 0) {
                this.currentPage = 0;
            }
            this.updateDisplayStacks();
            return true;
        }
        CraftingRecipeTreeManager.CraftResult selectedResult;
        if (button == 0 && !hasShiftDown() && this.handler.craftResults.size() > 1 && (selectedResult = getSelectedResult(mouseX, mouseY)) != null) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.put("Select", selectedResult.toNbt());
            NetworkHandler.sendToServer(nbtCompound);
            this.handler.selectedResult = selectedResult;
            return true;
        }
        if (button == 0 && hasShiftDown()) {
            for (ItemStackWidget[] widgets : this.displayResults) {
                for (ItemStackWidget widget : widgets) {
                    if (widget != null) {
                        if (widget.isMouseOver(mouseX, mouseY)) {
                            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                            NbtCompound nbtCompound = new NbtCompound();
                            ItemStack stack = widget.getStack().copyWithCount(1);
                            if (this.client != null && this.client.world != null && CraftingRecipeTreeManager.getOrCreate(this.client.world).getRecipe(stack).isEmpty()) {
                                return true;
                            }
                            nbtCompound.put("Craft", stack.writeNbt(new NbtCompound()));
                            if (!this.handler.result.getStack(0).isOf(Items.BARRIER)) {
                                this.handler.result.removeStack(0);
                            }
                            NetworkHandler.sendToServer(nbtCompound);
                            this.handler.craftResults.clear();
                            calculateTime = 0;
                            calculating = true;
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void onClickStack(int button, ItemStack stack) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        if (stack.isEmpty()) {
            return;
        }
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.put("Craft", stack.writeNbt(new NbtCompound()));
        nbtCompound.putInt("Button", button);
        nbtCompound.putBoolean("Shift", hasShiftDown());
        if (!this.handler.result.getStack(0).isOf(Items.BARRIER)) {
            this.handler.result.removeStack(0);
        }
        NetworkHandler.sendToServer(nbtCompound);
        this.handler.craftResults.clear();
        calculateTime = 0;
        calculating = true;
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        return !isClickInsideRecipeBook(mouseX, mouseY, true) && super.isClickOutsideBounds(mouseX, mouseY, left, top, button);
    }

    protected boolean isClickInsideRecipeBook(double mouseX, double mouseY, boolean containsBorder) {
        int offset = containsBorder ? 0 : 8;
        return mouseX >= this.getLeftX() + offset && mouseY >= this.y + offset && mouseX <= this.getLeftX() + RECIPE_WIDTH - offset && mouseY <= this.y + RECIPE_HEIGHT - offset;
    }

    protected CraftingRecipeTreeManager.CraftResult getSelectedResult(double mouseX, double mouseY) {
        if (!isClickInsideResults(mouseX, mouseY)) {
            return null;
        }
        List<CraftingRecipeTreeManager.CraftResult> results = this.handler.craftResults;
        if (!QuickCraftingScreenHandler.isCraftedList(results)) {
            return null;
        }
        int size = results.size();
        for (int i = 0; i < 3; i++) {
            if (mouseY >= this.y + 17 + i * 18 && mouseY <= this.y + 32 + i * 18) {
                int index = resultOffset + i;
                if (size > index) {
                    return results.get(index);
                }
            }
        }
        return null;
    }


    public int getLeftX() {
        return (this.width - 147) / 2 - 86;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        boolean shift = hasShiftDown();
        if (isClickInsideRecipeBook(mouseX, mouseY, false)) {
            if (this.prevPageButton.visible && this.prevPageButton.active && amount > 0) {
                if (shift) {
                    this.currentPage -= 5;
                } else {
                    --this.currentPage;
                }
                if (this.currentPage < 0) {
                    this.currentPage = 0;
                }
                this.updateDisplayStacks();
                return true;
            } else if (this.nextPageButton.visible && this.nextPageButton.active && amount < 0) {
                if (shift) {
                    this.currentPage += 5;
                } else {
                    ++this.currentPage;
                }
                if (this.currentPage >= this.pageCount) {
                    this.currentPage = this.pageCount - 1;
                }
                this.updateDisplayStacks();
                return true;
            }
        }
        if (isClickInsideResults(mouseX, mouseY)) {
            if (shift) {
                CraftingRecipeTreeManager.CraftResult craftResult = getSelectedResult(mouseX, mouseY);
                if (craftResult != null) {
                    int offset = this.resultOffsetMap.getOrDefault(craftResult, 0);
                    offset += amount > 0 ? -1 : 1;
                    int size = craftResult.getCost().size() - 6;
                    if (offset > size) {
                        offset = size;
                    }
                    if (offset < 0) {
                        offset = 0;
                    }
                    this.resultOffsetMap.put(craftResult, offset);
                }
            } else {
                this.resultOffset += amount > 0 ? -1 : 1;
                int size = this.handler.craftResults.size() - 3;
                if (this.resultOffset > size) {
                    this.resultOffset = size;
                }
                if (this.resultOffset < 0) {
                    this.resultOffset = 0;
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    protected boolean isClickInsideResults(double mouseX, double mouseY) {
        return mouseX >= this.x + 8 && mouseY >= this.y + 17 && mouseX <= this.x + 113 && mouseY <= this.y + 68;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.searchField.isFocused() && this.searchField.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putBoolean("Stop", true);
        NetworkHandler.sendToServer(nbtCompound);
        super.close();
    }

    public List<ItemStack> getDisplayStacks() {
        if (client == null || client.world == null) {
            return new ArrayList<>();
        }
        String text = this.searchField.getText();
        Stream<ItemStack> stackStream = CraftingRecipeTreeManager.getOrCreate(client.world).getCache().keySet().stream().filter(stack -> !stack.isEmpty()).sorted(Comparator.comparing(stack -> Registries.ITEM.getRawId(stack.getItem())));
        return text.isEmpty() ? stackStream.skip(currentPage * 20L).collect(Collectors.toList()) : stackStream.filter(stack -> StorageAssessorScreenHandler.checkTextFilter(stack, text)).skip(currentPage * 20L).collect(Collectors.toList());
    }

    public void reset() {
        String string = this.searchField != null ? this.searchField.getText() : "";
        this.searchField = new TextFieldWidget(this.textRenderer, this.getLeftX() + 26, this.y + 14, 79, this.textRenderer.fontHeight + 3, Text.translatable("itemGroup.search"));
        this.searchField.setMaxLength(50);
        this.searchField.setVisible(true);
        this.searchField.setEditableColor(0xFFFFFF);
        this.searchField.setText(string);
        this.searchField.setPlaceholder(SEARCH_HINT_TEXT);
        this.searchField.setChangedListener(s -> {
            List<ItemStack> stacks = this.getDisplayStacks();
            this.pageCount = (int) Math.ceil((double) stacks.size() / 20.0);
            if (this.pageCount <= this.currentPage) {
                this.currentPage = 0;
            }
            updateDisplayStacks();
        });
        this.pageCount = (int) Math.ceil((double) this.getDisplayStacks().size() / 20.0);
        if (this.pageCount <= this.currentPage) {
            this.currentPage = 0;
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String text = this.searchField.getText();
        int count = this.pageCount;
        int page = this.currentPage;
        super.resize(client, width, height);
        this.searchField.setText(text);
        this.pageCount = count;
        this.currentPage = page;
        updateDisplayStacks();
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        if (calculating) {
            this.calculateTime++;
        }
        if (this.nextPageButton != null) {
            this.nextPageButton.visible = this.pageCount > 1 && this.currentPage < this.pageCount - 1;
        }
        if (this.prevPageButton != null) {
            this.prevPageButton.visible = this.pageCount > 1 && this.currentPage > 0;
        }
        if (this.craftButton != null) {
            this.craftButton.visible = QuickCraftingScreenHandler.isCraftedList(this.handler.craftResults);
        }
        boolean visible = !this.handler.result.isEmpty() && QuickCraftingScreenHandler.isCraftedList(this.handler.craftResults);
        if (this.increaseButton != null) {
            this.increaseButton.visible = visible;
        }
        if (this.decreaseButton != null) {
            this.decreaseButton.visible = visible;
        }
    }

    @Override
    public void receive(NbtCompound nbt) {
        this.handler.updateResultsFromServer(nbt);
        calculating = false;
        resultOffset = 0;
    }

    @Override
    public void update() {
        updateDisplayStacks();
    }
}
