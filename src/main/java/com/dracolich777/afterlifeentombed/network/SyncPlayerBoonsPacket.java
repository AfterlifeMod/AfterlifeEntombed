package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.boons.ActiveBoon;
import com.dracolich777.afterlifeentombed.capabilities.PlayerBoonsCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Packet for syncing player boons from server to client
 */
public class SyncPlayerBoonsPacket {
    private final List<ActiveBoon> boons;

    public SyncPlayerBoonsPacket(List<ActiveBoon> boons) {
        this.boons = boons;
    }

    public static void encode(SyncPlayerBoonsPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.boons.size());
        for (ActiveBoon boon : packet.boons) {
            buf.writeNbt(boon.serializeNBT());
        }
    }

    public static SyncPlayerBoonsPacket decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<ActiveBoon> boons = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CompoundTag tag = buf.readNbt();
            if (tag != null) {
                ActiveBoon boon = ActiveBoon.deserializeNBT(tag);
                if (boon != null) {
                    boons.add(boon);
                }
            }
        }
        return new SyncPlayerBoonsPacket(boons);
    }

    public static void handle(SyncPlayerBoonsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side: Update the player's boons capability
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                minecraft.player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(cap -> {
                    // Clear existing and add all from packet
                    cap.getActiveBoons().clear();
                    packet.boons.forEach(cap::addBoon);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
