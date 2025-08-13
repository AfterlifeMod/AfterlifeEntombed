package com.dracolich777.afterlifeentombed.util;

import com.dracolich777.afterlifeentombed.util.BlockEffectHandler;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BlockEffectEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            BlockEffectHandler.handlePlayerBlockEffects(event.player);
        }
    }
}