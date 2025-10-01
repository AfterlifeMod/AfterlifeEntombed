package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.init.ModItems;
import com.dracolich777.afterlifeentombed.network.NetworkHandler;
import com.dracolich777.afterlifeentombed.network.ParticleEffectPacket;
import com.dracolich777.afterlifeentombed.util.ParticleManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "afterlifeentombed", value = Dist.CLIENT)
public class DoubleJumpHandler {
    
    private static final Map<UUID, Boolean> hasDoubleJumped = new HashMap<>();
    private static final Map<UUID, Boolean> wasOnGround = new HashMap<>();
    private static final Map<UUID, Boolean> wasJumpingPreviousTick = new HashMap<>();
    private static final Map<UUID, Integer> jumpCooldown = new HashMap<>();
    
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof LocalPlayer)) return;
        
        LocalPlayer player = (LocalPlayer) event.player;
        UUID playerId = player.getUUID();
        
        // Check if player has Breath of Shu equipped
        boolean hasBreathOfShu = CuriosApi.getCuriosInventory(player).map(inv -> {
            return inv.findFirstCurio(ModItems.BREATH_OF_SHU.get()).isPresent();
        }).orElse(false);
        
        if (!hasBreathOfShu) {
            hasDoubleJumped.remove(playerId);
            wasOnGround.remove(playerId);
            wasJumpingPreviousTick.remove(playerId);
            jumpCooldown.remove(playerId);
            return;
        }
        
        boolean currentlyOnGround = player.onGround();
        boolean previouslyOnGround = wasOnGround.getOrDefault(playerId, true);
        boolean isJumpingNow = player.input.jumping;
        boolean wasJumpingBefore = wasJumpingPreviousTick.getOrDefault(playerId, false);
        
        // Reset double jump when landing
        if (currentlyOnGround && !previouslyOnGround) {
            hasDoubleJumped.put(playerId, false);
            jumpCooldown.put(playerId, 0);
        }
        
        // Decrease cooldown
        int currentCooldown = jumpCooldown.getOrDefault(playerId, 0);
        if (currentCooldown > 0) {
            jumpCooldown.put(playerId, currentCooldown - 1);
        }
        
        // Handle double jump - only trigger on jump key PRESS (not hold)
        if (!currentlyOnGround && !hasDoubleJumped.getOrDefault(playerId, false)) {
            // Check if jump key was just pressed (wasn't pressed before, is pressed now)
            if (isJumpingNow && !wasJumpingBefore && !player.isInWater() && !player.isInLava() && currentCooldown == 0) {
                // Check if player is falling (not jumping up from previous jump)
                if (player.getDeltaMovement().y < 0.1) {
                    // Perform double jump
                    player.setDeltaMovement(player.getDeltaMovement().x, 0.6, player.getDeltaMovement().z);
                    hasDoubleJumped.put(playerId, true);
                    jumpCooldown.put(playerId, 10); // 10 tick cooldown
                    
                    // Play sound effect
                    player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), 
                        net.minecraft.sounds.SoundEvents.ENDER_DRAGON_FLAP, 
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.5F, false);
                    
                    // Play multiple shu_jump particles at normal scale underneath the player
                    for (int i = 0; i < 5; i++) {
                        double offsetX = (Math.random() - 0.5) * 0.8;
                        double offsetZ = (Math.random() - 0.5) * 0.8;
                        ParticleManager.spawnParticle(player.level(), "shu", 
                            player.getX() + offsetX, player.getY() - 0.3, player.getZ() + offsetZ, 0.5f, 0.5f, 0.5f);
                    }
                }
            }
        }
        
        wasOnGround.put(playerId, currentlyOnGround);
        wasJumpingPreviousTick.put(playerId, isJumpingNow);
    }
}