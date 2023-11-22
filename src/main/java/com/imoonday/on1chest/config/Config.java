package com.imoonday.on1chest.config;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.screen.client.StorageAssessorScreen;
import com.imoonday.on1chest.utils.FavouriteItemStack;
import com.imoonday.on1chest.utils.ItemStackFilter;
import com.imoonday.on1chest.utils.SortComparator;
import com.imoonday.on1chest.utils.Theme;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File file;
    private static Config INSTANCE = new Config();

    private String markItemStackKey = "key.keyboard.left.alt";
    private String takeAllStacksKey = "key.keyboard.space";
    private boolean displayButtonWidgets = true;
    private boolean displayFilterWidgets = true;
    private Set<FavouriteItemStack> favouriteStacks = new HashSet<>();
    private Set<ItemStackFilter> stackFilters = new HashSet<>();
    private SortComparator comparator = SortComparator.ID;
    private boolean reversed = false;
    private boolean noSortWithShift = true;
    private boolean updateOnInsert = true;
    private Theme theme = Theme.VANILLA;
    private boolean scrollOutside = true;
    private int selectedColor = Color.GREEN.getRGB();
    private int favouriteColor = Color.YELLOW.getRGB();
    private boolean displayCountBeforeName = true;

    private boolean randomMode = false;
    private boolean autoSpacing = false;
    private float scale = 1.25f;
    private double interval = 0.75;
    private float rotationSpeed = 1.0f;
    private float rotationDegrees = -1;
    private double itemYOffset = 0.0f;

    private boolean renderTargetItem = true;

    private static void prepareConfigFile() {
        if (file == null) {
            file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "on1chest.json");
        }
    }

    public static void initConfig() {
        load();
    }

    public static void load() {
        prepareConfigFile();

        try {
            if (!file.exists()) {
                save();
            }
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                JsonElement jsonElement = JsonParser.parseReader(br);
                Config config = Config.fromJson(jsonElement.toString());
                if (config != null) {
                    Config.INSTANCE = config;
                    Config.INSTANCE.checkNull();
                } else {
                    save();
                }
            }
        } catch (Exception e) {
            System.err.println("Couldn't load on1chest configuration file; reverting to defaults");
            e.printStackTrace();
            save();
        }
    }

    public static boolean save() {
        prepareConfigFile();
        String json;
        try {
            json = getInstance().toJson();
        } catch (Exception e) {
            json = e.getLocalizedMessage();
        }
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(json);
            return true;
        } catch (Exception e) {
            System.err.println("Couldn't save on1chest configuration file");
            e.printStackTrace();
            return false;
        }
    }

    public static Config getInstance() {
        INSTANCE.checkNull();
        return INSTANCE;
    }

    @Environment(EnvType.CLIENT)
    public static Screen createConfigScreen(Screen parent) {
        if (!OnlyNeedOneChest.clothConfig) {
            return parent;
        }
        try {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("group.on1chest.storages"))
                    .setSavingRunnable(Config::saveAndUpdate);

            ConfigCategory screenSettings = builder.getOrCreateCategory(Text.translatable("config.on1chest.categories.screen"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            screenSettings.addEntry(entryBuilder.startKeyCodeField(Text.translatable("key.on1chest.mark_item_stack"), getInstance().getMarkItemStackKey())
                    .setDefaultValue(InputUtil.fromTranslationKey("key.keyboard.left.alt"))
                    .setKeySaveConsumer(key -> getInstance().setMarkItemStackKey(key))
                    .setTooltip(Text.translatable("config.on1chest.screen.markItemStackKey"))
                    .build());

            screenSettings.addEntry(entryBuilder.startKeyCodeField(Text.translatable("key.on1chest.take_all_stacks"), getInstance().getTakeAllStacksKey())
                    .setDefaultValue(InputUtil.fromTranslationKey("key.keyboard.space"))
                    .setKeySaveConsumer(key -> getInstance().setTakeAllStacksKey(key))
                    .setTooltip(Text.translatable("config.on1chest.screen.takeAllStacksKey"))
                    .build());

            screenSettings.addEntry(entryBuilder.startEnumSelector(Text.translatable("config.on1chest.screen.theme"), Theme.class, getInstance().getTheme())
                    .setDefaultValue(Theme.VANILLA)
                    .setSaveConsumer(theme -> getInstance().setTheme(theme))
                    .setEnumNameProvider(theme -> ((Theme) theme).getLocalizeText())
                    .build());

            screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.screen.displayButtonWidgets"), getInstance().isDisplayButtonWidgets())
                    .setDefaultValue(true)
                    .setSaveConsumer(display -> getInstance().setDisplayButtonWidgets(display))
                    .build());

            screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.screen.displayFilterWidgets"), getInstance().isDisplayFilterWidgets())
                    .setDefaultValue(true)
                    .setSaveConsumer(display -> getInstance().setDisplayFilterWidgets(display))
                    .build());

            screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.screen.noSortWithShift"), getInstance().isNoSortWithShift())
                    .setDefaultValue(true)
                    .setSaveConsumer(noSort -> getInstance().setNoSortWithShift(noSort))
                    .build());

            screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.screen.updateOnInsert"), getInstance().isUpdateOnInsert())
                    .setDefaultValue(true)
                    .setSaveConsumer(update -> getInstance().setUpdateOnInsert(update))
                    .build());

            screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.screen.scrollOutside"), getInstance().isScrollOutside())
                    .setDefaultValue(true)
                    .setSaveConsumer(outside -> getInstance().setScrollOutside(outside))
                    .build());

            screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.screen.displayCountBeforeName"), getInstance().isDisplayCountBeforeName())
                    .setDefaultValue(true)
                    .setSaveConsumer(display -> getInstance().setDisplayCountBeforeName(display))
                    .build());

            screenSettings.addEntry(entryBuilder.startAlphaColorField(Text.translatable("config.on1chest.screen.selectedColor"), getInstance().getSelectedColor())
                    .setDefaultValue(Color.GREEN.getRGB())
                    .setSaveConsumer(color -> getInstance().setSelectedColor(color))
                    .build());

            screenSettings.addEntry(entryBuilder.startAlphaColorField(Text.translatable("config.on1chest.screen.favouriteColor"), getInstance().getFavouriteColor())
                    .setDefaultValue(Color.YELLOW.getRGB())
                    .setSaveConsumer(color -> getInstance().setFavouriteColor(color))
                    .build());

            screenSettings.addEntry(entryBuilder.startStrList(Text.translatable("config.on1chest.screen.favouriteStacks"), getInstance().getFavouriteStacks().stream().map(FavouriteItemStack::toString).collect(Collectors.toList()))
                    .setDefaultValue(new ArrayList<>())
                    .setSaveConsumer(strings -> getInstance().setFavouriteStacks(strings.stream().map(FavouriteItemStack::fromString).filter(Objects::nonNull).collect(Collectors.toSet())))
                    .setAddButtonTooltip(Text.literal("Example:\nminecraft:diamond_sword{Damage:0}\nminecraft:diamond_sword\ndiamond_sword\nminecraft:diamond_sword*\ndiamond_sword*"))
                    .build());

            ConfigCategory rendererSettings = builder.getOrCreateCategory(Text.translatable("config.on1chest.categories.renderer"));

            rendererSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.renderer.randomMode"), getInstance().isRandomMode())
                    .setDefaultValue(false)
                    .setSaveConsumer(random -> getInstance().setRandomMode(random))
                    .build());

            rendererSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.renderer.autoSpacing"), getInstance().isAutoSpacing())
                    .setDefaultValue(false)
                    .setSaveConsumer(autoSpacing -> getInstance().setAutoSpacing(autoSpacing))
                    .setRequirement(() -> !getInstance().isRandomMode())
                    .build());

            rendererSettings.addEntry(entryBuilder.startFloatField(Text.translatable("config.on1chest.renderer.scale"), getInstance().getScale())
                    .setDefaultValue(1.25f)
                    .setSaveConsumer(scale -> getInstance().setScale(scale))
                    .setRequirement(() -> !getInstance().isRandomMode())
                    .setMin(0)
                    .build());

            rendererSettings.addEntry(entryBuilder.startDoubleField(Text.translatable("config.on1chest.renderer.interval"), getInstance().getInterval())
                    .setDefaultValue(0.75)
                    .setSaveConsumer(interval -> getInstance().setInterval(interval))
                    .setRequirement(() -> !getInstance().isRandomMode())
                    .setMin(0)
                    .build());

            rendererSettings.addEntry(entryBuilder.startFloatField(Text.translatable("config.on1chest.renderer.rotationDegrees"), getInstance().getRotationDegrees())
                    .setDefaultValue(-1.0f)
                    .setSaveConsumer(angle -> getInstance().setRotationDegrees(angle))
                    .setRequirement(() -> !getInstance().isRandomMode())
                    .setMin(-1.0f)
                    .setMax(360.0f)
                    .build());

            rendererSettings.addEntry(entryBuilder.startFloatField(Text.translatable("config.on1chest.renderer.rotationSpeed"), getInstance().getRotationSpeed())
                    .setDefaultValue(1.0f)
                    .setSaveConsumer(speed -> getInstance().setRotationSpeed(speed))
                    .setRequirement(() -> !getInstance().isRandomMode() && getInstance().getRotationDegrees() < 0)
                    .setMin(0)
                    .build());

            rendererSettings.addEntry(entryBuilder.startDoubleField(Text.translatable("config.on1chest.renderer.itemYOffset"), getInstance().getItemYOffset())
                    .setDefaultValue(0)
                    .setSaveConsumer(offset -> getInstance().setItemYOffset(offset))
                    .setMin(-1)
                    .setMax(2)
                    .build());

            ConfigCategory blockSettings = builder.getOrCreateCategory(Text.translatable("config.on1chest.categories.block"));

            blockSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.block.renderTargetItem"), getInstance().isRenderTargetItem())
                    .setDefaultValue(true)
                    .setSaveConsumer(render -> getInstance().setRenderTargetItem(render))
                    .build());

            return builder.build();
        } catch (Exception e) {
            return parent;
        }
    }

    public static void saveAndUpdate() {
        if (MinecraftClient.getInstance().currentScreen instanceof StorageAssessorScreen screen) {
            screen.onScreenConfigUpdate(Config.getInstance());
        }
        save();
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    @Nullable
    public static Config fromJson(String json) {
        return GSON.fromJson(json, Config.class);
    }

    public void checkNull() {
        boolean hasNull = false;
        if (this.stackFilters == null) {
            this.stackFilters = new HashSet<>();
            hasNull = true;
        }
        if (this.favouriteStacks == null) {
            this.favouriteStacks = new HashSet<>();
            hasNull = true;
        }
        if (this.comparator == null) {
            this.comparator = SortComparator.ID;
            hasNull = true;
        }
        if (hasNull) {
            saveAndUpdate();
        }
    }

    public boolean isDisplayButtonWidgets() {
        return displayButtonWidgets;
    }

    public void setDisplayButtonWidgets(boolean displayButtonWidgets) {
        this.displayButtonWidgets = displayButtonWidgets;
        saveAndUpdate();
    }

    public boolean isDisplayFilterWidgets() {
        return displayFilterWidgets;
    }

    public void setDisplayFilterWidgets(boolean displayFilterWidgets) {
        this.displayFilterWidgets = displayFilterWidgets;
        saveAndUpdate();
    }

    public ImmutableSet<FavouriteItemStack> getFavouriteStacks() {
        return ImmutableSet.copyOf(favouriteStacks);
    }

    public void setFavouriteStacks(Collection<FavouriteItemStack> favouriteStacks) {
        this.favouriteStacks = new HashSet<>(favouriteStacks);
        saveAndUpdate();
    }

    public void addFavouriteStack(ItemStack stack) {
        this.favouriteStacks.add(new FavouriteItemStack(stack, true));
        saveAndUpdate();
    }

    public void removeFavouriteStack(ItemStack stack) {
        this.favouriteStacks.remove(new FavouriteItemStack(stack, true));
        saveAndUpdate();
    }

    public ImmutableSet<ItemStackFilter> getStackFilters() {
        return ImmutableSet.copyOf(stackFilters);
    }

    public void setStackFilters(Collection<ItemStackFilter> stackFilters) {
        this.stackFilters = new HashSet<>(stackFilters);
        saveAndUpdate();
    }

    public void addStackFilter(ItemStackFilter filter) {
        this.stackFilters.add(filter);
        saveAndUpdate();
    }

    public void removeStackFilter(ItemStackFilter filter) {
        this.stackFilters.remove(filter);
        saveAndUpdate();
    }

    public SortComparator getComparator() {
        return comparator;
    }

    public void setComparator(SortComparator comparator) {
        this.comparator = comparator;
        saveAndUpdate();
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
        saveAndUpdate();
    }

    public boolean isNoSortWithShift() {
        return noSortWithShift;
    }

    public void setNoSortWithShift(boolean noSortWithShift) {
        this.noSortWithShift = noSortWithShift;
        saveAndUpdate();
    }

    public boolean isUpdateOnInsert() {
        return updateOnInsert;
    }

    public void setUpdateOnInsert(boolean updateOnInsert) {
        this.updateOnInsert = updateOnInsert;
        saveAndUpdate();
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
        saveAndUpdate();
    }

    public InputUtil.Key getMarkItemStackKey() {
        return InputUtil.fromTranslationKey(markItemStackKey);
    }

    public void setMarkItemStackKey(InputUtil.Key markItemStackKey) {
        this.markItemStackKey = markItemStackKey.getTranslationKey();
        saveAndUpdate();
    }

    public InputUtil.Key getTakeAllStacksKey() {
        return InputUtil.fromTranslationKey(takeAllStacksKey);
    }

    public void setTakeAllStacksKey(InputUtil.Key takeAllStacksKey) {
        this.takeAllStacksKey = takeAllStacksKey.getTranslationKey();
        saveAndUpdate();
    }

    public boolean isScrollOutside() {
        return scrollOutside;
    }

    public void setScrollOutside(boolean scrollOutside) {
        this.scrollOutside = scrollOutside;
        saveAndUpdate();
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
        saveAndUpdate();
    }

    public int getFavouriteColor() {
        return favouriteColor;
    }

    public void setFavouriteColor(int favouriteColor) {
        this.favouriteColor = favouriteColor;
        saveAndUpdate();
    }

    public boolean isDisplayCountBeforeName() {
        return displayCountBeforeName;
    }

    public void setDisplayCountBeforeName(boolean displayCountBeforeName) {
        this.displayCountBeforeName = displayCountBeforeName;
        saveAndUpdate();
    }

    public boolean isAutoSpacing() {
        return autoSpacing;
    }

    public void setAutoSpacing(boolean autoSpacing) {
        this.autoSpacing = autoSpacing;
        saveAndUpdate();
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        saveAndUpdate();
    }

    public double getInterval() {
        return interval;
    }

    public void setInterval(double interval) {
        this.interval = interval;
        saveAndUpdate();
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
        saveAndUpdate();
    }

    public float getRotationDegrees() {
        return rotationDegrees;
    }

    public void setRotationDegrees(float rotationDegrees) {
        this.rotationDegrees = rotationDegrees;
        saveAndUpdate();
    }

    public boolean isRandomMode() {
        return randomMode;
    }

    public void setRandomMode(boolean randomMode) {
        this.randomMode = randomMode;
        saveAndUpdate();
    }

    public double getItemYOffset() {
        return itemYOffset;
    }

    public void setItemYOffset(double itemYOffset) {
        this.itemYOffset = itemYOffset;
        saveAndUpdate();
    }

    public boolean isRenderTargetItem() {
        return renderTargetItem;
    }

    public void setRenderTargetItem(boolean renderTargetItem) {
        this.renderTargetItem = renderTargetItem;
        saveAndUpdate();
    }
}
