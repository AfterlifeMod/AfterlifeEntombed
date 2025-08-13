package com.dracolich777.afterlifeentombed.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance; // Ensure this is the correct import
import net.minecraft.world.effect.MobEffects;      // Ensure this is imported
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose; // For potentially changing pose
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks; // For suffocation blocks if needed

public class RevengeOfGebEffect extends MobEffect {

    public RevengeOfGebEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            // Calculate sinking amount per tick based on amplifier
            // Base sink amount is 0.05 blocks, increasing by 0.02 for each amplifier level
            double sinkAmount = (0.01D + (amplifier * 0.02D));

            // Teleport the entity downwards
            // We use moveRelative as a slightly safer way to apply movement that respects collision,
            // but for actual sinking, a direct teleport is more effective.
            // Let's use direct setPos/teleport
            entity.teleportTo(entity.getX(), entity.getY() - sinkAmount, entity.getZ());

            // Preventing Jumping:
            // 1. Extreme Slowness: Makes jumping effectively impossible from player input.
            //    Duration is 2 ticks, constantly refreshed. Amplifier 255 is max.
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 2, false, false, false));

            // 2. Continuous downward teleportation already counteracts any jump attempt.

            // Optional: Visual/Audio cues for "suffocating"
            // If the entity's head is below the block surface, Minecraft naturally applies suffocation damage.
            // You could also explicitly add a suffocation effect if desired, but that's usually
            // handled by vanilla when the head is in a block.

            // Optional: Change pose to "crawling" or "swimming" if it fits the visual.
            // entity.setPose(Pose.SWIMMING); // Or Pose.CRAWLING. This is mainly visual.
            // Remember to reset pose on effect end if you set it here.
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // This effect needs to apply every tick to continuously sink the entity
        return true;
    }

    // Optional: Reset pose if you changed it in applyEffectTick
    // This is called when the effect is removed
    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, net.minecraft.world.entity.ai.attributes.AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        // If you set entity.setPose(Pose.SWIMMING); in applyEffectTick, you might want to reset it here
        // pLivingEntity.setPose(Pose.STANDING); // Or whatever their default pose should be
    }
}