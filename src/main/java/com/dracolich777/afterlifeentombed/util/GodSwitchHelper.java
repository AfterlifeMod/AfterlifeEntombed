package com.dracolich777.afterlifeentombed.util;

import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.client.hud.GodAvatarHudHelper;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.items.GodstoneItem;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.SyncGodAvatarPacket;
import com.dracolich777.afterlibs.api.AfterLibsAPI;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Centralised god-switching helper.
 *
 * Every avatar's "hold a godstone and press the ultimate key" logic is
 * identical except for which GodType is excluded (you can't switch TO the god
 * you already are).  All discrepancies between Ra / Geb / Shu / Isis / Horus
 * (wrong exclude check, missing unlockGod call, inconsistent particle names)
 * are fixed here in one place.
 *
 * Usage:
 *   boolean switched = GodSwitchHelper.tryGodSwitch(player, cap, currentGod);
 *   if (switched) return;
 *   // … normal ultimate activation …
 */
public final class GodSwitchHelper {

    private GodSwitchHelper() {}

    /**
     * All origin IDs that must be revoked before granting a new one.
     * Keep this list in sync with GodType values.
     */
    private static final String[] ALL_AVATAR_ORIGIN_IDS = {
        "afterlifeentombed:avatar_of_egypt",
        "afterlifeentombed:avatar_of_ra",
        "afterlifeentombed:avatar_of_seth",
        "afterlifeentombed:avatar_of_shu",
        "afterlifeentombed:avatar_of_anubis",
        "afterlifeentombed:avatar_of_thoth",
        "afterlifeentombed:avatar_of_geb",
        "afterlifeentombed:avatar_of_horus",
        "afterlifeentombed:avatar_of_isis",
    };

    /**
     * Returns the Effekseer particle name that plays when switching to a given god.
     * All names are verified against the effeks/ resource folder.
     */
    public static String getSwapParticle(GodType god) {
        return switch (god) {
            case RA    -> "ra_halo";
            case SHU   -> "shu_jump2";   // NOTE: was "shujump" in old code – fixed
            case ANUBIS -> "anubis_nuke";
            case GEB   -> "earth_aura";  // NOTE: was "seth_fog" in old code – now uses dedicated earth aura
            case HORUS -> "horus_shield";
            case ISIS  -> "isis_heal2";
            case SETH  -> "seth_fog";
            case THOTH -> "thoth_book";     // Thoth uses Ra's light halo (no dedicated particle yet)
            default    -> "ra_halo";
        };
    }

    /**
     * Returns the Origins origin ID for a given god.
     */
    public static String getOriginId(GodType god) {
        return switch (god) {
            case RA    -> "afterlifeentombed:avatar_of_ra";
            case SETH  -> "afterlifeentombed:avatar_of_seth";
            case SHU   -> "afterlifeentombed:avatar_of_shu";
            case ANUBIS -> "afterlifeentombed:avatar_of_anubis";
            case THOTH  -> "afterlifeentombed:avatar_of_thoth";
            case GEB   -> "afterlifeentombed:avatar_of_geb";
            case HORUS -> "afterlifeentombed:avatar_of_horus";
            case ISIS  -> "afterlifeentombed:avatar_of_isis";
            default    -> null;
        };
    }

    /**
     * Attempt a god-switch when the player presses their ultimate key while
     * holding a Godstone for a different god.
     *
     * @param player      the server player
     * @param cap         their GodAvatarCapability
     * @param currentGod  the god they are currently an avatar of (to exclude)
     * @return {@code true} if a switch was performed and the caller should return early
     */
    public static boolean tryGodSwitch(ServerPlayer player,
                                       GodAvatarCapability.IGodAvatar cap,
                                       GodType currentGod) {
        ItemStack mainHand = player.getMainHandItem();
        if (!(mainHand.getItem() instanceof GodstoneItem godstone)) {
            return false;
        }

        GodType newGod = godstone.getGodType();

        // Must be a different, valid god
        if (newGod == currentGod || newGod == GodType.NONE) {
            return false;
        }

        // Unlock and select the new god
        cap.unlockGod(newGod);
        cap.setSelectedGod(newGod);

        // Consume the godstone
        mainHand.shrink(1);

        // Spawn the appropriate swap particle
        if (player.level() instanceof ServerLevel level) {
            String particle = getSwapParticle(newGod);
            AfterLibsAPI.spawnAfterlifeParticle(level, particle,
                    player.getX(), player.getY() + 1, player.getZ(), 2.0f);
        }

        // Swap Origins: revoke every avatar origin, then grant the new one
        var server = player.getServer();
        if (server != null) {
            // Revoke all existing avatar origins
            for (String originId : ALL_AVATAR_ORIGIN_IDS) {
                server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack(),
                    "origin revoke " + player.getGameProfile().getName()
                            + " origins:origin " + originId);
            }

            // Grant the new origin
            String newOriginId = getOriginId(newGod);
            if (newOriginId != null) {
                server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack(),
                    "origin set " + player.getGameProfile().getName()
                            + " origins:origin " + newOriginId);
            }
        }

        GodAvatarHudHelper.sendNotification(player,
                "Now avatar of " + newGod.name(), GodAvatarHudHelper.COLOR_SPECIAL, 60);

        // Sync the new god to the client
        GodAvatarPackets.INSTANCE.send(
            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
            new SyncGodAvatarPacket(newGod)
        );
        return true;
    }
}
