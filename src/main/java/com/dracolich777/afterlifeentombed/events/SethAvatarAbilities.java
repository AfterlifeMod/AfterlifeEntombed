
package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.client.hud.GodAvatarHudHelper;
import com.dracolich777.afterlifeentombed.init.ModEffects;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.items.GodstoneItem;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.SyncGodAvatarPacket;
import com.dracolich777.afterlibs.api.AfterLibsAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Event handlers for Seth Avatar abilities
 */
@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SethAvatarAbilities {
    
    // Ability IDs
    public static final int ABILITY_ONE_WITH_CHAOS = 1;
    public static final int ABILITY_DAMAGE_NEGATION = 2;
    public static final int ABILITY_DESERT_WALKER = 3;
    public static final int ABILITY_CHAOS_INCARNATE = 4;
    
    private static final UUID CHAOS_INCARNATE_SIZE_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID CHAOS_INCARNATE_SPEED_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    
    /**
     * Check if player has Agent of Gods origin
     * Uses capability presence to determine if player has avatar powers
     */
    private static boolean hasAgentOfGodsOrigin(Player player) {
        return player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).isPresent();
    }
    
    /**
     * Check if player is Seth avatar
     */
    private static boolean isSethAvatar(Player player) {
        return player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).map(cap -> 
            cap.getSelectedGod() == GodType.SETH
        ).orElse(false);
    }
    
    // ===== PASSIVE ABILITIES =====
    
    /**
     * Trickster God - Hitting an entity in the back deals 3x damage and inflicts Revenge of Seth
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player) {
            Player player = (Player) event.getSource().getEntity();
            if (isSethAvatar(player) && event.getEntity() instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) event.getEntity();
                // Check if attacking from behind
                Vec3 playerPos = player.position();
                Vec3 targetPos = target.position();
                Vec3 targetLook = target.getLookAngle();
                Vec3 toPlayer = playerPos.subtract(targetPos).normalize();
                
                // If dot product < 0, player is behind target (opposite direction of look)
                double dot = targetLook.dot(toPlayer);
                if (dot < -0.5) { // Player is behind (negative dot = opposite direction)
                    event.setAmount(event.getAmount() * 3.0f);
                    // Apply Revenge of Seth for 30 seconds (600 ticks)
                    target.addEffect(new MobEffectInstance(ModEffects.REVENGE_OF_SETH.get(), 600, 0, false, true));
                    // Backstab notification via HUD
                    if (player instanceof ServerPlayer serverPlayer) {
                        GodAvatarHudHelper.sendNotification(serverPlayer, "Backstab!", GodAvatarHudHelper.COLOR_SPECIAL, 20);
                    }
                }
            }
        }
    }
    
    /**
     * Agent of Evil - Apply nightvision and immunity to negative effects
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        
        Player player = event.player;
        if (!isSethAvatar(player)) return;
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            
            // Apply Night Vision
            if (!player.hasEffect(MobEffects.NIGHT_VISION)) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false));
            }
            
            // Fire immunity handled in LivingAttackEvent
            
            // Handle One with Chaos ability timing
            if (cap.isOneWithChaosActive()) {
                int timeUsed = cap.getOneWithChaosTimeUsed();
                timeUsed++;
                cap.setOneWithChaosTimeUsed(timeUsed);
                
                // 2 minutes = 2400 ticks
                if (timeUsed >= 2400 && player instanceof ServerPlayer serverPlayer) {
                    deactivateOneWithChaos(serverPlayer, cap);
                }
            }
            
            // Handle Damage Negation timer
            if (cap.isDamageNegationActive()) {
                long activationTime = currentTime - cap.getDamageNegationCooldown();
                // 10 seconds = 200 ticks - notification handled by HUD display
            }
            
            // Handle Chaos Incarnate effects
            if (cap.isChaosIncarnateActive()) {
                long activationTime = currentTime - cap.getChaosIncarnateCooldown();
                
                // Apply all buffs
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 9, false, false)); // Strength 10
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 4, false, false)); // Speed 5
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20, 4, false, false)); // Haste 5
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 4, false, false)); // Jump Boost 5
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 254, false, false)); // Resistance 255
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20, 254, false, false)); // Absorption 255
                
                // Creative flight
                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
                
                // Set size to 10 feet tall (about 3 blocks)
                // Note: This requires additional entity size modification which is complex in vanilla
                // You may need to use a mod like Pehkui for proper size scaling
                
                // 1 minute = 1200 ticks
                if (activationTime >= 1200 && player instanceof ServerPlayer serverPlayer) {
                    deactivateChaosIncarnate(serverPlayer, cap, currentTime);
                }
            }
            
            // Handle Desert Walker flight
            if (cap.isDesertWalkerFlying() && player.onGround()) {
                cap.setDesertWalkerFlying(false);
                cap.setDesertWalkerCooldown(currentTime + 1200); // 1 minute cooldown
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
                if (player instanceof ServerPlayer serverPlayer) {
                    GodAvatarHudHelper.sendNotification(serverPlayer, "Flight ended", GodAvatarHudHelper.COLOR_SETH, 30);
                }
            }
        });
    }
    
    /**
     * Agent of Evil - Immunity to negative effects
     */
    @SubscribeEvent
    public static void onPotionApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player && isSethAvatar(player)) {
            MobEffect effect = event.getEffectInstance().getEffect();
            // Block harmful effects
            if (!effect.isBeneficial()) {
                event.setResult(Event.Result.DENY);
            }
        }
    }
    
    /**
     * Agent of Evil - Fire and lava immunity
     */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (isSethAvatar(player)) {
                // Check for fire or lava damage
                String damageType = event.getSource().getMsgId();
                if (damageType.contains("fire") || damageType.contains("lava") || 
                    damageType.contains("hot") || damageType.contains("flame")) {
                    event.setCanceled(true);
                }
            }
        }
    }
    
    /**
     * Chaos vs. Order - Gold block damage and gold weapon vulnerability
     */
    @SubscribeEvent
    public static void onGoldDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof Player player && isSethAvatar(player)) {
            // Check if damaged by gold weapon
            if (event.getSource().getDirectEntity() instanceof LivingEntity attacker) {
                ItemStack weapon = attacker.getMainHandItem();
                if (weapon.is(Items.GOLDEN_SWORD) || weapon.is(Items.GOLDEN_AXE)) {
                    event.setAmount(event.getAmount() * 2.0f);
                }
            }
        }
    }
    
    /**
     * Chaos vs. Order - Damage from standing on gold blocks
     */
    @SubscribeEvent
    public static void onPlayerTickGoldBlocks(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        
        Player player = event.player;
        if (!isSethAvatar(player)) return;
        
        BlockPos belowPos = player.blockPosition().below();
        if (player.level().getBlockState(belowPos).is(Blocks.GOLD_BLOCK)) {
            player.hurt(player.damageSources().magic(), 2.0f);
        }
    }
    
    // ===== ACTIVATED ABILITIES =====
    
    /**
     * Handle ability activation from client
     */
    public static void activateAbility(ServerPlayer player, int abilityId) {
        if (!isSethAvatar(player)) return;
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            
            switch (abilityId) {
                case ABILITY_ONE_WITH_CHAOS -> toggleOneWithChaos(player, cap, currentTime);
                case ABILITY_DAMAGE_NEGATION -> toggleDamageNegation(player, cap, currentTime);
                case ABILITY_DESERT_WALKER -> activateDesertWalker(player, cap, currentTime);
                case ABILITY_CHAOS_INCARNATE -> activateChaosIncarnate(player, cap, currentTime);
            }
        });
    }
    
    /**
     * One with Chaos - Invisibility and phasing (via Origins powers)
     */
    private static void toggleOneWithChaos(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        // Check if in Chaos Incarnate mode (unlimited)
        boolean unlimited = cap.isChaosIncarnateActive();
        
        if (cap.isOneWithChaosActive()) {
            // Deactivate
            deactivateOneWithChaos(player, cap);
        } else {
            // Check cooldown FIRST before allowing activation
            if (!unlimited && currentTime < cap.getOneWithChaosCooldown()) {
                long remaining = (cap.getOneWithChaosCooldown() - currentTime) / 20;
                GodAvatarHudHelper.sendCooldownMessage(player, "One with Chaos", remaining);
                return;
            }
            
            // Activate - toggle the Origins power on
            cap.setOneWithChaosActive(true);
            var server = player.getServer();
            if (server != null) {
                server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    "power grant " + player.getGameProfile().getName() + " afterlifeentombed:seth_one_with_chaos_active"
                );
            }
            
            // Spawn seth_fog particle effect
            if (player.level() instanceof ServerLevel level) {
                AfterLibsAPI.spawnAfterlifeParticle(level, "seth_fog", player.getX(), player.getY() + 1, player.getZ(), 1.0f);
            }
            
            GodAvatarHudHelper.sendActivationMessage(player, "One with Chaos", GodAvatarHudHelper.COLOR_SETH);
        }
    }
    
    private static void deactivateOneWithChaos(ServerPlayer player, GodAvatarCapability.IGodAvatar cap) {
        cap.setOneWithChaosActive(false);
        // Toggle the Origins power off
        var server = player.getServer();
        if (server != null) {
            server.getCommands().performPrefixedCommand(
                player.createCommandSourceStack(),
                "power revoke " + player.getGameProfile().getName() + " afterlifeentombed:seth_one_with_chaos_active"
            );
        }
        
        if (!cap.isChaosIncarnateActive()) {
            long currentTime = player.level().getGameTime();
            // Cooldown equals time used (max 2 minutes)
            cap.setOneWithChaosCooldown(currentTime + cap.getOneWithChaosTimeUsed());
        }
        
        // Reset time used counter
        cap.setOneWithChaosTimeUsed(0);
        
        GodAvatarHudHelper.sendDeactivationMessage(player, "One with Chaos", GodAvatarHudHelper.COLOR_SETH);
    }
    
    /**
     * Damage Negation - Store and release damage (two-stage activation)
     */
    private static void toggleDamageNegation(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        boolean unlimited = cap.isChaosIncarnateActive();
        
        if (cap.isDamageNegationActive()) {
            // Second activation - check if 10s collection period has passed
            long activationTime = cap.getDamageNegationCooldown();
            long elapsed = currentTime - activationTime;
            
            if (elapsed < 200) { // Less than 10 seconds
                long remaining = (200 - elapsed) / 20;
                GodAvatarHudHelper.sendNotification(player, "Wait " + remaining + "s to release", GodAvatarHudHelper.COLOR_ERROR, 20);
                return;
            }
            
            // Ready to release - HUD shows this automatically
            if (cap.getStoredDamage() <= 0) {
                GodAvatarHudHelper.sendNotification(player, "No damage stored", GodAvatarHudHelper.COLOR_ERROR, 30);
                cap.setDamageNegationActive(false);
            }
        } else {
            // First activation - check cooldown
            if (!unlimited && currentTime < cap.getDamageNegationCooldown()) {
                long remaining = (cap.getDamageNegationCooldown() - currentTime) / 20;
                GodAvatarHudHelper.sendCooldownMessage(player, "Damage Negation", remaining);
                return;
            }
            
            // Start damage collection - HUD shows this
            cap.setDamageNegationActive(true);
            cap.setStoredDamage(0);
            cap.setDamageNegationCooldown(currentTime);
        }
    }
    
    /**
     * Handle damage storage for Damage Negation ability
     */
    @SubscribeEvent
    public static void onDamageNegation(LivingDamageEvent event) {
        if (event.getEntity() instanceof Player player && isSethAvatar(player)) {
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                if (cap.isDamageNegationActive()) {
                    long currentTime = player.level().getGameTime();
                    long activationTime = cap.getDamageNegationCooldown();
                    long elapsed = currentTime - activationTime;
                    
                    if (elapsed < 200) { // Still in 10 second collection window
                        float stored = cap.getStoredDamage();
                        cap.setStoredDamage(stored + event.getAmount());
                        event.setAmount(0); // Negate the damage
                        // HUD shows storage amount automatically
                    }
                }
            });
        }
    }
    
    /**
     * Release stored damage on attack
     */
    @SubscribeEvent
    public static void onPlayerAttack(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player player && isSethAvatar(player)) {
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                if (cap.isDamageNegationActive() && cap.getStoredDamage() > 0) {
                    long currentTime = player.level().getGameTime();
                    long activationTime = cap.getDamageNegationCooldown();
                    long elapsed = currentTime - activationTime;
                    
                    if (elapsed >= 200) { // After 10 seconds collection period
                        event.setAmount(event.getAmount() + cap.getStoredDamage());
                        
                        if (player instanceof ServerPlayer serverPlayer) {
                            GodAvatarHudHelper.sendNotification(serverPlayer, "Released " + "%.1f".formatted(cap.getStoredDamage()) + " damage!", GodAvatarHudHelper.COLOR_ERROR, 40);
                        }
                        
                        cap.setStoredDamage(0);
                        cap.setDamageNegationActive(false);
                        
                        if (!cap.isChaosIncarnateActive()) {
                            cap.setDamageNegationCooldown(currentTime + 1200); // 1 minute cooldown
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Desert Walker - Teleport or fly
     */
    private static void activateDesertWalker(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        boolean unlimited = cap.isChaosIncarnateActive();
        
        // Check cooldown
        if (!unlimited && currentTime < cap.getDesertWalkerCooldown()) {
            long remaining = (cap.getDesertWalkerCooldown() - currentTime) / 20;
            GodAvatarHudHelper.sendCooldownMessage(player, "Desert Walker", remaining);
            return;
        }
        
        if (player.isOnFire()) {
            // Grant creative flight
            cap.setDesertWalkerFlying(true);
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            GodAvatarHudHelper.sendActivationMessage(player, "Flight", GodAvatarHudHelper.COLOR_SETH);
        } else {
            // Teleport to looking position
            Vec3 start = player.getEyePosition();
            Vec3 end = start.add(player.getLookAngle().scale(30));
            BlockHitResult result = player.level().clip(new ClipContext(start, end, 
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            
            if (result.getType() != HitResult.Type.MISS) {
                Vec3 teleportPos = result.getLocation();
                player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                GodAvatarHudHelper.sendNotification(player, "Teleported!", GodAvatarHudHelper.COLOR_SETH, 30);
                
                if (!unlimited) {
                    cap.setDesertWalkerCooldown(currentTime + 600); // 30 second cooldown
                }
            }
        }
    }
    
    /**
     * Chaos Incarnate - Ultimate ability
     */
    private static void activateChaosIncarnate(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        // If already active, deactivate it
        if (cap.isChaosIncarnateActive()) {
            deactivateChaosIncarnate(player, cap, currentTime);
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
        if (currentTime < cap.getChaosIncarnateCooldown()) {
            long remaining = (cap.getChaosIncarnateCooldown() - currentTime) / 20;
            GodAvatarHudHelper.sendCooldownMessage(player, "Chaos Incarnate", remaining);
            return;
        }
        
        
        
        // Activate Chaos Incarnate
        cap.setChaosIncarnateActive(true);
        cap.setChaosIncarnateCooldown(currentTime); // Store activation time
        
        // Grant the Origins power to enable size increase
        var server = player.getServer();
        if (server != null) {
            server.getCommands().performPrefixedCommand(
                player.createCommandSourceStack(),
                "power grant " + player.getGameProfile().getName() + " afterlifeentombed:seth_chaos_incarnate_active"
            );
        }
        
        GodAvatarHudHelper.sendNotification(player, "CHAOS INCARNATE!", GodAvatarHudHelper.COLOR_SETH, 60);
    }
    
    private static void deactivateChaosIncarnate(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        cap.setChaosIncarnateActive(false);
        cap.setChaosIncarnateCooldown(currentTime + 6000); // 5 minute cooldown
        
        // Revoke the Origins power to disable size increase
        var server = player.getServer();
        if (server != null) {
            server.getCommands().performPrefixedCommand(
                player.createCommandSourceStack(),
                "power revoke " + player.getGameProfile().getName() + " afterlifeentombed:seth_chaos_incarnate_active"
            );
        }
        
        // Remove flight
        if (!cap.isDesertWalkerFlying()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        
        // Add slow falling
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 0, false, true));
        
        GodAvatarHudHelper.sendNotification(player, "Chaos Incarnate ended", GodAvatarHudHelper.COLOR_SETH, 40);
    }
    
    // Note: AttachCapabilities and PlayerClone events are now handled in GodAvatarCapabilityHandler.java
}
