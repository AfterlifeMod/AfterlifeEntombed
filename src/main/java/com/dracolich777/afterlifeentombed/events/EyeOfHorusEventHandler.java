package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.items.TokenOfHorus;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import com.dracolich777.afterlifeentombed.init.ModEffects;

import java.util.List;

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
                             
                                // Push back and levitate nearby players and hostile mobs
                                double knockbackRadius = 8.0; // 8 block radius
                                
                                // Get all living entities in range
                                List<LivingEntity> nearbyEntities = entity.level().getEntitiesOfClass(LivingEntity.class, 
                                    entity.getBoundingBox().inflate(knockbackRadius));
                                
                                for (LivingEntity nearbyEntity : nearbyEntities) {
                                    // Don't affect the protected player themselves
                                    if (nearbyEntity == entity) continue;
                                    
                                    // Check if this is a target entity we should affect
                                    boolean isTargetEntity = false;
                                    
                                    // Always affect players
                                    if (nearbyEntity instanceof Player) {
                                        isTargetEntity = true;
                                    }
                                    // Affect mobs that implement Enemy interface (most hostile mobs)
                                    else if (nearbyEntity instanceof Enemy) {
                                        isTargetEntity = true;
                                    }
                                    // Affect mobs that have MONSTER mob type (additional hostile mob check)
                                    else if (nearbyEntity instanceof Mob mob && mob.getMobType() == MobType.UNDEAD) {
                                        isTargetEntity = true;
                                    }
                                    // Also affect any mob that currently has a target (actively hostile)
                                    else if (nearbyEntity instanceof Mob mob && mob.getTarget() != null) {
                                        isTargetEntity = true;
                                    }
                                    
                                    if (!isTargetEntity) continue;
                                    
                                    // Calculate direction from protected player to nearby entity
                                    Vec3 direction = nearbyEntity.position().subtract(entity.position());
                                    // Ensure we have a valid direction (avoid division by zero)
                                    if (direction.length() < 0.1) {
                                        direction = new Vec3(1, 0, 0); // Default direction if too close
                                    } else {
                                        direction = direction.normalize();
                                    }
                                    
                                    // Apply horizontal knockback (stronger)
                                    double knockbackStrength = 8.0; // Increased from 1.5 for more dramatic effect
                                    nearbyEntity.setDeltaMovement(
                                        nearbyEntity.getDeltaMovement().add(
                                            direction.x * knockbackStrength,
                                            0.8, // Upward velocity for about 1 block height
                                            direction.z * knockbackStrength
                                        )
                                    );
                                    
                                    // Mark entity as having been affected (force velocity update)
                                    nearbyEntity.hasImpulse = true;
                                    
                                    // Apply levitation effect for 2 seconds (40 ticks) - only to players
                                    if (nearbyEntity instanceof Player) {
                                        nearbyEntity.addEffect(new MobEffectInstance(
                                            MobEffects.LEVITATION,
                                            40, // 2 seconds
                                            0, // Level 0 (gentle levitation)
                                            false, // Not ambient
                                            true, // Show particles
                                            true // Show icon
                                        ));
                                    }
                                }
                                
                                // Spawn Horus shield particle at half scale if entity is a player
                                if (entity instanceof Player player) {
                                    com.dracolich777.afterlibs.AfterLibs.NETWORK.sendParticleToPlayer(
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