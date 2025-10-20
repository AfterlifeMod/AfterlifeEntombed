package com.dracolich777.afterlifeentombed.commands;

import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlibs.api.AfterLibsAPI;
import com.dracolich777.afterlifeentombed.util.UnlockedGodsSavedData;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.SyncUnlockedGodsPacket;
import net.minecraftforge.network.PacketDistributor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Set;

/**
 * /avatar command for managing god avatar powers and capabilities
 * 
 * Usage:
 * - /avatar set <player> <god> - Set player's god avatar
 * - /avatar power <ability> cd <ticks> - Set ability cooldown
 * - /avatar power <ability> active <true|false> - Toggle ability active state
 * - /avatar info [player] - Display avatar info
 * - /avatar reset [player] - Reset all avatar data
 */
public class AvatarCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("avatar")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            
            // /avatar set <player> <god>
            .then(Commands.literal("set")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("god", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (GodType god : GodType.values()) {
                                if (god != GodType.NONE) {
                                    builder.suggest(god.name().toLowerCase());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(AvatarCommand::setGod))))
            
            // /avatar power <ability> cd <ticks>
            .then(Commands.literal("power")
                .then(Commands.argument("ability", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        // Suggest abilities based on current god type
                        try {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                                GodType god = cap.getSelectedGod();
                                switch (god) {
                                    case SETH -> {
                                        builder.suggest("one_with_chaos");
                                        builder.suggest("damage_negation");
                                        builder.suggest("desert_walker");
                                        builder.suggest("chaos_incarnate");
                                    }
                                    case RA -> {
                                        builder.suggest("solar_flare");
                                        builder.suggest("purifying_light");
                                        builder.suggest("holy_inferno");
                                        builder.suggest("avatar_of_sun");
                                    }
                                    case ANUBIS -> {
                                        builder.suggest("undead_command");
                                        builder.suggest("lifelink");
                                        builder.suggest("summon_undead");
                                        builder.suggest("avatar_of_death");
                                    }
                                    case SHU -> {
                                        builder.suggest("launch");
                                        builder.suggest("air_boost");
                                        builder.suggest("extra_jumps");
                                        builder.suggest("wind_avatar");
                                    }
                                    case THOTH -> {
                                        builder.suggest("scholarly_teleport");
                                        builder.suggest("experience_surge");
                                        builder.suggest("divine_enchant");
                                        builder.suggest("avatar_of_wisdom");
                                    }
                                    case GEB -> {
                                        builder.suggest("telekinesis");
                                        builder.suggest("excavation");
                                        builder.suggest("earth_rise");
                                        builder.suggest("avatar_of_earth");
                                    }
                                    case HORUS, ISIS -> {
                                        // Abilities not yet implemented for these gods
                                    }
                                    default -> {
                                        // No god selected
                                    }
                                }
                            });
                        } catch (Exception e) {
                            // Fallback: suggest all known abilities
                            builder.suggest("one_with_chaos");
                            builder.suggest("damage_negation");
                            builder.suggest("desert_walker");
                            builder.suggest("chaos_incarnate");
                            builder.suggest("solar_flare");
                            builder.suggest("purifying_light");
                            builder.suggest("holy_inferno");
                            builder.suggest("avatar_of_sun");
                        }
                        return builder.buildFuture();
                    })
                    .then(Commands.literal("cd")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(0))
                            .executes(AvatarCommand::setCooldown)))
                    .then(Commands.literal("active")
                        .then(Commands.argument("state", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                builder.suggest("true");
                                builder.suggest("false");
                                return builder.buildFuture();
                            })
                            .executes(AvatarCommand::setActive)))))
            
            // /avatar info [player]
            .then(Commands.literal("info")
                .executes(context -> showInfo(context, context.getSource().getPlayerOrException()))
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> showInfo(context, EntityArgument.getPlayer(context, "target")))))
            
            // /avatar reset [player]
            .then(Commands.literal("reset")
                .executes(context -> resetAvatar(context, context.getSource().getPlayerOrException()))
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> resetAvatar(context, EntityArgument.getPlayer(context, "target")))))
            // /avatar god lock|unlock [player] <god>
            .then(Commands.literal("god")
                .then(Commands.literal("unlock")
                    // /avatar god unlock <god> - unlock for self
                    .then(Commands.argument("god", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (GodType god : GodType.values()) {
                                if (god != GodType.NONE) builder.suggest(god.name().toLowerCase());
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> unlockGodForSelf(context))
                        // /avatar god unlock <god> <player> - unlock for specific player(s)
                        .then(Commands.argument("targets", EntityArgument.players())
                            .executes(context -> unlockGodForPlayers(context)))))
                .then(Commands.literal("lock")
                    // /avatar god lock <god> - lock for self
                    .then(Commands.argument("god", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (GodType god : GodType.values()) {
                                if (god != GodType.NONE) builder.suggest(god.name().toLowerCase());
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> lockGodForSelf(context))
                        // /avatar god lock <god> <player> - lock for specific player(s)
                        .then(Commands.argument("targets", EntityArgument.players())
                            .executes(context -> lockGodForPlayers(context))))))
        );
    }
    
    private static int setGod(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
            String godName = StringArgumentType.getString(context, "god").toUpperCase();
            
            GodType god;
            try {
                god = GodType.valueOf(godName);
            } catch (IllegalArgumentException e) {
                context.getSource().sendFailure(Component.literal("Unknown god: " + godName));
                return 0;
            }
            
            if (god == GodType.NONE) {
                context.getSource().sendFailure(Component.literal("Cannot set god to NONE. Use /avatar reset instead."));
                return 0;
            }
            
            for (ServerPlayer target : targets) {
                target.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                    cap.setSelectedGod(god);
                    
                    // Grant the origin tag so validation passes
                    target.addTag("afterlifeentombed:has_avatar_origin");
                    
                    // Switch to the god-specific origin using the /origin command
                    var server = target.getServer();
                    if (server != null) {
                    String originId = switch (god) {
                        case SETH -> "afterlifeentombed:avatar_of_seth";
                        case RA -> "afterlifeentombed:avatar_of_ra";
                        case SHU -> "afterlifeentombed:avatar_of_shu";
                        case ANUBIS -> "afterlifeentombed:avatar_of_anubis";
                        case THOTH -> "afterlifeentombed:avatar_of_thoth";
                        case GEB -> "afterlifeentombed:avatar_of_geb";
                        // Add more gods as they are implemented
                        case HORUS, ISIS -> "afterlifeentombed:avatar_of_egypt"; // Fallback to egypt for unimplemented
                        default -> "afterlifeentombed:avatar_of_egypt";
                    };                        // Remove ALL existing avatar origins to prevent stacking
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + target.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_egypt"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + target.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_seth"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + target.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_ra"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + target.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_shu"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + target.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_anubis"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + target.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_thoth"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + target.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_geb"
                        );
                        
                        // Now grant the new god-specific origin
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin set " + target.getGameProfile().getName() + " origins:origin " + originId
                        );
                    }
                    
                    // Spawn particle effects based on god type
                    if (target.level() instanceof ServerLevel level) {
                        double x = target.getX();
                        double y = target.getY();
                        double z = target.getZ();
                        
                        switch (god) {
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
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Set " + target.getName().getString() + "'s god to " + god.name()), 
                        true);
                });
            }
            
            return targets.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int setCooldown(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String ability = StringArgumentType.getString(context, "ability").toLowerCase();
            int ticks = IntegerArgumentType.getInteger(context, "ticks");
            long targetTime = player.level().getGameTime() + ticks;
            
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                GodType god = cap.getSelectedGod();
                boolean handled = false;
                
                // Handle god-specific abilities
                switch (god) {
                    case SETH -> {
                        switch (ability) {
                            case "one_with_chaos" -> {
                                cap.setOneWithChaosCooldown(targetTime);
                                handled = true;
                            }
                            case "damage_negation" -> {
                                cap.setDamageNegationCooldown(targetTime);
                                handled = true;
                            }
                            case "desert_walker" -> {
                                cap.setDesertWalkerCooldown(targetTime);
                                handled = true;
                            }
                            case "chaos_incarnate" -> {
                                cap.setChaosIncarnateCooldown(targetTime);
                                handled = true;
                            }
                        }
                    }
                    case RA -> {
                        switch (ability) {
                            case "solar_flare" -> {
                                cap.setSolarFlareCooldown(targetTime);
                                handled = true;
                            }
                            case "purifying_light" -> {
                                cap.setPurifyingLightCooldown(targetTime);
                                handled = true;
                            }
                            case "holy_inferno" -> {
                                cap.setHolyInfernoCooldown(targetTime);
                                handled = true;
                            }
                            case "avatar_of_sun" -> {
                                cap.setAvatarOfSunCooldown(targetTime);
                                handled = true;
                            }
                        }
                    }
                    case ANUBIS -> {
                        switch (ability) {
                            case "undead_command" -> {
                                cap.setUndeadCommandCooldown(targetTime);
                                handled = true;
                            }
                            case "lifelink" -> {
                                cap.setLifelinkCooldown(targetTime);
                                handled = true;
                            }
                            case "summon_undead" -> {
                                cap.setSummonUndeadCooldown(targetTime);
                                handled = true;
                            }
                            case "avatar_of_death" -> {
                                cap.setAvatarOfDeathCooldown(targetTime);
                                handled = true;
                            }
                        }
                    }
                    case SHU -> {
                        switch (ability) {
                            case "launch" -> {
                                cap.setLaunchCooldown(targetTime);
                                handled = true;
                            }
                            case "air_boost" -> {
                                cap.setAirBoostCooldown(targetTime);
                                handled = true;
                            }
                            case "extra_jumps" -> {
                                cap.setExtraJumpsCooldown(targetTime);
                                handled = true;
                            }
                            case "wind_avatar" -> {
                                cap.setWindAvatarCooldown(targetTime);
                                handled = true;
                            }
                        }
                    }
                    case THOTH -> {
                        switch (ability) {
                            case "scholarly_teleport" -> {
                                cap.setScholarlyTeleportCooldown(targetTime);
                                handled = true;
                            }
                            case "experience_surge" -> {
                                cap.setExperienceMultiplierCooldown(targetTime);
                                handled = true;
                            }
                            case "divine_enchant" -> {
                                cap.setDivineEnchantCooldown(targetTime);
                                handled = true;
                            }
                            case "avatar_of_wisdom" -> {
                                cap.setAvatarOfWisdomCooldown(targetTime);
                                handled = true;
                            }
                        }
                    }
                    case GEB -> {
                        switch (ability) {
                            case "telekinesis" -> {
                                cap.setTelekinesisCooldown(targetTime);
                                handled = true;
                            }
                            case "excavation" -> {
                                cap.setExcavationCooldown(targetTime);
                                handled = true;
                            }
                            case "earth_rise" -> {
                                cap.setEarthRiseCooldown(targetTime);
                                handled = true;
                            }
                            case "avatar_of_earth" -> {
                                cap.setAvatarOfEarthCooldown(targetTime);
                                handled = true;
                            }
                        }
                    }
                    case HORUS, ISIS -> {
                        context.getSource().sendFailure(Component.literal("God " + god.name() + " abilities not yet implemented"));
                        handled = true;
                    }
                    default -> {
                        context.getSource().sendFailure(Component.literal("No god selected"));
                        handled = true;
                    }
                }
                
                if (!handled) {
                    context.getSource().sendFailure(Component.literal("Unknown ability '" + ability + "' for god " + god.name()));
                    return;
                }
                
                context.getSource().sendSuccess(() -> 
                    Component.literal("Set " + ability + " cooldown to " + ticks + " ticks"), 
                    true);
            });
            
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int setActive(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String ability = StringArgumentType.getString(context, "ability").toLowerCase();
            boolean active = Boolean.parseBoolean(StringArgumentType.getString(context, "state"));
            
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                GodType god = cap.getSelectedGod();
                boolean handled = false;
                
                // Handle god-specific abilities
                switch (god) {
                    case SETH -> {
                        switch (ability) {
                            case "one_with_chaos" -> {
                                cap.setOneWithChaosActive(active);
                                if (active) {
                                    cap.setOneWithChaosTimeUsed(0);
                                }
                                handled = true;
                            }
                            case "damage_negation" -> {
                                cap.setDamageNegationActive(active);
                                if (!active) {
                                    cap.setStoredDamage(0);
                                }
                                handled = true;
                            }
                            case "desert_walker" -> {
                                cap.setDesertWalkerFlying(active);
                                handled = true;
                            }
                            case "chaos_incarnate" -> {
                                cap.setChaosIncarnateActive(active);
                                handled = true;
                            }
                        }
                    }
                    case RA -> {
                        switch (ability) {
                            case "purifying_light" -> {
                                cap.setPurifyingLightActive(active);
                                if (!active) {
                                    cap.setPurifyingLightEndTime(0);
                                }
                                handled = true;
                            }
                            case "avatar_of_sun" -> {
                                cap.setAvatarOfSunActive(active);
                                handled = true;
                            }
                            case "solar_flare", "holy_inferno" -> {
                                context.getSource().sendFailure(Component.literal(ability + " is a one-shot ability, cannot set active state"));
                                handled = true;
                            }
                        }
                    }
                    case ANUBIS -> {
                        switch (ability) {
                            case "lifelink" -> {
                                cap.setLifelinkActive(active);
                                handled = true;
                            }
                            case "avatar_of_death" -> {
                                cap.setAvatarOfDeathActive(active);
                                handled = true;
                            }
                            case "undead_command", "summon_undead" -> {
                                context.getSource().sendFailure(Component.literal(ability + " is a one-shot ability, cannot set active state"));
                                handled = true;
                            }
                        }
                    }
                    case SHU -> {
                        switch (ability) {
                            case "wind_avatar" -> {
                                cap.setWindAvatarActive(active);
                                handled = true;
                            }
                            case "launch", "air_boost", "extra_jumps" -> {
                                context.getSource().sendFailure(Component.literal(ability + " is a one-shot ability, cannot set active state"));
                                handled = true;
                            }
                        }
                    }
                    case THOTH -> {
                        switch (ability) {
                            case "experience_surge" -> {
                                cap.setExperienceMultiplierActive(active);
                                handled = true;
                            }
                            case "avatar_of_wisdom" -> {
                                cap.setAvatarOfWisdomActive(active);
                                handled = true;
                            }
                            case "scholarly_teleport", "divine_enchant" -> {
                                context.getSource().sendFailure(Component.literal(ability + " is a one-shot ability, cannot set active state"));
                                handled = true;
                            }
                        }
                    }
                    case GEB -> {
                        switch (ability) {
                            case "avatar_of_earth" -> {
                                cap.setAvatarOfEarthActive(active);
                                handled = true;
                            }
                            case "telekinesis", "excavation", "earth_rise" -> {
                                context.getSource().sendFailure(Component.literal(ability + " is a one-shot ability, cannot set active state"));
                                handled = true;
                            }
                        }
                    }
                    case HORUS, ISIS -> {
                        context.getSource().sendFailure(Component.literal("God " + god.name() + " abilities not yet implemented"));
                        handled = true;
                    }
                    default -> {
                        context.getSource().sendFailure(Component.literal("No god selected"));
                        handled = true;
                    }
                }
                
                if (!handled) {
                    context.getSource().sendFailure(Component.literal("Unknown ability '" + ability + "' for god " + god.name()));
                    return;
                }
                
                context.getSource().sendSuccess(() -> 
                    Component.literal("Set " + ability + " active state to " + active), 
                    true);
            });
            
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
    
    private static int showInfo(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        target.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = target.level().getGameTime();
            GodType god = cap.getSelectedGod();
            
            context.getSource().sendSuccess(() -> 
                Component.literal("=== Avatar Info: " + target.getName().getString() + " ==="), 
                false);
            
            context.getSource().sendSuccess(() -> 
                Component.literal("God: " + god.name()), 
                false);
            
            // Show god-specific abilities
            switch (god) {
                case SETH -> {
                    context.getSource().sendSuccess(() -> 
                        Component.literal("One with Chaos: Active=" + cap.isOneWithChaosActive() + 
                            ", CD=" + Math.max(0, (cap.getOneWithChaosCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Damage Negation: Active=" + cap.isDamageNegationActive() + 
                            ", Stored=" + String.format("%.1f", cap.getStoredDamage()) +
                            ", CD=" + Math.max(0, (cap.getDamageNegationCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Desert Walker: Flying=" + cap.isDesertWalkerFlying() + 
                            ", CD=" + Math.max(0, (cap.getDesertWalkerCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Chaos Incarnate: Active=" + cap.isChaosIncarnateActive() + 
                            ", CD=" + Math.max(0, (cap.getChaosIncarnateCooldown() - currentTime) / 20) + "s"), 
                        false);
                }
                case RA -> {
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Solar Flare: CD=" + Math.max(0, (cap.getSolarFlareCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Purifying Light: Active=" + cap.isPurifyingLightActive() + 
                            ", Remaining=" + Math.max(0, (cap.getPurifyingLightEndTime() - currentTime) / 20) + "s" +
                            ", CD=" + Math.max(0, (cap.getPurifyingLightCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Holy Inferno: CD=" + Math.max(0, (cap.getHolyInfernoCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Avatar of Sun: Active=" + cap.isAvatarOfSunActive() + 
                            ", CD=" + Math.max(0, (cap.getAvatarOfSunCooldown() - currentTime) / 20) + "s"), 
                        false);
                }
                case ANUBIS -> {
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Undead Command: CD=" + Math.max(0, (cap.getUndeadCommandCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Lifelink: Active=" + cap.isLifelinkActive() + 
                            ", Remaining=" + Math.max(0, (cap.getLifelinkEndTime() - currentTime) / 20) + "s" +
                            ", CD=" + Math.max(0, (cap.getLifelinkCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Summon Undead: CD=" + Math.max(0, (cap.getSummonUndeadCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Avatar of Death: Active=" + cap.isAvatarOfDeathActive() + 
                            ", Remaining=" + Math.max(0, (cap.getAvatarOfDeathEndTime() - currentTime) / 20) + "s" +
                            ", CD=" + Math.max(0, (cap.getAvatarOfDeathCooldown() - currentTime) / 20) + "s"), 
                        false);
                }
                case THOTH -> {
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Scholarly Teleport: CD=" + Math.max(0, (cap.getScholarlyTeleportCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Experience Surge: Active=" + cap.isExperienceMultiplierActive() + 
                            ", CD=" + Math.max(0, (cap.getExperienceMultiplierCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Divine Enchant: CD=" + Math.max(0, (cap.getDivineEnchantCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Avatar of Wisdom: Active=" + cap.isAvatarOfWisdomActive() + 
                            ", Remaining=" + Math.max(0, (cap.getAvatarOfWisdomEndTime() - currentTime) / 20) + "s" +
                            ", CD=" + Math.max(0, (cap.getAvatarOfWisdomCooldown() - currentTime) / 20) + "s"), 
                        false);
                }
                case GEB -> {
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Telekinesis: CD=" + Math.max(0, (cap.getTelekinesisCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Excavation: CD=" + Math.max(0, (cap.getExcavationCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Earth Rise: CD=" + Math.max(0, (cap.getEarthRiseCooldown() - currentTime) / 20) + "s"), 
                        false);
                    
                    context.getSource().sendSuccess(() -> 
                        Component.literal("Avatar of Earth: Active=" + cap.isAvatarOfEarthActive() + 
                            ", Remaining=" + Math.max(0, (cap.getAvatarOfEarthEndTime() - currentTime) / 20) + "s" +
                            ", CD=" + Math.max(0, (cap.getAvatarOfEarthCooldown() - currentTime) / 20) + "s"), 
                        false);
                }
                case HORUS, ISIS, SHU -> {
                    context.getSource().sendSuccess(() -> 
                        Component.literal("God " + god.name() + " abilities not yet implemented"), 
                        false);
                }
                case NONE -> {
                    context.getSource().sendSuccess(() -> 
                        Component.literal("No god selected. Use /avatar set to choose a god."), 
                        false);
                }
            }
        });
        
        return 1;
    }
    
    private static int resetAvatar(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        target.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            cap.setSelectedGod(GodType.NONE);
            cap.setOneWithChaosActive(false);
            cap.setOneWithChaosTimeUsed(0);
            cap.setOneWithChaosCooldown(0);
            cap.setDamageNegationActive(false);
            cap.setStoredDamage(0);
            cap.setDamageNegationCooldown(0);
            cap.setDesertWalkerFlying(false);
            cap.setDesertWalkerCooldown(0);
            cap.setChaosIncarnateActive(false);
            cap.setChaosIncarnateCooldown(0);
            // Reset Ra abilities
            cap.setSolarFlareCooldown(0);
            cap.setPurifyingLightCooldown(0);
            cap.setPurifyingLightActive(false);
            cap.setPurifyingLightEndTime(0);
            cap.setHolyInfernoCooldown(0);
            cap.setAvatarOfSunCooldown(0);
            cap.setAvatarOfSunActive(false);
            // Reset Geb abilities
            cap.setTelekinesisCooldown(0);
            cap.setExcavationCooldown(0);
            cap.setEarthRiseCooldown(0);
            cap.setAvatarOfEarthCooldown(0);
            cap.setAvatarOfEarthActive(false);
            cap.setAvatarOfEarthEndTime(0);
            
            // Remove the origin tag
            target.removeTag("afterlifeentombed:has_avatar_origin");
            
            context.getSource().sendSuccess(() -> 
                Component.literal("Reset avatar data for " + target.getName().getString()), 
                true);
        });
        
        return 1;
    }

    private static int unlockGodForSelf(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String godName = StringArgumentType.getString(context, "god").toUpperCase();

            GodType god;
            try {
                god = GodType.valueOf(godName);
            } catch (IllegalArgumentException e) {
                context.getSource().sendFailure(Component.literal("Unknown god: " + godName));
                return 0;
            }

            if (god == GodType.NONE) {
                context.getSource().sendFailure(Component.literal("Cannot unlock NONE"));
                return 0;
            }

            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                cap.unlockGod(god);

                // Persist to saved data
                UnlockedGodsSavedData saved = UnlockedGodsSavedData.get(player.getServer());
                saved.unlockGod(player.getUUID(), god);

                // Sync to client
                GodAvatarPackets.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncUnlockedGodsPacket(cap.getUnlockedGods()));

                context.getSource().sendSuccess(() -> Component.literal("Unlocked " + god.name()), true);
            });

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int unlockGodForPlayers(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
            String godName = StringArgumentType.getString(context, "god").toUpperCase();

            GodType god;
            try {
                god = GodType.valueOf(godName);
            } catch (IllegalArgumentException e) {
                context.getSource().sendFailure(Component.literal("Unknown god: " + godName));
                return 0;
            }

            if (god == GodType.NONE) {
                context.getSource().sendFailure(Component.literal("Cannot unlock NONE"));
                return 0;
            }

            for (ServerPlayer target : targets) {
                target.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                    cap.unlockGod(god);

                    // Persist to saved data
                    UnlockedGodsSavedData saved = UnlockedGodsSavedData.get(target.getServer());
                    saved.unlockGod(target.getUUID(), god);

                    // Sync to client
                    GodAvatarPackets.INSTANCE.send(PacketDistributor.PLAYER.with(() -> target), new SyncUnlockedGodsPacket(cap.getUnlockedGods()));

                    context.getSource().sendSuccess(() -> Component.literal("Unlocked " + god.name() + " for " + target.getName().getString()), true);
                });
            }

            return targets.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int lockGodForSelf(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String godName = StringArgumentType.getString(context, "god").toUpperCase();

            GodType god;
            try {
                god = GodType.valueOf(godName);
            } catch (IllegalArgumentException e) {
                context.getSource().sendFailure(Component.literal("Unknown god: " + godName));
                return 0;
            }

            if (god == GodType.NONE) {
                context.getSource().sendFailure(Component.literal("Cannot lock NONE"));
                return 0;
            }

            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                if (!cap.hasUnlockedGod(god)) {
                    context.getSource().sendFailure(Component.literal("You do not have " + god.name() + " unlocked"));
                    return;
                }

                // Remove from capability
                cap.lockGod(god);

                // Persist to saved data
                UnlockedGodsSavedData saved = UnlockedGodsSavedData.get(player.getServer());
                saved.setUnlockedGods(player.getUUID(), cap.getUnlockedGods());

                // Sync to client
                GodAvatarPackets.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncUnlockedGodsPacket(cap.getUnlockedGods()));

                context.getSource().sendSuccess(() -> Component.literal("Locked " + god.name()), true);
            });

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int lockGodForPlayers(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
            String godName = StringArgumentType.getString(context, "god").toUpperCase();

            GodType god;
            try {
                god = GodType.valueOf(godName);
            } catch (IllegalArgumentException e) {
                context.getSource().sendFailure(Component.literal("Unknown god: " + godName));
                return 0;
            }

            if (god == GodType.NONE) {
                context.getSource().sendFailure(Component.literal("Cannot lock NONE"));
                return 0;
            }

            for (ServerPlayer target : targets) {
                target.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                    if (!cap.hasUnlockedGod(god)) {
                        context.getSource().sendFailure(Component.literal(target.getName().getString() + " does not have " + god.name() + " unlocked"));
                        return;
                    }

                    // Remove from capability
                    cap.lockGod(god);

                    // Persist to saved data
                    UnlockedGodsSavedData saved = UnlockedGodsSavedData.get(target.getServer());
                    saved.setUnlockedGods(target.getUUID(), cap.getUnlockedGods());

                    // Sync to client
                    GodAvatarPackets.INSTANCE.send(PacketDistributor.PLAYER.with(() -> target), new SyncUnlockedGodsPacket(cap.getUnlockedGods()));

                    context.getSource().sendSuccess(() -> Component.literal("Locked " + god.name() + " for " + target.getName().getString()), true);
                });
            }

            return targets.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
