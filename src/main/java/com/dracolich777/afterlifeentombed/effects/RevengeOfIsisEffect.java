package com.dracolich777.afterlifeentombed.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class RevengeOfIsisEffect extends MobEffect {
    
    public RevengeOfIsisEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // The main healing prevention is handled through events
        // This method can be used for any additional effects if needed
        
        // Optional: Add visual particles or other effects here
        // For example, you could add withering particles around the player
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Apply every tick for consistency
    }
    
    @Override
    public boolean isInstantenous() {
        return false;
    }
}
