package com.imoonday.on1chest.utils;

public interface InteractHandler {

    void onInteract(CombinedItemStack itemStack, SlotAction action, boolean rightClick);

    enum SlotAction {
        INSERT_OR_TAKE, MOVE_ALL, QUICK_MOVE, MARK;
    }
}
