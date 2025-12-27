package com.dracolich777.afterlifeentombed.init;

import net.minecraft.world.level.GameRules;

/**
 * Custom game rules for the Afterlife Entombed mod
 */
public class ModGameRules {
    
    /**
     * Game rule to enable/disable divine boons and curses
     * When false, boons and curses will not apply their effects
     */
    public static GameRules.Key<GameRules.BooleanValue> RULE_ENABLE_BOONS_AND_CURSES;

    /**
     * Register custom game rules
     * Must be called during mod initialization
     */
    public static void register() {
        RULE_ENABLE_BOONS_AND_CURSES = GameRules.register(
            "doBoonsAndCurses",
            GameRules.Category.UPDATES,
            GameRules.BooleanValue.create(false)
        );
    }
}
