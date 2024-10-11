package com.imoonday.on1chest.filter;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ItemFilterWrapper {

    private final ItemFilterSettings mainFilter;
    final List<ItemFilterSettings> subFilters;

    public ItemFilterWrapper(ItemFilter mainFilter) {
        this(new ItemFilterSettings(mainFilter));
    }

    public ItemFilterWrapper(ItemFilterSettings mainFilter) {
        this(mainFilter, new ArrayList<>());
    }

    public ItemFilterWrapper(ItemFilterSettings mainFilter, Collection<ItemFilterSettings> subFilters) {
        this.mainFilter = mainFilter;
        this.subFilters = new ArrayList<>(subFilters);
    }

    public ItemFilterSettings getMainFilter() {
        return mainFilter;
    }

    public List<ItemFilterSettings> getSubFilters() {
        return ImmutableList.copyOf(subFilters);
    }

    public void addSubFilter(ItemFilterSettings filter) {
        subFilters.add(filter);
    }

    public void addSubFilter(ItemFilter filter) {
        addSubFilter(new ItemFilterSettings(filter));
    }

    public void addSubFilters(Collection<ItemFilterSettings> filters) {
        filters.forEach(this::addSubFilter);
    }

    public boolean removeSubFilter(Identifier id) {
        return subFilters.removeIf(filter -> filter.is(id));
    }

    public boolean setSubFilterEnabled(Identifier id, boolean enabled) {
        for (ItemFilterSettings filter : subFilters) {
            if (filter.is(id)) {
                filter.setEnabled(enabled);
                return true;
            }
        }
        return false;
    }

    public boolean isEnabled() {
        return mainFilter.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        this.mainFilter.setEnabled(enabled);
    }

    public boolean setSubFilterHide(Identifier id, boolean hide) {
        for (ItemFilterSettings filter : subFilters) {
            if (filter.is(id)) {
                filter.setHide(hide);
                return true;
            }
        }
        return false;
    }

    public boolean isHide() {
        return mainFilter.isHide();
    }

    public void setHide(boolean hide) {
        this.mainFilter.setHide(hide);
    }

    public boolean test(ItemStack stack, ItemFilter.FilteringLogic filteringLogic) {
        if (!isEnabled()) return filteringLogic.isAnd();
        if (!mainFilter.test(stack)) return false;
        Stream<ItemFilterSettings> settingsStream = subFilters.stream().filter(ItemFilterSettings::isEnabled);
        if (filteringLogic.isAnd()) {
            return settingsStream.allMatch(entry -> entry.test(stack));
        } else {
            List<ItemFilterSettings> settings = settingsStream.toList();
            return settings.isEmpty() && !mainFilter.getFilter().alwaysTrue() || settings.stream().anyMatch(entry -> entry.test(stack));
        }
    }

    public ItemFilterWrapper copy() {
        return new ItemFilterWrapper(mainFilter.copy(), subFilters.stream().map(ItemFilterSettings::copy).toList());
    }
}
