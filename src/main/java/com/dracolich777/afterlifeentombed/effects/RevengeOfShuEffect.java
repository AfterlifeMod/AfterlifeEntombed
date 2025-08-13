package com.dracolich777.afterlifeentombed.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RevengeOfShuEffect extends MobEffect {
    
    public RevengeOfShuEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            // Increase fall speed by applying additional downward velocity
            Vec3 velocity = player.getDeltaMovement();
            if (velocity.y < 0) { // Only if already falling
                // Apply 3x fall speed (multiply downward velocity by 3)
                double newY = velocity.y * 3.0;
                player.setDeltaMovement(velocity.x, newY, velocity.z);
            }
            
            // Prevent jumping by canceling upward movement
            if (velocity.y > 0 && player.onGround()) {
                player.setDeltaMovement(velocity.x, 0, velocity.z);
            }
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Apply every tick
    }
    
    @Override
    public boolean isInstantenous() {
        return false;
    }
}