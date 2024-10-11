package com.imoonday.on1chest.filter;

import com.imoonday.on1chest.OnlyNeedOneChest;
import com.imoonday.on1chest.utils.ListUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CustomItemFilter implements ItemFilter {

    public static final Identifier ID = OnlyNeedOneChest.id("custom_item");
    private static final Text DATA_TOOLTIP = Text.translatable("filter.on1chest.custom_item.data.tooltip");
    private static final ArrayList<String> EXAMPLE = new ArrayList<>(List.of("minecraft:example"));

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean hasExtraData() {
        return true;
    }

    @Override
    public Supplier<List<String>> getDefaultData() {
        return () -> EXAMPLE;
    }

    @Override
    public Text getTooltip(ItemFilterSettings settings) {
        Object data = settings.getData();
        List<String> list = ListUtils.toStringList(data);
        if (list == null || list.isEmpty()) return NO_DATA_TEXT;
        String validIds = list.stream().map(Identifier::tryParse)
                              .filter(Objects::nonNull)
                              .map(Registries.ITEM::getOrEmpty)
                              .filter(Optional::isPresent)
                              .map(Optional::get)
                              .filter(item -> item != Items.AIR)
                              .map(item -> item.getName().getString())
                              .collect(Collectors.collectingAndThen(Collectors.toList(), ids -> {
                                  int size = ids.size();
                                  if (size > 9) {
                                      return String.join("\n", ids.subList(0, 9)) + "\n...(+" + (size - 9) + ")";
                                  } else {
                                      return String.join("\n", ids);
                                  }
                              }));
        return Text.literal(validIds);
    }

    @Override
    public Text getDataTooltip() {
        return DATA_TOOLTIP;
    }

    @Override
    public boolean hiddenByDefault() {
        return true;
    }

    @Override
    public boolean test(ItemStack stack, @Nullable Object data) {
        List<String> list = ListUtils.toStringList(data);
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        return list == null || list.stream().map(Identifier::tryParse)
                                   .filter(Objects::nonNull)
                                   .anyMatch(itemId::equals);
    }
}
