package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.boons.ActiveBoon;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.capabilities.PlayerBoonsCapability;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.SyncPlayerBoonsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Attaches the God Avatar capability to all players
 */
@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GodAvatarCapabilityHandler {
    
    private static final ResourceLocation GOD_AVATAR_CAP = new ResourceLocation(AfterlifeEntombedMod.MOD_ID, "god_avatar");
    private static final ResourceLocation PLAYER_BOONS_CAP = new ResourceLocation(AfterlifeEntombedMod.MOD_ID, "player_boons");
    
    // Store god info before player dies (during death event)
    private static final Map<UUID, GodType> playerDeathGods = new HashMap<>();
    // Store unlocked gods before player dies
    private static final Map<UUID, Set<GodType>> playerDeathUnlockedGods = new HashMap<>();
    // Store boons before player dies
    private static final Map<UUID, List<ActiveBoon>> playerDeathBoons = new HashMap<>();
    // Track which players just respawned
    private static final Map<UUID, Integer> playerRespawnCooldown = new HashMap<>();
    
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        // Log ALL deaths, not just players
        AfterlifeEntombedMod.LOGGER.info("=== DEATH EVENT FIRED for entity: {} ===", 
            event.getEntity().getClass().getSimpleName());
        
        if (event.getEntity() instanceof ServerPlayer player) {
            AfterlifeEntombedMod.LOGGER.info("=== DEATH EVENT for PLAYER {} ===", player.getGameProfile().getName());
            
            var cap = player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY);
            AfterlifeEntombedMod.LOGGER.info("Capability present on death: {}", cap.isPresent());
            
            if (cap.isPresent()) {
                var godCap = cap.resolve().get();
                GodType godAtDeath = godCap.getSelectedGod();
                Set<GodType> unlockedGods = new HashSet<>(godCap.getUnlockedGods());
                
                AfterlifeEntombedMod.LOGGER.info("BEFORE storing - Selected: {}, Unlocked: {} gods: {}", 
                    godAtDeath.name(),
                    unlockedGods.size(),
                    unlockedGods);
                
                playerDeathGods.put(player.getUUID(), godAtDeath);
                playerDeathUnlockedGods.put(player.getUUID(), unlockedGods);
                playerRespawnCooldown.put(player.getUUID(), 0);
                OriginValidationHandler.setPlayerInRespawnGrace(player.getUUID());
                
                // Also store boons before death
                player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(boonsCap -> {
                    List<ActiveBoon> boons = new ArrayList<>(boonsCap.getActiveBoons());
                    playerDeathBoons.put(player.getUUID(), boons);
                    AfterlifeEntombedMod.LOGGER.info("Stored {} boons/curses before death", boons.size());
                    for (ActiveBoon boon : boons) {
                        AfterlifeEntombedMod.LOGGER.info("  - Stored: {} ({})", boon.getType().getDisplayName(), boon.getType().name());
                    }
                });
                
                AfterlifeEntombedMod.LOGGER.info("AFTER storing - Map has selected: {}, Map has unlocked: {}", 
                    playerDeathGods.get(player.getUUID()).name(),
                    playerDeathUnlockedGods.get(player.getUUID()).size());
                    
                AfterlifeEntombedMod.LOGGER.info("=== END DEATH EVENT ===");
            } else {
                AfterlifeEntombedMod.LOGGER.warn("Capability NOT PRESENT on death for {}!", player.getGameProfile().getName());
            }
        }
    }
    
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            // Attach God Avatar capability
            GodAvatarCapability.GodAvatarProvider avatarProvider = new GodAvatarCapability.GodAvatarProvider();
            event.addCapability(GOD_AVATAR_CAP, avatarProvider);
            
            // Attach Player Boons capability
            PlayerBoonsCapability.PlayerBoonsProvider boonsProvider = new PlayerBoonsCapability.PlayerBoonsProvider();
            event.addCapability(PLAYER_BOONS_CAP, boonsProvider);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        AfterlifeEntombedMod.LOGGER.info("PlayerClone event triggered for player {} - isWasDeath: {}", 
            event.getEntity().getGameProfile().getName(), 
            event.isWasDeath());
        
        if (event.isWasDeath()) {
            ServerPlayer newPlayer = null;
            if (event.getEntity() instanceof ServerPlayer) {
                newPlayer = (ServerPlayer) event.getEntity();
            }
            
            final ServerPlayer finalPlayer = newPlayer;
            if (finalPlayer != null) {
                UUID playerUUID = finalPlayer.getUUID();
                
                // CRITICAL: Set respawn grace period FIRST to prevent OriginValidationHandler
                // from clearing the capability data before we restore it
                OriginValidationHandler.setPlayerInRespawnGrace(playerUUID);
                
                AfterlifeEntombedMod.LOGGER.info("=== CLONE EVENT for {} ===", 
                    finalPlayer.getGameProfile().getName());
                
                // BACKUP: If death event was cancelled or didn't fire, try to get data from old entity now
                if (!playerDeathGods.containsKey(playerUUID)) {
                    AfterlifeEntombedMod.LOGGER.warn("No death data stored! Death event may have been cancelled. Attempting backup retrieval from old entity...");
                    
                    event.getOriginal().getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(oldCap -> {
                        GodType godAtDeath = oldCap.getSelectedGod();
                        Set<GodType> unlockedGods = new HashSet<>(oldCap.getUnlockedGods());
                        
                        AfterlifeEntombedMod.LOGGER.info("BACKUP: Retrieved from old entity - Selected: {}, Unlocked: {} gods: {}", 
                            godAtDeath.name(),
                            unlockedGods.size(),
                            unlockedGods);
                        
                        playerDeathGods.put(playerUUID, godAtDeath);
                        playerDeathUnlockedGods.put(playerUUID, unlockedGods);
                        playerRespawnCooldown.put(playerUUID, 0);
                    });
                }
                
                AfterlifeEntombedMod.LOGGER.info("playerDeathGods map contains UUID: {}", 
                    playerDeathGods.containsKey(playerUUID));
                
                if (playerDeathGods.containsKey(playerUUID)) {
                    AfterlifeEntombedMod.LOGGER.info("Map stored data - Selected: {}, Unlocked: {}", 
                        playerDeathGods.get(playerUUID).name(),
                        playerDeathUnlockedGods.get(playerUUID) != null ? playerDeathUnlockedGods.get(playerUUID).size() : "NULL");
                }
                
                // Check if capabilities exist
                boolean oldCapExists = event.getOriginal().getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).isPresent();
                boolean newCapExists = event.getEntity().getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).isPresent();
                AfterlifeEntombedMod.LOGGER.info("Old cap exists: {}, New cap exists: {}", oldCapExists, newCapExists);
                
                // Clone God Avatar capability
                event.getOriginal().getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(oldCap -> {
                    // Old cap DOES exist - serialize its NBT
                    net.minecraft.nbt.CompoundTag capabilityNBT = oldCap.serializeNBT();
                    
                    AfterlifeEntombedMod.LOGGER.info("Serialized old cap NBT: Selected={}, UnlockedGods={}", 
                        capabilityNBT.getString("SelectedGod"),
                        capabilityNBT.getString("UnlockedGods"));
                    
                    // Copy NBT to new capability
                    event.getEntity().getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(newCap -> {
                        newCap.deserializeNBT(capabilityNBT);
                        
                        // Reset ability states but keep god selection and unlocked gods
                        newCap.setOneWithChaosActive(false);
                        newCap.setOneWithChaosTimeUsed(0);
                        newCap.setOneWithChaosCooldown(0);
                        newCap.setDamageNegationActive(false);
                        newCap.setStoredDamage(0);
                        newCap.setDamageNegationCooldown(0);
                        newCap.setDesertWalkerFlying(false);
                        newCap.setDesertWalkerCooldown(0);
                        newCap.setChaosIncarnateActive(false);
                        newCap.setChaosIncarnateCooldown(0);
                        newCap.setSolarFlareCooldown(0);
                        newCap.setPurifyingLightCooldown(0);
                        newCap.setPurifyingLightActive(false);
                        newCap.setPurifyingLightEndTime(0);
                        newCap.setHolyInfernoCooldown(0);
                        newCap.setAvatarOfSunCooldown(0);
                        newCap.setAvatarOfSunActive(false);
                        
                        AfterlifeEntombedMod.LOGGER.info("After cloning via NBT - Selected: {}, Unlocked gods: {}", 
                            newCap.getSelectedGod().name(),
                            newCap.getUnlockedGods().size());
                        
                        // Force sync to client immediately after respawn
                        if (finalPlayer.getServer() != null) {
                            GodAvatarSyncHandler.syncToClient(finalPlayer);
                        }
                    });
                });
                
                // Clone Player Boons capability using NBT serialization (boons persist through death)
                boolean oldBoonsExists = event.getOriginal().getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).isPresent();
                boolean newBoonsExists = event.getEntity().getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).isPresent();
                AfterlifeEntombedMod.LOGGER.info("Old boons cap exists: {}, New boons cap exists: {}", oldBoonsExists, newBoonsExists);
                
                // Try to clone from old player first
                if (oldBoonsExists) {
                    event.getOriginal().getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(oldBoons -> {
                        AfterlifeEntombedMod.LOGGER.info("OLD PLAYER boons before serialization - {} active boons/curses", 
                            oldBoons.getActiveBoons().size());
                        
                        for (ActiveBoon boon : oldBoons.getActiveBoons()) {
                            AfterlifeEntombedMod.LOGGER.info("  - Old boon: {} ({})", boon.getType().getDisplayName(), boon.getType().name());
                        }
                        
                        // Serialize the old boons to NBT
                        CompoundTag boonsNBT = oldBoons.serializeNBT();
                        
                        AfterlifeEntombedMod.LOGGER.info("Serialized NBT contains: {}", boonsNBT.toString());
                        
                        event.getEntity().getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(newBoons -> {
                            AfterlifeEntombedMod.LOGGER.info("NEW PLAYER boons before deserialization - {} active boons/curses", 
                                newBoons.getActiveBoons().size());
                            
                            // Deserialize the NBT into the new boons capability
                            newBoons.deserializeNBT(boonsNBT);
                            
                            AfterlifeEntombedMod.LOGGER.info("NEW PLAYER boons after deserialization - {} active boons/curses", 
                                newBoons.getActiveBoons().size());
                            
                            // Log each restored boon
                            for (ActiveBoon boon : newBoons.getActiveBoons()) {
                                AfterlifeEntombedMod.LOGGER.info("  - Restored boon: {} ({})", boon.getType().getDisplayName(), boon.getType().name());
                            }
                            
                            // Sync boons to client after death
                            if (finalPlayer != null) {
                                GodAvatarPackets.sendToPlayer(finalPlayer, new SyncPlayerBoonsPacket(newBoons.getActiveBoons()));
                                AfterlifeEntombedMod.LOGGER.info("Synced {} boons to client after death", newBoons.getActiveBoons().size());
                            }
                        });
                    });
                } else if (playerDeathBoons.containsKey(playerUUID)) {
                    // FALLBACK: Old capability doesn't exist, use stored boons from death event
                    List<ActiveBoon> storedBoons = playerDeathBoons.get(playerUUID);
                    AfterlifeEntombedMod.LOGGER.info("OLD PLAYER boons cap missing, using fallback storage - {} boons/curses", 
                        storedBoons.size());
                    
                    event.getEntity().getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(newBoons -> {
                        // Re-add all stored boons
                        for (ActiveBoon boon : storedBoons) {
                            newBoons.addBoon(boon);
                            AfterlifeEntombedMod.LOGGER.info("  - Restored from fallback: {} ({})", boon.getType().getDisplayName(), boon.getType().name());
                        }
                        
                        AfterlifeEntombedMod.LOGGER.info("After fallback restoration - {} boons/curses restored", 
                            newBoons.getActiveBoons().size());
                        
                        // Sync boons to client after death
                        if (finalPlayer != null) {
                            GodAvatarPackets.sendToPlayer(finalPlayer, new SyncPlayerBoonsPacket(newBoons.getActiveBoons()));
                            AfterlifeEntombedMod.LOGGER.info("Synced {} boons to client after death (fallback)", newBoons.getActiveBoons().size());
                        }
                        
                        // Clean up the stored boons
                        playerDeathBoons.remove(playerUUID);
                    });
                }
                
                // FALLBACK: If old cap doesn't exist, try using playerDeathGods map
                if (!oldCapExists && playerDeathGods.containsKey(playerUUID)) {
                    GodType storedGod = playerDeathGods.get(playerUUID);
                    Set<GodType> storedUnlockedGods = playerDeathUnlockedGods.get(playerUUID);
                    
                    event.getEntity().getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(newCap -> {
                        AfterlifeEntombedMod.LOGGER.info("Restoring from fallback storage - Selected: {}, Unlocked: {}", 
                            storedGod.name(),
                            storedUnlockedGods != null ? storedUnlockedGods.size() : 0);
                        
                        // Restore selected god
                        newCap.setSelectedGod(storedGod);
                        
                        // Restore all unlocked gods
                        if (storedUnlockedGods != null) {
                            for (GodType god : storedUnlockedGods) {
                                newCap.unlockGod(god);
                                AfterlifeEntombedMod.LOGGER.info("Restored unlocked god: {}", god.name());
                            }
                        }
                        
                        AfterlifeEntombedMod.LOGGER.info("After fallback restoration - Selected: {}, Unlocked gods: {}", 
                            newCap.getSelectedGod().name(),
                            newCap.getUnlockedGods().size());
                        
                        // Force sync to client
                        if (finalPlayer.getServer() != null) {
                            GodAvatarSyncHandler.syncToClient(finalPlayer);
                        }
                    });
                }
            }
        }
    }
    
    /**
     * Ensure capability data is saved when player data is saved
     */
    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                AfterlifeEntombedMod.LOGGER.info("Saving god avatar data for player {} - Selected: {}, Unlocked gods: {}", 
                    player.getGameProfile().getName(),
                    cap.getSelectedGod().name(),
                    cap.getUnlockedGods().size());
            });
        }
    }
    
    /**
     * Ensure capability data is loaded when player data is loaded
     */
    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                AfterlifeEntombedMod.LOGGER.info("Loaded god avatar data for player {} - Selected: {}, Unlocked gods: {}", 
                    player.getGameProfile().getName(),
                    cap.getSelectedGod().name(),
                    cap.getUnlockedGods().size());
            });
            
            // Also log loaded boons
            player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(cap -> {
                AfterlifeEntombedMod.LOGGER.info("Loaded {} boons/curses for player {}", 
                    cap.getActiveBoons().size(),
                    player.getGameProfile().getName());
            });
        }
    }
    
    /**
     * Sync boons to client when player logs in
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Sync boons to client
            player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(cap -> {
                GodAvatarPackets.sendToPlayer(player, new SyncPlayerBoonsPacket(cap.getActiveBoons()));
                AfterlifeEntombedMod.LOGGER.info("Synced {} boons/curses to client for player {}", 
                    cap.getActiveBoons().size(),
                    player.getGameProfile().getName());
            });
        }
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        
        UUID playerUUID = player.getUUID();
        
        // Check if this player just respawned
        if (playerRespawnCooldown.containsKey(playerUUID)) {
            int cooldown = playerRespawnCooldown.get(playerUUID);
            cooldown++;
            
            // Run for 12 seconds (240 ticks at 20 ticks/second)
            if (cooldown <= 240) {
                // Only run commands at specific intervals to avoid spam
                if (cooldown % 20 == 0) {  // Every 1 second
                    var server = player.getServer();
                    if (server != null) {
                        // Get the player's selected god and set the correct origin
                        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                            GodType selectedGod = cap.getSelectedGod();
                            
                            // CRITICAL: Only set origin if player actually has a god selected
                            // Don't interfere with players who aren't using god avatars
                            if (selectedGod == GodType.NONE) {
                                AfterlifeEntombedMod.LOGGER.info("Player {} has no god selected, skipping origin restoration", 
                                    player.getGameProfile().getName());
                                return;
                            }
                            
                            String originLayer = "afterlifeentombed:avatar_of_egypt"; // Default (shouldn't be reached)
                            
                            // Map god types to their origin layers
                            switch (selectedGod) {
                                case SETH -> originLayer = "afterlifeentombed:avatar_of_seth";
                                case RA -> originLayer = "afterlifeentombed:avatar_of_ra";
                                case SHU -> originLayer = "afterlifeentombed:avatar_of_shu";
                                case ANUBIS -> originLayer = "afterlifeentombed:avatar_of_anubis";
                                case THOTH -> originLayer = "afterlifeentombed:avatar_of_thoth";
                                case GEB -> originLayer = "afterlifeentombed:avatar_of_geb";
                                case HORUS -> originLayer = "afterlifeentombed:avatar_of_horus";
                                case ISIS -> originLayer = "afterlifeentombed:avatar_of_isis";
                                case NONE -> originLayer = "afterlifeentombed:avatar_of_egypt"; // Fallback
                            }
                            
                            // Set origin to the player's selected god using correct command syntax
                            server.getCommands().performPrefixedCommand(
                                server.createCommandSourceStack(),
                                "origin set " + player.getGameProfile().getName() + " origins:origin " + originLayer
                            );
                            
                            AfterlifeEntombedMod.LOGGER.info("Reapplying origin {} for respawned player {}", 
                                originLayer, player.getGameProfile().getName());
                        });
                    }
                }
                
                // At tick 5, revoke the phasing powers once
                if (cooldown == 5) {
                    var server = player.getServer();
                    if (server != null) {
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "power revoke " + player.getGameProfile().getName() + " afterlifeentombed:seth_one_with_chaos_active"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "power revoke " + player.getGameProfile().getName() + " afterlifeentombed:seth_chaos_incarnate_active"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "power revoke " + player.getGameProfile().getName() + " afterlifeentombed:ra_avatar_of_sun_active"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "power revoke " + player.getGameProfile().getName() + " afterlifeentombed:geb_avatar_of_earth_active"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "power revoke " + player.getGameProfile().getName() + " afterlifeentombed:horus_avatar_of_war_active"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "power revoke " + player.getGameProfile().getName() + " afterlifeentombed:horus_eye_of_protection_active"
                        );
                    }
                }
                
                // Update cooldown
                playerRespawnCooldown.put(playerUUID, cooldown);
            } else {
                // 12 seconds have passed, clean up
                playerRespawnCooldown.remove(playerUUID);
                // No longer giving godstone - god type persists on respawn
                playerDeathGods.remove(playerUUID);
            }
        }
    }
}

