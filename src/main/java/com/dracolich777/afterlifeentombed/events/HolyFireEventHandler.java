package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.init.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HolyFireEventHandler {
    
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        
        // Check if the entity has the Holy Fire effect
        if (entity.hasEffect(ModEffects.HOLY_FIRE.get())) {
            // If they have the effect but aren't on fire, set them on fire again
            if (!entity.isOnFire()) {
                entity.setSecondsOnFire(2);
            }
            
            // Prevent water from extinguishing them
            if (entity.isInWater() || entity.isInWaterRainOrBubble()) {
                entity.setSecondsOnFire(2); // Re-ignite if in water
            }
        }
    }
}
