package com.imoonday.on1chest.filter;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

public class ItemFilterManager {

    private static final Map<Identifier, ItemFilter> FILTERS = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Map<Identifier, List<Identifier>> SUB_FILTERS = Collections.synchronizedMap(new LinkedHashMap<>());

    public static void initFilters() {
        register(new RegularExpressionFilter());
        register(new CustomItemFilter());
        register(new CompositeFilter("item_group"));
        ItemGroups.getGroups().stream()
                  .filter(group -> group.getType() == ItemGroup.Type.CATEGORY)
                  .map(group -> Registries.ITEM_GROUP.getKey(group).map(key -> Map.entry(group, key)).orElse(null))
                  .filter(Objects::nonNull)
                  .sorted(Comparator.comparing(e -> !e.getValue().getValue().getNamespace().equals(Identifier.DEFAULT_NAMESPACE)))
                  .forEach(e -> register(new ItemGroupFilter(e.getValue(), e.getKey().getDisplayName())));
        Arrays.stream(CommonFilters.values()).forEach(ItemFilterManager::register);
    }

    public static List<ItemFilter> getFilters() {
        return List.copyOf(FILTERS.values());
    }

    public static Optional<ItemFilter> getFilter(Identifier id) {
        return Optional.ofNullable(FILTERS.get(id));
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
