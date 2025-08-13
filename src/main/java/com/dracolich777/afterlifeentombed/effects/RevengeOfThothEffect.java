package com.dracolich777.afterlifeentombed.effects;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class RevengeOfThothEffect extends MobEffect {
    
    private static final int DRAIN_INTERVAL = 30; // Drain every 2 seconds (40 ticks)
    
    public RevengeOfThothEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            // Check if the player has any experience levels to drain
            if (player.experienceLevel > 0) {
                // Remove one level
                player.experienceLevel--;
                
                // Reset experience progress when losing a level
                player.experienceProgress = 0.0F;
                
                // Deal damage based on amplifier (1 + amplifier damage per level)
                float damage = 1.0F + amplifier;
                player.hurt(player.damageSources().magic(), damage);
                
                // Add visual and audio effects
                if (player.level() instanceof ServerLevel serverLevel) {
                    // Spawn dark particles around the player
                    serverLevel.sendParticles(
                        ParticleTypes.WITCH,
                        player.getX(),
                        player.getY() + 1.0,
                        player.getZ(),
                        10, // particle count
                        0.5, 0.5, 0.5, // spread
                        0.1 // speed
                    );
                    
                    // Play a haunting sound
                    serverLevel.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.SOUL_SAND_BREAK,
                        SoundSource.HOSTILE,
                        0.5F,
                        0.8F
                    );
                }
            }
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Only drain every DRAIN_INTERVAL ticks (every 2 seconds)
        return duration % DRAIN_INTERVAL == 0;
    }
    
    @Override
    public boolean isInstantenous() {
        return false;
    }
}
