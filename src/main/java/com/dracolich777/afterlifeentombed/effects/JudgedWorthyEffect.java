package com.dracolich777.afterlifeentombed.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class JudgedWorthyEffect extends MobEffect {
    // A map to define the conversion from negative vanilla effects to their positive counterparts.
    private static final Map<MobEffect, MobEffect> NEGATIVE_TO_POSITIVE_MAP = new HashMap<>();

    static {
        // Initialize the map with common vanilla effect conversions.
        // For effects without a direct opposite, a generally beneficial effect is chosen.
        NEGATIVE_TO_POSITIVE_MAP.put(MobEffects.MOVEMENT_SLOWDOWN, MobEffects.MOVEMENT_SPEED);
        NEGATIVE_TO_POSITIVE_MAP.put(MobEffects.DIG_SLOWDOWN, MobEffects.DIG_SPEED); // Renamed from DIG_HASTE in older versions
        NEGATIVE_TO_POSITIVE_MAP.put(MobEffects.WEAKNESS, MobEffects.DAMAGE_BOOST);
        NEGATIVE_TO_POSITIVE_MAP.put(MobEffects.POISON, MobEffects.REGENERATION);
        NEGATIVE_TO_POSITIVE_MAP.put(MobEffects.WITHER, MobEffects.DAMAGE_RESISTANCE);
        NEGATIVE_TO_POSITIVE_MAP.put(MobEffects.BLINDNESS, MobEffects.NIGHT_VISION);
        NEGATIVE_TO_POSITIVE_MAP.put(MobEffects.HUNGER, MobEffects.SATURATION);
        NEGATIVE_TO_POSITIVE_MAP.put(MobEffects.CONFUSION, MobEffects.JUMP);
        NEGATIVE_TO_POSITIVE_MAP.put(MobEffects.GLOWING, MobEffects.INVISIBILITY);
        NEGATIVE_TO_POSITIVE_MAP.put(MobEffects.UNLUCK, MobEffects.LUCK);
        NEGATIVE_TO_POSITIVE_MAP.put(MobEffects.BAD_OMEN, MobEffects.HERO_OF_THE_VILLAGE);
        // Instant effects like HARM are handled separately in applyEffectTick as they are not continuous.
    }

    /**
     * Constructs a new JudgedWorthyEffect.
     * @param pCategory The category of the mob effect (e.g., HARMFUL, BENEFICIAL).
     * @param pColor The color of the effect particles (hexadecimal).
     */
    public JudgedWorthyEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    /**
     * This method is called every tick while the effect is active.
     * For this effect, it's designed to run once upon application (by giving it a 1-tick duration)
     * to convert existing negative effects to positive ones.
     * @param pLivingEntity The entity on which the effect is active.
     * @param pAmplifier The amplifier level of this effect (0 for level I, 1 for level II, etc.).
     */
    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        // Ensure this logic only runs on the server side to prevent desync issues.
        // This effect is intended to be applied for a very short duration (e.g., 1 tick)
        // to perform its conversion logic immediately.
        if (!pLivingEntity.level().isClientSide) {
            // Use temporary lists to avoid ConcurrentModificationException when modifying active effects.
            List<MobEffectInstance> effectsToRemove = new ArrayList<>();
            List<MobEffectInstance> effectsToAdd = new ArrayList<>();

            // Iterate through all active effects on the living entity.
            for (MobEffectInstance instance : pLivingEntity.getActiveEffects()) {
                MobEffect currentEffect = instance.getEffect();

                // Skip the 'Judged Worthy' effect itself to prevent infinite loops or self-removal.
                if (currentEffect == this) {
                    continue;
                }

                // Special handling for instant HARM effect: convert to HEAL.
                if (currentEffect == MobEffects.HARM) {
                    effectsToRemove.add(instance); // Mark HARM for removal
                    effectsToAdd.add(new MobEffectInstance(
                            MobEffects.HEAL,
                            1, // Instant effects typically have a duration of 1 tick
                            instance.getAmplifier(),
                            instance.isAmbient(),
                            instance.isVisible(), // Preserve original particle visibility
                            instance.showIcon()
                    ));
                    continue; // Move to the next effect instance
                }

                // Check if the current effect is a negative vanilla effect that has a defined positive counterpart.
                if (NEGATIVE_TO_POSITIVE_MAP.containsKey(currentEffect)) {
                    MobEffect positiveCounterpart = NEGATIVE_TO_POSITIVE_MAP.get(currentEffect);
                    effectsToRemove.add(instance); // Mark the negative effect for removal

                    // Add the corresponding positive effect, maintaining original duration and amplifier
                    // and other display properties.
                    effectsToAdd.add(new MobEffectInstance(
                            positiveCounterpart,
                            instance.getDuration(), // Preserve the original duration
                            instance.getAmplifier(), // Preserve the original amplifier
                            instance.isAmbient(),    // Preserve ambient property
                            instance.isVisible(),    // Preserve original particle visibility
                            instance.showIcon()      // Preserve icon visibility
                    ));
                }
            }

            // Apply all marked changes: first remove effects, then add new ones.
            for (MobEffectInstance effectToRemove : effectsToRemove) {
                pLivingEntity.removeEffect(effectToRemove.getEffect());
            }
            for (MobEffectInstance effectToAdd : effectsToAdd) {
                pLivingEntity.addEffect(effectToAdd);
            }
        }
    }

    /**
     * Determines if applyEffectTick should be called for the current tick.
     * For this effect, it should always run its logic when active, typically for 1 tick.
     * @param pDuration The remaining duration of the effect.
     * @param pAmplifier The amplifier level of the effect.
     * @return True if applyEffectTick should be called, false otherwise.
     */
    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        // This effect is designed to be applied for a very short duration (e.g., 1 tick).
        // Returning true ensures applyEffectTick runs for that duration.
        return true;
    }
}