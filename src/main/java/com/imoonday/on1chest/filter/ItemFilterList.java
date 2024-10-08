package com.imoonday.on1chest.filter;

import com.mojang.logging.LogUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.*;

public class ItemFilterList {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<ItemFilterData> filters;

    protected ItemFilterList(Collection<ItemFilterData> filters) {
        this.filters = List.copyOf(filters);
    }

    public List<ItemFilterData> getFilters() {
        return filters;
    }

    public boolean isFilterEnabled(Identifier id) {
        for (ItemFilterData info : this.filters) {
            if (info.getMainFilter().is(id)) {
                return info.isEnabled();
            }
            if (info.getSubFilters().stream().anyMatch(e -> e.is(id) && e.isEnabled())) {
                return true;
            }
        }
        return false;
    }

    public boolean isFilterEnabled(ItemFilter filter) {
        return isFilterEnabled(filter.getId());
    }

    public boolean setFilterEnabled(Identifier id, boolean enabled) {
        for (ItemFilterData info : this.filters) {
            if (info.getMainFilter().is(id)) {
                info.setEnabled(enabled);
                return true;
            }
            if (info.setSubFilterEnabled(id, enabled)) {
                return true;
            }
        }
        return false;
    }

    public boolean setFilterEnabled(ItemFilter filter, boolean enabled) {
        return setFilterEnabled(filter.getId(), enabled);
    }

    public boolean isFilterHide(Identifier id) {
        for (ItemFilterData info : this.filters) {
            if (info.getMainFilter().is(id)) {
                return info.isHide();
            }
            if (info.getSubFilters().stream().anyMatch(e -> e.is(id) && e.isHide())) {
                return true;
            }
        }
        return false;
    }

    public boolean isFilterHide(ItemFilter filter) {
        return isFilterHide(filter.getId());
    }

    public boolean setFilterHide(Identifier id, boolean hide) {
        for (ItemFilterData info : this.filters) {
            if (info.getMainFilter().is(id)) {
                info.setHide(hide);
                return true;
            }
            if (info.setSubFilterHide(id, hide)) {
                return true;
            }
        }
        return false;
    }

    public boolean setFilterHide(ItemFilter filter, boolean hide) {
        return setFilterHide(filter.getId(), hide);
    }

    public boolean test(ItemStack stack) {
        return this.filters.stream().allMatch(info -> info.test(stack));
    }

    public static ItemFilterList create() {
        Map<Identifier, ItemFilterData> filterMap = new HashMap<>();
        List<ItemFilter> subFilters = new ArrayList<>();

        for (ItemFilter filter : ItemFilterManager.getFilters()) {
            ItemFilterInstance filterInstance = new ItemFilterInstance(filter, false, filter.hiddenByDefault());
            if (filter.hasParent()) {
                Identifier parent = filter.getParent();
                if (filterMap.containsKey(parent)) {
                    filterMap.get(parent).addSubFilter(filterInstance);
                } else {
                    subFilters.add(filter);
                }
                continue;
            }
            filterMap.put(filter.getId(), new ItemFilterData(filterInstance));
        }

        for (ItemFilter filter : subFilters) {
            Identifier parent = filter.getParent();
            if (!filterMap.containsKey(parent)) {
                LOGGER.warn("Parent filter {} not found for filter {}", parent, filter);
                continue;
            }
            filterMap.get(parent).addSubFilter(new ItemFilterInstance(filter, false, filter.hiddenByDefault()));
        }

        return new ItemFilterList(filterMap.values());
    }
}
