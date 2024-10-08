package com.imoonday.on1chest.filter;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemFilterData {

    private final ItemFilterInstance mainFilter;
    private final List<ItemFilterInstance> subFilters;

    public ItemFilterData(ItemFilter mainFilter) {
        this(new ItemFilterInstance(mainFilter));
    }

    public ItemFilterData(ItemFilterInstance mainFilter) {
        this(mainFilter, new ArrayList<>());
    }

    public ItemFilterData(ItemFilterInstance mainFilter, Collection<ItemFilterInstance> subFilters) {
        this.mainFilter = mainFilter;
        this.subFilters = new ArrayList<>(subFilters);
    }

    public ItemFilterInstance getMainFilter() {
        return mainFilter;
    }

    public List<ItemFilterInstance> getSubFilters() {
        return ImmutableList.copyOf(subFilters);
    }

    public void addSubFilter(ItemFilterInstance filter) {
        subFilters.add(filter);
    }

    public void addSubFilter(ItemFilter filter) {
        addSubFilter(new ItemFilterInstance(filter));
    }

    public void addSubFilters(Collection<ItemFilterInstance> filters) {
        filters.forEach(this::addSubFilter);
    }

    public boolean removeSubFilter(Identifier id) {
        return subFilters.removeIf(filter -> filter.is(id));
    }

    public boolean setSubFilterEnabled(Identifier id, boolean enabled) {
        for (ItemFilterInstance filter : subFilters) {
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
        for (ItemFilterInstance filter : subFilters) {
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

    public boolean test(ItemStack stack) {
        return !isEnabled() || mainFilter.test(stack) && subFilters.stream().filter(ItemFilterInstance::isEnabled).allMatch(entry -> entry.test(stack));
    }

    public ItemFilterData copy() {
        return new ItemFilterData(mainFilter.copy(), subFilters.stream().map(ItemFilterInstance::copy).toList());
    }
}
