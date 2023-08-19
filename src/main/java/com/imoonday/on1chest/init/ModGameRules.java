package com.imoonday.on1chest.init;

import com.imoonday.on1chest.mixin.GameRulesInvoker;
import com.imoonday.on1chest.mixin.IntRuleInvoker;
import net.minecraft.world.GameRules;

public class ModGameRules {

    public static final GameRules.Key<GameRules.IntRule> MAX_MEMORY_RANGE = GameRulesInvoker.invokeRegister("maxMemoryRange", GameRules.Category.MISC, IntRuleInvoker.create(64));

    public static void register() {

    }
}
