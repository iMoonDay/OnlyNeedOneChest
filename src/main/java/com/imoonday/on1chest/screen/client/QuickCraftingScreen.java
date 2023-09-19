package com.imoonday.on1chest.screen.client;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.network.NetworkHandler;
import com.imoonday.on1chest.screen.QuickCraftingScreenHandler;
import com.imoonday.on1chest.screen.StorageAssessorScreenHandler;
import com.imoonday.on1chest.screen.widgets.ItemStackWidget;
import com.imoonday.on1chest.utils.CraftingRecipeTreeManager;
import com.imoonday.on1chest.utils.IScreenDataReceiver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuickCraftingScreen extends HandledScreen<QuickCraftingScreenHandler> implements IScreenDataReceiver {

    private static final Identifier TEXTURE = OnlyNeedOneChest.id("textures/gui/quick_crafting.png");
    private static final Identifier RECIPE_BOOK = new Identifier("textures/gui/recipe_book.png");
    private static final Text SEARCH_HINT_TEXT = Text.translatable("gui.recipebook.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
    private ItemStackWidget[][] displayStacks = new ItemStackWidget[5][4];
    private TextFieldWidget searchField;
    private ToggleButtonWidget nextPageButton;
    private ToggleButtonWidget prevPageButton;
    private int currentPage;
    private int pageCount;
    private boolean calculating;
    private int calculateTime;

    public QuickCraftingScreen(QuickCraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 240;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        if (this.pageCount > 1) {
            String string = this.currentPage + 1 + "/" + this.pageCount;
            int i = this.textRenderer.getWidth(string);
            context.drawText(this.textRenderer, string, x - i / 2 + 73, y + 141 - 14, -1, false);
        }

        Arrays.stream(this.displayStacks).flatMap(Arrays::stream).filter(Objects::nonNull).forEach(widget -> widget.render(context, mouseX, mouseY, delta));

        int x = this.x + this.backgroundWidth - 28;
        int startY = this.y + 18;
        int y = startY;
        boolean added = false;
        for (CraftingRecipeTreeManager.CraftResult craftResult : this.handler.craftResults) {
            Set<ItemStack> stacks = craftResult.getCost();
            if (stacks.isEmpty() || craftResult.isMissing()) {
                Map<Ingredient, Integer> map = craftResult.getMissing().iterator().next();
                Set<ItemStack> set = new HashSet<>();
                for (Map.Entry<Ingredient, Integer> entry : map.entrySet()) {
                    ItemStack itemStack = entry.getKey().getMatchingStacks()[0].copyWithCount(entry.getValue());
                    set.add(itemStack);
                }
                stacks = set;
            }
            List<ItemStack> sorted = new ArrayList<>(stacks);
            sorted.sort(Comparator.comparing(stack -> stack.getNbt() != null ? stack.getName().getString() + " " + stack.getNbt() : stack.getName().getString()));
            for (ItemStack itemStack : sorted) {
                context.drawItem(itemStack, x, y);
                context.drawItemInSlot(textRenderer, itemStack, x, y);
                y += 18;
            }
            x += 18;
            if (!added) {
                x += 12;
                added = true;
            }
            y = startY;
        }

        if (calculating && ++calculateTime > 20) {
            context.drawText(textRenderer, "计算中" + ".".repeat(calculateTime % 200 / 50), this.x + this.backgroundWidth - 70, this.y + 25, Color.WHITE.getRGB(), true);
        }

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        this.reset();
        this.addDrawableChild(this.searchField);
        this.nextPageButton = new ToggleButtonWidget(this.x + 93, this.y + 123, 12, 17, false);
        this.nextPageButton.setTextureUV(1, 208, 13, 18, RECIPE_BOOK);
        this.addDrawableChild(this.nextPageButton);
        this.prevPageButton = new ToggleButtonWidget(this.x + 38, this.y + 123, 12, 17, true);
        this.prevPageButton.setTextureUV(1, 208, 13, 18, RECIPE_BOOK);
        this.prevPageButton.visible = false;
        this.addDrawableChild(this.prevPageButton);
        updateDisplayStacks();
    }

    private void updateDisplayStacks() {
        int row = 1;
        int column = 1;
        boolean empty = Arrays.stream(this.displayStacks).allMatch(Objects::isNull);
        this.displayStacks = new ItemStackWidget[5][4];
        for (ItemStack stack : this.getDisplayStacks()) {
            ItemStackWidget widget = new ItemStackWidget(this.textRenderer, this.x + 22 * column, this.y + 20 + 20 * row, stack, false);
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.searchField.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(null);
        }
        Arrays.stream(this.displayStacks).flatMap(Arrays::stream).filter(Objects::nonNull).filter(widget -> widget.isMouseOver(mouseX, mouseY)).findFirst().ifPresent(widget -> {
            NbtCompound nbtCompound = new NbtCompound();
            ItemStack stack = widget.getStack();
            nbtCompound.put("Craft", stack.writeNbt(new NbtCompound()));
            nbtCompound.putInt("Button", button);
            nbtCompound.putBoolean("Shift", hasShiftDown());
            NetworkHandler.sendToServer(nbtCompound);
            calculating = true;
        });
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
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseX >= this.x + 8 && mouseY >= this.y + 17 && mouseX <= this.x + 138 && mouseY <= this.y + 142) {
            if (this.prevPageButton.visible && this.prevPageButton.active && amount > 0) {
                if (hasShiftDown()) {
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
                if (hasShiftDown()) {
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
        return super.mouseScrolled(mouseX, mouseY, amount);
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
        if (!calculating || calculateTime > 20 * 10) {
            super.close();
        }
    }

    public List<ItemStack> getDisplayStacks() {
        if (client == null || client.world == null) {
            return new ArrayList<>();
        }
        String text = this.searchField.getText();
        Stream<ItemStack> stackStream = CraftingRecipeTreeManager.getOrCreate(client.world).getCache().keySet().stream().filter(stack -> !stack.isEmpty()).sorted(Comparator.comparing(stack -> Registries.ITEM.getRawId(stack.getItem()))).skip(currentPage * 20L);
        return text.isEmpty() ? stackStream.collect(Collectors.toList()) : stackStream.filter(stack -> StorageAssessorScreenHandler.checkTextFilter(stack, text)).collect(Collectors.toList());
    }

    public void reset() {
        int i = (this.backgroundWidth - 147) / 2;
        int j = (this.backgroundHeight - 166) / 2;
        String string = this.searchField != null ? this.searchField.getText() : "";
        this.searchField = new TextFieldWidget(this.textRenderer, this.x + i + 12, this.y + j - 14, 79, this.textRenderer.fontHeight + 3, Text.translatable("itemGroup.search"));
        this.searchField.setMaxLength(50);
        this.searchField.setVisible(true);
        this.searchField.setEditableColor(0xFFFFFF);
        this.searchField.setText(string);
        this.searchField.setPlaceholder(SEARCH_HINT_TEXT);
        this.searchField.setChangedListener(s -> {
            this.pageCount = (int) Math.ceil((double) this.getDisplayStacks().size() / 20.0);
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
        if (this.nextPageButton != null) {
            this.nextPageButton.visible = this.pageCount > 1 && this.currentPage < this.pageCount - 1;
        }
        if (this.prevPageButton != null) {
            this.prevPageButton.visible = this.pageCount > 1 && this.currentPage > 0;
        }
    }

    @Override
    public void receive(NbtCompound nbt) {
        this.handler.updateResultsFromServer(nbt);
        calculating = false;
        calculateTime = 0;
    }

    @Override
    public void update() {
        updateDisplayStacks();
    }
}
