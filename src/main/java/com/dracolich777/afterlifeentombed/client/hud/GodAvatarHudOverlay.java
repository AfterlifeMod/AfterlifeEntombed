package com.dracolich777.afterlifeentombed.client.hud;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.items.GodType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * HUD overlay that displays god avatar information in the top-left corner
 * Compact design - only shows when player has a god selected (god != NONE)
 * Fully transparent background with only colored border and text visible
 */
@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class GodAvatarHudOverlay {
    
    private static final int HUD_X = 5;
    private static final int HUD_Y = 5;
    private static final int HUD_WIDTH = 120;
    
    // Notification system
    private static String currentNotification = "";
    private static long notificationEndTime = 0;
    private static int notificationColor = 0xFFFFFF;
    
    /**
     * Display a temporary notification on the HUD
     */
    public static void showNotification(String message, int color, int durationTicks) {
        currentNotification = message;
        notificationColor = color;
        notificationEndTime = System.currentTimeMillis() + (durationTicks * 50); // 50ms per tick
    }
    
    /**
     * Quick notification with default duration (2 seconds)
     */
    public static void showNotification(String message, int color) {
        showNotification(message, color, 40); // 2 seconds
    }
    
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null || mc.options.hideGui) return;
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            GodType god = cap.getSelectedGod();
            
            // Only render if player has selected a god
            // This ensures HUD only shows when actively using the origin
            if (god == GodType.NONE) return;
            
            GuiGraphics guiGraphics = event.getGuiGraphics();
            long currentTime = player.level().getGameTime();
            
            // Get god-specific colors
            GodColorScheme colors = getGodColors(god);
            
            int x = HUD_X;
            int y = HUD_Y;
            
            // Calculate dynamic height based on active abilities
            int contentHeight = calculateContentHeight(cap, god, currentTime);
            int displayHeight = Math.max(30, contentHeight + 10); // Minimum 30px height
            
            // NO BACKGROUND - Fully transparent!
            // Only border and text are visible
            
            // Draw thin colored border (1px)
            drawBorder(guiGraphics, x, y, HUD_WIDTH, displayHeight, colors.primary);
            
            // Draw compact god name/title with shadow for visibility on any background
            String godName = getGodDisplayName(god);
            guiGraphics.drawString(mc.font, godName, x + 3, y + 3, colors.primary, true);
            
            // Draw ability status (compact) with shadow for visibility
            int lineY = y + 14;
            
            // Check active abilities and cooldowns
            renderAbilityStatus(guiGraphics, mc, player, cap, god, x + 3, lineY, colors, currentTime);
            
            // Draw notification if active (below HUD) with shadow
            if (System.currentTimeMillis() < notificationEndTime) {
                int notifY = y + displayHeight + 3;
                guiGraphics.drawString(mc.font, currentNotification, x + 3, notifY, notificationColor, true);
            }
        });
    }
    
    /**
     * Calculate dynamic height based on active abilities to keep HUD compact
     */
    private static int calculateContentHeight(GodAvatarCapability.IGodAvatar cap, GodType god, long currentTime) {
        int lines = 1; // God name
        int lineHeight = 9;
        
        switch (god) {
            case SETH:
                lines++; // One with Chaos
                lines++; // Damage Negation
                lines++; // Desert Walker
                lines++; // Chaos Incarnate
                break;
            case RA:
                lines++; // Solar Flare
                lines++; // Purifying Light
                lines++; // Holy Inferno
                lines++; // Avatar of Sun
                break;
            case SHU:
                lines++; // Launch
                lines++; // Air Boost
                lines++; // Extra Jumps
                lines++; // Wind Avatar
                break;
            case ANUBIS:
                lines++; // Undead Command
                lines++; // Lifelink
                lines++; // Summon Undead
                lines++; // Avatar of Death
                break;
            case THOTH:
                lines++; // Scholarly Teleport
                lines++; // Experience Surge
                lines++; // Divine Enchant
                lines++; // Avatar of Wisdom
                break;
            case HORUS:
            case ISIS:
            case GEB:
                lines++; // Ability 1
                lines++; // Ability 2
                lines++; // Ability 3
                lines++; // Ability 4
                break;
            case NONE:
                // Should never happen
                break;
        }
        
        return lines * lineHeight;
    }
    
    private static void renderAbilityStatus(GuiGraphics guiGraphics, Minecraft mc, LocalPlayer player,
                                            GodAvatarCapability.IGodAvatar cap, GodType god, 
                                            int x, int y, GodColorScheme colors, long currentTime) {
        int lineHeight = 9; // Compact line spacing
        int currentY = y;
        
        switch (god) {
            case SETH:
                // One with Chaos status (always show)
                if (cap.isOneWithChaosActive()) {
                    int timeUsed = cap.getOneWithChaosTimeUsed();
                    int remaining = 2400 - timeUsed;
                    String status = "Phase: " + formatTime(remaining / 20);
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.active, true);
                } else if (currentTime < cap.getOneWithChaosCooldown()) {
                    long cooldown = (cap.getOneWithChaosCooldown() - currentTime) / 20;
                    String status = "Phase: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Phase: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Damage Negation status (always show)
                if (cap.isDamageNegationActive()) {
                    long elapsed = currentTime - cap.getDamageNegationCooldown();
                    if (elapsed < 200) {
                        String status = "Store: " + String.format("%.0f", cap.getStoredDamage());
                        guiGraphics.drawString(mc.font, status, x, currentY, colors.active, true);
                    } else {
                        String status = "Stored: " + String.format("%.0f", cap.getStoredDamage());
                        guiGraphics.drawString(mc.font, status, x, currentY, colors.ready, true);
                    }
                } else if (currentTime < cap.getDamageNegationCooldown()) {
                    long cooldown = (cap.getDamageNegationCooldown() - currentTime) / 20;
                    String status = "Negate: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Negate: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Desert Walker status (always show)
                if (cap.isDesertWalkerFlying()) {
                    guiGraphics.drawString(mc.font, "Tele: Flying", x, currentY, colors.active, true);
                } else if (currentTime < cap.getDesertWalkerCooldown()) {
                    long cooldown = (cap.getDesertWalkerCooldown() - currentTime) / 20;
                    String status = "Tele: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Tele: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Chaos Incarnate status (always show)
                if (cap.isChaosIncarnateActive()) {
                    long elapsed = currentTime - cap.getChaosIncarnateCooldown();
                    long remaining = (1200 - elapsed) / 20;
                    String status = "AVATAR: " + remaining + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.ultimate, true);
                } else if (currentTime < cap.getChaosIncarnateCooldown()) {
                    long cooldown = (cap.getChaosIncarnateCooldown() - currentTime) / 20;
                    String status = "Avatar: " + formatTime(cooldown);
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Avatar: Ready", x, currentY, colors.ready, true);
                }
                break;
                
            case RA:
                // Solar Flare (always show)
                if (currentTime < cap.getSolarFlareCooldown()) {
                    long cooldown = (cap.getSolarFlareCooldown() - currentTime) / 20;
                    String status = "Flare: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Flare: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Purifying Light (always show)
                if (cap.isPurifyingLightActive() && cap.getPurifyingLightEndTime() > currentTime) {
                    long remaining = (cap.getPurifyingLightEndTime() - currentTime) / 20;
                    String status = "Light: " + remaining + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.active, true);
                } else if (cap.getPurifyingLightCooldown() > currentTime) {
                    long cooldown = (cap.getPurifyingLightCooldown() - currentTime) / 20;
                    String status = "Light: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Light: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Holy Inferno (always show)
                if (currentTime < cap.getHolyInfernoCooldown()) {
                    long cooldown = (cap.getHolyInfernoCooldown() - currentTime) / 20;
                    String status = "Inferno: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Inferno: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Avatar of Sun (always show)
                if (cap.isAvatarOfSunActive()) {
                    long elapsed = currentTime - cap.getAvatarOfSunCooldown();
                    long remaining = (1200 - elapsed) / 20;
                    String status = "AVATAR: " + remaining + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.ultimate, true);
                } else if (currentTime < cap.getAvatarOfSunCooldown()) {
                    long cooldown = (cap.getAvatarOfSunCooldown() - currentTime) / 20;
                    String status = "Avatar: " + formatTime(cooldown);
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Avatar: Ready", x, currentY, colors.ready, true);
                }
                break;
                
            case SHU:
                // Launch cooldown
                if (currentTime < cap.getLaunchCooldown()) {
                    long cooldown = (cap.getLaunchCooldown() - currentTime) / 20;
                    String status = "Launch: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Launch: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Air Boost cooldown
                if (currentTime < cap.getAirBoostCooldown()) {
                    long cooldown = (cap.getAirBoostCooldown() - currentTime) / 20;
                    String status = "Air Boost: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Air Boost: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Extra Jumps status - use client-side tracking
                int jumpsUsed = com.dracolich777.afterlifeentombed.events.ExtraJumpHandler.getClientJumpsUsed(player.getUUID());
                if (currentTime < cap.getExtraJumpsCooldown()) {
                    // On cooldown
                    long cooldown = (cap.getExtraJumpsCooldown() - currentTime) / 20;
                    String status = "Extra Jumps: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else if (jumpsUsed >= 0 && jumpsUsed < 3) {
                    // Mode active with jumps remaining
                    int jumpsRemaining = 3 - jumpsUsed;
                    String status = "Jumps: " + jumpsRemaining + "/3";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.active, true);
                } else {
                    // Ready to activate
                    guiGraphics.drawString(mc.font, "Extra Jumps: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Wind Avatar status
                if (cap.isWindAvatarActive() && cap.getWindAvatarEndTime() > currentTime) {
                    long remaining = (cap.getWindAvatarEndTime() - currentTime) / 20;
                    String status = "Avatar: " + remaining + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.ultimate, true);
                } else if (currentTime < cap.getWindAvatarCooldown()) {
                    long cooldown = (cap.getWindAvatarCooldown() - currentTime) / 20;
                    String status = "Avatar: " + formatTime(cooldown);
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Avatar: Ready", x, currentY, colors.ready, true);
                }
                break;
                
            case ANUBIS:
                // Undead Command cooldown
                if (currentTime < cap.getUndeadCommandCooldown()) {
                    long cooldown = (cap.getUndeadCommandCooldown() - currentTime) / 20;
                    String status = "Command: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Command: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Lifelink status
                if (cap.isLifelinkActive() && cap.getLifelinkEndTime() > currentTime) {
                    long remaining = (cap.getLifelinkEndTime() - currentTime) / 20;
                    String status = "Lifelink: " + remaining + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.active, true);
                } else if (currentTime < cap.getLifelinkCooldown()) {
                    long cooldown = (cap.getLifelinkCooldown() - currentTime) / 20;
                    String status = "Lifelink: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Lifelink: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Summon Undead cooldown
                if (currentTime < cap.getSummonUndeadCooldown()) {
                    long cooldown = (cap.getSummonUndeadCooldown() - currentTime) / 20;
                    String status = "Summon: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Summon: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Avatar of Death status
                if (cap.isAvatarOfDeathActive() && cap.getAvatarOfDeathEndTime() > currentTime) {
                    long remaining = (cap.getAvatarOfDeathEndTime() - currentTime) / 20;
                    String status = "AVATAR: " + remaining + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.ultimate, true);
                } else if (currentTime < cap.getAvatarOfDeathCooldown()) {
                    long cooldown = (cap.getAvatarOfDeathCooldown() - currentTime) / 20;
                    String status = "Avatar: " + formatTime(cooldown);
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Avatar: Ready", x, currentY, colors.ready, true);
                }
                break;
                
            case THOTH:
                // Scholarly Teleport cooldown
                if (currentTime < cap.getScholarlyTeleportCooldown()) {
                    long cooldown = (cap.getScholarlyTeleportCooldown() - currentTime) / 20;
                    String status = "Teleport: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Teleport: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Experience Surge status
                if (cap.isExperienceMultiplierActive()) {
                    guiGraphics.drawString(mc.font, "XP Surge: ACTIVE", x, currentY, colors.active, true);
                } else if (currentTime < cap.getExperienceMultiplierCooldown()) {
                    long cooldown = (cap.getExperienceMultiplierCooldown() - currentTime) / 20;
                    String status = "XP Surge: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "XP Surge: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Divine Enchant cooldown
                if (currentTime < cap.getDivineEnchantCooldown()) {
                    long cooldown = (cap.getDivineEnchantCooldown() - currentTime) / 20;
                    String status = "Enchant: " + formatTime(cooldown);
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Enchant: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Avatar of Wisdom status
                if (cap.isAvatarOfWisdomActive() && cap.getAvatarOfWisdomEndTime() > currentTime) {
                    long remaining = (cap.getAvatarOfWisdomEndTime() - currentTime) / 20;
                    String status = "AVATAR: " + remaining + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.ultimate, true);
                } else if (currentTime < cap.getAvatarOfWisdomCooldown()) {
                    long cooldown = (cap.getAvatarOfWisdomCooldown() - currentTime) / 20;
                    String status = "Avatar: " + formatTime(cooldown);
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Avatar: Ready", x, currentY, colors.ready, true);
                }
                break;
                
            case GEB:
                // Telekinesis (Ability 1)
                if (currentTime < cap.getTelekinesisCooldown()) {
                    long cooldown = (cap.getTelekinesisCooldown() - currentTime) / 20;
                    String status = "Telekinesis: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Telekinesis: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Excavation (Ability 2)
                if (currentTime < cap.getExcavationCooldown()) {
                    long cooldown = (cap.getExcavationCooldown() - currentTime) / 20;
                    String status = "Excavation: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Excavation: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Earth Rise (Ability 3)
                if (currentTime < cap.getEarthRiseCooldown()) {
                    long cooldown = (cap.getEarthRiseCooldown() - currentTime) / 20;
                    String status = "Earth Rise: " + cooldown + "s";
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Earth Rise: Ready", x, currentY, colors.ready, true);
                }
                currentY += lineHeight;
                
                // Avatar of Earth (Ability 4)
                if (cap.isAvatarOfEarthActive()) {
                    long remaining = (cap.getAvatarOfEarthEndTime() - currentTime) / 20;
                    String status = "Avatar: " + formatTime(remaining);
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.active, true);
                } else if (currentTime < cap.getAvatarOfEarthCooldown()) {
                    long cooldown = (cap.getAvatarOfEarthCooldown() - currentTime) / 20;
                    String status = "Avatar: " + formatTime(cooldown);
                    guiGraphics.drawString(mc.font, status, x, currentY, colors.cooldown, true);
                } else {
                    guiGraphics.drawString(mc.font, "Avatar: Ready", x, currentY, colors.ready, true);
                }
                break;
                
            // Other gods - show ready message (with shadow)
            case HORUS:
            case ISIS:
                guiGraphics.drawString(mc.font, "Ready", x, currentY, colors.ready, true);
                break;
                
            case NONE:
                // Should never happen due to early return, but handle it anyway
                break;
        }
    }
    
    private static void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        // Top
        guiGraphics.fill(x, y, x + width, y + 1, color);
        // Bottom
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
        // Left
        guiGraphics.fill(x, y, x + 1, y + height, color);
        // Right
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color);
    }
    
    private static String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            return minutes + "m " + secs + "s";
        }
    }
    
    private static String getGodDisplayName(GodType god) {
        return switch (god) {
            case SETH -> "Seth (Chaos)";
            case RA -> "Ra (Sun)";
            case ISIS -> "Isis (Life)";
            case GEB -> "Geb (Earth)";
            case ANUBIS -> "Anubis (Death)";
            case SHU -> "Shu (Wind)";
            case THOTH -> "Thoth (Wisdom)";
            case HORUS -> "Horus (War)";
            default -> "Unknown";
        };
    }
    
    private static GodColorScheme getGodColors(GodType god) {
        return switch (god) {
            case SETH -> new GodColorScheme(
                0xFF8B00FF,  // Purple primary
                0xFFFF00FF,  // Bright purple active
                0xFFDA70D6,  // Orchid ready
                0xFF808080,  // Gray cooldown
                0xFFFF00FF   // Bright purple ultimate
            );
            case RA -> new GodColorScheme(
                0xFFFFD700,  // Gold primary
                0xFFFFFFFF,  // White active (sun glare)
                0xFFFFA500,  // Orange ready (warm sunlight)
                0xFF808080,  // Gray cooldown
                0xFFFFFFFF   // White ultimate (blinding sun)
            );
            case ISIS -> new GodColorScheme(
                0xFF00FFFF,  // Cyan primary
                0xFF00CED1,  // Aqua active
                0xFF87CEEB,  // Sky blue ready
                0xFF808080,  // Gray cooldown
                0xFF4169E1   // Royal blue ultimate
            );
            case GEB -> new GodColorScheme(
                0xFF8B4513,  // Brown primary
                0xFF228B22,  // Green active
                0xFF90EE90,  // Light green ready
                0xFF808080,  // Gray cooldown
                0xFF654321   // Dark brown ultimate
            );
            case ANUBIS -> new GodColorScheme(
                0xFF663399,  // Purple primary
                0xFF4B0082,  // Indigo active
                0xFF9370DB,  // Medium purple ready
                0xFF808080,  // Gray cooldown
                0xFF8B008B   // Dark magenta ultimate
            );
            case SHU -> new GodColorScheme(
                0xFF87CEEB,  // Sky blue primary
                0xFF00FFFF,  // Cyan active
                0xFFE0FFFF,  // Light cyan ready
                0xFF808080,  // Gray cooldown
                0xFF4682B4   // Steel blue ultimate
            );
            case THOTH -> new GodColorScheme(
                0xFFDAA520,  // Goldenrod primary
                0xFF4169E1,  // Royal blue active
                0xFF9370DB,  // Medium purple ready
                0xFF808080,  // Gray cooldown
                0xFFFFD700   // Gold ultimate
            );
            case HORUS -> new GodColorScheme(
                0xFFDC143C,  // Crimson primary
                0xFFFF4500,  // Orange red active
                0xFFFFD700,  // Gold ready
                0xFF808080,  // Gray cooldown
                0xFF8B0000   // Dark red ultimate
            );
            default -> new GodColorScheme(
                0xFFFFFFFF,  // White
                0xFFAAAAAA,  // Light gray
                0xFF888888,  // Gray
                0xFF666666,  // Dark gray
                0xFFFFFFFF   // White
            );
        };
    }
    
    /**
     * Color scheme for a god's HUD
     */
    private static class GodColorScheme {
        final int primary;    // Main color for borders and title
        final int active;     // Color for active abilities
        final int ready;      // Color for ready-to-use abilities
        final int cooldown;   // Color for abilities on cooldown
        final int ultimate;   // Color for avatar/ultimate ability
        
        GodColorScheme(int primary, int active, int ready, int cooldown, int ultimate) {
            this.primary = primary;
            this.active = active;
            this.ready = ready;
            this.cooldown = cooldown;
            this.ultimate = ultimate;
        }
    }
}
