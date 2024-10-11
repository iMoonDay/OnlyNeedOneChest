package com.imoonday.on1chest.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListUtils {

    public static List<String> toStringList(Object obj) {
        return obj instanceof List<?> list ? list.stream().map(Object::toString).collect(Collectors.toList()) : new ArrayList<>();
    }

    public static <T> List<T> toList(Object obj, Class<T> clazz) {
        return obj instanceof List<?> list ? list.stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList()) : new ArrayList<>();
    }

    public static boolean isListOfString(Object obj) {
        return obj instanceof List<?> list && list.stream().allMatch(element -> element instanceof String);
    }

    public static boolean isList(Object obj, Class<?> listClass) {
        return obj instanceof List<?> list && list.stream().allMatch(listClass::isInstance);
    }
}
