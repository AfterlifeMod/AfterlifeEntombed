
package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlibs.api.AfterLibsAPI;
import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.client.hud.GodAvatarHudHelper;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.items.GodstoneItem;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.SyncGodAvatarPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

/**
 * Event handlers for Thoth Avatar abilities - God of Knowledge and Wisdom
 */
@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ThothAvatarAbilities {
    
    // Ability IDs
    public static final int ABILITY_SCHOLARLY_TELEPORT = 1;
    public static final int ABILITY_EXPERIENCE_MULTIPLIER = 2;
    public static final int ABILITY_DIVINE_ENCHANT = 3;
    public static final int ABILITY_AVATAR_OF_WISDOM = 4;
    
    // Knowledge damage attribute modifier UUID
    private static final UUID KNOWLEDGE_DAMAGE_UUID = UUID.fromString("c1d2e3f4-5a6b-7c8d-9e0f-1a2b3c4d5e6f");
    
    // Track players with experience multiplier active: <player UUID, activation time>
    private static final Map<UUID, Long> EXP_MULTIPLIER_ACTIVE = new HashMap<>();
    
    /**
     * Check if player is Thoth avatar
     */
    private static boolean isThothAvatar(Player player) {
        return player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).map(cap -> 
            cap.getSelectedGod() == GodType.THOTH).orElse(false);
    }
    
    /**
     * Main entry point for activating Thoth abilities
     */
    public static void activateAbility(ServerPlayer player, int abilityId) {
        long currentTime = player.level().getGameTime();
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            switch (abilityId) {
                case ABILITY_SCHOLARLY_TELEPORT -> activateScholarlyTeleport(player, cap, currentTime);
                case ABILITY_EXPERIENCE_MULTIPLIER -> activateExperienceMultiplier(player, cap, currentTime);
                case ABILITY_DIVINE_ENCHANT -> activateDivineEnchant(player, cap, currentTime);
                case ABILITY_AVATAR_OF_WISDOM -> activateAvatarOfWisdom(player, cap, currentTime);
            }
        });
    }
    
    /**
     * Ability 1: Scholarly Teleport - Teleport to last used workstation
     * Costs 5 XP levels (free in Avatar of Wisdom mode)
     */
    private static void activateScholarlyTeleport(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        // Check if in Avatar of Wisdom mode (free and no cooldown)
        boolean unlimited = cap.isAvatarOfWisdomActive();
        
        // Check cooldown (skip if in Avatar of Wisdom mode)
        if (!unlimited) {
            long cooldown = cap.getScholarlyTeleportCooldown();
            if (currentTime < cooldown) {
                long ticksRemaining = cooldown - currentTime;
                long secondsRemaining = ticksRemaining / 20;
                GodAvatarHudHelper.sendNotification(player, "Scholarly Teleport on cooldown: " + secondsRemaining + "s", 
                    GodAvatarHudHelper.COLOR_ERROR, 40);
                return;
            }
            
            // Check experience cost
            if (player.experienceLevel < 5) {
                GodAvatarHudHelper.sendNotification(player, "Need 5 XP levels to teleport", 
                    GodAvatarHudHelper.COLOR_ERROR, 40);
                return;
            }
        }
        
        // Check if player has used a workstation
        if (!cap.hasWorkstation()) {
            GodAvatarHudHelper.sendNotification(player, "No workstation to teleport to!", 
                GodAvatarHudHelper.COLOR_ERROR, 60);
            return;
        }
        
        // Get workstation position from capability
        BlockPos workstationPos = new BlockPos((int)cap.getLastWorkstationX(), (int)cap.getLastWorkstationY(), (int)cap.getLastWorkstationZ());
        String dimension = cap.getLastWorkstationDimension();
        
        // Verify dimension matches
        if (!player.level().dimension().location().toString().equals(dimension)) {
            GodAvatarHudHelper.sendNotification(player, "Workstation is in another dimension!", 
                GodAvatarHudHelper.COLOR_ERROR, 60);
            return;
        }
        
        // Verify workstation still exists
        ServerLevel level = player.serverLevel();
        Block block = level.getBlockState(workstationPos).getBlock();
        boolean isValidWorkstation = block instanceof EnchantmentTableBlock || 
                                    block instanceof AnvilBlock || 
                                    block instanceof SmithingTableBlock;
        
        if (!isValidWorkstation) {
            GodAvatarHudHelper.sendNotification(player, "Workstation no longer exists!", 
                GodAvatarHudHelper.COLOR_ERROR, 60);
            cap.clearWorkstation();
            return;
        }
        
        // Consume experience and set cooldown (skip if unlimited)
        if (!unlimited) {
            player.giveExperienceLevels(-5);
            cap.setScholarlyTeleportCooldown(currentTime + 600); // 30 seconds
        }
        
        // Teleport to workstation (safe position above it)
        player.teleportTo(level, workstationPos.getX() + 0.5, workstationPos.getY() + 1.0, workstationPos.getZ() + 0.5, 
                         player.getYRot(), player.getXRot());
        
        // Visual/audio feedback
        level.playSound(null, workstationPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        GodAvatarHudHelper.sendNotification(player, "✦ Teleported to Workstation ✦", 
            GodAvatarHudHelper.COLOR_THOTH, 60);
    }
    
    /**
     * Ability 2: Experience Multiplier - Next mob kill grants 12x XP
     */
    private static void activateExperienceMultiplier(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        // Check if in Avatar of Wisdom mode (no cooldown)
        boolean unlimited = cap.isAvatarOfWisdomActive();
        
        // Check cooldown (skip if in Avatar of Wisdom mode)
        if (!unlimited) {
            long cooldown = cap.getExperienceMultiplierCooldown();
            if (currentTime < cooldown) {
                long ticksRemaining = cooldown - currentTime;
                long secondsRemaining = ticksRemaining / 20;
                GodAvatarHudHelper.sendNotification(player, "Experience Surge on cooldown: " + secondsRemaining + "s", 
                    GodAvatarHudHelper.COLOR_ERROR, 40);
                return;
            }
        }
        
        // Activate the multiplier
        cap.setExperienceMultiplierActive(true);
        EXP_MULTIPLIER_ACTIVE.put(player.getUUID(), currentTime);
        
        // Set cooldown (skip if unlimited)
        if (!unlimited) {
            cap.setExperienceMultiplierCooldown(currentTime + 1200); // 60 seconds
        }
        
        GodAvatarHudHelper.sendNotification(player, "✦ EXPERIENCE SURGE ACTIVE ✦", 
            GodAvatarHudHelper.COLOR_THOTH, 80);
    }
    
    /**
     * Ability 3: Divine Enchant - Enchant held item with a powerful enchantment
     * Consumes random amount of XP (5-15 levels)
     */
    private static void activateDivineEnchant(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        AfterlifeEntombedMod.LOGGER.info("Divine Enchant: Method called for player {}", player.getName().getString());
        
        // Check if in Avatar of Wisdom mode (no cooldown)
        boolean unlimited = cap.isAvatarOfWisdomActive();
        
        // Check cooldown (skip if in Avatar of Wisdom mode)
        if (!unlimited) {
            long cooldown = cap.getDivineEnchantCooldown();
            if (currentTime < cooldown) {
                long ticksRemaining = cooldown - currentTime;
                long secondsRemaining = ticksRemaining / 20;
                GodAvatarHudHelper.sendNotification(player, "Divine Enchant on cooldown: " + secondsRemaining + "s", 
                    GodAvatarHudHelper.COLOR_ERROR, 40);
                return;
            }
        }
        
        // Check held item
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            GodAvatarHudHelper.sendNotification(player, "No item to enchant!", 
                GodAvatarHudHelper.COLOR_ERROR, 40);
            return;
        }
        
        // Debug: Log item details
        AfterlifeEntombedMod.LOGGER.info("Divine Enchant: Attempting to enchant {} (isEnchantable: {})", 
            heldItem.getDescriptionId(), 
            heldItem.isEnchantable());
        
        // Get suitable enchantments for this item
        // Use category matching instead of canEnchant() which may be overly restrictive
        List<EnchantmentInstance> availableEnchants = new ArrayList<>();
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS.getValues()) {
            // Filter out curses (curses have negative effects and isCurse() returns true)
            if (enchantment.isCurse()) {
                continue;
            }
            
            // Filter out inferior damage enchantments (Smite and Bane of Arthropods)
            if (enchantment == Enchantments.SMITE || enchantment == Enchantments.BANE_OF_ARTHROPODS) {
                continue;
            }
            
            // Check if enchantment's category accepts this item
            if (enchantment.category.canEnchant(heldItem.getItem())) {
                // Always use maximum level - Thoth grants only the finest enchantments
                int maxLevel = enchantment.getMaxLevel();
                availableEnchants.add(new EnchantmentInstance(enchantment, maxLevel));
            }
        }
        
        AfterlifeEntombedMod.LOGGER.info("Divine Enchant: Found {} suitable enchantments", availableEnchants.size());
        
        if (availableEnchants.isEmpty()) {
            GodAvatarHudHelper.sendNotification(player, "No suitable enchantments for this item!", 
                GodAvatarHudHelper.COLOR_ERROR, 60);
            return;
        }
        
        // Determine XP cost (random 5-15 levels, free if unlimited)
        int xpCost = unlimited ? 0 : 5 + player.getRandom().nextInt(11);
        
        if (!unlimited && player.experienceLevel < xpCost) {
            GodAvatarHudHelper.sendNotification(player, "Need " + xpCost + " XP levels to enchant!", 
                GodAvatarHudHelper.COLOR_ERROR, 40);
            return;
        }
        
        // Select and apply a random enchantment
        EnchantmentInstance selectedEnchant = availableEnchants.get(player.getRandom().nextInt(availableEnchants.size()));
        
        // Get current enchantments to check compatibility
        Map<Enchantment, Integer> currentEnchants = EnchantmentHelper.getEnchantments(heldItem);
        
        // Check if enchantment is compatible with existing ones
        boolean compatible = true;
        for (Enchantment existing : currentEnchants.keySet()) {
            if (!selectedEnchant.enchantment.isCompatibleWith(existing)) {
                compatible = false;
                break;
            }
        }
        
        // If not compatible, find a compatible one or apply to fresh item
        if (!compatible) {
            // Try to find a compatible enchantment
            for (EnchantmentInstance enchant : availableEnchants) {
                boolean isCompatible = true;
                for (Enchantment existing : currentEnchants.keySet()) {
                    if (!enchant.enchantment.isCompatibleWith(existing)) {
                        isCompatible = false;
                        break;
                    }
                }
                if (isCompatible) {
                    selectedEnchant = enchant;
                    break;
                }
            }
        }
        
        // Apply the enchantment
        currentEnchants.put(selectedEnchant.enchantment, selectedEnchant.level);
        EnchantmentHelper.setEnchantments(currentEnchants, heldItem);
        
        // Force inventory update to ensure client sees the change
        player.inventoryMenu.broadcastChanges();
        
        // Debug logging
        AfterlifeEntombedMod.LOGGER.info("Divine Enchant: Applied {} level {} to {}", 
            selectedEnchant.enchantment.getDescriptionId(), 
            selectedEnchant.level,
            heldItem.getDescriptionId());
        AfterlifeEntombedMod.LOGGER.info("Divine Enchant: Item now has {} enchantments total", 
            EnchantmentHelper.getEnchantments(heldItem).size());
        
        // Consume XP and set cooldown (skip if unlimited)
        if (!unlimited) {
            player.giveExperienceLevels(-xpCost);
            cap.setDivineEnchantCooldown(currentTime + 2400); // 120 seconds
        }
        
        // Visual/audio feedback
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, 
            SoundSource.PLAYERS, 1.0f, 1.0f);
        
        String enchantName = selectedEnchant.enchantment.getFullname(selectedEnchant.level).getString();
        GodAvatarHudHelper.sendNotification(player, "✦ Granted: " + enchantName + " ✦", 
            GodAvatarHudHelper.COLOR_THOTH, 80);
    }
    
    /**
     * Ability 4: Avatar of Wisdom - Ultimate ability with toggle
     * 60 second duration, 600 second cooldown (10 minutes)
     * Makes all abilities free and removes cooldowns, grants creative flight
     */
    private static void activateAvatarOfWisdom(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        // Check if already active (toggle off)
        if (cap.isAvatarOfWisdomActive()) {
            deactivateAvatarOfWisdom(player, cap, currentTime);
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
        long cooldown = cap.getAvatarOfWisdomCooldown();
        if (currentTime < cooldown) {
            long ticksRemaining = cooldown - currentTime;
            long secondsRemaining = ticksRemaining / 20;
            GodAvatarHudHelper.sendNotification(player, "Avatar of Wisdom on cooldown: " + formatTime(secondsRemaining), 
                GodAvatarHudHelper.COLOR_ERROR, 60);
            return;
        }
        
        // Activate ultimate
        cap.setAvatarOfWisdomActive(true);
        cap.setAvatarOfWisdomEndTime(currentTime + 1200); // 60 seconds
        cap.setAvatarOfWisdomCooldown(currentTime); // Store activation time

        // Apply standardised godly buffs for ultimate (1 minute duration)
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 254, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 6, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 6, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 254, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 254, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 6, false, false));
        // Grant creative flight
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
        // Remove cooldowns on other active abilities
        cap.setNoAbilityCooldowns(true);

        GodAvatarHudHelper.sendNotification(player, "✦ AVATAR OF WISDOM ✦", 
            GodAvatarHudHelper.COLOR_THOTH, 100);

        // Visual/audio feedback
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, 
            SoundSource.PLAYERS, 1.0f, 0.5f);
    }
    
    /**
     * Deactivate Avatar of Wisdom early (toggle off)
     */
    private static void deactivateAvatarOfWisdom(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        cap.setAvatarOfWisdomActive(false);
        cap.setAvatarOfWisdomEndTime(0);
        cap.setAvatarOfWisdomCooldown(currentTime + 12000); // 600 seconds (10 minutes)
        
        // Remove creative flight
        if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        
        GodAvatarHudHelper.sendDeactivationMessage(player, "Avatar of Wisdom", GodAvatarHudHelper.COLOR_THOTH);
    }
    
    /**
     * Format time for display (converts seconds to minutes:seconds if > 60)
     */
    private static String formatTime(long seconds) {
        if (seconds >= 60) {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            return minutes + "m " + secs + "s";
        }
        return seconds + "s";
    }
    
    /**
     * Track workstation usage
     */
    @SubscribeEvent
    public static void onWorkstationUse(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isThothAvatar(player)) return;
        
        BlockPos pos = event.getPos();
        Block block = event.getLevel().getBlockState(pos).getBlock();
        
        // Track enchanting table, anvil, or smithing table usage
        if (block instanceof EnchantmentTableBlock || 
            block instanceof AnvilBlock || 
            block instanceof SmithingTableBlock) {
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                cap.setLastWorkstationX(pos.getX());
                cap.setLastWorkstationY(pos.getY());
                cap.setLastWorkstationZ(pos.getZ());
                cap.setLastWorkstationDimension(player.level().dimension().location().toString());
            });
        }
    }
    
    /**
     * Handle experience multiplier on mob kill
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMobDeath(LivingExperienceDropEvent event) {
        Player killer = event.getAttackingPlayer();
        if (!(killer instanceof ServerPlayer player)) return;
        if (!isThothAvatar(player)) return;
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            if (cap.isExperienceMultiplierActive()) {
                // Multiply experience by 12
                int originalExp = event.getDroppedExperience();
                event.setDroppedExperience(originalExp * 12);
                
                // Deactivate multiplier after first kill
                cap.setExperienceMultiplierActive(false);
                EXP_MULTIPLIER_ACTIVE.remove(player.getUUID());
                
                GodAvatarHudHelper.sendNotification(player, "Experience Surge consumed! (" + (originalExp * 12) + " XP)", 
                    GodAvatarHudHelper.COLOR_THOTH, 60);
            }
        });
    }
    
    /**
     * Apply knowledge damage passive and handle ultimate expiration
     */
    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        if (!isThothAvatar(player)) return;
        
        long currentTime = player.level().getGameTime();
        
        // Apply knowledge damage passive
        applyKnowledgeDamage(player);
        
        // Handle Avatar of Wisdom ultimate
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            if (!cap.isAvatarOfWisdomActive()) {
                // Not active, nothing to do
                return;
            }
            
            // Check if expired
            if (currentTime >= cap.getAvatarOfWisdomEndTime()) {
                // Just expired - deactivate
                cap.setAvatarOfWisdomActive(false);
                cap.setAvatarOfWisdomCooldown(currentTime + 12000); // 600 seconds (10 minutes)
                
                // Remove creative flight
                if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
                
                // Apply slow falling for 1 minute (1200 ticks) after ultimate ends
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 1200, 0, false, true));
                
                GodAvatarHudHelper.sendDeactivationMessage(player, "Avatar of Wisdom", GodAvatarHudHelper.COLOR_THOTH);
                return;
            }
            
            // Apply powerful buffs while Avatar of Wisdom is active
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 20, 4, false, false)); // Strength 5
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 20, 2, false, false)); // Speed 3
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED, 20, 2, false, false)); // Haste 3
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 20, 1, false, false)); // Regeneration 2
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 20, 2, false, false)); // Resistance 3
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 20, 0, false, false)); // Glowing
            
            // Grant creative flight
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        });
    }
    
    /**
     * Apply damage bonus based on experience level
     * Each level grants +2% damage (capped at 50 levels = 100% bonus damage)
     */
    private static void applyKnowledgeDamage(ServerPlayer player) {
        var attackAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttribute == null) return;
        
        // Remove old modifier if it exists
        AttributeModifier oldModifier = attackAttribute.getModifier(KNOWLEDGE_DAMAGE_UUID);
        if (oldModifier != null) {
            attackAttribute.removeModifier(KNOWLEDGE_DAMAGE_UUID);
        }
        
        // Calculate damage bonus: 2% per level (capped at 50 levels)
        int level = Math.min(player.experienceLevel, 50);
        double damageMultiplier = level * 0.02; // 0.02 = 2% per level
        
        if (damageMultiplier > 0) {
            AttributeModifier newModifier = new AttributeModifier(
                KNOWLEDGE_DAMAGE_UUID,
                "Knowledge is Power",
                damageMultiplier,
                AttributeModifier.Operation.MULTIPLY_BASE
            );
            attackAttribute.addTransientModifier(newModifier);
        }
    }
}
