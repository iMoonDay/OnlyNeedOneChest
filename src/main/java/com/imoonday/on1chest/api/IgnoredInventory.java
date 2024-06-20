package com.imoonday.on1chest.api;

public interface IgnoredInventory {

    default boolean isIgnored() {
        return true;
    }

    static boolean isIgnored(Object object) {
        return object instanceof IgnoredInventory ignored && ignored.isIgnored();
    }
}
