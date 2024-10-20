package com.imoonday.on1chest.init;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class ModGameRules {

    public static final GameRules.Key<GameRules.IntRule> MAX_MEMORY_RANGE = GameRuleRegistry.register("maxMemoryRange", GameRules.Category.MISC, GameRuleFactory.createIntRule(64, 0));

    public static void register() {

    }
}
