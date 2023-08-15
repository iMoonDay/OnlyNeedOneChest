package com.imoonday.on1chest.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerInvoker {

    @Invoker("handleSlotClick")
    boolean invokeHandleSlotClick(PlayerEntity player, ClickType clickType, Slot slot, ItemStack stack, ItemStack cursorStack);
}
