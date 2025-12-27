package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.client.hud.GodAvatarHudHelper;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.items.GodstoneItem;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.SyncGodAvatarPacket;
import com.dracolich777.afterlibs.api.AfterLibsAPI;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Event handlers for Anubis Avatar abilities - God of Death & the Underworld
 */
@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnubisAvatarAbilities {

    // Ability IDs
    public static final int ABILITY_UNDEAD_COMMAND = 1;
    public static final int ABILITY_LIFELINK = 2;
    public static final int ABILITY_SUMMON_UNDEAD = 3;
    public static final int ABILITY_AVATAR_OF_DEATH = 4;

    // Death's Bargain attribute modifier UUIDs
    private static final UUID DEATHS_BARGAIN_ARMOR_UUID = UUID.fromString("a8b7c6d5-4e3f-2a1b-9c8d-7e6f5a4b3c2d");
    private static final UUID DEATHS_BARGAIN_DEBUFF_UUID = UUID.fromString("b9c8d7e6-5f4e-3b2c-ad9e-8f7a6b5c4d3e");

    // Track undead command primed state: <player UUID, prime time>
    private static final Map<UUID, Long> UNDEAD_COMMAND_PRIMED = new HashMap<>();

    // Track recently hit opponents for armor debuff: <entity UUID, <attacker UUID,
    // hit time>>
    private static final Map<UUID, Map<UUID, Long>> DEATH_MARKED_ENTITIES = new HashMap<>();

    /**
     * Check if player is Anubis avatar
     */
    private static boolean isAnubisAvatar(Player player) {
        return player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY)
                .map(cap -> cap.getSelectedGod() == GodType.ANUBIS).orElse(false);
    }

    /**
     * Main entry point for activating Anubis abilities
     */
    public static void activateAbility(ServerPlayer player, int abilityId) {
        long currentTime = player.level().getGameTime();

        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            switch (abilityId) {
                case ABILITY_UNDEAD_COMMAND -> activateUndeadCommand(player, cap, currentTime);
                case ABILITY_LIFELINK -> activateLifelink(player, cap, currentTime);
                case ABILITY_SUMMON_UNDEAD -> activateSummonUndead(player, cap, currentTime);
                case ABILITY_AVATAR_OF_DEATH -> activateAvatarOfDeath(player, cap, currentTime);
            }
        });
    }

    /**
     * Ability 1: Undead Command - Two-stage ability
     * Stage 1: Activate ability, enters "primed" state for 10 seconds
     * Stage 2: Either hit a player to set target, or auto-detonate after 10s
     */
    private static void activateUndeadCommand(ServerPlayer player, GodAvatarCapability.IGodAvatar cap,
            long currentTime) {
        // Check if in Avatar of Death mode (unlimited)
        boolean unlimited = cap.isAvatarOfDeathActive();

        // Check cooldown (skip if in Avatar of Death mode)
        if (!unlimited) {
            long cooldown = cap.getUndeadCommandCooldown();
            if (currentTime < cooldown) {
                long ticksRemaining = cooldown - currentTime;
                long secondsRemaining = ticksRemaining / 20;
                GodAvatarHudHelper.sendNotification(player, "Undead Command on cooldown: " + secondsRemaining + "s",
                        GodAvatarHudHelper.COLOR_ERROR, 40);
                return;
            }
        }

        // Prime the ability - player now has 10 seconds to hit someone or it
        // auto-detonates
        UUID playerUUID = player.getUUID();
        UNDEAD_COMMAND_PRIMED.put(playerUUID, currentTime);

        GodAvatarHudHelper.sendNotification(player, "✦ UNDEAD COMMAND PRIMED ✦", GodAvatarHudHelper.COLOR_ANUBIS, 60);
        GodAvatarHudHelper.sendNotification(player, "Hit a player to target, or wait 10s to detonate!",
                GodAvatarHudHelper.COLOR_ANUBIS, 200);
    }

    /**
     * Execute undead command with a specific target
     */
    private static void executeUndeadCommand(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime, LivingEntity target, boolean unlimited) {
        // Find all undead in 32 block radius
        AABB searchBox = new AABB(
            player.getX() - 32, player.getY() - 32, player.getZ() - 32,
            player.getX() + 32, player.getY() + 32, player.getZ() + 32
        );
        
        List<Mob> undead = player.level().getEntitiesOfClass(Mob.class, searchBox, mob -> isUndead(mob));
        
        if (undead.isEmpty()) {
            GodAvatarHudHelper.sendNotification(player, "No undead nearby!", GodAvatarHudHelper.COLOR_ERROR, 40);
            return;
        }
        
        int commandedCount = 0;
        int explodedCount = 0;
        
        for (Mob undeadMob : undead) {
            if (target != null && !target.equals(player)) {
                // Command them to attack the target
                undeadMob.setTarget(target);
                commandedCount++;
            } else {
                // No valid target - detonate violently (kill mob, damage and knockback nearby entities)
                if (player.level() instanceof ServerLevel level) {
                    Vec3 explosionPos = undeadMob.position();
                    
                    // Spawn blood_lance particle at explosion point
                    AfterLibsAPI.spawnAfterlifeParticle(level, "blood_lance", 
                        explosionPos.x, explosionPos.y + 1, explosionPos.z, 1.0f);
                    
                    // Kill the undead mob that detonated
                    undeadMob.kill();
                    
                    // Apply custom damage and severe knockback to nearby entities (12 block radius)
                    AABB damageBox = new AABB(
                        explosionPos.x - 12, explosionPos.y - 12, explosionPos.z - 12,
                        explosionPos.x + 12, explosionPos.y + 12, explosionPos.z + 12
                    );
                    
                    List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, damageBox);
                    for (LivingEntity entity : nearbyEntities) {
                        if (entity.equals(player)) continue; // Don't affect Anubis avatar who triggered it
                        if (entity.equals(undeadMob)) continue; // Skip the detonating mob itself
                        
                        double distance = entity.position().distanceTo(explosionPos);
                        if (distance > 12) continue;
                        
                        // Calculate damage based on distance (10-25 damage, closer = more damage).
                        float distanceRatio = (float)(1.0 - (distance / 12.0));
                        float damage = 10.0f + (15.0f * distanceRatio); // 10-25 damage
                        
                        // Deal damage
                        entity.hurt(level.damageSources().explosion(null, player), damage);
                        
                        // Apply severe knockback
                        Vec3 direction = entity.position().subtract(explosionPos).normalize();
                        double knockbackStrength = 3.0 * distanceRatio; // Severe knockback, stronger closer to center
                        entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(knockbackStrength)));
                        entity.hurtMarked = true;
                    }
                    
                    explodedCount++;
                }
            }
        }
        
        if (commandedCount > 0) {
            GodAvatarHudHelper.sendActivationMessage(player, commandedCount + " undead commanded!", GodAvatarHudHelper.COLOR_ANUBIS);
        }
        if (explodedCount > 0) {
            GodAvatarHudHelper.sendActivationMessage(player, explodedCount + " undead detonated!", GodAvatarHudHelper.COLOR_ANUBIS);
        }
        
        // Set 20 second cooldown (400 ticks) - skip if in Avatar of Death mode
        if (!unlimited) {
            cap.setUndeadCommandCooldown(currentTime + 400);
        }
        GodAvatarHudHelper.sendNotification(player, "✦ UNDEAD COMMAND ✦", GodAvatarHudHelper.COLOR_ANUBIS, 60);
    }

    /**
     * Ability 2: Lifelink - Gain HP equal to damage dealt (30s duration)
     */
    private static void activateLifelink(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        // Check if in Avatar of Death mode (unlimited)
        boolean unlimited = cap.isAvatarOfDeathActive();

        // Check cooldown (skip if in Avatar of Death mode)
        if (!unlimited) {
            long cooldown = cap.getLifelinkCooldown();
            if (currentTime < cooldown) {
                long ticksRemaining = cooldown - currentTime;
                long secondsRemaining = ticksRemaining / 20;
                GodAvatarHudHelper.sendNotification(player, "Lifelink on cooldown: " + secondsRemaining + "s",
                        GodAvatarHudHelper.COLOR_ERROR, 40);
                return;
            }
        }

        // Activate lifelink for 30 seconds (600 ticks)
        cap.setLifelinkActive(true);
        cap.setLifelinkEndTime(currentTime + 600);

        GodAvatarHudHelper.sendNotification(player, "✦ LIFELINK ACTIVE ✦", GodAvatarHudHelper.COLOR_ANUBIS, 60);
    }

    /**
     * Ability 3: Summon Undead - Summon random undead around you
     */
    private static void activateSummonUndead(ServerPlayer player, GodAvatarCapability.IGodAvatar cap,
            long currentTime) {
        // Check if in Avatar of Death mode (unlimited)
        boolean unlimited = cap.isAvatarOfDeathActive();

        // Check cooldown (skip if in Avatar of Death mode)
        if (!unlimited) {
            long cooldown = cap.getSummonUndeadCooldown();
            if (currentTime < cooldown) {
                long ticksRemaining = cooldown - currentTime;
                long secondsRemaining = ticksRemaining / 20;
                GodAvatarHudHelper.sendNotification(player, "Summon Undead on cooldown: " + secondsRemaining + "s",
                        GodAvatarHudHelper.COLOR_ERROR, 40);
                return;
            }
        }

        if (player.level() instanceof ServerLevel level) {
            // Summon 5-8 random undead
            int count = 5 + level.random.nextInt(4);

            for (int i = 0; i < count; i++) {
                // Random position around player (5-10 blocks away)
                double angle = level.random.nextDouble() * Math.PI * 2;
                double distance = 5 + level.random.nextDouble() * 5;
                double x = player.getX() + Math.cos(angle) * distance;
                double z = player.getZ() + Math.sin(angle) * distance;
                double y = player.getY();

                // Choose random undead type
                EntityType<?> undeadType = switch (level.random.nextInt(5)) {
                    case 0 -> EntityType.ZOMBIE;
                    case 1 -> EntityType.SKELETON;
                    case 2 -> EntityType.ZOMBIE_VILLAGER;
                    case 3 -> EntityType.HUSK;
                    default -> EntityType.STRAY;
                };

                Mob undead = (Mob) undeadType.create(level);
                if (undead != null) {
                    undead.moveTo(x, y, z, level.random.nextFloat() * 360, 0);
                    undead.finalizeSpawn(level, level.getCurrentDifficultyAt(undead.blockPosition()),
                            MobSpawnType.MOB_SUMMONED, null, null);
                    level.addFreshEntity(undead);
                }
            }

            GodAvatarHudHelper.sendActivationMessage(player, "Summoned " + count + " undead!",
                    GodAvatarHudHelper.COLOR_ANUBIS);
        }

        // Set 60 second cooldown (1200 ticks) - skip if in Avatar of Death mode
        if (!unlimited) {
            cap.setSummonUndeadCooldown(currentTime + 1200);
        }
        GodAvatarHudHelper.sendNotification(player, "✦ UNDEAD SUMMONED ✦", GodAvatarHudHelper.COLOR_ANUBIS, 60);
    }

    /**
     * Ability 4: Avatar of Death - 60 second ultimate transformation
     * Can be toggled off early by pressing the button again
     */
    private static void activateAvatarOfDeath(ServerPlayer player, GodAvatarCapability.IGodAvatar cap,
            long currentTime) {
        // If already active, deactivate it early
        if (cap.isAvatarOfDeathActive()) {
            deactivateAvatarOfDeath(player, cap, currentTime);
            return;
        }

        // Check if holding a different god's stone to switch
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof GodstoneItem godstone) {
            GodType newGod = godstone.getGodType();
            if (newGod != GodType.SETH && newGod != GodType.NONE) {
                // Switch gods!
                cap.setSelectedGod(newGod);
                
                // Consume the godstone
                mainHand.shrink(1);
                
                // Spawn swap particles based on new god
                if (player.level() instanceof ServerLevel level) {
                    String particleName = switch (newGod) {
                        case RA -> "ra_halo";
                        case SHU -> "shujump";
                        case ANUBIS -> "anubis_nuke";
                        case GEB -> "seth_fog";
                        case HORUS, ISIS, THOTH -> "seth_fog"; // Default to seth_fog for other gods
                        default -> "seth_fog";
                    };
                    AfterLibsAPI.spawnAfterlifeParticle(level, particleName, player.getX(), player.getY() + 1, player.getZ(), 2.0f);
                }
                
                // Switch to the new god's origin
                var server = player.getServer();
                if (server != null) {
                    String originId = switch (newGod) {
                        case RA -> "afterlifeentombed:avatar_of_ra";
                        case SHU -> "afterlifeentombed:avatar_of_shu";
                        case ANUBIS -> "afterlifeentombed:avatar_of_anubis";
                        case THOTH -> "afterlifeentombed:avatar_of_thoth";
                        case GEB -> "afterlifeentombed:avatar_of_geb";
                        case HORUS, ISIS -> "afterlifeentombed:avatar_of_egypt";
                        default -> null;
                    };
                    
                    if (originId != null) {
                        // Remove ALL existing avatar origins first
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_egypt"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_seth"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_ra"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_shu"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_anubis"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_thoth"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_geb"
                        );
                        
                        // Now grant the new origin
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin set " + player.getGameProfile().getName() + " origins:origin " + originId
                        );
                    }
                }
                
                GodAvatarHudHelper.sendNotification(player, "Now avatar of " + newGod.name(), GodAvatarHudHelper.COLOR_SPECIAL, 60);
                // Sync to client
                GodAvatarPackets.INSTANCE.sendToServer(new SyncGodAvatarPacket(newGod));
                return;
            }
        }

        // Check cooldown
        long cooldown = cap.getAvatarOfDeathCooldown();
        if (currentTime < cooldown) {
            long ticksRemaining = cooldown - currentTime;
            long secondsRemaining = ticksRemaining / 20;
            GodAvatarHudHelper.sendNotification(player, "Avatar of Death on cooldown: " + secondsRemaining + "s",
                    GodAvatarHudHelper.COLOR_ERROR, 40);
            return;
        }

        // Activate for 60 seconds (1200 ticks)
        cap.setAvatarOfDeathActive(true);
        cap.setAvatarOfDeathEndTime(currentTime + 1200);
        cap.setAvatarOfDeathCooldown(currentTime); // Store activation time

        // Apply standardised godly buffs for ultimate (1 minute duration)
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 254, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 6, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 6, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 254, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 254, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 6, false, false));
        // Grant creative flight
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
        // Remove cooldowns on other active abilities
        cap.setNoAbilityCooldowns(true);

        GodAvatarHudHelper.sendNotification(player, "✦ AVATAR OF DEATH ✦", GodAvatarHudHelper.COLOR_ANUBIS, 60);
    }

    /**
     * Deactivate Avatar of Death early and apply cooldown
     */
    private static void deactivateAvatarOfDeath(ServerPlayer player, GodAvatarCapability.IGodAvatar cap,
            long currentTime) {
        cap.setAvatarOfDeathActive(false);
        // Set cooldown (120 seconds = 2400 ticks)
        cap.setAvatarOfDeathCooldown(currentTime + 2400);

        // Remove creative flight
        if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        GodAvatarHudHelper.sendDeactivationMessage(player, "Avatar of Death", GodAvatarHudHelper.COLOR_ANUBIS);
    }

    /**
     * Handle undead command target selection, lifelink healing, and Death's Bargain
     * marking when player damages entities
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            if (!isAnubisAvatar(player))
                return;

            long currentTime = player.level().getGameTime();
            UUID playerUUID = player.getUUID();
            LivingEntity victim = event.getEntity();

            // Mark entity for Death's Bargain armor debuff (10 seconds)
            if (victim != null) {
                UUID victimUUID = victim.getUUID();
                DEATH_MARKED_ENTITIES.computeIfAbsent(victimUUID, k -> new HashMap<>()).put(playerUUID, currentTime);
            }

            // Check if undead command is primed and player hit someone
            Long primedTime = UNDEAD_COMMAND_PRIMED.get(playerUUID);
            if (primedTime != null && event.getEntity() instanceof LivingEntity) {
                // Player hit someone - use them as the target for undead command
                LivingEntity target = (LivingEntity) event.getEntity();
                UNDEAD_COMMAND_PRIMED.remove(playerUUID);

                player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                    boolean unlimited = cap.isAvatarOfDeathActive();
                    executeUndeadCommand(player, cap, currentTime, target, unlimited);
                });
                return; // Don't process lifelink on the same hit
            }

            // Handle lifelink healing
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                if (cap.isLifelinkActive() && currentTime < cap.getLifelinkEndTime()) {
                    // Lifelink is active - heal player for damage dealt
                    float damageDealt = event.getAmount();
                    float currentHealth = player.getHealth();
                    float maxHealth = player.getMaxHealth();

                    if (currentHealth < maxHealth) {
                        // Heal up to max health
                        float healAmount = Math.min(damageDealt, maxHealth - currentHealth);
                        player.setHealth(currentHealth + healAmount);
                        damageDealt -= healAmount;
                    }

                    // Any excess becomes absorption hearts
                    if (damageDealt > 0) {
                        float currentAbsorption = player.getAbsorptionAmount();
                        player.setAbsorptionAmount(currentAbsorption + damageDealt);
                    }
                }
            });
        }
    }

    /**
     * Apply Avatar of Death buffs while active
     */
    @SubscribeEvent
    public static void onAvatarOfDeathTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player))
            return;
        if (player.level().isClientSide)
            return;
        if (!isAnubisAvatar(player))
            return;

        long currentTime = player.level().getGameTime();

        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            if (!cap.isAvatarOfDeathActive() || currentTime >= cap.getAvatarOfDeathEndTime()) {
                // Avatar is not active or has expired
                if (cap.isAvatarOfDeathActive()) {
                    // Was just deactivated
                    cap.setAvatarOfDeathActive(false);
                    // Set cooldown (120 seconds = 2400 ticks)
                    cap.setAvatarOfDeathCooldown(currentTime + 2400);

                    // Remove creative flight
                    if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                    }

                    GodAvatarHudHelper.sendDeactivationMessage(player, "Avatar of Death",
                            GodAvatarHudHelper.COLOR_ANUBIS);
                }
                return;
            }

            // Apply massive buffs while Avatar of Death is active (matching Chaos
            // Incarnate)
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 9, false, false)); // Strength 10
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 4, false, false)); // Speed 5
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20, 4, false, false)); // Haste 5
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 4, false, false)); // Jump Boost 5
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 254, false, false)); // Resistance
                                                                                                          // 255
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20, 254, false, false)); // Absorption 255
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false)); // Glowing

            // Creative flight
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        });
    }

    /**
     * Handle lifelink expiration, undead command timeout, and Death's Bargain armor
     * system
     */
    @SubscribeEvent
    public static void onLifelinkTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player))
            return;
        if (player.level().isClientSide)
            return;

        long currentTime = player.level().getGameTime();
        UUID playerUUID = player.getUUID();

        // Only handle Anubis-specific logic for Anubis avatars
        if (isAnubisAvatar(player)) {
            // Apply Death's Bargain armor buff to self based on missing health
            applyDeathsBargainArmor(player);

            // Check for undead command timeout (10 seconds = 200 ticks)
            Long primedTime = UNDEAD_COMMAND_PRIMED.get(playerUUID);
            if (primedTime != null && currentTime >= primedTime + 200) {
                // 10 seconds elapsed without hitting anyone - execute with no target (detonate
                // mode)
                UNDEAD_COMMAND_PRIMED.remove(playerUUID);

                player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                    boolean unlimited = cap.isAvatarOfDeathActive();
                    executeUndeadCommand(player, cap, currentTime, null, unlimited);
                });
            }

            // Handle lifelink expiration
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                if (cap.isLifelinkActive() && currentTime >= cap.getLifelinkEndTime()) {
                    // Lifelink just expired
                    cap.setLifelinkActive(false);
                    // Set cooldown (40 seconds = 800 ticks) - skip if in Avatar of Death mode
                    if (!cap.isAvatarOfDeathActive()) {
                        cap.setLifelinkCooldown(currentTime + 800);
                    }
                    GodAvatarHudHelper.sendDeactivationMessage(player, "Lifelink", GodAvatarHudHelper.COLOR_ANUBIS);
                }
            });
        }

        // Death's Bargain passive: Apply armor debuff to recently hit entities (runs
        // every tick for all players)
        if (player.level() instanceof ServerLevel level) {
            applyDeathsBargainDebuff(level, currentTime);
        }
    }

    /**
     * Apply Death's Bargain armor buff to Anubis avatar based on missing health
     * +5 armor per heart lost
     */
    private static void applyDeathsBargainArmor(ServerPlayer player) {
        var armorAttribute = player.getAttribute(Attributes.ARMOR);
        if (armorAttribute == null)
            return;

        // Remove old modifier if it exists
        AttributeModifier oldModifier = armorAttribute.getModifier(DEATHS_BARGAIN_ARMOR_UUID);
        if (oldModifier != null) {
            armorAttribute.removeModifier(DEATHS_BARGAIN_ARMOR_UUID);
        }

        // Calculate armor bonus: 5 armor per heart lost (2.5 armor per HP lost)
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float heartsLost = (maxHealth - currentHealth) / 2.0f;
        double armorBonus = heartsLost * 5.0;

        // Apply new modifier if there's any armor to gain
        if (armorBonus > 0) {
            AttributeModifier newModifier = new AttributeModifier(
                    DEATHS_BARGAIN_ARMOR_UUID,
                    "Death's Bargain Armor",
                    armorBonus,
                    AttributeModifier.Operation.ADDITION);
            armorAttribute.addTransientModifier(newModifier);
        }
    }

    /**
     * Apply Death's Bargain armor debuff to entities recently hit by Anubis avatars
     * -5 armor per heart lost
     */
    private static void applyDeathsBargainDebuff(ServerLevel level, long currentTime) {
        // Clean up expired marks and apply debuff to marked entities
        DEATH_MARKED_ENTITIES.entrySet().removeIf(entry -> {
            UUID entityUUID = entry.getKey();
            Map<UUID, Long> attackerMarks = entry.getValue();

            // Remove expired marks (older than 10 seconds = 200 ticks)
            attackerMarks.entrySet().removeIf(mark -> currentTime - mark.getValue() > 200);

            // If entity still has active marks, apply armor debuff based on missing health
            if (!attackerMarks.isEmpty()) {
                Entity entity = level.getEntity(entityUUID);
                if (entity instanceof LivingEntity living) {
                    var armorAttribute = living.getAttribute(Attributes.ARMOR);
                    if (armorAttribute != null) {
                        // Remove old debuff
                        AttributeModifier oldModifier = armorAttribute.getModifier(DEATHS_BARGAIN_DEBUFF_UUID);
                        if (oldModifier != null) {
                            armorAttribute.removeModifier(DEATHS_BARGAIN_DEBUFF_UUID);
                        }

                        // Calculate armor debuff: -5 armor per heart lost (-2.5 armor per HP lost)
                        float currentHealth = living.getHealth();
                        float maxHealth = living.getMaxHealth();
                        float heartsLost = (maxHealth - currentHealth) / 2.0f;
                        double armorDebuff = -heartsLost * 5.0;

                        // Apply debuff if there's any armor to remove
                        if (armorDebuff < 0) {
                            AttributeModifier newModifier = new AttributeModifier(
                                    DEATHS_BARGAIN_DEBUFF_UUID,
                                    "Death's Bargain Debuff",
                                    armorDebuff,
                                    AttributeModifier.Operation.ADDITION);
                            armorAttribute.addTransientModifier(newModifier);

                            // Apply weakness effect as visual indicator
                            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 0, false, false));
                        }
                    }
                }
            } else {
                // No more marks - remove debuff if it exists
                Entity entity = level.getEntity(entityUUID);
                if (entity instanceof LivingEntity living) {
                    var armorAttribute = living.getAttribute(Attributes.ARMOR);
                    if (armorAttribute != null) {
                        AttributeModifier oldModifier = armorAttribute.getModifier(DEATHS_BARGAIN_DEBUFF_UUID);
                        if (oldModifier != null) {
                            armorAttribute.removeModifier(DEATHS_BARGAIN_DEBUFF_UUID);
                        }
                    }
                }
            }

            // Remove entity from map if no more active marks
            return attackerMarks.isEmpty();
        });
    }

    /**
     * Check if an entity is undead
     */
    private static boolean isUndead(Entity entity) {
        return entity instanceof Zombie ||
                entity instanceof Skeleton ||
                entity instanceof Phantom ||
                entity instanceof ZombieVillager ||
                entity instanceof Drowned ||
                entity instanceof Husk ||
                entity instanceof Stray ||
                entity instanceof WitherSkeleton ||
                entity instanceof ZombifiedPiglin ||
                entity instanceof Zoglin;
    }
}
