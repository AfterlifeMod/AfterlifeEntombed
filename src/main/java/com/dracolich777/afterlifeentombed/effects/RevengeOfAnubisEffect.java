package com.dracolich777.afterlifeentombed.effects;

import com.dracolich777.afterlifeentombed.init.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class RevengeOfAnubisEffect extends MobEffect {
    
    public RevengeOfAnubisEffect(MobEffectCategory category, int color) {
        super(category, color); // Dark brownish-black color like mummy wrappings
        // Only frozen hearts effect - no attribute modifications
    }
    
    // Get the number of frozen hearts based on amplifier level
    public static int getFrozenHearts(int amplifier) {
        return amplifier + 0;
    }
    
    // Check if a specific heart index should be frozen
    public static boolean isHeartFrozen(LivingEntity entity, int heartIndex) {
        if (!(entity instanceof Player player)) return false;
        
        var effect = player.getEffect(ModEffects.REVENGE_OF_ANUBIS.get());
        if (effect == null) return false;
        
        int frozenHearts = getFrozenHearts(effect.getAmplifier());
        int totalHearts = (int) Math.ceil(player.getMaxHealth() / 2.0f);
        
        // Freeze hearts from the right side (highest indices)
        return heartIndex >= (totalHearts - frozenHearts);
    }
    
    // Get the effective max health (excluding frozen hearts)
    public static float getEffectiveMaxHealth(LivingEntity entity) {
        if (!(entity instanceof Player player)) return entity.getMaxHealth();
        
        var effect = player.getEffect(ModEffects.REVENGE_OF_ANUBIS.get());
        if (effect == null) return entity.getMaxHealth();
        
        int frozenHearts = getFrozenHearts(effect.getAmplifier());
        return Math.max(2.0f, entity.getMaxHealth() - (frozenHearts * 2.0f));
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        
        // Ensure health doesn't exceed effective max health
        if (entity instanceof Player player) {
            float effectiveMaxHealth = getEffectiveMaxHealth(player);
            if (player.getHealth() > effectiveMaxHealth) {
                player.setHealth(effectiveMaxHealth);
            }
        }
            

        
        
        // Spawn dark particles around the entity
        if (level instanceof ServerLevel serverLevel) {
            // Create a circle of dark particles around the entity
            for (int i = 0; i < 6; i++) {
                double angle = (i / 6.0) * Math.PI * 2;
                double x = entity.getX() + Math.cos(angle) * 1.2;
                double z = entity.getZ() + Math.sin(angle) * 1.2;
                double y = entity.getY() + entity.getBbHeight() * 0.3;
                
                serverLevel.sendParticles(ParticleTypes.SMOKE, 
                    x, y, z, 1, 0.0, 0.1, 0.0, 0.01);
                
                if (entity.getRandom().nextFloat() < 0.4F) {
                    serverLevel.sendParticles(ParticleTypes.SOUL, 
                        x, y + 0.5, z, 1, 0.0, 0.05, 0.0, 0.02);
                }
            }
            if (entity.getRandom().nextFloat() < 0.3F) {
                serverLevel.sendParticles(ParticleTypes.ENTITY_EFFECT, 
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.8, entity.getZ(), 
                    2, 0.2, 0.2, 0.2, 0.1);
            }
        }
        
        if (entity.getRandom().nextFloat() < 0.08F) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 0.3F, 0.7F);
        }
        
        
  
        if (amplifier >= 2) {

            if (entity.getRandom().nextFloat() < 0.02F) {
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.BLINDNESS, 40, 0));
            }
        }
        
        if (amplifier >= 4) {
            // Stronger periodic damage for high-level curses
            if (entity.getRandom().nextFloat() < 0.05F) {
                DamageSource damageSource = new DamageSource(
                    entity.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.MAGIC));
                entity.hurt(damageSource, 1.5F);
            }
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Tick every 20 ticks (1 second) for consistent effect
        return duration % 20 == 0;
    }
    
    @Override
    public boolean isInstantenous() {
        return false;
    }
}
