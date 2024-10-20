package com.imoonday.on1chest.filter;

import com.imoonday.on1chest.config.Config;
import com.mojang.logging.LogUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.*;

public class ItemFilterList {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<ItemFilterWrapper> filters;

    protected ItemFilterList(Collection<ItemFilterWrapper> filters) {
        this.filters = new ArrayList<>(filters);
    }

    public List<ItemFilterWrapper> getFilters() {
        return List.copyOf(filters);
    }

    public List<ItemFilterSettings> getSortedFilters() {
        return filters.stream().map(wrapper -> {
            List<ItemFilterSettings> settingsList = new ArrayList<>(wrapper.getSubFilters());
            settingsList.add(0, wrapper.getMainFilter());
            return settingsList;
        }).flatMap(Collection::stream).toList();
    }

    public boolean checkMissingFilters() {
        ItemFilterList defaultList = create();
        List<ItemFilterSettings> defaultSortedFilters = defaultList.getSortedFilters();
        List<ItemFilterSettings> sortedFilters = this.getSortedFilters();
        if (defaultSortedFilters.size() != sortedFilters.size() || defaultSortedFilters.stream().anyMatch(s -> sortedFilters.stream().noneMatch(s1 -> s1.is(s.getFilter())))) {
            LOGGER.warn("Missing filters detected, resetting to default...");
            this.filters.clear();
            this.filters.addAll(defaultList.filters);
            return true;
        }
        return false;
    }

    public boolean isFilterEnabled(Identifier id) {
        for (ItemFilterWrapper wrapper : this.filters) {
            if (wrapper.getMainFilter().is(id)) {
                return wrapper.isEnabled();
            }
            if (wrapper.getSubFilters().stream().anyMatch(e -> e.is(id) && e.isEnabled())) {
                return true;
            }
        }
        return false;
    }

    public boolean isFilterEnabled(ItemFilter filter) {
        return isFilterEnabled(filter.getId());
    }

    public boolean setFilterEnabled(Identifier id, boolean enabled) {
        for (ItemFilterWrapper wrapper : this.filters) {
            if (wrapper.getMainFilter().is(id)) {
                wrapper.setEnabled(enabled);
                return true;
            }
            if (wrapper.setSubFilterEnabled(id, enabled)) {
                return true;
            }
        }
        return false;
    }

    public boolean setFilterEnabled(ItemFilter filter, boolean enabled) {
        return setFilterEnabled(filter.getId(), enabled);
    }

    public void disableAll() {
        for (ItemFilterWrapper wrapper : this.filters) {
            wrapper.setEnabled(false);
            wrapper.getSubFilters().forEach(s -> s.setEnabled(false));
        }
    }

    public boolean isFilterHide(Identifier id) {
        for (ItemFilterWrapper wrapper : this.filters) {
            if (wrapper.getMainFilter().is(id)) {
                return wrapper.isHide();
            }
            if (wrapper.getSubFilters().stream().anyMatch(e -> e.is(id) && e.isHide())) {
                return true;
            }
        }
        return false;
    }

    public boolean isFilterHide(ItemFilter filter) {
        return isFilterHide(filter.getId());
    }

    public boolean setFilterHide(Identifier id, boolean hide) {
        for (ItemFilterWrapper wrapper : this.filters) {
            if (wrapper.getMainFilter().is(id)) {
                wrapper.setHide(hide);
                return true;
            }
            if (wrapper.setSubFilterHide(id, hide)) {
                return true;
            }
        }
        return false;
    }

    public boolean setFilterHide(ItemFilter filter, boolean hide) {
        return setFilterHide(filter.getId(), hide);
    }

    public boolean test(ItemStack stack) {
        ItemFilter.FilteringLogic filteringLogic = Config.getInstance().getFilteringLogic();
        List<ItemFilterWrapper> wrappers = this.filters.stream().filter(ItemFilterWrapper::isEnabled).toList();
        return wrappers.isEmpty() || (filteringLogic.isAnd() ? wrappers.stream().allMatch(wrapper -> wrapper.test(stack, filteringLogic)) : wrappers.stream().anyMatch(wrapper -> wrapper.test(stack, filteringLogic)));
    }

    public boolean moveForward(ItemFilterSettings settings) {
        List<ItemFilterWrapper> wrappers = this.filters;
        if (settings.getFilter().hasParent()) {
            for (ItemFilterWrapper wrapper : wrappers) {
                List<ItemFilterSettings> subFilters = wrapper.subFilters;
                for (int i = 1; i < subFilters.size(); i++) {
                    ItemFilterSettings subFilter = subFilters.get(i);
                    if (subFilter == settings) {
                        Collections.swap(subFilters, i, i - 1);
                        return true;
                    }
                }
            }
        } else {
            for (int i = 1; i < wrappers.size(); i++) {
                ItemFilterWrapper wrapper = wrappers.get(i);
                if (wrapper.getMainFilter() == settings) {
                    Collections.swap(wrappers, i, i - 1);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean moveBackward(ItemFilterSettings settings) {
        List<ItemFilterWrapper> wrappers = this.filters;
        if (settings.getFilter().hasParent()) {
            for (ItemFilterWrapper wrapper : wrappers) {
                List<ItemFilterSettings> subFilters = wrapper.subFilters;
                for (int i = 0; i < subFilters.size() - 1; i++) {
                    ItemFilterSettings subFilter = subFilters.get(i);
                    if (subFilter == settings) {
                        Collections.swap(subFilters, i, i + 1);
                        return true;
                    }
                }
            }
        } else {
            for (int i = 0; i < wrappers.size() - 1; i++) {
                ItemFilterWrapper wrapper = wrappers.get(i);
                if (wrapper.getMainFilter() == settings) {
                    Collections.swap(wrappers, i, i + 1);
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemFilterList create() {
        Map<Identifier, ItemFilterWrapper> filterMap = new LinkedHashMap<>();
        List<ItemFilter> subFilters = new ArrayList<>();

        for (ItemFilter filter : ItemFilterManager.getFilters()) {
            ItemFilterSettings settings = new ItemFilterSettings(filter, false, filter.hiddenByDefault());
            if (filter.hasParent()) {
                Identifier parent = filter.getParent();
                if (filterMap.containsKey(parent)) {
                    filterMap.get(parent).addSubFilter(settings);
                } else {
                    subFilters.add(filter);
                }
                continue;
            }
            filterMap.put(filter.getId(), new ItemFilterWrapper(settings));
        }

        for (ItemFilter filter : subFilters) {
            Identifier parent = filter.getParent();
            if (!filterMap.containsKey(parent)) {
                LOGGER.warn("Parent filter {} not found for filter {}", parent, filter);
                continue;
            }
            filterMap.get(parent).addSubFilter(new ItemFilterSettings(filter, false, filter.hiddenByDefault()));
        }

        return new ItemFilterList(filterMap.values());
    }
}
