package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.init.ModItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "afterlifeentombed", value = Dist.CLIENT)
public class DoubleJumpHandler {
    
    private static final Map<UUID, Boolean> hasDoubleJumped = new HashMap<>();
    private static final Map<UUID, Boolean> wasOnGround = new HashMap<>();
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
            jumpCooldown.remove(playerId);
            return;
        }
        
        boolean currentlyOnGround = player.onGround();
        boolean previouslyOnGround = wasOnGround.getOrDefault(playerId, true);
        
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
        
        // Handle double jump
        if (!currentlyOnGround && !previouslyOnGround && !hasDoubleJumped.getOrDefault(playerId, false)) {
            if (player.input.jumping && !player.isInWater() && !player.isInLava() && currentCooldown == 0) {
                // Check if player is falling (not jumping up)
                if (player.getDeltaMovement().y < 0.1) {
                    // Perform double jump
                    player.setDeltaMovement(player.getDeltaMovement().x, 0.6, player.getDeltaMovement().z);
                    hasDoubleJumped.put(playerId, true);
                    jumpCooldown.put(playerId, 10); // 10 tick cooldown
                    
                    // Play sound effect
                    player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), 
                        net.minecraft.sounds.SoundEvents.ENDER_DRAGON_FLAP, 
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.5F, false);
                }
            }
        }
        
        wasOnGround.put(playerId, currentlyOnGround);
    }
}