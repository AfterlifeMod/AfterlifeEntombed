package com.dracolich777.afterlifeentombed.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class RevengeOfSethEffect extends MobEffect {
    
    public RevengeOfSethEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000); // Dark red color
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Effect logic is handled client-side through events
        // This method can be used for any server-side effects if needed
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
