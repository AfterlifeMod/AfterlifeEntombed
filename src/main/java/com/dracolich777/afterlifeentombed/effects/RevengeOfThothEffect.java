package com.dracolich777.afterlifeentombed.effects;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class RevengeOfThothEffect extends MobEffect {
    

    
    public RevengeOfThothEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            // Only proceed if player has XP to drain
            if (player.experienceLevel > 0 || player.experienceProgress > 0) {
                // Calculate XP to drain - scale with current level
                // Higher levels drain faster (more XP per tick)
                int currentLevel = player.experienceLevel;
                int xpNeededForCurrentLevel = getXpNeededForLevel(currentLevel);
                
                // Scale drain amount with level - higher levels lose more XP per tick
                // Base drain is 1 XP at level 1, scales up with level
                float baseDrainRate = Math.max(1, currentLevel * 0.1f); // Adjust multiplier as needed
                float progressToDrain = baseDrainRate / xpNeededForCurrentLevel;
                
                // Store original level to detect level loss
                int originalLevel = currentLevel;
                
                // Drain the progress
                player.experienceProgress -= progressToDrain;
                
                // Handle level loss if progress goes negative
                if (player.experienceProgress < 0 && currentLevel > 0) {
                    player.experienceLevel--;
                    
                    // Set progress to the remaining negative amount converted to the previous level
                    if (player.experienceLevel > 0) {
                        int xpNeededForPreviousLevel = getXpNeededForLevel(player.experienceLevel);
                        player.experienceProgress = 1.0f + (player.experienceProgress * xpNeededForCurrentLevel / xpNeededForPreviousLevel);
                    } else {
                        player.experienceProgress = 0.0f;
                    }
                }
                
                // Ensure progress doesn't go below 0
                if (player.experienceProgress < 0) {
                    player.experienceProgress = 0.0f;
                }
                
                // Check if player lost a level
                boolean lostLevel = player.experienceLevel < originalLevel;
                
                // Add visual and audio effects
                if (player.level() instanceof ServerLevel serverLevel) {
                    if (lostLevel) {
                        // Deal damage based on amplifier when losing a level
                        DamageSource smoothBrainDamage = new DamageSource(
                                player.level().registryAccess()
                                        .registryOrThrow(Registries.DAMAGE_TYPE)
                                        .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,
                                                new ResourceLocation("afterlifeentombed", "smooth_brain")))
                        );

                        float damage = 1.0F + amplifier;
                        player.hurt(smoothBrainDamage, damage);
                        
                        // Spawn more intense dark particles for level loss
                        serverLevel.sendParticles(
                            ParticleTypes.WITCH,
                            player.getX(),
                            player.getY() + 1.0,
                            player.getZ(),
                            20, // more particles for level loss
                            0.8, 0.8, 0.8, // wider spread
                            0.2 // faster speed
                        );
                        
                        // Play enderman scream for level loss
                        serverLevel.playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.ENDERMAN_SCREAM,
                            SoundSource.HOSTILE,
                            0.8F,
                            0.7F
                        );
                    } else {
                        // Subtle effects for regular XP drain
                        serverLevel.sendParticles(
                            ParticleTypes.SMOKE,
                            player.getX(),
                            player.getY() + 0.5,
                            player.getZ(),
                            3, // fewer particles
                            0.3, 0.3, 0.3, // smaller spread
                            0.05 // slow speed
                        );
                        
                        // Play a subtle drain sound (reverse of XP gain)
                        serverLevel.playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.HOSTILE,
                            0.1F,
                            1.5F // higher pitch
                        );
                    }
                }
            }
        }
    }
    
    // Helper method to get XP needed for a specific level (matches Minecraft's formula)
    private int getXpNeededForLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Drain every tick
        return true;
    }
    
    @Override
    public boolean isInstantenous() {
        return false;
    }
}