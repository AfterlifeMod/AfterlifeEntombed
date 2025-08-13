package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.init.ModEffects;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RevengeOfIsisEventHandlerEnhanced {
    
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Check if the player has the Revenge of Isis effect
            if (player.hasEffect(ModEffects.REVENGE_OF_ISIS.get())) {
                // Cancel all healing
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Check if the player has the Revenge of Isis effect
            if (player.hasEffect(ModEffects.REVENGE_OF_ISIS.get())) {
                FoodData foodData = player.getFoodData();
                
                // Prevent natural health regeneration from food
                if (foodData.getFoodLevel() >= 18 && player.getHealth() < player.getMaxHealth()) {
                    // Force exhaustion to prevent regeneration
                    foodData.setExhaustion(4.0F);
                }
                
                // Remove any regeneration effects
                if (player.hasEffect(MobEffects.REGENERATION)) {
                    player.removeEffect(MobEffects.REGENERATION);
                }
                
                // Remove instant health effects
                if (player.hasEffect(MobEffects.HEAL)) {
                    player.removeEffect(MobEffects.HEAL);
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();
        
        // Check if the player has the Revenge of Isis effect
        if (player.hasEffect(ModEffects.REVENGE_OF_ISIS.get())) {
            // Prevent using healing items
            if (itemStack.getItem() == Items.GOLDEN_APPLE || 
                itemStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE ||
                itemStack.getItem() == Items.SUSPICIOUS_STEW) {
                event.setCanceled(true);
                return;
            }
            
            // Prevent using healing potions
            if (itemStack.getItem() instanceof PotionItem) {
                if (PotionUtils.getPotion(itemStack) == Potions.HEALING ||
                    PotionUtils.getPotion(itemStack) == Potions.STRONG_HEALING ||
                    PotionUtils.getPotion(itemStack) == Potions.REGENERATION ||
                    PotionUtils.getPotion(itemStack) == Potions.LONG_REGENERATION ||
                    PotionUtils.getPotion(itemStack) == Potions.STRONG_REGENERATION) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
