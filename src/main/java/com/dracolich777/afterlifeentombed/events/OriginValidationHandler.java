package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.items.GodType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Validates that players with god avatars have the required Origins origin
 * Resets god to NONE if they lose the origin
 */
@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OriginValidationHandler {
    
    private static final int CHECK_INTERVAL = 100; // Check every 5 seconds (100 ticks)
    // Track players currently in respawn grace period
    private static final Map<UUID, Integer> respawnGracePeriod = new HashMap<>();
    
    public static void setPlayerInRespawnGrace(UUID uuid) {
        respawnGracePeriod.put(uuid, 600); // 30 seconds of grace
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;
        
        UUID playerUUID = serverPlayer.getUUID();
        
        // Update respawn grace period
        if (respawnGracePeriod.containsKey(playerUUID)) {
            int grace = respawnGracePeriod.get(playerUUID);
            grace--;
            if (grace <= 0) {
                respawnGracePeriod.remove(playerUUID);
            } else {
                respawnGracePeriod.put(playerUUID, grace);
            }
        }
        
        // Skip validation if player is in respawn grace period
        if (respawnGracePeriod.containsKey(playerUUID)) {
            return;
        }
        
        // Only check periodically to avoid performance issues
        if (serverPlayer.tickCount % CHECK_INTERVAL != 0) return;
        
        serverPlayer.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            GodType currentGod = cap.getSelectedGod();
            
            // If player has a god selected, verify they have the origin
            if (currentGod != GodType.NONE) {
                // Check if player has the "Avatar of Egypt" origin
                // Origins mod uses power tags - check if player has the god_avatar_tracker power
                boolean hasOrigin = hasAvatarOfEgyptOrigin(serverPlayer);
                
                if (!hasOrigin) {
                    // Player lost the origin - reset to NONE
                    cap.setSelectedGod(GodType.NONE);
                    
                    // Also reset all active abilities
                    cap.setOneWithChaosActive(false);
                    cap.setDamageNegationActive(false);
                    cap.setDesertWalkerFlying(false);
                    cap.setChaosIncarnateActive(false);
                    
                    AfterlifeEntombedMod.LOGGER.info("Player {} lost Avatar of Egypt origin, resetting god to NONE", 
                        serverPlayer.getName().getString());
                }
            }
        });
    }
    
    /**
     * Check if player has the "Avatar of Egypt" origin
     * This uses a simple tag-based check that works even without Origins installed
     */
    private static boolean hasAvatarOfEgyptOrigin(ServerPlayer player) {
        // Check for custom tag that can be applied via commands or datapack
        // This allows the mod to work with or without Origins
        if (player.getTags().contains("afterlifeentombed:has_avatar_origin")) {
            return true;
        }
        
        // Alternative: Check if player has been granted the origin via /avatar command
        // If they used /avatar set command, they implicitly have permission
        // This is handled by capability existence
        
        // For now, if Origins isn't installed, allow anyone to use the powers
        // (They still need to use /avatar set or godstones to activate)
        try {
            Class.forName("io.github.apace100.apoli.power.PowerTypeRegistry");
            // Origins is installed - check for the power
            // Note: Actual Origins integration would require Origins API dependency
            // For now, we'll use the tag-based system
            return player.getTags().contains("afterlifeentombed:has_avatar_origin");
        } catch (ClassNotFoundException e) {
            // Origins not installed - allow powers to work standalone
            return true;
        }
    }
}
