package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.client.hud.GodAvatarHudHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.function.Function;

/**
 * Event handlers for Horus Avatar abilities - God of War
 * 
 * Passives:
 * - Strength II while having any positive effect
 * - Slow falling after falling more than 10 blocks
 * 
 * Ability 1: Single Combat - Force 1v1 arena duel with time limit
 * Ability 2: Warrior Bond - Link health with target entity
 * Ability 3: Eye of Protection - Reflect damage to nearby entities (30s)
 * Ability 4: Avatar of War - Ultimate transformation (60 seconds, 5 min cooldown)
 */
@Mod.EventBusSubscriber
public class HorusAvatarAbilities {
    
    // Arena dimension key
    private static final ResourceKey<Level> ARENA_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        new ResourceLocation("afterlifeentombed", "horus_arena")
    );
    
    // Ability IDs
    public static final int ABILITY_SINGLE_COMBAT = 1;
    public static final int ABILITY_WARRIOR_BOND = 2;
    public static final int ABILITY_EYE_OF_PROTECTION = 3;
    public static final int ABILITY_AVATAR_OF_WAR = 4;
    
    // Track active arena combats: player UUID -> ArenaData
    private static final Map<UUID, ArenaData> ACTIVE_ARENAS = new HashMap<>();
    
    // Track warrior bonds: player UUID -> target entity UUID
    private static final Map<UUID, UUID> WARRIOR_BONDS = new HashMap<>();
    
    // Track fall distances for slow falling passive
    private static final Map<UUID, Double> FALL_DISTANCES = new HashMap<>();
    
    // Cooldown constants
    private static final long SINGLE_COMBAT_COOLDOWN = 3000; // 2.5 minutes
    private static final long WARRIOR_BOND_COOLDOWN = 600; // 30 seconds
    private static final long EYE_OF_PROTECTION_COOLDOWN = 1200; // 60 seconds
    private static final long EYE_OF_PROTECTION_DURATION = 600; // 30 seconds
    private static final long AVATAR_OF_WAR_COOLDOWN = 6000; // 5 minutes
    private static final long AVATAR_OF_WAR_DURATION = 1200; // 60 seconds
    
    private static final long ARENA_DURATION = 3600; // 3 minutes (180 seconds)
    private static final int ARENA_RADIUS = 40; // Increased from 20 to 40
    
    /**
     * Check if player is Horus avatar
     */
    private static boolean isHorusAvatar(Player player) {
        return player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY)
                .map(cap -> cap.getSelectedGod() == GodType.HORUS)
                .orElse(false);
    }
    
    /**
     * Main ability activation entry point
     */
    public static void activateAbility(ServerPlayer player, int abilityId) {
        if (!isHorusAvatar(player)) return;
        
        switch (abilityId) {
            case ABILITY_SINGLE_COMBAT -> activateSingleCombat(player);
            case ABILITY_WARRIOR_BOND -> activateWarriorBond(player);
            case ABILITY_EYE_OF_PROTECTION -> activateEyeOfProtection(player);
            case ABILITY_AVATAR_OF_WAR -> activateAvatarOfWar(player);
        }
    }
    
    /**
     * Passive effects tick
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!isHorusAvatar(player)) return;
        
        // Passive 1: Strength II while having positive effects
        boolean hasPositiveEffect = false;
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (effect.getEffect().isBeneficial()) {
                hasPositiveEffect = true;
                break;
            }
        }
        
        if (hasPositiveEffect) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 1, false, false));
        }
        
        // Passive 2: Slow falling after falling more than 10 blocks
        UUID playerId = player.getUUID();
        double currentY = player.getY();
        
        if (player.onGround()) {
            FALL_DISTANCES.remove(playerId);
        } else if (player.getDeltaMovement().y < 0) {
            // Falling
            double maxY = FALL_DISTANCES.getOrDefault(playerId, currentY);
            if (currentY > maxY) {
                FALL_DISTANCES.put(playerId, currentY);
            } else {
                double fallDistance = maxY - currentY;
                if (fallDistance > 10) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20, 0, false, false));
                }
            }
        } else if (player.getDeltaMovement().y > 0) {
            // Rising - update max Y
            FALL_DISTANCES.put(playerId, currentY);
        }
        
        // Handle arena combat timer
        if (ACTIVE_ARENAS.containsKey(playerId)) {
            handleArenaCombat(player);
        }
        
        // Handle warrior bond syncing
        if (WARRIOR_BONDS.containsKey(playerId)) {
            syncWarriorBond(player);
        }
        
        // Apply Eye of Protection buffs
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            long endTime = cap.getEyeOfProtectionEndTime();
            
            if (cap.isEyeOfProtectionActive() && currentTime < endTime) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 2, false, false));
            } else if (cap.isEyeOfProtectionActive()) {
                deactivateEyeOfProtection(player);
            }
        });
        
        // Apply Avatar of War buffs
        applyAvatarOfWarBuffs(player);
    }
    
    /**
     * Ability 1: Single Combat - Force 1v1 arena duel
     */
    private static void activateSingleCombat(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            
            // Check cooldown
            long cooldown = cap.getSingleCombatCooldown();
            if (currentTime < cooldown) {
                long remaining = (cooldown - currentTime) / 20;
                GodAvatarHudHelper.sendCooldownMessage(player, "Single Combat", remaining);
                return;
            }
            
            // Find target entity using raycast
            Entity target = raycastForEntity(player, 50);
            if (!(target instanceof LivingEntity livingTarget)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cNo valid target in crosshair"), true);
                return;
            }
            
            // Create arena
            createArena(player, livingTarget);
            
            // Set cooldown
            cap.setSingleCombatCooldown(currentTime + SINGLE_COMBAT_COOLDOWN);
            
            GodAvatarHudHelper.sendNotification(player, "✦ SINGLE COMBAT ✦", GodAvatarHudHelper.COLOR_SPECIAL, 60);
        });
    }
    
    /**
     * Create arena dimension and teleport combatants
     */
    private static void createArena(ServerPlayer player, LivingEntity target) {
        // Get the custom arena dimension
        ServerLevel arenaWorld = player.server.getLevel(ARENA_DIMENSION);
        if (arenaWorld == null) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cArena dimension not loaded!"), false);
            return;
        }
        
        // Arena center at a fixed position in the void dimension
        BlockPos arenaCenter = new BlockPos(0, 100, 0);
        
        // Build bedrock platform and walls
        buildArena(arenaWorld, arenaCenter);
        
        // Store original positions and dimension
        ArenaData arenaData = new ArenaData(
            player.getUUID(),
            target.getUUID(),
            player.level().dimension(),
            player.position(),
            target.position(),
            arenaWorld.getGameTime() + ARENA_DURATION,
            arenaCenter
        );
        
        ACTIVE_ARENAS.put(player.getUUID(), arenaData);
        ACTIVE_ARENAS.put(target.getUUID(), arenaData);
        
        // Teleport combatants
        BlockPos playerPos = arenaCenter.offset(-10, 1, 0);
        BlockPos targetPos = arenaCenter.offset(10, 1, 0);
        
        // Teleport player to arena
        player.teleportTo(arenaWorld, playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5, 
            player.getYRot(), player.getXRot());
        
        // Teleport target to arena (different method for players vs mobs)
        if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.teleportTo(arenaWorld, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5,
                targetPlayer.getYRot(), targetPlayer.getXRot());
        } else {
            // For non-player entities, use changeDimension with proper positioning
            target.changeDimension(arenaWorld, new net.minecraftforge.common.util.ITeleporter() {
                @Override
                public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, 
                                         float yaw, Function<Boolean, Entity> repositionEntity) {
                    Entity repositioned = repositionEntity.apply(false);
                    if (repositioned != null) {
                        repositioned.moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, yaw, entity.getXRot());
                    }
                    return repositioned;
                }
            });
        }
        
        // Apply glowing effect to both combatants to prevent visibility issues
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, (int)(ARENA_DURATION + 100), 0, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, (int)(ARENA_DURATION + 100), 0, false, false));
        
        // Also apply night vision to player
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, (int)(ARENA_DURATION + 100), 0, false, false));
        if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, (int)(ARENA_DURATION + 100), 0, false, false));
        }
        
        // Send messages
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c§lSINGLE COMBAT - 3 MINUTES"), false);
        if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.displayClientMessage(net.minecraft.network.chat.Component.literal("§c§lSINGLE COMBAT - 3 MINUTES"), false);
        }
    }
    
    /**
     * Build the arena structure (circular)
     */
    private static void buildArena(ServerLevel level, BlockPos center) {
        // Build circular bedrock floor and clear air above
        for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
            for (int z = -ARENA_RADIUS; z <= ARENA_RADIUS; z++) {
                // Only place if within circle
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= ARENA_RADIUS) {
                    level.setBlock(center.offset(x, -1, z), Blocks.BEDROCK.defaultBlockState(), 3);
                    // Clear multiple layers above
                    for (int y = 0; y < 20; y++) {
                        level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        
        // Build circular bedrock walls (15 blocks high) - fill all blocks at radius
        for (int y = 0; y < 15; y++) {
            for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
                for (int z = -ARENA_RADIUS; z <= ARENA_RADIUS; z++) {
                    double distance = Math.sqrt(x * x + z * z);
                    // Place bedrock if on or just outside the radius to ensure no gaps
                    if (distance >= ARENA_RADIUS - 0.5 && distance <= ARENA_RADIUS + 0.5) {
                        level.setBlock(center.offset(x, y, z), Blocks.BEDROCK.defaultBlockState(), 3);
                    }
                }
            }
        }
        
        // Add barrier ceiling at height 15 to prevent escape
        for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
            for (int z = -ARENA_RADIUS; z <= ARENA_RADIUS; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= ARENA_RADIUS) {
                    level.setBlock(center.offset(x, 15, z), Blocks.BARRIER.defaultBlockState(), 3);
                }
            }
        }
        
        // Add some glowstone for lighting
        for (int angle = 0; angle < 360; angle += 45) {
            double radians = Math.toRadians(angle);
            int x = (int) ((ARENA_RADIUS - 2) * Math.cos(radians));
            int z = (int) ((ARENA_RADIUS - 2) * Math.sin(radians));
            level.setBlock(center.offset(x, 5, z), Blocks.GLOWSTONE.defaultBlockState(), 3);
        }
    }
    
    /**
     * Disassemble arena (circular cleanup)
     */
    private static void disassembleArena(ServerLevel level, BlockPos center) {
        // Remove circular bedrock floor
        for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
            for (int z = -ARENA_RADIUS; z <= ARENA_RADIUS; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= ARENA_RADIUS) {
                    level.setBlock(center.offset(x, -1, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        
        // Remove circular walls - match build pattern
        for (int y = 0; y < 15; y++) {
            for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
                for (int z = -ARENA_RADIUS; z <= ARENA_RADIUS; z++) {
                    double distance = Math.sqrt(x * x + z * z);
                    if (distance >= ARENA_RADIUS - 0.5 && distance <= ARENA_RADIUS + 0.5) {
                        level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        
        // Remove barrier ceiling
        for (int x = -ARENA_RADIUS; x <= ARENA_RADIUS; x++) {
            for (int z = -ARENA_RADIUS; z <= ARENA_RADIUS; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= ARENA_RADIUS) {
                    level.setBlock(center.offset(x, 15, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        
        // Remove glowstone
        for (int angle = 0; angle < 360; angle += 45) {
            double radians = Math.toRadians(angle);
            int x = (int) ((ARENA_RADIUS - 2) * Math.cos(radians));
            int z = (int) ((ARENA_RADIUS - 2) * Math.sin(radians));
            level.setBlock(center.offset(x, 5, z), Blocks.AIR.defaultBlockState(), 3);
        }
    }
    
    /**
     * Handle arena combat timer and outcomes
     */
    private static void handleArenaCombat(ServerPlayer player) {
        UUID playerId = player.getUUID();
        ArenaData arena = ACTIVE_ARENAS.get(playerId);
        if (arena == null) return;
        
        long currentTime = player.level().getGameTime();
        
        // Check if time expired - kill both
        if (currentTime >= arena.endTime) {
            endArena(player.server, arena, null, true);
        }
    }
    
    /**
     * Handle death in arena
     */
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        
        UUID deadId = event.getEntity().getUUID();
        ArenaData arena = ACTIVE_ARENAS.get(deadId);
        
        if (arena != null) {
            // Cancel the death, teleport back, then kill the loser
            event.setCanceled(true);
            
            UUID winnerId = arena.challenger.equals(deadId) ? arena.target : arena.challenger;
            endArena(level.getServer(), arena, winnerId, false);
        }
    }
    
    /**
     * End arena and handle aftermath
     */
    private static void endArena(net.minecraft.server.MinecraftServer server, ArenaData arena, UUID winnerId, boolean timeExpired) {
        // Get the arena dimension
        ServerLevel arenaWorld = server.getLevel(ARENA_DIMENSION);
        
        if (arenaWorld != null) {
            disassembleArena(arenaWorld, arena.arenaCenter);
            
            // Find entities in arena dimension
            Entity challenger = arenaWorld.getEntity(arena.challenger);
            Entity target = arenaWorld.getEntity(arena.target);
            
            // Teleport back to original dimension
            ServerLevel originalWorld = server.getLevel(arena.originalDimension);
            
            if (originalWorld != null) {
                if (challenger != null) {
                    if (challenger instanceof ServerPlayer player) {
                        player.teleportTo(originalWorld, arena.challengerPos.x, arena.challengerPos.y, arena.challengerPos.z,
                            player.getYRot(), player.getXRot());
                    } else if (challenger instanceof LivingEntity living) {
                        challenger.teleportTo(arena.challengerPos.x, arena.challengerPos.y, arena.challengerPos.z);
                    }
                }
                
                if (target != null) {
                    if (target instanceof ServerPlayer player) {
                        player.teleportTo(originalWorld, arena.targetPos.x, arena.targetPos.y, arena.targetPos.z,
                            player.getYRot(), player.getXRot());
                    } else if (target instanceof LivingEntity living) {
                        target.teleportTo(arena.targetPos.x, arena.targetPos.y, arena.targetPos.z);
                    }
                }
            }
            
            // Kill the appropriate entity/entities
            if (timeExpired) {
                // Both die
                if (challenger instanceof LivingEntity living) living.kill();
                if (target instanceof LivingEntity living) living.kill();
            } else {
                // Winner survives, loser dies
                if (winnerId != null) {
                    if (arena.challenger.equals(winnerId) && target instanceof LivingEntity living) {
                        living.kill();
                    } else if (arena.target.equals(winnerId) && challenger instanceof LivingEntity living) {
                        living.kill();
                    }
                }
            }
        }
        
        // Clean up
        ACTIVE_ARENAS.remove(arena.challenger);
        ACTIVE_ARENAS.remove(arena.target);
    }
    
    /**
     * Ability 2: Warrior Bond - Link health with target
     */
    private static void activateWarriorBond(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            UUID playerId = player.getUUID();
            
            // Check if already active - deactivate
            if (WARRIOR_BONDS.containsKey(playerId)) {
                WARRIOR_BONDS.remove(playerId);
                GodAvatarHudHelper.sendDeactivationMessage(player, "Warrior Bond", GodAvatarHudHelper.COLOR_SPECIAL);
                return;
            }
            
            // Check cooldown
            long cooldown = cap.getWarriorBondCooldown();
            if (currentTime < cooldown) {
                long remaining = (cooldown - currentTime) / 20;
                GodAvatarHudHelper.sendCooldownMessage(player, "Warrior Bond", remaining);
                return;
            }
            
            // Find target entity using raycast
            Entity target = raycastForEntity(player, 50);
            if (!(target instanceof LivingEntity)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cNo valid target in crosshair"), true);
                return;
            }
            
            // Create bond
            WARRIOR_BONDS.put(playerId, target.getUUID());
            
            // Set cooldown
            cap.setWarriorBondCooldown(currentTime + WARRIOR_BOND_COOLDOWN);
            
            GodAvatarHudHelper.sendNotification(player, "✦ Warrior Bond Active ✦", GodAvatarHudHelper.COLOR_SPECIAL, 60);
        });
    }
    
    /**
     * Sync health between bonded entities (bidirectional)
     */
    private static void syncWarriorBond(ServerPlayer player) {
        UUID targetId = WARRIOR_BONDS.get(player.getUUID());
        if (targetId == null) return;
        
        Entity targetEntity = ((ServerLevel)player.level()).getEntity(targetId);
        if (!(targetEntity instanceof LivingEntity target) || !target.isAlive()) {
            WARRIOR_BONDS.remove(player.getUUID());
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cWarrior Bond broken"), true);
            return;
        }
        
        // Calculate average health and absorption for true synchronization
        float playerHealth = player.getHealth();
        float playerAbsorption = player.getAbsorptionAmount();
        float targetHealth = target.getHealth();
        float targetAbsorption = target.getAbsorptionAmount();
        
        // Average them for true link
        float avgHealth = (playerHealth + targetHealth) / 2.0f;
        float avgAbsorption = (playerAbsorption + targetAbsorption) / 2.0f;
        
        // Apply to both
        player.setHealth(Math.max(0.5f, avgHealth)); // Prevent death from sync
        player.setAbsorptionAmount(avgAbsorption);
        target.setHealth(Math.max(0.5f, avgHealth));
        target.setAbsorptionAmount(avgAbsorption);
    }
    
    /**
     * Handle damage to bonded entities
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        
        UUID damagedId = event.getEntity().getUUID();
        
        // Check if damaged entity is bonded to a Horus avatar
        for (Map.Entry<UUID, UUID> bond : WARRIOR_BONDS.entrySet()) {
            if (bond.getValue().equals(damagedId)) {
                // Target was damaged, damage the Horus avatar too
                Entity horusEntity = level.getEntity(bond.getKey());
                if (horusEntity instanceof ServerPlayer horus && isHorusAvatar(horus)) {
                    horus.hurt(event.getSource(), event.getAmount());
                }
                break;
            }
        }
        
        // Check if damaged entity IS a Horus avatar with a bond
        if (WARRIOR_BONDS.containsKey(damagedId) && event.getEntity() instanceof ServerPlayer player && isHorusAvatar(player)) {
            UUID targetId = WARRIOR_BONDS.get(damagedId);
            Entity targetEntity = level.getEntity(targetId);
            if (targetEntity instanceof LivingEntity target) {
                target.hurt(event.getSource(), event.getAmount());
            }
        }
        
        // Eye of Protection damage reflection - only to the attacker
        if (event.getEntity() instanceof ServerPlayer player && isHorusAvatar(player)) {
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                if (cap.isEyeOfProtectionActive()) {
                    // Reflect damage only to the entity that attacked
                    Entity attacker = event.getSource().getEntity();
                    if (attacker instanceof LivingEntity livingAttacker) {
                        livingAttacker.hurt(level.damageSources().magic(), event.getAmount());
                    }
                }
            });
        }
    }
    
    /**
     * Ability 3: Eye of Protection - Regen + damage reflection
     */
    private static void activateEyeOfProtection(ServerPlayer player) {
        com.dracolich777.afterlifeentombed.AfterlifeEntombedMod.LOGGER.info("Eye of Protection activated for player {}", player.getGameProfile().getName());
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            
            com.dracolich777.afterlifeentombed.AfterlifeEntombedMod.LOGGER.info("Current time: {}, Already active: {}, Cooldown: {}", 
                currentTime, cap.isEyeOfProtectionActive(), cap.getEyeOfProtectionCooldown());
            
            // Check if already active - deactivate
            if (cap.isEyeOfProtectionActive()) {
                deactivateEyeOfProtection(player);
                return;
            }
            
            // Check cooldown
            long cooldown = cap.getEyeOfProtectionCooldown();
            if (currentTime < cooldown) {
                long remaining = (cooldown - currentTime) / 20;
                GodAvatarHudHelper.sendCooldownMessage(player, "Protection", remaining);
                return;
            }
            
            // Activate
            cap.setEyeOfProtectionActive(true);
            cap.setEyeOfProtectionEndTime(currentTime + EYE_OF_PROTECTION_DURATION);
            
            com.dracolich777.afterlifeentombed.AfterlifeEntombedMod.LOGGER.info("Setting Eye of Protection active, end time: {}", currentTime + EYE_OF_PROTECTION_DURATION);
            
            // Grant the Origins toggle power
            var server = player.getServer();
            if (server != null) {
                server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    "power grant " + player.getGameProfile().getName() + " afterlifeentombed:horus_eye_of_protection_active"
                );
            }
            
            GodAvatarHudHelper.sendNotification(player, "✦ EYE OF PROTECTION ✦", GodAvatarHudHelper.COLOR_SPECIAL, 60);
        });
    }
    
    private static void deactivateEyeOfProtection(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            cap.setEyeOfProtectionActive(false);
            cap.setEyeOfProtectionEndTime(0);
            
            // Revoke the Origins toggle power
            var server = player.getServer();
            if (server != null) {
                server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    "power revoke " + player.getGameProfile().getName() + " afterlifeentombed:horus_eye_of_protection_active"
                );
            }
            
            // Set cooldown
            long cooldownTime = player.level().getGameTime() + EYE_OF_PROTECTION_COOLDOWN;
            cap.setEyeOfProtectionCooldown(cooldownTime);
            
            GodAvatarHudHelper.sendDeactivationMessage(player, "Protection", GodAvatarHudHelper.COLOR_SPECIAL);
        });
    }
    
    /**
     * Ability 4: Avatar of War - Ultimate transformation
     */
    private static void activateAvatarOfWar(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            
            // Check if already active - deactivate early
            if (cap.isAvatarOfWarActive()) {
                deactivateAvatarOfWar(player);
                return;
            }
            
            // Check cooldown
            long cooldown = cap.getAvatarOfWarCooldown();
            if (currentTime < cooldown) {
                long remaining = (cooldown - currentTime) / 20;
                GodAvatarHudHelper.sendCooldownMessage(player, "Avatar of War", remaining);
                return;
            }
            
            // Activate
            cap.setAvatarOfWarActive(true);
            long endTime = currentTime + AVATAR_OF_WAR_DURATION;
            cap.setAvatarOfWarEndTime(endTime);
            
            // Grant the Origins toggle power
            var server = player.getServer();
            if (server != null) {
                server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    "power grant " + player.getGameProfile().getName() + " afterlifeentombed:horus_avatar_of_war_active"
                );
            }
            
            GodAvatarHudHelper.sendNotification(player, "✦ AVATAR OF WAR ✦", GodAvatarHudHelper.COLOR_SPECIAL, 60);
        });
    }
    
    private static void deactivateAvatarOfWar(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            cap.setAvatarOfWarActive(false);
            cap.setAvatarOfWarEndTime(0);
            
            // Remove creative flight
            if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            
            // Revoke the Origins toggle power
            var server = player.getServer();
            if (server != null) {
                server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    "power revoke " + player.getGameProfile().getName() + " afterlifeentombed:horus_avatar_of_war_active"
                );
            }
            
            // Grant slow falling for 1 minute after ultimate ends
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 0, false, false));
            
            // Set cooldown
            long cooldownTime = player.level().getGameTime() + AVATAR_OF_WAR_COOLDOWN;
            cap.setAvatarOfWarCooldown(cooldownTime);
            
            GodAvatarHudHelper.sendDeactivationMessage(player, "Avatar of War", GodAvatarHudHelper.COLOR_SPECIAL);
        });
    }
    
    /**
     * Apply Avatar of War buffs
     */
    private static void applyAvatarOfWarBuffs(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            if (!cap.isAvatarOfWarActive()) return;
            
            long currentTime = player.level().getGameTime();
            long endTime = cap.getAvatarOfWarEndTime();
            
            // Check if expired
            if (currentTime >= endTime) {
                deactivateAvatarOfWar(player);
                return;
            }
            
            // Apply ultimate buffs
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 6, false, false)); // Resistance VII
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 254, false, false)); // Strength 255
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 254, false, false)); // Regen 255
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 40, 254, false, false)); // Absorption 255
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 6, false, false)); // Haste VII
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 6, false, false)); // Speed VII
            
            // Creative flight
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            
            // Instant build (no cooldowns on other abilities during ultimate)
            cap.setSingleCombatCooldown(0);
            cap.setWarriorBondCooldown(0);
            cap.setEyeOfProtectionCooldown(0);
        });
    }
    
    /**
     * Raycast for entity player is looking at
     */
    private static Entity raycastForEntity(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 end = start.add(lookVec.scale(range));
        
        // Create a slightly inflated search box along the ray
        AABB searchBox = new AABB(start, end).inflate(1.0);
        List<Entity> entities = player.level().getEntities(player, searchBox);
        
        Entity closestEntity = null;
        double closestDistance = range;
        
        // Check each entity for intersection with the ray
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity) || entity.equals(player)) {
                continue;
            }
            
            // Get entity bounding box
            AABB entityBox = entity.getBoundingBox().inflate(0.3); // Slightly inflate for easier targeting
            
            // Check if ray intersects with entity bounding box
            java.util.Optional<Vec3> hitVec = entityBox.clip(start, end);
            
            if (hitVec.isPresent()) {
                double distance = start.distanceTo(hitVec.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }
        
        return closestEntity;
    }
    
    /**
     * Arena data class
     */
    private static class ArenaData {
        final UUID challenger;
        final UUID target;
        final net.minecraft.resources.ResourceKey<Level> originalDimension;
        final Vec3 challengerPos;
        final Vec3 targetPos;
        final long endTime;
        final BlockPos arenaCenter;
        
        ArenaData(UUID challenger, UUID target, net.minecraft.resources.ResourceKey<Level> originalDimension,
                  Vec3 challengerPos, Vec3 targetPos, long endTime, BlockPos arenaCenter) {
            this.challenger = challenger;
            this.target = target;
            this.originalDimension = originalDimension;
            this.challengerPos = challengerPos;
            this.targetPos = targetPos;
            this.endTime = endTime;
            this.arenaCenter = arenaCenter;
        }
    }
}
