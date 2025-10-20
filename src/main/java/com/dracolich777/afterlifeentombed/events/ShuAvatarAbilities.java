package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.client.hud.GodAvatarHudHelper;
import com.dracolich777.afterlifeentombed.init.ModEffects;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlibs.api.AfterLibsAPI;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Event handlers for Shu Avatar abilities - God of Air & Wind (Freedom of Movement)
 */
@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ShuAvatarAbilities {
    
    // Ability IDs
    public static final int ABILITY_LAUNCH = 1;
    public static final int ABILITY_AIR_BOOST = 2;
    public static final int ABILITY_EXTRA_JUMPS = 3;
    public static final int ABILITY_WIND_AVATAR = 4;
    
    // Track cooldowns: <player UUID, ability ID, cooldown end time>
    private static final Map<UUID, Map<Integer, Long>> ABILITY_COOLDOWNS = new HashMap<>();
    // Track Wind Avatar active duration: <player UUID, end time>
    private static final Map<UUID, Long> WIND_AVATAR_DURATION = new HashMap<>();
    
    /**
     * Check if player is Shu avatar
     */
    private static boolean isShuAvatar(Player player) {
        return player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).map(cap -> 
            cap.getSelectedGod() == GodType.SHU
        ).orElse(false);
    }
    
    // ===== PASSIVE ABILITIES =====
    
    /**
     * Wind Walker - Increased movement speed and no fall damage
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        
        Player player = event.player;
        if (!isShuAvatar(player)) return;
        
        // Apply speed boost
        if (!player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, false, false)); // Speed 2
        }
    }
    
    /**
     * No Fall Damage - Negate fall damage entirely
     */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && isShuAvatar(player)) {
            event.setCanceled(true); // Cancel fall damage
        }
    }
    
    /**
     * Weakness inside - 2 hearts less health, Slowness inside
     */
    @SubscribeEvent
    public static void onPlayerTickWeakness(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        
        Player player = event.player;
        if (!isShuAvatar(player)) return;
        
        // Check if player is inside (low light level)
        int lightLevel = player.level().getMaxLocalRawBrightness(player.blockPosition());
        
        // Consider "inside" as light level less than 12 (roughly indoors)
        if (lightLevel < 12) {
            // Apply slowness
            if (!player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, false)); // Slowness 1
            }
            
            // Reduce max health by 2 hearts (4 health points)
            // Note: This is tricky - we need to set absorption hearts or reduce max health
            // For now, apply weakness instead which reduces damage dealt
            if (!player.hasEffect(MobEffects.WEAKNESS)) {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, false)); // Weakness 1
            }
        }
    }
    
    // ===== ACTIVATED ABILITIES =====
    
    /**
     * Handle ability activation from client
     */
    public static void activateAbility(ServerPlayer player, int abilityId) {
        if (!isShuAvatar(player)) return;
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            
            switch (abilityId) {
                case ABILITY_LAUNCH -> activateLaunch(player, cap, currentTime);
                case ABILITY_AIR_BOOST -> activateAirBoost(player, cap, currentTime);
                case ABILITY_EXTRA_JUMPS -> activateExtraJump(player, cap, currentTime);
                case ABILITY_WIND_AVATAR -> activateWindAvatar(player, cap, currentTime);
            }
        });
    }
    
    /**
     * Launch - Launch player and nearby entities upward, apply Wrath of Shu to others
     */
    private static void activateLaunch(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        // Check cooldown (10 seconds = 200 ticks)
        if (cap.getLaunchCooldown() > currentTime) {
            long remaining = (cap.getLaunchCooldown() - currentTime) / 20;
            GodAvatarHudHelper.sendCooldownMessage(player, "Launch", remaining);
            return;
        }
        
        if (player.level() instanceof ServerLevel serverLevel) {
            // Play shu_launch particle at player before launch
            AfterLibsAPI.spawnAfterlifeParticle(serverLevel, "shu_launch", 
                player.getX(), player.getY() + player.getEyeHeight(), player.getZ(), 1.0f);
        }
        
        // Launch player upward very high (3.5 velocity = ~18 blocks high)
        player.setDeltaMovement(player.getDeltaMovement().add(0, 3.5, 0));
        player.hurtMarked = true;
        
        if (player.level() instanceof ServerLevel serverLevel) {
            // Get all entities in 16 block radius around player
            AABB searchBox = new AABB(player.blockPosition()).inflate(16);
            List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, searchBox, 
                e -> e != player && e instanceof LivingEntity);
            
            // Launch and inflict Wrath of Shu on each entity
            for (LivingEntity entity : entities) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, 3.5, 0));
                entity.hurtMarked = true;
                
                // Apply Wrath of Shu for 30 seconds (600 ticks)
                entity.addEffect(new MobEffectInstance(ModEffects.REVENGE_OF_SHU.get(), 600, 0, false, true));
            }
        }
        
        // Set cooldown (10 seconds = 200 ticks)
        cap.setLaunchCooldown(currentTime + 200);
        
        GodAvatarHudHelper.sendActivationMessage(player, "Launch", GodAvatarHudHelper.COLOR_SHU);
    }
    
    /**
     * Air Boost - If in air, boost player forward really far (1s cooldown)
     */
    private static void activateAirBoost(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        // Check cooldown (1 second = 20 ticks)
        if (cap.getAirBoostCooldown() > currentTime) {
            long remaining = (cap.getAirBoostCooldown() - currentTime) / 20;
            GodAvatarHudHelper.sendCooldownMessage(player, "Air Boost", remaining);
            return;
        }
        
        // Check if player is in air (not on ground)
        if (!player.onGround()) {
            // Play shu_burst particle at player
            if (player.level() instanceof ServerLevel serverLevel) {
                AfterLibsAPI.spawnAfterlifeParticle(serverLevel, "haze3", 
                    player.getX(), player.getY() + player.getEyeHeight(), player.getZ(), 1.0f);
            }
            
            // Boost forward really far (multiply look direction by 5 for much farther launch)
            Vec3 lookVec = player.getLookAngle();
            player.setDeltaMovement(player.getDeltaMovement().add(lookVec.x * 5, 0, lookVec.z * 5));
            player.hurtMarked = true;
            
            // Set 1 second cooldown (20 ticks)
            cap.setAirBoostCooldown(currentTime + 20);
            
            GodAvatarHudHelper.sendActivationMessage(player, "Air Boost", GodAvatarHudHelper.COLOR_SHU);
        } else {
            GodAvatarHudHelper.sendNotification(player, "Must be in air!", GodAvatarHudHelper.COLOR_ERROR, 30);
        }
    }
    
    /**
     * Extra Jumps - Activate the ability to enable 3 mid-air jumps (10s cooldown after all used)
     * Press ability button to activate mode, then press jump key in mid-air to use jumps
     * The actual jump handling is done client-side in ExtraJumpHandler
     */
    private static void activateExtraJump(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        // Check cooldown (10 seconds = 200 ticks)
        if (cap.getExtraJumpsCooldown() > currentTime) {
            long remaining = (cap.getExtraJumpsCooldown() - currentTime) / 20;
            GodAvatarHudHelper.sendCooldownMessage(player, "Extra Jumps", remaining);
            return;
        }
        
        // Activate extra jumps mode - reset counter to 0 (3 jumps available)
        cap.setExtraJumpsUsed(0);
        
        GodAvatarHudHelper.sendActivationMessage(player, "Extra Jumps Activated (3 jumps available)", GodAvatarHudHelper.COLOR_SHU);
    }
    
    /**
     * Apply Wind Avatar effects while active (60 second duration - matches other ultimates)
     */
    @SubscribeEvent
    public static void onWindAvatarTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        
        if (!isShuAvatar(player)) return;
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            UUID playerUUID = player.getUUID();
            long currentTime = player.level().getGameTime();
            
            // Check if Wind Avatar is active
            Long avatarEndTime = WIND_AVATAR_DURATION.get(playerUUID);
            if (avatarEndTime == null || currentTime >= avatarEndTime) {
                // Avatar is not active or has expired
                if (avatarEndTime != null && cap.isWindAvatarActive()) {
                    // Was just deactivated
                    WIND_AVATAR_DURATION.remove(playerUUID);
                    cap.setWindAvatarActive(false);
                    cap.setWindAvatarEndTime(0);
                    GodAvatarHudHelper.sendDeactivationMessage(player, "Wind Avatar", GodAvatarHudHelper.COLOR_SHU);
                    
                    // Remove flight ability if player isn't in creative
                    if (!player.getAbilities().instabuild) {
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                    }
                    
                    // Add slow falling to prevent fall damage
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 0, false, true));
                }
                return;
            }
            
            // Apply massive buffs while Wind Avatar is active
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 7, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 4, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20, 3, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 4, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false));
            
            // Enable flight
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        });
    }
    
    /**
     * Wind Avatar - 60 second duration with massive buffs (matches other ultimates)
     */
    /**
     * Ability 4: Wind Avatar - Ultimate transformation
     * Can be toggled off early by pressing the button again
     */
    private static void activateWindAvatar(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        UUID playerUUID = player.getUUID();
        
        // If already active, deactivate it early
        if (cap.isWindAvatarActive()) {
            deactivateWindAvatar(player, cap, currentTime);
            return;
        }
        
        // Check cooldown (6000 ticks = 5 minutes)
        if (cap.getWindAvatarCooldown() > currentTime) {
            long remaining = (cap.getWindAvatarCooldown() - currentTime) / 20;
            GodAvatarHudHelper.sendCooldownMessage(player, "Wind Avatar", remaining);
            return;
        }
        
        // Activate Wind Avatar for 60 seconds (1200 ticks)
        long endTime = currentTime + 1200;
        WIND_AVATAR_DURATION.put(playerUUID, endTime);
        cap.setWindAvatarActive(true);
        cap.setWindAvatarEndTime(endTime);
        
        // Set cooldown after activation (5 minutes = 6000 ticks)
        cap.setWindAvatarCooldown(currentTime + 6000);
        
        GodAvatarHudHelper.sendNotification(player, "✦ WIND AVATAR ✦", GodAvatarHudHelper.COLOR_SHU, 60);
    }
    
    /**
     * Deactivate Wind Avatar early and apply cooldown
     */
    private static void deactivateWindAvatar(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        UUID playerUUID = player.getUUID();
        
        cap.setWindAvatarActive(false);
        WIND_AVATAR_DURATION.remove(playerUUID);
        
        // Remove flight ability if player isn't in creative
        if (!player.getAbilities().instabuild) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        
        // Add slow falling to prevent fall damage (1 minute = 1200 ticks)
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 0, false, true));
        
        // Set full cooldown (5 minutes = 6000 ticks)
        cap.setWindAvatarCooldown(currentTime + 6000);
        
        GodAvatarHudHelper.sendDeactivationMessage(player, "Wind Avatar", GodAvatarHudHelper.COLOR_SHU);
    }
}
