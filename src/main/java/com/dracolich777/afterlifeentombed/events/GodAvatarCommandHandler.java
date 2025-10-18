package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.init.ModItems;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.items.GodstoneItem;
import com.dracolich777.afterlibs.api.AfterLibsAPI;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles trigger scoreboard detection to bridge Origins power activation to Java ability execution
 */
@Mod.EventBusSubscriber(modid = "afterlifeentombed")
public class GodAvatarCommandHandler {
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Only process on server side during player tick end phase
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        
        Scoreboard scoreboard = player.getScoreboard();
        
        // Check for godstone consumption trigger
        Objective consumeObjective = scoreboard.getObjective("god_avatar_consume_godstone");
        if (consumeObjective != null) {
            Score consumeScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), consumeObjective);
            if (consumeScore.getScore() > 0) {
                handleGodstoneConsumption(player);
                consumeScore.setScore(0);
            }
        }
        
        // Check the trigger scoreboard for abilities
        Objective objective = scoreboard.getObjective("god_avatar_ability");
        
        if (objective != null) {
            Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), objective);
            int abilityId = score.getScore();
            
            // If a trigger was set, activate the ability and reset
            if (abilityId > 0) {
                player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(godAvatar -> {
                    // Route to correct god's ability handler based on selected god
                    GodType selectedGod = godAvatar.getSelectedGod();
                    
                    switch (selectedGod) {
                        case SETH -> SethAvatarAbilities.activateAbility(player, abilityId);
                        case RA -> RaAvatarAbilities.activateAbility(player, abilityId);
                        case SHU -> ShuAvatarAbilities.activateAbility(player, abilityId);
                        case ANUBIS -> AnubisAvatarAbilities.activateAbility(player, abilityId);
                        case THOTH -> ThothAvatarAbilities.activateAbility(player, abilityId);
                        case HORUS, ISIS, GEB -> 
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§e" + selectedGod.name() + " abilities are not yet implemented!"));
                        default -> player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cNo god selected!"));
                    }
                });
                
                // Reset the trigger
                score.setScore(0);
            }
        }
    }
    
    private static void handleGodstoneConsumption(ServerPlayer player) {
        // Check if player is holding a godstone in main hand
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GodstoneItem godstone) {
            GodType newGodType = godstone.getGodType();
            
            // Set the god in capability
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                // Get the previous god type before switching
                GodType previousGodType = cap.getSelectedGod();
                
                // Give back the previous god's godstone (if not NONE)
                if (previousGodType != GodType.NONE && previousGodType != newGodType) {
                    ItemStack previousGodstone = getPreviousGodstone(previousGodType);
                    if (!previousGodstone.isEmpty()) {
                        // Add to inventory or drop if full
                        if (!player.getInventory().add(previousGodstone)) {
                            player.drop(previousGodstone, false);
                        }
                    }
                }
                
                cap.setSelectedGod(newGodType);
                
                // Grant the appropriate origin tag
                player.addTag("afterlifeentombed:has_avatar_origin");
                
                // Grant the god-specific origin
                var server = player.getServer();
                if (server != null) {
                    String originName = switch (newGodType) {
                        case SETH -> "afterlifeentombed:avatar_of_seth";
                        case RA -> "afterlifeentombed:avatar_of_ra";
                        case HORUS -> "afterlifeentombed:avatar_of_horus";
                        case ISIS -> "afterlifeentombed:avatar_of_isis";
                        case GEB -> "afterlifeentombed:avatar_of_geb";
                        case ANUBIS -> "afterlifeentombed:avatar_of_anubis";
                        case SHU -> "afterlifeentombed:avatar_of_shu";
                        case THOTH -> "afterlifeentombed:avatar_of_thoth";
                        default -> null; // NONE or unknown
                    };
                    
                    if (originName != null) {
                        // Remove ALL existing avatar origins to avoid stacking
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
                        
                        // Now grant the god-specific origin
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin set " + player.getGameProfile().getName() + " origins:origin " + originName
                        );
                    }
                }
                
                // Consume the godstone
                heldItem.shrink(1);
                
                // Spawn particle effects based on god type
                if (player.level() instanceof ServerLevel level) {
                    double x = player.getX();
                    double y = player.getY();
                    double z = player.getZ();
                    
                    switch (newGodType) {
                        case SETH -> {
                            // Spawn seth_fog at player position
                            AfterLibsAPI.spawnAfterlifeParticle(level, "seth_fog", x, y + 1, z, 1.0f);
                        }
                        case RA -> {
                            // Spawn ra_halo 5 blocks above player at 0.5 scale
                            AfterLibsAPI.spawnAfterlifeParticle(level, "ra_halo", x, y + 5, z, 0.5f);
                        }
                        case HORUS -> {
                            // Spawn horus_shield particle
                            AfterLibsAPI.spawnAfterlifeParticle(level, "horus_shield", x, y + 1, z, 1.0f);
                        }
                        case SHU -> {
                            // Spawn shu_jump2 particle
                            AfterLibsAPI.spawnAfterlifeParticle(level, "shu_jump2", x, y + 1, z, 1.0f);
                        }
                        default -> {
                            // Other gods will use available particles when their abilities are implemented
                            // Available: disasphere, haze, haze2, haze3, haze_flash, shield_wall, solar_laser
                        }
                    }
                }
                
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6You have become the Avatar of " + newGodType.name() + "!"));
            });
        }
    }
    
    /**
     * Get the godstone item for a specific god type
     */
    private static ItemStack getPreviousGodstone(GodType godType) {
        return switch (godType) {
            case SETH -> new ItemStack(ModItems.GODSTONE_OF_SETH.get());
            case RA -> new ItemStack(ModItems.GODSTONE_OF_RA.get());
            case HORUS -> new ItemStack(ModItems.GODSTONE_OF_HORUS.get());
            case ISIS -> new ItemStack(ModItems.GODSTONE_OF_ISIS.get());
            case GEB -> new ItemStack(ModItems.GODSTONE_OF_GEB.get());
            case ANUBIS -> new ItemStack(ModItems.GODSTONE_OF_ANUBIS.get());
            case SHU -> new ItemStack(ModItems.GODSTONE_OF_SHU.get());
            case THOTH -> new ItemStack(ModItems.GODSTONE_OF_THOTH.get());
            default -> ItemStack.EMPTY;
        };
    }
}
