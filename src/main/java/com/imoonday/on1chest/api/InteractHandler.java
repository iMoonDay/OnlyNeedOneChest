package com.imoonday.on1chest.api;

import com.imoonday.on1chest.utils.CombinedItemStack;
import com.imoonday.on1chest.utils.SlotAction;

public interface InteractHandler {

    void onInteract(CombinedItemStack itemStack, SlotAction action, boolean shift);
}
