package com.imoonday.on1chest.config;

import com.imoonday.on1chest.client.OnlyNeedOneChestClient;
import com.imoonday.on1chest.filter.ItemFilter;
import com.imoonday.on1chest.filter.ItemFilterSettings;
import com.imoonday.on1chest.filter.ItemFilterWrapper;
import com.imoonday.on1chest.filter.StickyFilter;
import com.imoonday.on1chest.utils.FavouriteItemStack;
import com.imoonday.on1chest.utils.ListUtils;
import com.imoonday.on1chest.utils.Theme;
import com.mojang.logging.LogUtils;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConfigScreenHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Environment(EnvType.CLIENT)
    public static Screen createConfigScreen(Screen parent) {
        if (!OnlyNeedOneChestClient.clothConfig) {
            return parent;
        }
        try {
            ConfigBuilder builder = ConfigBuilder.create()
                                                 .setParentScreen(parent)
                                                 .setTitle(Text.translatable("group.on1chest.storages"))
                                                 .setSavingRunnable(Config::saveAndUpdate);

            ConfigCategory screenSettings = builder.getOrCreateCategory(Text.translatable("config.on1chest.categories.screen"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            screenSettings.addEntry(entryBuilder.startKeyCodeField(Text.translatable("key.on1chest.mark_item_stack"), Config.getInstance().getMarkItemStackKey())
                                                .setDefaultValue(InputUtil.fromTranslationKey("key.keyboard.left.alt"))
                                                .setKeySaveConsumer(key -> Config.getInstance().setMarkItemStackKey(key))
                                                .setTooltip(Text.translatable("config.on1chest.screen.markItemStackKey"))
                                                .build());

            screenSettings.addEntry(entryBuilder.startKeyCodeField(Text.translatable("key.on1chest.take_all_stacks"), Config.getInstance().getTakeAllStacksKey())
                                                .setDefaultValue(InputUtil.fromTranslationKey("key.keyboard.space"))
                                                .setKeySaveConsumer(key -> Config.getInstance().setTakeAllStacksKey(key))
                                                .setTooltip(Text.translatable("config.on1chest.screen.takeAllStacksKey"))
                                                .build());

            screenSettings.addEntry(entryBuilder.startEnumSelector(Text.translatable("config.on1chest.screen.theme"), Theme.class, Config.getInstance().getTheme())
                                                .setDefaultValue(Theme.VANILLA)
                                                .setSaveConsumer(theme -> Config.getInstance().setTheme(theme))
                                                .setEnumNameProvider(theme -> ((Theme) theme).getLocalizeText())
                                                .build());

            screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.screen.displayButtonWidgets"), Config.getInstance().isDisplayButtonWidgets())
                                                .setDefaultValue(true)
                                                .setSaveConsumer(display -> Config.getInstance().setDisplayButtonWidgets(display))
                                                .build());

            screenSettings.addEntry(entryBuilder.startEnumSelector(Text.translatable("config.on1chest.screen.displayFilterWidgets"), ItemFilter.DisplayType.class, Config.getInstance().getDisplayFilterWidgets())
                                                .setDefaultValue(ItemFilter.DisplayType.DISPLAY)
                                                .setSaveConsumer(display -> Config.getInstance().setDisplayFilterWidgets(display))
                                                .setEnumNameProvider(value -> ((ItemFilter.DisplayType) value).getDisplayName())
                                                .build());

            screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.screen.noSortWithShift"), Config.getInstance().isNoSortWithShift())
                                                .setDefaultValue(true)
                                                .setSaveConsumer(noSort -> Config.getInstance().setNoSortWithShift(noSort))
                                                .build());

            screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.screen.updateOnInsert"), Config.getInstance().isUpdateOnInsert())
                                                .setDefaultValue(true)
                                                .setSaveConsumer(update -> Config.getInstance().setUpdateOnInsert(update))
                                                .build());

            screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.screen.scrollOutside"), Config.getInstance().isScrollOutside())
                                                .setDefaultValue(true)
                                                .setSaveConsumer(outside -> Config.getInstance().setScrollOutside(outside))
                                                .build());

            screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.screen.displayCountBeforeName"), Config.getInstance().isDisplayCountBeforeName())
                                                .setDefaultValue(true)
                                                .setSaveConsumer(display -> Config.getInstance().setDisplayCountBeforeName(display))
                                                .build());

            screenSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.screen.resetWithRightClick"), Config.getInstance().isResetWithRightClick())
                                                .setDefaultValue(true)
                                                .setSaveConsumer(reset -> Config.getInstance().setResetWithRightClick(reset))
                                                .build());

            screenSettings.addEntry(entryBuilder.startAlphaColorField(Text.translatable("config.on1chest.screen.selectedColor"), Config.getInstance().getSelectedColor())
                                                .setDefaultValue(Color.GREEN.getRGB())
                                                .setSaveConsumer(color -> Config.getInstance().setSelectedColor(color))
                                                .build());

            screenSettings.addEntry(entryBuilder.startAlphaColorField(Text.translatable("config.on1chest.screen.favouriteColor"), Config.getInstance().getFavouriteColor())
                                                .setDefaultValue(Color.YELLOW.getRGB())
                                                .setSaveConsumer(color -> Config.getInstance().setFavouriteColor(color))
                                                .build());

            screenSettings.addEntry(entryBuilder.startStrList(Text.translatable("config.on1chest.screen.favouriteStacks"), Config.getInstance().getFavouriteStacks().stream().map(FavouriteItemStack::toString).collect(Collectors.toList()))
                                                .setDefaultValue(new ArrayList<>())
                                                .setSaveConsumer(strings -> Config.getInstance().setFavouriteStacks(strings.stream().map(FavouriteItemStack::fromString).filter(Objects::nonNull).collect(Collectors.toSet())))
                                                .setAddButtonTooltip(Text.literal("Example:\nminecraft:diamond_sword{Damage:0}\nminecraft:diamond_sword\ndiamond_sword\nminecraft:diamond_sword*\ndiamond_sword*"))
                                                .build());

            ConfigCategory rendererSettings = builder.getOrCreateCategory(Text.translatable("config.on1chest.categories.renderer"));

            rendererSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.renderer.randomMode"), Config.getInstance().isRandomMode())
                                                  .setDefaultValue(false)
                                                  .setSaveConsumer(random -> Config.getInstance().setRandomMode(random))
                                                  .build());

            rendererSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.renderer.autoSpacing"), Config.getInstance().isAutoSpacing())
                                                  .setDefaultValue(false)
                                                  .setSaveConsumer(autoSpacing -> Config.getInstance().setAutoSpacing(autoSpacing))
                                                  .setRequirement(() -> !Config.getInstance().isRandomMode())
                                                  .build());

            rendererSettings.addEntry(entryBuilder.startFloatField(Text.translatable("config.on1chest.renderer.scale"), Config.getInstance().getScale())
                                                  .setDefaultValue(1.25f)
                                                  .setSaveConsumer(scale -> Config.getInstance().setScale(scale))
                                                  .setRequirement(() -> !Config.getInstance().isRandomMode())
                                                  .setMin(0)
                                                  .build());

            rendererSettings.addEntry(entryBuilder.startDoubleField(Text.translatable("config.on1chest.renderer.interval"), Config.getInstance().getInterval())
                                                  .setDefaultValue(0.75)
                                                  .setSaveConsumer(interval -> Config.getInstance().setInterval(interval))
                                                  .setRequirement(() -> !Config.getInstance().isRandomMode())
                                                  .setMin(0)
                                                  .build());

            rendererSettings.addEntry(entryBuilder.startFloatField(Text.translatable("config.on1chest.renderer.rotationDegrees"), Config.getInstance().getRotationDegrees())
                                                  .setDefaultValue(-1.0f)
                                                  .setSaveConsumer(angle -> Config.getInstance().setRotationDegrees(angle))
                                                  .setRequirement(() -> !Config.getInstance().isRandomMode())
                                                  .setMin(-1.0f)
                                                  .setMax(360.0f)
                                                  .build());

            rendererSettings.addEntry(entryBuilder.startFloatField(Text.translatable("config.on1chest.renderer.rotationSpeed"), Config.getInstance().getRotationSpeed())
                                                  .setDefaultValue(1.0f)
                                                  .setSaveConsumer(speed -> Config.getInstance().setRotationSpeed(speed))
                                                  .setRequirement(() -> !Config.getInstance().isRandomMode() && Config.getInstance().getRotationDegrees() < 0)
                                                  .setMin(0)
                                                  .build());

            rendererSettings.addEntry(entryBuilder.startDoubleField(Text.translatable("config.on1chest.renderer.itemYOffset"), Config.getInstance().getItemYOffset())
                                                  .setDefaultValue(0)
                                                  .setSaveConsumer(offset -> Config.getInstance().setItemYOffset(offset))
                                                  .setMin(-1)
                                                  .setMax(2)
                                                  .build());

            ConfigCategory blockSettings = builder.getOrCreateCategory(Text.translatable("config.on1chest.categories.block"));

            blockSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.block.renderTargetItem"), Config.getInstance().isRenderTargetItem())
                                               .setDefaultValue(true)
                                               .setSaveConsumer(render -> Config.getInstance().setRenderTargetItem(render))
                                               .build());

            blockSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.block.renderDisplayItemCount"), Config.getInstance().isRenderDisplayItemCount())
                                               .setDefaultValue(true)
                                               .setSaveConsumer(render -> Config.getInstance().setRenderDisplayItemCount(render))
                                               .build());

            blockSettings.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.on1chest.block.renderDisplayItemInCenter"), Config.getInstance().isRenderDisplayItemInCenter())
                                               .setDefaultValue(false)
                                               .setSaveConsumer(render -> Config.getInstance().setRenderDisplayItemInCenter(render))
                                               .build());

            ConfigCategory filterSettings = builder.getOrCreateCategory(Text.translatable("config.on1chest.categories.filter"));

            filterSettings.addEntry(entryBuilder.startStrField(Text.translatable("config.on1chest.filter.text_filter"), Config.getInstance().getTextFilter())
                                                .setDefaultValue("")
                                                .setSaveConsumer(text -> Config.getInstance().setTextFilter(text))
                                                .build());

            filterSettings.addEntry(entryBuilder.startEnumSelector(Text.translatable("config.on1chest.filter.filtering_logic"), ItemFilter.FilteringLogic.class, Config.getInstance().getFilteringLogic())
                                                .setDefaultValue(ItemFilter.FilteringLogic.AND)
                                                .setSaveConsumer(logic -> Config.getInstance().setFilteringLogic(logic))
                                                .setEnumNameProvider(logic -> ((ItemFilter.FilteringLogic) logic).getDisplayName())
                                                .build());

            filterSettings.addEntry(entryBuilder.startEnumSelector(Text.translatable("config.on1chest.filter.sticky_filter"), StickyFilter.class, Config.getInstance().getStickyFilter())
                                                .setDefaultValue(StickyFilter.BOTH)
                                                .setSaveConsumer(sticky -> Config.getInstance().setStickyFilter(sticky))
                                                .setEnumNameProvider(sticky -> ((StickyFilter) sticky).getDisplayName())
                                                .build());

            for (ItemFilterWrapper data : Config.getInstance().getItemFilterList()) {
                ItemFilterSettings filter = data.getMainFilter();
                SubCategoryBuilder filterBuilder = createItemFilterCategory(entryBuilder, filter);
                for (ItemFilterSettings subFilter : data.getSubFilters()) {
                    SubCategoryBuilder subFilterBuilder = createItemFilterCategory(entryBuilder, subFilter);
                    filterBuilder.add(subFilterBuilder.build());
                }
                filterSettings.addEntry(filterBuilder.build());
            }

            return builder.build();
        } catch (Exception e) {
            LOGGER.error("Failed to create config screen", e);
            return parent;
        }
    }

    private static SubCategoryBuilder createItemFilterCategory(ConfigEntryBuilder entryBuilder, ItemFilterSettings settings) {
        ItemFilter filter = settings.getFilter();
        SubCategoryBuilder entries = entryBuilder.startSubCategory(filter.getDisplayName());
        addEntries(entryBuilder, settings, entries);
        return entries;
    }

    private static void addEntries(ConfigEntryBuilder entryBuilder, ItemFilterSettings settings, SubCategoryBuilder entries) {
        ItemFilter filter = settings.getFilter();
        entries.add(entryBuilder.startBooleanToggle(Text.translatable("filter.on1chest.enabled"), settings.isEnabled())
                                .setDefaultValue(false)
                                .setSaveConsumer(settings::setEnabled)
                                .build());
        entries.add(entryBuilder.startBooleanToggle(Text.translatable("filter.on1chest.hide"), settings.isHide())
                                .setDefaultValue(false)
                                .setSaveConsumer(settings::setHide)
                                .build());

        if (filter.hasExtraData()) {
            Object data = settings.getData();
            Text tooltip = filter.getDataTooltip();
            Optional<Text[]> optTooltips = Optional.ofNullable(!tooltip.getString().isEmpty() ? new Text[]{tooltip} : null);
            Object defaultData = filter.getDefaultData().get();

            if (defaultData instanceof String stringDefaultData) {
                entries.add(entryBuilder.startTextField(filter.getDataDisplayName(), data instanceof String s ? s : "")
                                        .setDefaultValue(stringDefaultData)
                                        .setSaveConsumer(settings::setData)
                                        .setTooltip(optTooltips)
                                        .build());
            } else if (defaultData instanceof Integer integerDefaultData) {
                entries.add(entryBuilder.startIntField(filter.getDataDisplayName(), data instanceof Integer i ? i : 0)
                                        .setDefaultValue(integerDefaultData)
                                        .setSaveConsumer(settings::setData)
                                        .setTooltip(optTooltips)
                                        .build());
            } else if (defaultData instanceof Float floatDefaultData) {
                entries.add(entryBuilder.startFloatField(filter.getDataDisplayName(), data instanceof Float f ? f : 0)
                                        .setDefaultValue(floatDefaultData)
                                        .setSaveConsumer(settings::setData)
                                        .setTooltip(optTooltips)
                                        .build());
            } else if (defaultData instanceof Double doubleDefaultData) {
                entries.add(entryBuilder.startDoubleField(filter.getDataDisplayName(), data instanceof Double d ? d : 0)
                                        .setDefaultValue(doubleDefaultData)
                                        .setSaveConsumer(settings::setData)
                                        .setTooltip(optTooltips)
                                        .build());
            } else if (defaultData instanceof Boolean booleanDefaultData) {
                entries.add(entryBuilder.startBooleanToggle(filter.getDataDisplayName(), data instanceof Boolean b ? b : false)
                                        .setDefaultValue(booleanDefaultData)
                                        .setSaveConsumer(settings::setData)
                                        .setTooltip(optTooltips)
                                        .build());
            } else if (defaultData instanceof Long longDefaultData) {
                entries.add(entryBuilder.startLongField(filter.getDataDisplayName(), data instanceof Long l ? l : 0)
                                        .setDefaultValue(longDefaultData)
                                        .setSaveConsumer(settings::setData)
                                        .setTooltip(optTooltips)
                                        .build());
            } else if (ListUtils.isListOfString(defaultData)) {
                entries.add(entryBuilder.startStrList(filter.getDataDisplayName(), ListUtils.toStringList(data))
                                        .setDefaultValue(ListUtils.toStringList(defaultData))
                                        .setSaveConsumer(settings::setData)
                                        .setTooltip(optTooltips)
                                        .build());
            } else if (ListUtils.isList(defaultData, Integer.class)) {
                entries.add(entryBuilder.startIntList(filter.getDataDisplayName(), ListUtils.toList(data, Integer.class))
                                        .setDefaultValue(ListUtils.toList(defaultData, Integer.class))
                                        .setSaveConsumer(settings::setData)
                                        .setTooltip(optTooltips)
                                        .build());
            } else if (ListUtils.isList(defaultData, Long.class)) {
                entries.add(entryBuilder.startLongList(filter.getDataDisplayName(), ListUtils.toList(data, Long.class))
                                        .setDefaultValue(ListUtils.toList(defaultData, Long.class))
                                        .setSaveConsumer(settings::setData)
                                        .setTooltip(optTooltips)
                                        .build());
            } else if (ListUtils.isList(defaultData, Float.class)) {
                entries.add(entryBuilder.startFloatList(filter.getDataDisplayName(), ListUtils.toList(data, Float.class))
                                        .setDefaultValue(ListUtils.toList(defaultData, Float.class))
                                        .setSaveConsumer(settings::setData)
                                        .setTooltip(optTooltips)
                                        .build());
            } else if (ListUtils.isList(defaultData, Double.class)) {
                entries.add(entryBuilder.startDoubleList(filter.getDataDisplayName(), ListUtils.toList(data, Double.class))
                                        .setDefaultValue(ListUtils.toList(defaultData, Double.class))
                                        .setSaveConsumer(settings::setData)
                                        .setTooltip(optTooltips)
                                        .build());
            } else {
                LOGGER.warn("Unsupported default data type: {}", defaultData.getClass());
            }
        }
    }
}
