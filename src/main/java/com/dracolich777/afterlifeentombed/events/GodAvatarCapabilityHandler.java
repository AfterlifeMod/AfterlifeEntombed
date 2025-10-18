package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Attaches the God Avatar capability to all players
 */
@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GodAvatarCapabilityHandler {
    
    private static final ResourceLocation GOD_AVATAR_CAP = new ResourceLocation(AfterlifeEntombedMod.MOD_ID, "god_avatar");
    
    // Store god info before player dies (during death event)
    private static final Map<UUID, GodType> playerDeathGods = new HashMap<>();
    // Track which players just respawned
    private static final Map<UUID, Integer> playerRespawnCooldown = new HashMap<>();
    
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var cap = player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY);
            if (cap.isPresent()) {
                GodType godAtDeath = cap.resolve().get().getSelectedGod();
                playerDeathGods.put(player.getUUID(), godAtDeath);
                playerRespawnCooldown.put(player.getUUID(), 0);
                OriginValidationHandler.setPlayerInRespawnGrace(player.getUUID());
            }
        }
    }
    
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            GodAvatarCapability.GodAvatarProvider provider = new GodAvatarCapability.GodAvatarProvider();
            event.addCapability(GOD_AVATAR_CAP, provider);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            ServerPlayer newPlayer = null;
            if (event.getEntity() instanceof ServerPlayer) {
                newPlayer = (ServerPlayer) event.getEntity();
            }
            
            final ServerPlayer finalPlayer = newPlayer;
            if (finalPlayer != null) {
                UUID playerUUID = finalPlayer.getUUID();
                
                if (playerDeathGods.containsKey(playerUUID)) {
                    GodType storedGod = playerDeathGods.get(playerUUID);
                    
                    var newCap = event.getEntity().getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY);
                    if (newCap.isPresent()) {
                        newCap.ifPresent(cap -> {
                            cap.setSelectedGod(storedGod);
                            
                            AfterlifeEntombedMod.LOGGER.info("Player {} respawned as god: {}", 
                                finalPlayer.getGameProfile().getName(),
                                storedGod.name());
                            
                            // Reset ability states but keep the god type
                            cap.setOneWithChaosActive(false);
                            cap.setOneWithChaosTimeUsed(0);
                            cap.setOneWithChaosCooldown(0);
                            cap.setDamageNegationActive(false);
                            cap.setStoredDamage(0);
                            cap.setDamageNegationCooldown(0);
                            cap.setDesertWalkerFlying(false);
                            cap.setDesertWalkerCooldown(0);
                            cap.setChaosIncarnateActive(false);
                            cap.setChaosIncarnateCooldown(0);
                            cap.setSolarFlareCooldown(0);
                            cap.setPurifyingLightCooldown(0);
                            cap.setPurifyingLightActive(false);
                            cap.setPurifyingLightEndTime(0);
                            cap.setHolyInfernoCooldown(0);
                            cap.setAvatarOfSunCooldown(0);
                            cap.setAvatarOfSunActive(false);
                        });
                    }
                }
            }
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
                        // Set origin to avatar_of_egypt every second (less spam)
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin set " + player.getGameProfile().getName() + " afterlifeentombed:avatar_of_egypt"
                        );
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

