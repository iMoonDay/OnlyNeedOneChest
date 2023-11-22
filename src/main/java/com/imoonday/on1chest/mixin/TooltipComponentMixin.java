package com.imoonday.on1chest.mixin;

import com.imoonday.on1chest.client.gui.tooltip.IngredientTooltip;
import com.imoonday.on1chest.client.gui.tooltip.RecipeTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TooltipComponent.class)
public interface TooltipComponentMixin {

    @Inject(method = "of(Lnet/minecraft/client/item/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;", at = @At("HEAD"), cancellable = true)
    private static void of(TooltipData data, CallbackInfoReturnable<TooltipComponent> cir) {
        if (data instanceof RecipeTooltipComponent.RecipeTooltipData recipeTooltipData) {
            cir.setReturnValue(new RecipeTooltipComponent(recipeTooltipData));
        } else if (data instanceof IngredientTooltip tooltip) {
            cir.setReturnValue(tooltip);
        }
    }
}
