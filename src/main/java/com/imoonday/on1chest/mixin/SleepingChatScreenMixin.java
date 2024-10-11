package com.imoonday.on1chest.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SleepingChatScreen.class)
public class SleepingChatScreenMixin extends ChatScreenMixin {

    protected SleepingChatScreenMixin(Text title) {
        super(title);
    }

    @ModifyArg(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private Screen setScreen(@Nullable Screen screen) {
        return checkCommand(screen);
    }

    @Override
    protected @Nullable Screen getParentScreen() {
        return this.client != null ? this.client.currentScreen : null;
    }
}
