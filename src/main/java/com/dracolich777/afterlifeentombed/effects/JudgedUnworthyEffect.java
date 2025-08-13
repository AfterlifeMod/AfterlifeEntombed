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

public class JudgedUnworthyEffect extends MobEffect {
    // A map to define the conversion from positive vanilla effects to their negative counterparts.
    private static final Map<MobEffect, MobEffect> POSITIVE_TO_NEGATIVE_MAP = new HashMap<>();

    static {
        // Initialize the map with common vanilla effect conversions.
        // For effects without a direct opposite, a generally harmful effect is chosen.
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.MOVEMENT_SPEED, MobEffects.MOVEMENT_SLOWDOWN);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.DIG_SPEED, MobEffects.DIG_SLOWDOWN); // Renamed from DIG_HASTE in older versions
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.DAMAGE_BOOST, MobEffects.WEAKNESS);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.JUMP, MobEffects.CONFUSION);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.REGENERATION, MobEffects.POISON);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.DAMAGE_RESISTANCE, MobEffects.WITHER);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.FIRE_RESISTANCE, MobEffects.POISON); // User's choice, alternative could be WITHER
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.WATER_BREATHING, MobEffects.DARKNESS); // User's choice, alternative could be BLINDNESS
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.INVISIBILITY, MobEffects.GLOWING);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.NIGHT_VISION, MobEffects.BLINDNESS);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.HEALTH_BOOST, MobEffects.WITHER);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.ABSORPTION, MobEffects.WITHER);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.SATURATION, MobEffects.HUNGER);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.LUCK, MobEffects.UNLUCK);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.SLOW_FALLING, MobEffects.MOVEMENT_SLOWDOWN);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.CONDUIT_POWER, MobEffects.WEAKNESS);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.DOLPHINS_GRACE, MobEffects.MOVEMENT_SLOWDOWN);
        POSITIVE_TO_NEGATIVE_MAP.put(MobEffects.HERO_OF_THE_VILLAGE, MobEffects.BAD_OMEN);
        // Instant effects like HEAL are handled separately in applyEffectTick as they are not continuous.
    }

    /**
     * Constructs a new JudgedUnworthyEffect.
     * @param pCategory The category of the mob effect (e.g., HARMFUL, BENEFICIAL).
     * @param pColor The color of the effect particles (hexadecimal).
     */
    public JudgedUnworthyEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    /**
     * This method is called every tick while the effect is active.
     * For this effect, it's designed to run once upon application (by giving it a 1-tick duration)
     * to convert existing positive effects to negative ones.
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

                // Skip the 'Judged Unworthy' effect itself to prevent infinite loops or self-removal.
                if (currentEffect == this) {
                    continue;
                }

                // Special handling for instant HEAL effect: convert to HARM.
                if (currentEffect == MobEffects.HEAL) {
                    effectsToRemove.add(instance); // Mark HEAL for removal
                    effectsToAdd.add(new MobEffectInstance(
                            MobEffects.HARM,
                            1, // Instant effects typically have a duration of 1 tick
                            instance.getAmplifier(),
                            instance.isAmbient(),
                            instance.isVisible(), // Preserve original particle visibility
                            instance.showIcon()
                    ));
                    continue; // Move to the next effect instance
                }

                // Check if the current effect is a positive vanilla effect that has a defined negative counterpart.
                if (POSITIVE_TO_NEGATIVE_MAP.containsKey(currentEffect)) {
                    MobEffect negativeCounterpart = POSITIVE_TO_NEGATIVE_MAP.get(currentEffect);
                    effectsToRemove.add(instance); // Mark the positive effect for removal

                    // Add the corresponding negative effect, maintaining original duration and amplifier
                    // and other display properties.
                    effectsToAdd.add(new MobEffectInstance(
                            negativeCounterpart,
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