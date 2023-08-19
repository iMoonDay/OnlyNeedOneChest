package com.imoonday.on1chest.utils;

public interface InteractHandler {

    void onInteract(CombinedItemStack itemStack, SlotAction action, boolean shift);

    enum SlotAction {
        LEFT_CLICK, RIGHT_CLICK, TAKE_ALL, QUICK_MOVE, COPY
    }
}
