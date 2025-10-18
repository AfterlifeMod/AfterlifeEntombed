package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.items.GodType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/**
 * Packet for syncing god avatar selection between client and server
 */
public class SyncGodAvatarPacket {
    private final String godName;

    public SyncGodAvatarPacket(GodType god) {
        this.godName = god.name();
    }

    public SyncGodAvatarPacket(String godName) {
        this.godName = godName;
    }

    public static void encode(SyncGodAvatarPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.godName);
    }

    public static SyncGodAvatarPacket decode(FriendlyByteBuf buf) {
        return new SyncGodAvatarPacket(buf.readUtf());
    }

    public static void handle(SyncGodAvatarPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                    try {
                        GodType god = GodType.valueOf(packet.godName);
                        cap.setSelectedGod(god);
                    } catch (IllegalArgumentException e) {
                        // Invalid god name, ignore
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
