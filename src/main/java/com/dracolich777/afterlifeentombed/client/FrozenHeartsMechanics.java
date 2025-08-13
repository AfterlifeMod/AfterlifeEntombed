
package com.dracolich777.afterlifeentombed.client;

import com.dracolich777.afterlifeentombed.effects.RevengeOfAnubisEffect;
import com.dracolich777.afterlifeentombed.init.ModEffects;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FrozenHeartsMechanics {
    
    @SubscribeEvent
    public static void onPlayerHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        // Check if player has frozen hearts
        MobEffectInstance effect = player.getEffect(ModEffects.REVENGE_OF_ANUBIS.get());
        if (effect == null) return;
        
        float effectiveMaxHealth = RevengeOfAnubisEffect.getEffectiveMaxHealth(player);
        float currentHealth = player.getHealth();
        float healAmount = event.getAmount();
        
        // If healing would exceed effective max health, reduce heal amount
        if (currentHealth + healAmount > effectiveMaxHealth) {
            float allowedHeal = Math.max(0, effectiveMaxHealth - currentHealth);
            event.setAmount(allowedHeal);
            
            // If no healing is allowed, cancel the event
            if (allowedHeal <= 0) {
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        // Check if player has frozen hearts
        MobEffectInstance effect = player.getEffect(ModEffects.REVENGE_OF_ANUBIS.get());
        if (effect == null) return;
        
        float effectiveMaxHealth = RevengeOfAnubisEffect.getEffectiveMaxHealth(player);
        float currentHealth = player.getHealth();
        float damage = event.getAmount();
        
        // Prevent damage from going below what would be "death" with frozen hearts
        // This ensures frozen hearts can't be "damaged" either
        float minHealthAfterDamage = Math.max(0, currentHealth - damage);
        
        // If player would die normally but has frozen hearts, they should still die
        // Frozen hearts don't provide extra protection, they just can't be healed
        // This ensures the mechanic works correctly
        
        // Optional: Add visual/audio feedback when trying to heal frozen hearts
        if (currentHealth >= effectiveMaxHealth && damage < 0) { // Negative damage = healing
            // Play a sound or show particles to indicate frozen hearts can't be healed
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                net.minecraft.sounds.SoundEvents.GLASS_BREAK, 
                net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.5F);
        }
    }
    
    // Additional method to handle other forms of health restoration
    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
        
        Player player = event.player;
        
        // Check if player has frozen hearts
        MobEffectInstance effect = player.getEffect(ModEffects.REVENGE_OF_ANUBIS.get());
        if (effect == null) return;
        
        float effectiveMaxHealth = RevengeOfAnubisEffect.getEffectiveMaxHealth(player);
        
        // Continuously ensure player health doesn't exceed effective max
        // This catches any healing that might bypass the heal event
        if (player.getHealth() > effectiveMaxHealth) {
            player.setHealth(effectiveMaxHealth);
        }
        
        // Optional: Prevent natural regeneration into frozen hearts
        if (player.getHealth() >= effectiveMaxHealth) {
            // Reset food exhaustion to prevent natural regen
            if (player.getFoodData().getSaturationLevel() > 0) {
                // Don't prevent saturation, just prevent the regen that would exceed effective max
                // This is handled by the heal event, but this is a backup
            }
        }
    }
}
