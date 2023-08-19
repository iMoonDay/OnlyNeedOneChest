package com.imoonday.on1chest.mixin;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.IntRule.class)
public interface IntRuleInvoker {

    @Invoker("create")
    static GameRules.Type<GameRules.IntRule> create(int initialValue) {
        throw new AssertionError();
    }
}
