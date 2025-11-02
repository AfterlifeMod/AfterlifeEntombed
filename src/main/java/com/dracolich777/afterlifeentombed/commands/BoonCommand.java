package com.dracolich777.afterlifeentombed.commands;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.boons.ActiveBoon;
import com.dracolich777.afterlifeentombed.boons.BoonType;
import com.dracolich777.afterlifeentombed.capabilities.PlayerBoonsCapability;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.OpenBoonSelectionPacket;
import com.dracolich777.afterlifeentombed.network.SyncPlayerBoonsPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * Command for manually triggering divine boon/curse events for testing
 * 
 * Usage:
 * - /boon blessing <player> [god] - Trigger a blessing offer (opens GUI)
 * - /boon curse <player> [god] - Trigger a curse (opens GUI)
 * - /boon give <player> <boon_name> - Directly grant a specific boon/curse
 * - /boon remove_blessing <player> <boon_name> - Remove a specific blessing
 * - /boon remove_curse <player> <boon_name> - Remove a specific curse
 */
public class BoonCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("boon")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            
            // /boon blessing <player> [god]
            .then(Commands.literal("blessing")
                .then(Commands.argument("targets", EntityArgument.players())
                    .executes(ctx -> triggerBlessing(ctx, null))
                    .then(Commands.argument("god", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (GodType god : GodType.values()) {
                                if (god != GodType.NONE) {
                                    builder.suggest(god.name().toLowerCase());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> triggerBlessing(ctx, StringArgumentType.getString(ctx, "god"))))))
            
            // /boon curse <player> [god]
            .then(Commands.literal("curse")
                .then(Commands.argument("targets", EntityArgument.players())
                    .executes(ctx -> triggerCurse(ctx, null))
                    .then(Commands.argument("god", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (GodType god : GodType.values()) {
                                if (god != GodType.NONE) {
                                    builder.suggest(god.name().toLowerCase());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> triggerCurse(ctx, StringArgumentType.getString(ctx, "god"))))))
            
            // /boon give <player> <boon_name>
            .then(Commands.literal("give")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("boon", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (BoonType boon : BoonType.values()) {
                                builder.suggest(boon.name().toLowerCase());
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> giveBoon(ctx, StringArgumentType.getString(ctx, "boon"))))))
            
            // /boon remove_blessing <player> <boon_name>
            .then(Commands.literal("remove_blessing")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("boon", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (BoonType boon : BoonType.values()) {
                                if (boon.isBlessing()) {
                                    builder.suggest(boon.name().toLowerCase());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> removeBoon(ctx, StringArgumentType.getString(ctx, "boon"), true)))))
            
            // /boon remove_curse <player> <boon_name>
            .then(Commands.literal("remove_curse")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("boon", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (BoonType boon : BoonType.values()) {
                                if (boon.isCurse()) {
                                    builder.suggest(boon.name().toLowerCase());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> removeBoon(ctx, StringArgumentType.getString(ctx, "boon"), false)))))
        );
    }
    
    private static int triggerBlessing(CommandContext<CommandSourceStack> context, String godName) {
        try {
            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
            GodType god;
            
            if (godName == null) {
                // Random god
                god = selectRandomGod();
            } else {
                god = GodType.valueOf(godName.toUpperCase());
                if (god == GodType.NONE) {
                    context.getSource().sendFailure(Component.literal("Cannot use NONE as a god type"));
                    return 0;
                }
            }
            
            for (ServerPlayer target : targets) {
                // Get 3 random blessings from this god
                BoonType[] blessings = BoonType.getRandomBlessings(god, 3);
                
                if (blessings.length == 0) {
                    context.getSource().sendFailure(Component.literal("No blessings available for god: " + god.name()));
                    return 0;
                }
                
                // Spawn particle effect
                spawnBlessingParticle(target, god);
                
                // Send packet to open selection GUI
                GodAvatarPackets.sendToPlayer(target, new OpenBoonSelectionPacket(god, blessings, true));
                
                context.getSource().sendSuccess(() -> Component.literal(
                    "Triggered blessing from " + god.name() + " for " + target.getName().getString()), true);
            }
            
            return targets.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int triggerCurse(CommandContext<CommandSourceStack> context, String godName) {
        try {
            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
            GodType god;
            
            if (godName == null) {
                // Random god
                god = selectRandomGod();
            } else {
                god = GodType.valueOf(godName.toUpperCase());
                if (god == GodType.NONE) {
                    context.getSource().sendFailure(Component.literal("Cannot use NONE as a god type"));
                    return 0;
                }
            }
            
            for (ServerPlayer target : targets) {
                // Get 3 random curses from this god
                BoonType[] curses = BoonType.getRandomCurses(god, 3);
                
                if (curses.length == 0) {
                    context.getSource().sendFailure(Component.literal("No curses available for god: " + god.name()));
                    return 0;
                }
                
                // Spawn particle effect
                spawnCurseParticle(target, god);
                
                // Send packet to open selection GUI
                GodAvatarPackets.sendToPlayer(target, new OpenBoonSelectionPacket(god, curses, false));
                
                context.getSource().sendSuccess(() -> Component.literal(
                    "Triggered curse from " + god.name() + " for " + target.getName().getString()), true);
            }
            
            return targets.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
    
    /**
     * Select a random god (excluding NONE)
     */
    private static GodType selectRandomGod() {
        GodType[] gods = GodType.values();
        GodType selected;
        
        do {
            selected = gods[new java.util.Random().nextInt(gods.length)];
        } while (selected == GodType.NONE);
        
        return selected;
    }
    
    /**
     * Spawn god-specific particle effect for blessing
     */
    private static void spawnBlessingParticle(ServerPlayer player, GodType god) {
        if (!(player.level() instanceof ServerLevel level)) return;
        
        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();
        
        String particleName = getBlessingParticle(god);
        if (particleName != null) {
            com.dracolich777.afterlibs.api.AfterLibsAPI.spawnAfterlifeParticle(level, particleName, x, y, z, 1.0f);
        }
    }
    
    /**
     * Spawn god-specific particle effect for curse
     */
    private static void spawnCurseParticle(ServerPlayer player, GodType god) {
        if (!(player.level() instanceof ServerLevel level)) return;
        
        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();
        
        String particleName = getCurseParticle(god);
        if (particleName != null) {
            com.dracolich777.afterlibs.api.AfterLibsAPI.spawnAfterlifeParticle(level, particleName, x, y, z, 1.0f);
        }
    }
    
    /**
     * Get blessing particle name for each god
     */
    private static String getBlessingParticle(GodType god) {
        return switch (god) {
            case SETH -> "seth_fog";
            case RA -> "ra_halo";
            case HORUS -> "horus_shield";
            case SHU -> "shu_jump2";
            case ISIS -> "healing_burst";
            case GEB -> "earth_aura";
            case THOTH -> "haze_flash";
            case ANUBIS -> "blood_lance";
            default -> null;
        };
    }
    
    /**
     * Get curse particle name for each god
     */
    private static String getCurseParticle(GodType god) {
        return switch (god) {
            case SETH -> "seth_fog";
            case RA -> "haze";
            case HORUS -> "shield_wall";
            case SHU -> "haze2";
            case ISIS -> "haze3";
            case GEB -> "earth_aura";
            case THOTH -> "haze";
            case ANUBIS -> "blood_lance";
            default -> null;
        };
    }
    
    /**
     * Directly grants a specific boon to players
     */
    private static int giveBoon(CommandContext<CommandSourceStack> context, String boonName) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        
        // Validate boon name
        BoonType boonType;
        try {
            boonType = BoonType.valueOf(boonName.toUpperCase());
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("Invalid boon name: " + boonName)
                .withStyle(ChatFormatting.RED));
            return 0;
        }
        
        // Grant the boon to each target
        for (ServerPlayer player : targets) {
            player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(boonsData -> {
                // Create the active boon with current game time
                long currentTime = player.level().getGameTime();
                ActiveBoon activeBoon = new ActiveBoon(boonType, currentTime);
                
                // Add to player's boons
                boonsData.addBoon(activeBoon);
                
                // Sync to client
                GodAvatarPackets.sendToPlayer(player, new SyncPlayerBoonsPacket(boonsData.getActiveBoons()));
                
                // Log the grant
                AfterlifeEntombedMod.LOGGER.info("Granted boon {} to player {}", boonType.name(), player.getName().getString());
                
                // Send success message
                Component message = Component.literal("Granted ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(boonType.getDisplayName())
                        .withStyle(boonType.isBlessing() ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE))
                    .append(Component.literal(" to ")
                        .withStyle(ChatFormatting.GRAY))
                    .append(player.getDisplayName());
                
                context.getSource().sendSuccess(() -> message, true);
                
                // Notify the player
                player.sendSystemMessage(Component.literal("You have been granted: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(boonType.getDisplayName())
                        .withStyle(boonType.isBlessing() ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE)));
            });
        }
        
        return targets.size();
    }
    
    /**
     * Remove a specific boon or curse from players
     */
    private static int removeBoon(CommandContext<CommandSourceStack> context, String boonName, boolean isBlessing) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        
        // Validate boon name
        BoonType boonType;
        try {
            boonType = BoonType.valueOf(boonName.toUpperCase());
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("Invalid " + (isBlessing ? "blessing" : "curse") + " name: " + boonName)
                .withStyle(ChatFormatting.RED));
            return 0;
        }
        
        // Verify it matches the expected type
        if (isBlessing && !boonType.isBlessing()) {
            context.getSource().sendFailure(Component.literal(boonType.getDisplayName() + " is not a blessing!")
                .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (!isBlessing && !boonType.isCurse()) {
            context.getSource().sendFailure(Component.literal(boonType.getDisplayName() + " is not a curse!")
                .withStyle(ChatFormatting.RED));
            return 0;
        }
        
        // Remove the boon from each target
        int removedCount = 0;
        for (ServerPlayer player : targets) {
            boolean[] removed = {false};
            player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(boonsData -> {
                // Find and remove the boon
                ActiveBoon toRemove = boonsData.getBoon(boonType);
                if (toRemove != null) {
                    boonsData.removeBoon(toRemove);
                    removed[0] = true;
                    
                    // Sync to client
                    GodAvatarPackets.sendToPlayer(player, new SyncPlayerBoonsPacket(boonsData.getActiveBoons()));
                    
                    // Log the removal
                    AfterlifeEntombedMod.LOGGER.info("Removed boon {} from player {}", boonType.name(), player.getName().getString());
                    
                    // Send success message
                    Component message = Component.literal("Removed ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(boonType.getDisplayName())
                            .withStyle(boonType.isBlessing() ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE))
                        .append(Component.literal(" from ")
                            .withStyle(ChatFormatting.GRAY))
                        .append(player.getDisplayName());
                    
                    context.getSource().sendSuccess(() -> message, true);
                    
                    // Notify the player
                    player.sendSystemMessage(Component.literal("You have lost: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(boonType.getDisplayName())
                            .withStyle(boonType.isBlessing() ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE)));
                } else {
                    context.getSource().sendFailure(
                        Component.literal(player.getName().getString() + " does not have " + boonType.getDisplayName())
                            .withStyle(ChatFormatting.RED));
                }
            });
            
            if (removed[0]) {
                removedCount++;
            }
        }
        
        return removedCount;
    }
}
