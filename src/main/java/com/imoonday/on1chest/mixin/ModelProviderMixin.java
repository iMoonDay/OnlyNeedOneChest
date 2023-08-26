package com.imoonday.on1chest.mixin;

import net.minecraft.block.Block;
import net.minecraft.data.client.ModelProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ModelProvider.class)
public class ModelProviderMixin {

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    public boolean isEmpty(List<Block> instance) {
        return true;
    }
}
