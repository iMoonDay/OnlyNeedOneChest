package com.imoonday.on1chest.mixin;

import com.imoonday.on1chest.client.OnlyNeedOneChestClient;
import com.imoonday.on1chest.config.ConfigScreenHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

    @Unique
    private static final String SETTINGS_COMMAND = "/on1chest settings";
    @Shadow
    protected TextFieldWidget chatField;

    protected ChatScreenMixin(Text title) {
        super(title);
        throw new IllegalStateException("Mixin constructor called");
    }

    @ModifyArg(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", ordinal = 1))
    private Screen setScreen(@Nullable Screen screen) {
        return checkCommand(screen);
    }

    @Unique
    protected Screen checkCommand(@Nullable Screen screen) {
        if (OnlyNeedOneChestClient.clothConfig && SETTINGS_COMMAND.equals(this.chatField.getText())) {
            return ConfigScreenHandler.createConfigScreen(getParentScreen());
        }
        return screen;
    }

    @Unique
    protected @Nullable Screen getParentScreen() {
        return null;
    }
}
