package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.SyncGodAvatarDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Syncs god avatar capability data from server to client for HUD display
 */
@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GodAvatarSyncHandler {
    
    private static final int SYNC_INTERVAL = 10; // Sync every 10 ticks (0.5 seconds)
    
    /**
     * Sync capability data when player logs in
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }
    
    /**
     * Sync capability data when player respawns
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }
    
    /**
     * Sync capability data when player changes dimension
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }
    
    /**
     * Periodically sync capability data to client for real-time HUD updates
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;
        
        // Only sync periodically to reduce network traffic
        if (serverPlayer.tickCount % SYNC_INTERVAL != 0) return;
        
        syncToClient(serverPlayer);
    }
    
    /**
     * Send full capability data to client
     */
    public static void syncToClient(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            SyncGodAvatarDataPacket packet = new SyncGodAvatarDataPacket(
                cap.getSelectedGod(),
                cap.isOneWithChaosActive(),
                cap.getOneWithChaosTimeUsed(),
                cap.getOneWithChaosCooldown(),
                cap.isDamageNegationActive(),
                cap.getStoredDamage(),
                cap.getDamageNegationCooldown(),
                cap.isDesertWalkerFlying(),
                cap.getDesertWalkerCooldown(),
                cap.isChaosIncarnateActive(),
                cap.getChaosIncarnateCooldown(),
                cap.getSolarFlareCooldown(),
                cap.getPurifyingLightCooldown(),
                cap.isPurifyingLightActive(),
                cap.getPurifyingLightEndTime(),
                cap.getHolyInfernoCooldown(),
                cap.getAvatarOfSunCooldown(),
                cap.isAvatarOfSunActive(),
                cap.getLaunchCooldown(),
                cap.getAirBoostCooldown(),
                cap.getWindAvatarCooldown(),
                cap.isWindAvatarActive(),
                cap.getWindAvatarEndTime(),
                cap.getExtraJumpsUsed(),
                cap.getExtraJumpsCooldown(),
                cap.getUndeadCommandCooldown(),
                cap.getLifelinkCooldown(),
                cap.isLifelinkActive(),
                cap.getLifelinkEndTime(),
                cap.getSummonUndeadCooldown(),
                cap.getAvatarOfDeathCooldown(),
                cap.isAvatarOfDeathActive(),
                cap.getAvatarOfDeathEndTime()
            );
            
            GodAvatarPackets.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
        });
    }
}
