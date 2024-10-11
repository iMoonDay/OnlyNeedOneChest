package com.imoonday.on1chest.utils;

import java.util.LinkedHashMap;
import java.util.List;

public class MapUtils {

    public static <K, V> void sortMapByList(LinkedHashMap<K, V> map, List<K> list) {
        LinkedHashMap<K, V> tempMap = new LinkedHashMap<>(map);
        map.clear();

        for (K key : list) {
            if (tempMap.containsKey(key)) {
                map.put(key, tempMap.get(key));
            }
        }
    }
}
