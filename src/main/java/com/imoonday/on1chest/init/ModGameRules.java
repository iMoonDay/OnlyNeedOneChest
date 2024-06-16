package com.imoonday.on1chest.init;

import net.minecraft.world.GameRules;

public class ModGameRules {

    public static final GameRules.Key<GameRules.IntRule> MAX_MEMORY_RANGE = GameRules.register("maxMemoryRange", GameRules.Category.MISC, GameRules.IntRule.create(64));

    public static void register() {

    }
}
