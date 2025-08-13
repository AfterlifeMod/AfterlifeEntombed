package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.init.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class RevengeOfHorusEventHandler {
    
    // Unique UUIDs for the armor nullification modifiers
    private static final UUID ARMOR_TOUGHNESS_NULLIFIER_UUID = UUID.fromString("a1b2c3d4-5678-90ab-cdef-123456789abc");
    private static final UUID KNOCKBACK_RESISTANCE_NULLIFIER_UUID = UUID.fromString("fedcba98-7654-3210-fedc-ba9876543210");

    // Continuously nullify armor stats every tick for entities with Revenge of Horus effect
    @SubscribeEvent
    public static void onLivingTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        
        if (!entity.level().isClientSide && entity.hasEffect(ModEffects.REVENGE_OF_HORUS.get())) {
            // Force armor stats to 0 every tick - prevents armor changes from taking effect
            forceNullifyArmorStats(entity);
        }
    }
    
    // Handle when Revenge of Horus effect is added
    @SubscribeEvent
    public static void onPotionAdded(MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect() == ModEffects.REVENGE_OF_HORUS.get()) {
            LivingEntity entity = event.getEntity();
            nullifyArmorStats(entity);
        }
    }
    
    // Handle when Revenge of Horus effect is removed
    @SubscribeEvent
    public static void onPotionRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == ModEffects.REVENGE_OF_HORUS.get()) {
            LivingEntity entity = event.getEntity();
            restoreArmorStats(entity);
        }
    }
    
    // Handle when Revenge of Horus effect expires
    @SubscribeEvent
    public static void onPotionExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance().getEffect() == ModEffects.REVENGE_OF_HORUS.get()) {
            LivingEntity entity = event.getEntity();
            restoreArmorStats(entity);
        }
    }
    
    private static void nullifyArmorStats(LivingEntity entity) {
        // Get current values to calculate how much to subtract
        double currentArmorToughness = entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        double currentKnockbackRes = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        
        // Create modifiers that subtract the current values (making them 0)
        AttributeModifier armorToughnessNullifier = new AttributeModifier(
            ARMOR_TOUGHNESS_NULLIFIER_UUID,
            "horus_armor_toughness_nullifier",
            -currentArmorToughness, // Subtract current value to make it 0
            AttributeModifier.Operation.ADDITION
        );
        
        AttributeModifier knockbackResNullifier = new AttributeModifier(
            KNOCKBACK_RESISTANCE_NULLIFIER_UUID,
            "horus_knockback_resistance_nullifier",
            -currentKnockbackRes, // Subtract current value to make it 0
            AttributeModifier.Operation.ADDITION
        );
        
        // Apply the modifiers if not already applied
        if (!entity.getAttribute(Attributes.ARMOR_TOUGHNESS).hasModifier(armorToughnessNullifier)) {
            entity.getAttribute(Attributes.ARMOR_TOUGHNESS).addTransientModifier(armorToughnessNullifier);
        }
        
        if (!entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).hasModifier(knockbackResNullifier)) {
            entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addTransientModifier(knockbackResNullifier);
        }
    }
    
    private static void forceNullifyArmorStats(LivingEntity entity) {
        // Get current armor values
        double currentArmorToughness = entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        double currentKnockbackRes = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        
        // If armor stats are not 0, force them to be 0
        if (currentArmorToughness > 0) {
            // Remove old modifier if it exists
            entity.getAttribute(Attributes.ARMOR_TOUGHNESS).removeModifier(ARMOR_TOUGHNESS_NULLIFIER_UUID);
            
            // Add new modifier to nullify current value
            AttributeModifier armorToughnessNullifier = new AttributeModifier(
                ARMOR_TOUGHNESS_NULLIFIER_UUID,
                "horus_armor_toughness_nullifier",
                -currentArmorToughness, // Subtract current value to make it 0
                AttributeModifier.Operation.ADDITION
            );
            entity.getAttribute(Attributes.ARMOR_TOUGHNESS).addTransientModifier(armorToughnessNullifier);
        }
        
        if (currentKnockbackRes > 0) {
            // Remove old modifier if it exists
            entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(KNOCKBACK_RESISTANCE_NULLIFIER_UUID);
            
            // Add new modifier to nullify current value
            AttributeModifier knockbackResNullifier = new AttributeModifier(
                KNOCKBACK_RESISTANCE_NULLIFIER_UUID,
                "horus_knockback_resistance_nullifier",
                -currentKnockbackRes, // Subtract current value to make it 0
                AttributeModifier.Operation.ADDITION
            );
            entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addTransientModifier(knockbackResNullifier);
        }
    }
    
    private static void restoreArmorStats(LivingEntity entity) {
        // Remove the nullifying modifiers to restore original armor stats
        entity.getAttribute(Attributes.ARMOR_TOUGHNESS).removeModifier(ARMOR_TOUGHNESS_NULLIFIER_UUID);
        entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(KNOCKBACK_RESISTANCE_NULLIFIER_UUID);
    }
}