package com.imoonday.on1chest.filter;

import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ItemFilterManager {

    private static final Map<Identifier, ItemFilter> FILTERS = new ConcurrentHashMap<>();
    private static final Map<Identifier, List<Identifier>> SUB_FILTERS = new ConcurrentHashMap<>();

    public static void initFilters() {
        Arrays.stream(ItemFilters.values()).forEach(ItemFilterManager::register);
        //TODO 改成子类型
        ItemGroups.getGroupsToDisplay().stream()
                  .map(Registries.ITEM_GROUP::getKey)
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .forEach(key -> register(new ItemGroupFilter(key)));
    }

    public static List<ItemFilter> getFilters() {
        return List.copyOf(FILTERS.values());
    }

    public static ItemFilter getFilter(Identifier id) {
        return FILTERS.get(id);
    }

    public static List<Identifier> getSubFilters(Identifier id) {
        return List.copyOf(SUB_FILTERS.getOrDefault(id, List.of()));
    }

    public static void register(ItemFilter filter) {
        FILTERS.put(filter.getId(), filter);
        if (filter.hasParent()) {
            SUB_FILTERS.computeIfAbsent(filter.getParent(), id -> new ArrayList<>()).add(filter.getId());
        }
    }

    public static boolean unregister(Identifier id) {
        boolean removed = FILTERS.remove(id) != null;
        if (removed) {
            SUB_FILTERS.remove(id);
            SUB_FILTERS.values().forEach(list -> list.removeIf(i -> i.equals(id)));
        }
        return removed;
    }
}
