package com.dracolich777.afterlifeentombed.events;
import com.dracolich777.afterlifeentombed.util.ParticleManager;

import com.dracolich777.afterlifeentombed.items.TokenOfHorus;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import com.dracolich777.afterlifeentombed.init.ModEffects;

@Mod.EventBusSubscriber(modid = "afterlifeentombed")
public class EyeOfHorusEventHandler {
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        float damage = event.getAmount();
        
        // Check if this damage would be fatal
        if (entity.getHealth() - damage <= 0) {
            // Check if entity has Token of Horus equipped
            CuriosApi.getCuriosInventory(entity).ifPresent(curiosInventory -> {
                curiosInventory.getStacksHandler("charm").ifPresent(stacksHandler -> {
                    for (int i = 0; i < stacksHandler.getSlots(); i++) {
                        ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                        
                        if (stack.getItem() instanceof TokenOfHorus tokenOfHorus) {
                            // Check if the charm is on cooldown
                            if (tokenOfHorus.isOnCooldown(stack, entity.level())) {
                                // Apply revenge effect for 1 minute (1200 ticks)
                                MobEffectInstance revengeEffect = new MobEffectInstance(
                                    ModEffects.REVENGE_OF_HORUS.get(), // Use .get() to get the actual MobEffect
                                    1200, // 1 minute (60 seconds * 20 ticks)
                                    0, // Amplifier level 0
                                    false, // Not ambient
                                    true, // Show particles
                                    true // Show icon
                                );
                                entity.addEffect(revengeEffect);
                                
                                return;
                                
                            }
                            
                            // If not on cooldown, try to activate the totem
                            if (tokenOfHorus.tryActivateTotem(entity, stack, event.getSource())) {
                                // Cancel the damage event to prevent death
                                event.setCanceled(true);
                                
                                // Spawn Horus shield particle at half scale if entity is a player
                                if (entity instanceof Player player) {
                                    ParticleManager.sendParticleToPlayer(
                                        player,
                                        "horus_shield",
                                        entity.getX(),
                                        entity.getY() + entity.getBbHeight() * 0.5, // Middle of the entity
                                        entity.getZ(),
                                        0.5f // Half scale
                                    );
                                }
                                
                                return;
                            }
                        }
                    }
                });
            });
        }
    }
}