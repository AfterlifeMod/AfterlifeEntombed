package com.dracolich777.afterlifeentombed.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class SwarmedEffect extends MobEffect {
    
    public SwarmedEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B4513); // Brown color for swarm
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Calculate damage based on amplifier (amplifier 0 = 1 damage, amplifier 1 = 2 damage, etc.)
        int armorDamage = amplifier + 1;
        
        // Get all armor slots
        EquipmentSlot[] armorSlots = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
        };
        
        // Damage each piece of armor
        for (EquipmentSlot slot : armorSlots) {
            ItemStack armorPiece = entity.getItemBySlot(slot);
            
            // Check if there's armor in this slot and it's damageable
            if (!armorPiece.isEmpty() && armorPiece.isDamageableItem()) {
                // Damage the armor piece
                armorPiece.hurtAndBreak(armorDamage, entity, (livingEntity) -> {
                    // This callback is called when the item breaks
                    livingEntity.broadcastBreakEvent(slot);
                });
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Apply effect every 20 ticks (1 second)
        return duration % 20 == 0;
    }
}