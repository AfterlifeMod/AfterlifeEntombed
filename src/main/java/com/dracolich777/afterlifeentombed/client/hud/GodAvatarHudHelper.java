package com.dracolich777.afterlifeentombed.client.hud;

import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.HudNotificationPacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * Helper class for sending notifications to the God Avatar HUD
 */
public class GodAvatarHudHelper {
    
    // Color constants
    public static final int COLOR_ERROR = 0xFFFF5555;       // Red
    public static final int COLOR_WARNING = 0xFFFFAA00;     // Orange
    public static final int COLOR_SUCCESS = 0xFF55FF55;     // Green
    public static final int COLOR_INFO = 0xFF55FFFF;        // Cyan
    public static final int COLOR_SPECIAL = 0xFFFFFF55;     // Yellow
    
    // God-specific colors
    public static final int COLOR_SETH = 0xFF8B00FF;        // Purple
    public static final int COLOR_RA = 0xFFFFD700;          // Gold
    public static final int COLOR_ISIS = 0xFF00FFFF;        // Cyan
    public static final int COLOR_GEB = 0xFF8B4513;         // Brown
    public static final int COLOR_ANUBIS = 0xFF663399;      // Purple
    public static final int COLOR_SHU = 0xFF87CEEB;         // Sky blue
    public static final int COLOR_THOTH = 0xFFDAA520;       // Goldenrod
    public static final int COLOR_HORUS = 0xFFDC143C;       // Crimson
    
    /**
     * Send a notification to the player's HUD (server-side)
     */
    public static void sendNotification(ServerPlayer player, String message, int color, int durationTicks) {
        GodAvatarPackets.INSTANCE.sendTo(new HudNotificationPacket(message, color, durationTicks), player.connection.connection, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT);
    }
    
    /**
     * Send a notification with default duration (2 seconds / 40 ticks)
     */
    public static void sendNotification(ServerPlayer player, String message, int color) {
        sendNotification(player, message, color, 40);
    }
    
    /**
     * Send a cooldown message
     */
    public static void sendCooldownMessage(ServerPlayer player, String abilityName, long secondsRemaining) {
        sendNotification(player, abilityName + " on cooldown: " + secondsRemaining + "s", COLOR_ERROR, 30);
    }
    
    /**
     * Send an activation message
     */
    public static void sendActivationMessage(ServerPlayer player, String abilityName, int color) {
        sendNotification(player, abilityName + " activated!", color, 40);
    }
    
    /**
     * Send a deactivation message
     */
    public static void sendDeactivationMessage(ServerPlayer player, String abilityName, int color) {
        sendNotification(player, abilityName + " deactivated", color, 40);
    }
}
