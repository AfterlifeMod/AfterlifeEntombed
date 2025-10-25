package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlibs.api.AfterLibsAPI;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.client.hud.GodAvatarHudHelper;
import com.dracolich777.afterlifeentombed.init.ModEffects;
import com.dracolich777.afterlifeentombed.items.GodType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class IsisAvatarAbilities {
    
    // Ally tracking (not stored in capability - session-only)
    private static final Map<UUID, List<UUID>> ALLY_LISTS = new HashMap<>();
    private static final Map<UUID, Integer> ALLY_SELECTION_COUNT = new HashMap<>();
    
    // Max health modification tracking (session-only)
    private static final Map<UUID, UUID> HEARTSTEALER_ATTRIBUTE_IDS = new HashMap<>();
    
    private static final UUID HEALING_AVATAR_HEALTH_MODIFIER_ID = UUID.fromString("d4f3e2a1-1234-5678-90ab-cdef12345678");
    
    private static final int LIGHT_OF_ISIS_COOLDOWN = 1200; // 60 seconds
    private static final int STRENGTH_IN_NUMBERS_COOLDOWN = 1200; // 60 seconds
    private static final int HEARTSTEALER_COOLDOWN = 6000; // 5 minutes
    private static final int HEARTSTEALER_DURATION = 600; // 30 seconds
    private static final int AVATAR_OF_HEALING_DURATION = 1200; // 60 seconds
    private static final int AVATAR_OF_HEALING_COOLDOWN = 6000; // 5 minutes
    
    /**
     * Public API for activating abilities from external handlers
     */
    public static void activateAbility(ServerPlayer player, int abilityId) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            if (cap.getSelectedGod() != GodType.ISIS) return;
            
            switch (abilityId) {
                case 1 -> activateLightOfIsis(player);
                case 2 -> activateStrengthInNumbers(player);
                case 3 -> activateHeartstealer(player);
                case 4 -> activateAvatarOfHealing(player);
            }
        });
    }
    
    /**
     * Main ability handler - processes scoreboard triggers
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            if (cap.getSelectedGod() != GodType.ISIS) return;
            
            // Check for ability activations via scoreboard
            int abilityValue = player.getScoreboard()
                .getOrCreatePlayerScore(player.getScoreboardName(), 
                    player.getScoreboard().getOrCreateObjective("god_avatar_ability"))
                .getScore();
            
            if (abilityValue > 0) {
                switch (abilityValue) {
                    case 1 -> activateLightOfIsis(player);
                    case 2 -> activateStrengthInNumbers(player);
                    case 3 -> activateHeartstealer(player);
                    case 4 -> activateAvatarOfHealing(player);
                }
                // Reset the scoreboard
                player.getScoreboard()
                    .getOrCreatePlayerScore(player.getScoreboardName(),
                        player.getScoreboard().getOrCreateObjective("god_avatar_ability"))
                    .setScore(0);
            }
            
            // Handle active Heartstealer
            handleHeartstealer(player);
            
            // Handle active Avatar of Healing
            applyAvatarOfHealingBuffs(player);
        });
    }
    
    /**
     * Passive: Healing Magic - 50% more healing + excess becomes absorption
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            if (cap.getSelectedGod() != GodType.ISIS) return;
            
            float originalHeal = event.getAmount();
            float boostedHeal = originalHeal * 1.5f;
            
            // Calculate current and max health
            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();
            
            // Calculate excess healing that would overflow
            float newHealth = currentHealth + boostedHeal;
            float excess = Math.max(0, newHealth - maxHealth);
            
            // Apply the boosted healing
            event.setAmount(boostedHeal);
            
            // Convert excess to absorption
            if (excess > 0) {
                float currentAbsorption = player.getAbsorptionAmount();
                player.setAbsorptionAmount(currentAbsorption + excess);
            }
        });
    }
    
    /**
     * Active 1: Light of Isis - Full heal, cleanse, regen 3 for 30s
     */
    private static void activateLightOfIsis(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            
            // Check cooldown
            long cooldownEnd = cap.getLightOfIsisCooldown();
            if (currentTime < cooldownEnd) {
                long remainingSeconds = (cooldownEnd - currentTime) / 20;
                GodAvatarHudHelper.sendCooldownMessage(player, "Light of Isis", remainingSeconds);
                return;
            }
            
            // Spawn healing particle
            AfterLibsAPI.spawnAfterlifeParticle(player.serverLevel(), "isis_heal1", player.getX(), player.getY(), player.getZ(), 2.0f);
            
            // Full heal
            player.setHealth(player.getMaxHealth());
            
            // Remove all negative effects
            player.removeAllEffects();
            
            // Grant Regeneration 3 for 30 seconds
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2, false, true));
            
            // Set cooldown
            cap.setLightOfIsisCooldown(currentTime + LIGHT_OF_ISIS_COOLDOWN);
            
            GodAvatarHudHelper.sendActivationMessage(player, "Light of Isis", GodAvatarHudHelper.COLOR_ISIS);
        });
    }
    
    /**
     * Active 2: Strength in Numbers - Select allies and buff them
     */
    private static void activateStrengthInNumbers(ServerPlayer player) {
        UUID playerId = player.getUUID();
        
        if (player.isCrouching()) {
            // Selection mode - add ally to list
            HitResult hitResult = player.pick(100.0D, 0.0F, false);
            
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hitResult;
                if (entityHit.getEntity() instanceof ServerPlayer targetPlayer) {
                    if (targetPlayer.getUUID().equals(playerId)) {
                        GodAvatarHudHelper.sendNotification(player, "Cannot select yourself as ally", GodAvatarHudHelper.COLOR_ERROR);
                        return;
                    }
                    
                    List<UUID> allyList = ALLY_LISTS.computeIfAbsent(playerId, k -> new ArrayList<>());
                    int selectionCount = ALLY_SELECTION_COUNT.getOrDefault(playerId, 0);
                    
                    if (selectionCount >= 5) {
                        GodAvatarHudHelper.sendNotification(player, "Used all 5 ally selections", GodAvatarHudHelper.COLOR_ERROR);
                        return;
                    }
                    
                    if (allyList.size() >= 10) {
                        GodAvatarHudHelper.sendNotification(player, "Ally list full (10 max)", GodAvatarHudHelper.COLOR_ERROR);
                        return;
                    }
                    
                    if (!allyList.contains(targetPlayer.getUUID())) {
                        allyList.add(targetPlayer.getUUID());
                        ALLY_SELECTION_COUNT.put(playerId, selectionCount + 1);
                        GodAvatarHudHelper.sendNotification(player, "Added " + targetPlayer.getName().getString() + " (" + allyList.size() + "/10)", GodAvatarHudHelper.COLOR_SUCCESS);
                    } else {
                        GodAvatarHudHelper.sendNotification(player, targetPlayer.getName().getString() + " already in list", GodAvatarHudHelper.COLOR_WARNING);
                    }
                }
            }
        } else {
            // Buff mode - buff all allies
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                long currentTime = player.level().getGameTime();
                long cooldownEnd = cap.getStrengthInNumbersCooldown();
                if (currentTime < cooldownEnd) {
                    long remainingSeconds = (cooldownEnd - currentTime) / 20;
                    GodAvatarHudHelper.sendCooldownMessage(player, "Strength in Numbers", remainingSeconds);
                    return;
                }
                
                List<UUID> allyList = ALLY_LISTS.get(playerId);
                if (allyList == null || allyList.isEmpty()) {
                    GodAvatarHudHelper.sendNotification(player, "No allies selected", GodAvatarHudHelper.COLOR_ERROR);
                    return;
                }
                
                int buffedCount = 0;
                for (UUID allyId : allyList) {
                    ServerPlayer ally = player.server.getPlayerList().getPlayer(allyId);
                    if (ally != null) {
                        // Spawn spiral particle at ally
                        AfterLibsAPI.spawnAfterlifeParticle(ally.serverLevel(), "spiral_up", ally.getX(), ally.getY() + 1, ally.getZ(), 2.0f);
                        
                        // Full heal
                        ally.setHealth(ally.getMaxHealth());
                        
                        // Grant buffs for 60 seconds
                        ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 2, false, true));
                        ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 2, false, true));
                        ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 2, false, true));
                        
                        GodAvatarHudHelper.sendNotification(ally, player.getName().getString() + " empowers you!", GodAvatarHudHelper.COLOR_ISIS);
                        buffedCount++;
                    }
                }
                
                if (buffedCount > 0) {
                    GodAvatarHudHelper.sendNotification(player, "Empowered " + buffedCount + " allies!", GodAvatarHudHelper.COLOR_SUCCESS);
                    cap.setStrengthInNumbersCooldown(currentTime + STRENGTH_IN_NUMBERS_COOLDOWN);
                } else {
                    GodAvatarHudHelper.sendNotification(player, "No allies online", GodAvatarHudHelper.COLOR_ERROR);
                }
            });
        }
    }
    
    /**
     * Active 3: Heartstealer - Steal max health on hit
     */
    private static void activateHeartstealer(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            
            // Check cooldown
            long cooldownEnd = cap.getHeartstealerCooldown();
            if (currentTime < cooldownEnd) {
                long remainingSeconds = (cooldownEnd - currentTime) / 20;
                GodAvatarHudHelper.sendCooldownMessage(player, "Heartstealer", remainingSeconds);
                return;
            }
            
            // Activate Heartstealer
            cap.setHeartstealerActive(true);
            cap.setHeartstealerEndTime(currentTime + HEARTSTEALER_DURATION);
            
            // Generate unique modifier ID for this activation
            HEARTSTEALER_ATTRIBUTE_IDS.put(player.getUUID(), UUID.randomUUID());
            
            GodAvatarHudHelper.sendActivationMessage(player, "Heartstealer", GodAvatarHudHelper.COLOR_ISIS);
        });
    }
    
    /**
     * Handle active Heartstealer effect
     */
    private static void handleHeartstealer(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            if (!cap.isHeartstealerActive()) return;
            
            long currentTime = player.level().getGameTime();
            long endTime = cap.getHeartstealerEndTime();
            
            if (currentTime >= endTime) {
                // Deactivate
                cap.setHeartstealerActive(false);
                cap.setHeartstealerCooldown(currentTime + HEARTSTEALER_COOLDOWN);
                GodAvatarHudHelper.sendDeactivationMessage(player, "Heartstealer", GodAvatarHudHelper.COLOR_ISIS);
            }
        });
    }
    
    /**
     * Handle Heartstealer damage stealing
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        
        attacker.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            if (cap.getSelectedGod() != GodType.ISIS) return;
            if (!cap.isHeartstealerActive()) return;
            
            long currentTime = attacker.level().getGameTime();
            long endTime = cap.getHeartstealerEndTime();
            if (currentTime >= endTime) return;
            
            float damage = event.getAmount();
            
            // Check if damage is at least 1 full heart (2 HP)
            if (damage >= 2.0f) {
                int heartsLost = (int) (damage / 2.0f);
                float healthToSteal = heartsLost * 2.0f;
                
                UUID attackerId = attacker.getUUID();
                // Get or create modifier ID for this Heartstealer activation
                UUID modifierId = HEARTSTEALER_ATTRIBUTE_IDS.get(attackerId);
                if (modifierId == null) {
                    modifierId = UUID.randomUUID();
                    HEARTSTEALER_ATTRIBUTE_IDS.put(attackerId, modifierId);
                }
                
                // Reduce victim's max health
                AttributeInstance victimMaxHealth = victim.getAttribute(Attributes.MAX_HEALTH);
                if (victimMaxHealth != null) {
                    // Remove existing modifier if present
                    UUID victimModifierId = HEARTSTEALER_ATTRIBUTE_IDS.get(victim.getUUID());
                    if (victimModifierId != null) {
                        victimMaxHealth.removeModifier(victimModifierId);
                    } else {
                        victimModifierId = UUID.randomUUID();
                        HEARTSTEALER_ATTRIBUTE_IDS.put(victim.getUUID(), victimModifierId);
                    }
                    
                    // Add new modifier
                    AttributeModifier victimModifier = new AttributeModifier(
                        victimModifierId,
                        "isis_heartstealer_victim",
                        -healthToSteal,
                        AttributeModifier.Operation.ADDITION
                    );
                    victimMaxHealth.addTransientModifier(victimModifier);
                    
                    // Ensure victim health doesn't exceed new max
                    if (victim.getHealth() > victimMaxHealth.getValue()) {
                        victim.setHealth((float) victimMaxHealth.getValue());
                    }
                }
                
                // Increase attacker's max health
                AttributeInstance attackerMaxHealth = attacker.getAttribute(Attributes.MAX_HEALTH);
                if (attackerMaxHealth != null) {
                    // Remove existing modifier if present
                    attackerMaxHealth.removeModifier(modifierId);
                    
                    // Add new modifier
                    AttributeModifier attackerModifier = new AttributeModifier(
                        modifierId,
                        "isis_heartstealer_attacker",
                        healthToSteal,
                        AttributeModifier.Operation.ADDITION
                    );
                    attackerMaxHealth.addTransientModifier(attackerModifier);
                    
                    // Heal attacker for the stolen amount
                    attacker.heal(healthToSteal);
                }
                
                GodAvatarHudHelper.sendNotification(attacker, "Stole " + heartsLost + " hearts from " + victim.getName().getString(), GodAvatarHudHelper.COLOR_ISIS, 20);
                GodAvatarHudHelper.sendNotification(victim, attacker.getName().getString() + " stole " + heartsLost + " hearts!", GodAvatarHudHelper.COLOR_ERROR, 40);
            }
        });
    }
    
    /**
     * Reset max health modifiers on death
     */
    @SubscribeEvent
    public static void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        UUID playerId = player.getUUID();
        
        // Remove max health modifiers
        UUID modifierId = HEARTSTEALER_ATTRIBUTE_IDS.get(playerId);
        if (modifierId != null) {
            AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.removeModifier(modifierId);
            }
            HEARTSTEALER_ATTRIBUTE_IDS.remove(playerId);
        }
        
        // Clear ally list
        ALLY_LISTS.remove(playerId);
        ALLY_SELECTION_COUNT.remove(playerId);
    }
    
    /**
     * Active 4: Avatar of Healing - Ultimate transformation
     */
    private static void activateAvatarOfHealing(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            
            // Check if already active - if so, deactivate
            if (cap.isAvatarOfHealingActive()) {
                deactivateAvatarOfHealing(player);
                return;
            }
            
            // Check cooldown
            long cooldownEnd = cap.getAvatarOfHealingCooldown();
            if (currentTime < cooldownEnd) {
                long remainingSeconds = (cooldownEnd - currentTime) / 20;
                GodAvatarHudHelper.sendCooldownMessage(player, "Avatar of Healing", remainingSeconds);
                return;
            }
            
            // Activate Avatar of Healing
            cap.setAvatarOfHealingActive(true);
            cap.setAvatarOfHealingEndTime(currentTime + AVATAR_OF_HEALING_DURATION);
            
            // Grant creative flight
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            
            // Clear all cooldowns for instant build
            cap.setLightOfIsisCooldown(0);
            cap.setStrengthInNumbersCooldown(0);
            cap.setHeartstealerCooldown(0);
            
            // Add max health boost
            AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.removeModifier(HEALING_AVATAR_HEALTH_MODIFIER_ID);
                AttributeModifier healthModifier = new AttributeModifier(
                    HEALING_AVATAR_HEALTH_MODIFIER_ID,
                    "isis_avatar_healing",
                    20.0,
                    AttributeModifier.Operation.ADDITION
                );
                maxHealth.addTransientModifier(healthModifier);
            }
            
            // Grant the Origins toggle power
            var server = player.getServer();
            if (server != null) {
                server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    "power grant " + player.getGameProfile().getName() + " afterlifeentombed:isis_avatar_of_healing_active"
                );
            }
            
            GodAvatarHudHelper.sendNotification(player, "✦ AVATAR OF HEALING ✦", GodAvatarHudHelper.COLOR_SPECIAL, 60);
        });
    }
    
    /**
     * Deactivate Avatar of Healing
     */
    private static void deactivateAvatarOfHealing(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            
            // Remove active state
            cap.setAvatarOfHealingActive(false);
            cap.setAvatarOfHealingEndTime(0);
            
            // Remove flight if not in creative mode
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            
            // Remove max health boost
            AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.removeModifier(HEALING_AVATAR_HEALTH_MODIFIER_ID);
            }
            
            // Remove all the extreme buffs
            player.removeEffect(MobEffects.REGENERATION);
            player.removeEffect(MobEffects.ABSORPTION);
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            player.removeEffect(MobEffects.DIG_SPEED);
            player.removeEffect(MobEffects.DAMAGE_BOOST);
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
            
            // Revoke the Origins toggle power
            var server = player.getServer();
            if (server != null) {
                server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    "power revoke " + player.getGameProfile().getName() + " afterlifeentombed:isis_avatar_of_healing_active"
                );
            }
            
            // Grant slow falling for 60 seconds
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 0, false, true));
            
            // Set cooldown
            cap.setAvatarOfHealingCooldown(currentTime + AVATAR_OF_HEALING_COOLDOWN);
            
            GodAvatarHudHelper.sendDeactivationMessage(player, "Avatar of Healing", GodAvatarHudHelper.COLOR_SPECIAL);
        });
    }
    
    /**
     * Apply Avatar of Healing buffs while active
     */
    private static void applyAvatarOfHealingBuffs(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            if (!cap.isAvatarOfHealingActive()) return;
            
            long currentTime = player.level().getGameTime();
            long endTime = cap.getAvatarOfHealingEndTime();
            
            if (currentTime >= endTime) {
                deactivateAvatarOfHealing(player);
                return;
            }
            
            // Apply ultimate buffs (refresh every tick)
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 254, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 40, 254, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 6, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 6, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 254, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 6, false, false));
            
            // Ensure creative flight is maintained
            if (!player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            
            // Instant build - clear cooldowns on other abilities
            cap.setLightOfIsisCooldown(0);
            cap.setStrengthInNumbersCooldown(0);
            cap.setHeartstealerCooldown(0);
        });
    }
}
