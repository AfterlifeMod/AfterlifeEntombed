package com.dracolich777.afterlifeentombed.effects;

import com.dracolich777.afterlifeentombed.init.ModDamageTypes;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;

public class HolyFireEffect extends MobEffect {

    public HolyFireEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Get the Holder<DamageType> from the registry
        Holder<DamageType> holyFireDamageTypeHolder = entity.level().registryAccess()
            .registryOrThrow(Registries.DAMAGE_TYPE)
            .getHolderOrThrow(ModDamageTypes.HOLY_FIRE);

        // Create a DamageSource using the Holder
        DamageSource holyFireSource = new DamageSource(holyFireDamageTypeHolder);

        // Damage the entity
        entity.hurt(holyFireSource, 1.0F + amplifier * 0.5F);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Apply every tick to maintain the effect and damage
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }
}