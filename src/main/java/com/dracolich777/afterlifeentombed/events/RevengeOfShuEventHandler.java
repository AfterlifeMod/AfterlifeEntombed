package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.init.ModEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RevengeOfShuEventHandler {
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Check if the player has the Revenge of Shu effect
            if (player.hasEffect(ModEffects.REVENGE_OF_SHU.get())) {
                // Check if the damage is from falling
                if (event.getSource().equals(player.damageSources().fall())) {
                    // Triple the fall damage
                    float originalDamage = event.getAmount();
                    event.setAmount(originalDamage * 3.0F);
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Check if the player has the Revenge of Shu effect
            if (player.hasEffect(ModEffects.REVENGE_OF_SHU.get())) {
                // Cancel the jump by setting vertical velocity to 0
                player.setDeltaMovement(player.getDeltaMovement().multiply(1.0, 0.0, 1.0));
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Check if the player has the Revenge of Shu effect
            if (player.hasEffect(ModEffects.REVENGE_OF_SHU.get())) {
                // Increase fall speed
                if (!player.onGround() && player.getDeltaMovement().y < 0) {
                    // Apply additional downward velocity for faster falling
                    double currentY = player.getDeltaMovement().y;
                    double newY = currentY * 1.5; // Increase fall speed by 50% each tick (compounds to ~3x)
                    player.setDeltaMovement(
                        player.getDeltaMovement().x,
                        Math.max(newY, -3.0), // Cap at -3.0 to prevent ridiculous speeds
                        player.getDeltaMovement().z
                    );
                }
            }
        }
    }
}

