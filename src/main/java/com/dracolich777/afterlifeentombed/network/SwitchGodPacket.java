package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.client.hud.GodAvatarHudHelper;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlibs.api.AfterLibsAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from client to server to request god switching from the GUI
 */
public class SwitchGodPacket {
    private final GodType targetGod;
    
    public SwitchGodPacket(GodType targetGod) {
        this.targetGod = targetGod;
    }
    
    public SwitchGodPacket(FriendlyByteBuf buf) {
        this.targetGod = GodType.valueOf(buf.readUtf());
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(targetGod.name());
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                // Check if player has unlocked this god
                if (!cap.hasUnlockedGod(targetGod)) {
                    GodAvatarHudHelper.sendNotification(player, "You haven't unlocked " + targetGod.name() + " yet!", 
                        GodAvatarHudHelper.COLOR_ERROR, 60);
                    return;
                }
                
                // Check if already this god
                if (cap.getSelectedGod() == targetGod) {
                    return;
                }
                
                // Switch the god
                cap.setSelectedGod(targetGod);
                
                // Set grace period to prevent immediate ability activation
                long currentTime = player.level().getGameTime();
                cap.setGodSwitchGracePeriod(currentTime + 10);
                
                // Grant the appropriate origin
                var server = player.getServer();
                if (server != null) {
                    String originName = switch (targetGod) {
                        case SETH -> "afterlifeentombed:avatar_of_seth";
                        case RA -> "afterlifeentombed:avatar_of_ra";
                        case HORUS -> "afterlifeentombed:avatar_of_horus";
                        case ISIS -> "afterlifeentombed:avatar_of_isis";
                        case GEB -> "afterlifeentombed:avatar_of_geb";
                        case ANUBIS -> "afterlifeentombed:avatar_of_anubis";
                        case SHU -> "afterlifeentombed:avatar_of_shu";
                        case THOTH -> "afterlifeentombed:avatar_of_thoth";
                        default -> null;
                    };
                    
                    if (originName != null) {
                        // Revoke all god origins first
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
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_anubis"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_shu"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_thoth"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_geb"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_horus"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_isis"
                        );
                        
                        // Grant the new origin
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin set " + player.getGameProfile().getName() + " origins:origin " + originName
                        );
                    }
                }
                
                // Spawn particle effects
                if (player.level() instanceof ServerLevel level) {
                    double x = player.getX();
                    double y = player.getY();
                    double z = player.getZ();
                    
                    switch (targetGod) {
                        case SETH -> AfterLibsAPI.spawnAfterlifeParticle(level, "seth_fog", x, y + 1, z, 1.0f);
                        case RA -> AfterLibsAPI.spawnAfterlifeParticle(level, "ra_halo", x, y + 5, z, 0.5f);
                        case HORUS -> AfterLibsAPI.spawnAfterlifeParticle(level, "horus_shield", x, y + 1, z, 1.0f);
                        case SHU -> AfterLibsAPI.spawnAfterlifeParticle(level, "shu_jump2", x, y + 1, z, 1.0f);
                        case ISIS -> AfterLibsAPI.spawnAfterlifeParticle(level, "healing_burst", x, y + 1, z, 1.0f);
                        case GEB -> AfterLibsAPI.spawnAfterlifeParticle(level, "earth_aura", x, y + 1, z, 1.0f);
                    }
                }
                
                // Send notification
                GodAvatarHudHelper.sendNotification(player, "§6Transformed into Avatar of " + targetGod.name() + "!", 
                    GodAvatarHudHelper.COLOR_SPECIAL, 80);
            });
        });
        context.setPacketHandled(true);
    }
}
