package com.dracolich777.afterlifeentombed.events;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class ScorpionWhipHandler {

    private static final Map<Player, Integer> damageBoostTicks = new HashMap<>();

    public static void setDamageBoost(Player player, int ticks) {
        damageBoostTicks.put(player, ticks);
    }

    public static boolean hasDamageBoost(Player player) {
        return damageBoostTicks.getOrDefault(player, 0) > 0;
    }

    public static void tick(Player player) {
        damageBoostTicks.computeIfPresent(player, (p, ticks) -> ticks > 0 ? ticks - 1 : 0);
    }

    public static void remove(Player player) {
        damageBoostTicks.remove(player);
    }
}
