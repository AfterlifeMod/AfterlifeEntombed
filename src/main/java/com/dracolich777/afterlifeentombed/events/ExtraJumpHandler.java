package com.dracolich777.afterlifeentombed.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.dracolich777.afterlibs.api.AfterLibsAPI;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.items.GodType;

import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side handler for Shu's Extra Jumps ability
 * Based on DoubleJumpHandler pattern
 */
@Mod.EventBusSubscriber(modid = "afterlifeentombed", value = Dist.CLIENT)
public class ExtraJumpHandler {
    
    private static final Map<UUID, Boolean> wasOnGround = new HashMap<>();
    private static final Map<UUID, Boolean> wasJumpingPreviousTick = new HashMap<>();
    private static final Map<UUID, Integer> jumpCooldown = new HashMap<>();
    // Track jumps used locally on client (since capability is read-only on client)
    private static final Map<UUID, Integer> clientJumpsUsed = new HashMap<>();
    private static final Map<UUID, Long> clientActivationTime = new HashMap<>();
    
    /**
     * Get the client-side tracked jumps used for the given player
     * Returns -1 if mode not active, 0-3 if active
     */
    public static int getClientJumpsUsed(UUID playerId) {
        return clientJumpsUsed.getOrDefault(playerId, -1);
    }
    
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof LocalPlayer)) return;
        
        LocalPlayer player = (LocalPlayer) event.player;
        UUID playerId = player.getUUID();
        
        // Check if player is Shu avatar
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            if (cap.getSelectedGod() != GodType.SHU) {
                wasOnGround.remove(playerId);
                wasJumpingPreviousTick.remove(playerId);
                jumpCooldown.remove(playerId);
                clientJumpsUsed.remove(playerId);
                clientActivationTime.remove(playerId);
                return;
            }
            
            // Get extra jumps state from capability (server syncs cooldown)
            long cooldownEnd = cap.getExtraJumpsCooldown();
            long currentTime = player.level().getGameTime();
            
            // Check if mode was just activated (capability changed from server)
            // Server sets extraJumpsUsed to 0 when activated
            int serverJumpsUsed = cap.getExtraJumpsUsed();
            Long lastActivation = clientActivationTime.get(playerId);
            
            // Detect activation from server (extraJumpsUsed went from -1 to 0)
            if (serverJumpsUsed == 0 && (lastActivation == null || currentTime - lastActivation > 100)) {
                // Mode was just activated
                clientJumpsUsed.put(playerId, 0);
                clientActivationTime.put(playerId, currentTime);
                com.dracolich777.afterlifeentombed.AfterlifeEntombedMod.LOGGER.info("Extra Jumps: MODE ACTIVATED - 3 jumps available");
            }
            
            // Get local jump count
            int jumpsUsed = clientJumpsUsed.getOrDefault(playerId, -1);
            
            // If on cooldown or mode not activated, don't do anything
            if (cooldownEnd > currentTime || jumpsUsed < 0) {
                wasOnGround.put(playerId, player.onGround());
                wasJumpingPreviousTick.put(playerId, player.input.jumping);
                return;
            }
            
            boolean currentlyOnGround = player.onGround();
            boolean previouslyOnGround = wasOnGround.getOrDefault(playerId, currentlyOnGround);
            boolean isJumpingNow = player.input.jumping;
            boolean wasJumpingBefore = wasJumpingPreviousTick.getOrDefault(playerId, false);
            
            // Debug logging
            if (jumpsUsed >= 0 && currentlyOnGround != previouslyOnGround) {
                com.dracolich777.afterlifeentombed.AfterlifeEntombedMod.LOGGER.info(
                    "Extra Jumps: onGround={}, wasOnGround={}, jumpsUsed={}, currentlyOnGround && !previouslyOnGround = {}",
                    currentlyOnGround, previouslyOnGround, jumpsUsed, (currentlyOnGround && !previouslyOnGround)
                );
            }
            
            // Reset extra jumps when landing - deactivates mode
            // Only reset if we were actually in the air before and mode is active (jumpsUsed >= 0)
            if (currentlyOnGround && !previouslyOnGround && jumpsUsed >= 0) {
                com.dracolich777.afterlifeentombed.AfterlifeEntombedMod.LOGGER.info("Extra Jumps: RESETTING - Landing detected");
                clientJumpsUsed.put(playerId, -1);
                jumpCooldown.put(playerId, 0);
                wasOnGround.put(playerId, currentlyOnGround);
                wasJumpingPreviousTick.put(playerId, isJumpingNow);
                return;
            }
            
            // Decrease cooldown
            int currentCooldown = jumpCooldown.getOrDefault(playerId, 0);
            if (currentCooldown > 0) {
                jumpCooldown.put(playerId, currentCooldown - 1);
            }
            
            // Handle extra jump - only trigger on jump key PRESS (not hold)
            if (!currentlyOnGround && jumpsUsed < 3) {
                // Check if jump key was just pressed (wasn't pressed before, is pressed now)
                if (isJumpingNow && !wasJumpingBefore && !player.isInWater() && !player.isInLava() && currentCooldown == 0) {
                    // Check if player is falling (not jumping up from previous jump)
                    if (player.getDeltaMovement().y < 0.1) {
                        // Perform extra jump
                        player.setDeltaMovement(player.getDeltaMovement().x, 0.6, player.getDeltaMovement().z);
                        
                        // Consume one jump locally
                        clientJumpsUsed.put(playerId, jumpsUsed + 1);
                        
                        jumpCooldown.put(playerId, 10); // 10 tick cooldown between jumps
                        
                        com.dracolich777.afterlifeentombed.AfterlifeEntombedMod.LOGGER.info("Extra Jumps: Used jump {} of 3", jumpsUsed + 1);
                        
                        // Play sound effect
                        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), 
                            net.minecraft.sounds.SoundEvents.ENDER_DRAGON_FLAP, 
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.5F, false);
                        
                        // Play haze_flash particle at feet position
                        AfterLibsAPI.spawnAfterlifeParticle(player.level(), "haze_flash", 
                            player.getX(), player.getY(), player.getZ(), 1.0f);
                        
                        // Check if all jumps are used
                        if (jumpsUsed + 1 >= 3) {
                            com.dracolich777.afterlifeentombed.AfterlifeEntombedMod.LOGGER.info("Extra Jumps: ALL JUMPS USED - Deactivating");
                            // All jumps used, deactivate locally (cooldown is managed by server)
                            clientJumpsUsed.put(playerId, -1);
                        }
                    }
                }
            }
            
            wasOnGround.put(playerId, currentlyOnGround);
            wasJumpingPreviousTick.put(playerId, isJumpingNow);
        });
    }
}
