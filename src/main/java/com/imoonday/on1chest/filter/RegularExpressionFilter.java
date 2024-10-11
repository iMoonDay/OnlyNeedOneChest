package com.imoonday.on1chest.filter;

import com.imoonday.on1chest.OnlyNeedOneChest;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.PatternSyntaxException;

public class RegularExpressionFilter implements ItemFilter {

    public static final Identifier ID = OnlyNeedOneChest.id("regular_expression");
    private static final String EXAMPLE = "example:.*";

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public boolean hasExtraData() {
        return true;
    }

    @Override
    public Supplier<String> getDefaultData() {
        return () -> EXAMPLE;
    }

    @Override
    public Text getTooltip(ItemFilterSettings settings) {
        Object data = settings.getData();
        return data instanceof String dataString && !dataString.isEmpty() ? Text.literal(dataString) : NO_DATA_TEXT;
    }

    @Override
    public boolean hiddenByDefault() {
        return true;
    }

    @Override
    public boolean test(ItemStack stack, @Nullable Object data) {
        String str = data != null ? data.toString() : null;
        if (str == null) return true;
        List<String> strings = List.of(
                Registries.ITEM.getId(stack.getItem()).toString(),
                stack.getName().getString()
        );
        try {
            if (strings.stream().noneMatch(s -> s.matches(str))) {
                return false;
            }
        } catch (PatternSyntaxException ignored) {

        }
        return true;
    }
}
