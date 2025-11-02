package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.boons.ActiveBoon;
import com.dracolich777.afterlifeentombed.boons.BoonType;
import com.dracolich777.afterlifeentombed.capabilities.PlayerBoonsCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from client to server when player selects a boon
 */
public class SelectBoonPacket {
    private final BoonType selectedBoon;

    public SelectBoonPacket(BoonType selectedBoon) {
        this.selectedBoon = selectedBoon;
    }

    public static void encode(SelectBoonPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.selectedBoon.name());
    }

    public static SelectBoonPacket decode(FriendlyByteBuf buf) {
        return new SelectBoonPacket(BoonType.valueOf(buf.readUtf()));
    }

    public static void handle(SelectBoonPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // Add the boon to the player's capability
                player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(cap -> {
                    long currentTime = player.level().getGameTime();
                    ActiveBoon boon = new ActiveBoon(packet.selectedBoon, currentTime);
                    cap.addBoon(boon);
                    
                    // Log for debugging
                    com.dracolich777.afterlifeentombed.AfterlifeEntombedMod.LOGGER.info(
                        "Player {} selected boon: {}", 
                        player.getName().getString(), 
                        packet.selectedBoon.getDisplayName()
                    );
                    
                    // Sync to client
                    GodAvatarPackets.sendToPlayer(player, new SyncPlayerBoonsPacket(cap.getActiveBoons()));
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
