package com.imoonday.on1chest.config;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.imoonday.on1chest.client.renderer.DisplayStorageMemoryBlockEntityRenderer;
import com.imoonday.on1chest.filter.*;
import com.imoonday.on1chest.screen.client.StorageAssessorScreen;
import com.imoonday.on1chest.utils.FavouriteItemStack;
import com.imoonday.on1chest.utils.SortComparator;
import com.imoonday.on1chest.utils.Theme;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class Config {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeHierarchyAdapter(ItemFilter.class, new ItemFilterSerialization()).create();
    private static File file;
    private static Config config = new Config();

    private String markItemStackKey = "key.keyboard.left.alt";
    private String takeAllStacksKey = "key.keyboard.space";
    private boolean displayButtonWidgets = true;
    private ItemFilter.DisplayType displayFilterWidgets = ItemFilter.DisplayType.DISPLAY;
    private Set<FavouriteItemStack> favouriteStacks = new HashSet<>();
    private String textFilter = "";
    private ItemFilterList itemFilters = ItemFilterList.create();
    private ItemFilter.FilteringLogic filteringLogic = ItemFilter.FilteringLogic.AND;
    private StickyFilter stickyFilter = StickyFilter.BOTH;
    private SortComparator comparator = SortComparator.ID;
    private boolean reversed = false;
    private boolean noSortWithShift = true;
    private boolean updateOnInsert = true;
    private Theme theme = Theme.VANILLA;
    private boolean scrollOutside = true;
    private int selectedColor = Color.GREEN.getRGB();
    private int favouriteColor = Color.YELLOW.getRGB();
    private boolean displayCountBeforeName = true;
    private boolean resetWithRightClick = true;

    private boolean randomMode = false;
    private boolean autoSpacing = false;
    private float scale = 1.25f;
    private double interval = 0.75;
    private float rotationSpeed = 1.0f;
    private float rotationDegrees = -1;
    private double itemYOffset = 0.0f;

    private boolean renderDisplayItemCount = true;
    private boolean renderDisplayItemInCenter = true;
    private double displayItemYOffset = 0.0f;
    private DisplayStorageMemoryBlockEntityRenderer.CountRenderType displayItemCountRenderType = DisplayStorageMemoryBlockEntityRenderer.CountRenderType.AROUND_BLOCK;
    private boolean renderCountOnlyWhenAimed = false;

    private boolean renderTargetItem = true;

    private static void prepareConfigFile() {
        if (file == null) {
            file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "on1chest.json");
        }
    }

    public static File getFile() {
        prepareConfigFile();
        return file;
    }

    public static void initConfig() {
        load();
        config.checkValid();
        if (config.getItemFilters().checkMissingFilters()) {
            save();
        }
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
                    Config.config = config;
                    Config.config.checkValid();
                } else {
                    save();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't load on1chest configuration file; reverting to defaults", e);
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
            LOGGER.error("Couldn't save on1chest configuration file", e);
            return false;
        }
    }

    public static Config getInstance() {
        return config;
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

    public void checkValid() {
        boolean modified = false;
        if (this.itemFilters == null || this.itemFilters.getFilters().isEmpty()) {
            this.itemFilters = ItemFilterList.create();
            modified = true;
        }
        if (this.favouriteStacks == null) {
            this.favouriteStacks = new HashSet<>();
            modified = true;
        }
        if (this.comparator == null) {
            this.comparator = SortComparator.ID;
            modified = true;
        }
        if (modified) {
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

    public ItemFilter.DisplayType getDisplayFilterWidgets() {
        return displayFilterWidgets;
    }

    public void setDisplayFilterWidgets(ItemFilter.DisplayType displayFilterWidgets) {
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

    public String getTextFilter() {
        if (textFilter == null) {
            textFilter = "";
        }
        return textFilter;
    }

    public void setTextFilter(String textFilter) {
        if (textFilter == null) {
            textFilter = "";
        }
        this.textFilter = textFilter;
        saveAndUpdate();
    }

    public ItemFilterList getItemFilters() {
        return itemFilters;
    }

    public List<ItemFilterWrapper> getItemFilterList() {
        return itemFilters.getFilters();
    }

    public void setItemFilters(ItemFilterList list) {
        this.itemFilters = list;
        saveAndUpdate();
    }

    public void setItemFilterEnabled(ItemFilter filter, boolean enabled) {
        setItemFilterEnabled(filter.getId(), enabled);
    }

    public void setItemFilterEnabled(Identifier id, boolean enabled) {
        boolean modified = this.itemFilters.setFilterEnabled(id, enabled);
        if (modified) {
            saveAndUpdate();
        }
    }

    public void disableAllItemFilters() {
        this.itemFilters.disableAll();
        saveAndUpdate();
    }

    public ItemFilter.FilteringLogic getFilteringLogic() {
        return filteringLogic;
    }

    public void setFilteringLogic(ItemFilter.FilteringLogic filteringLogic) {
        this.filteringLogic = filteringLogic;
    }

    public StickyFilter getStickyFilter() {
        return stickyFilter;
    }

    public void setStickyFilter(StickyFilter stickyFilter) {
        this.stickyFilter = stickyFilter;
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

    public boolean isResetWithRightClick() {
        return resetWithRightClick;
    }

    public void setResetWithRightClick(boolean resetWithRightClick) {
        this.resetWithRightClick = resetWithRightClick;
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

    public boolean isRenderDisplayItemCount() {
        return renderDisplayItemCount;
    }

    public void setRenderDisplayItemCount(boolean renderDisplayItemCount) {
        this.renderDisplayItemCount = renderDisplayItemCount;
        saveAndUpdate();
    }

    public boolean isRenderDisplayItemInCenter() {
        return renderDisplayItemInCenter;
    }

    public void setRenderDisplayItemInCenter(boolean renderDisplayItemInCenter) {
        this.renderDisplayItemInCenter = renderDisplayItemInCenter;
        saveAndUpdate();
    }

    public double getDisplayItemYOffset() {
        return displayItemYOffset;
    }

    public void setDisplayItemYOffset(double displayItemYOffset) {
        this.displayItemYOffset = displayItemYOffset;
        saveAndUpdate();
    }

    public DisplayStorageMemoryBlockEntityRenderer.CountRenderType getDisplayItemCountRenderType() {
        return displayItemCountRenderType;
    }

    public void setDisplayItemCountRenderType(DisplayStorageMemoryBlockEntityRenderer.CountRenderType displayItemCountRenderType) {
        this.displayItemCountRenderType = displayItemCountRenderType;
        saveAndUpdate();
    }

    public boolean isRenderCountOnlyWhenAimed() {
        return renderCountOnlyWhenAimed;
    }

    public void setRenderCountOnlyWhenAimed(boolean renderCountOnlyWhenAimed) {
        this.renderCountOnlyWhenAimed = renderCountOnlyWhenAimed;
        saveAndUpdate();
    }
}
