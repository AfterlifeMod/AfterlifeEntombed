package com.dracolich777.afterlifeentombed.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class RevengeOfHorusEffect extends MobEffect {
    
    public RevengeOfHorusEffect(MobEffectCategory category, int color) {
        super(category, color); // Dark red color
    }
    
    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        // This effect doesn't need to tick, it's handled in damage events
        super.applyEffectTick(livingEntity, amplifier);
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false; // No periodic ticking needed
    }
}
