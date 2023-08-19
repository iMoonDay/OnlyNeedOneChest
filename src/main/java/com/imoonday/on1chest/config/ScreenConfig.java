package com.imoonday.on1chest.config;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.imoonday.on1chest.client.KeyBindings;
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
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ScreenConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File file;
    private static ScreenConfig INSTANCE = new ScreenConfig();

    private boolean displayButtonWidgets = true;
    private boolean displayFilterWidgets = true;
    private Set<FavouriteItemStack> favouriteStacks = new HashSet<>();
    private Set<ItemStackFilter> stackFilters = new HashSet<>();
    private SortComparator comparator = SortComparator.ID;
    private boolean reversed = false;
    private boolean noSortWithShift = true;
    private boolean updateOnInsert = true;
    private Theme theme = Theme.VANILLA;

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
                ScreenConfig config = ScreenConfig.fromJson(jsonElement.toString());
                if (config != null) {
                    ScreenConfig.INSTANCE = config;
                    ScreenConfig.INSTANCE.checkNull();
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

    public static ScreenConfig getInstance() {
        INSTANCE.checkNull();
        return INSTANCE;
    }

    @Environment(EnvType.CLIENT)
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("On1chest"))
                .setSavingRunnable(ScreenConfig::saveAndUpdate);

        ConfigCategory screenSettings = builder.getOrCreateCategory(Text.literal("Screen Settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        screenSettings.addEntry(entryBuilder.fillKeybindingField(Text.translatable(KeyBindings.markItemStackKey.getTranslationKey()), KeyBindings.markItemStackKey)
                .setTooltip(Text.literal("按住键点击物品进行标记收藏"))
                .build());

        screenSettings.addEntry(entryBuilder.fillKeybindingField(Text.translatable(KeyBindings.takeAllStacksKey.getTranslationKey()), KeyBindings.takeAllStacksKey)
                .setTooltip(Text.literal("按住键点击物品拿取全部"))
                .build());

        screenSettings.addEntry(entryBuilder.startEnumSelector(Text.literal("主题"), Theme.class, getInstance().getTheme())
                .setDefaultValue(Theme.VANILLA)
                .setSaveConsumer(theme -> getInstance().setTheme(theme))
                .setEnumNameProvider(theme -> ((Theme) theme).getLocalizeText())
                .build());

        screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.literal("显示设置按钮"), getInstance().isDisplayButtonWidgets())
                .setDefaultValue(true)
                .setSaveConsumer(display -> getInstance().setDisplayButtonWidgets(display))
                .build());

        screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.literal("显示过滤选项"), getInstance().isDisplayFilterWidgets())
                .setDefaultValue(true)
                .setSaveConsumer(display -> getInstance().setDisplayFilterWidgets(display))
                .build());

        screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.literal("按住Shift时暂停自动排序"), getInstance().isNoSortWithShift())
                .setDefaultValue(true)
                .setSaveConsumer(noSort -> getInstance().setNoSortWithShift(noSort))
                .build());

        screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.literal("存入新物品时强制更新排序"), getInstance().isUpdateOnInsert())
                .setDefaultValue(true)
                .setSaveConsumer(update -> getInstance().setUpdateOnInsert(update))
                .build());

        return builder.build();
    }

    public static void saveAndUpdate() {
        if (MinecraftClient.getInstance().currentScreen instanceof StorageAssessorScreen screen) {
            screen.onScreenConfigUpdate(ScreenConfig.getInstance());
        }
        save();
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    @Nullable
    public static ScreenConfig fromJson(String json) {
        return GSON.fromJson(json, ScreenConfig.class);
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

    public void setFavouriteStacks(Set<FavouriteItemStack> favouriteStacks) {
        this.favouriteStacks = favouriteStacks;
        saveAndUpdate();
    }

    public void addFavouriteStack(ItemStack stack) {
        this.favouriteStacks.add(new FavouriteItemStack(stack));
        saveAndUpdate();
    }

    public void removeFavouriteStack(ItemStack stack) {
        this.favouriteStacks.remove(new FavouriteItemStack(stack));
        saveAndUpdate();
    }

    public ImmutableSet<ItemStackFilter> getStackFilters() {
        return ImmutableSet.copyOf(stackFilters);
    }

    public void setStackFilters(Set<ItemStackFilter> stackFilters) {
        this.stackFilters = stackFilters;
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
}
