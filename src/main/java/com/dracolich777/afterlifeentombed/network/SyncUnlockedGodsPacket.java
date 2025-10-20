package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.items.GodType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Packet for syncing unlocked gods from server to client
 * Sent whenever a player unlocks a new god
 */
public class SyncUnlockedGodsPacket {
    private final Set<GodType> unlockedGods;
    
    public SyncUnlockedGodsPacket(Set<GodType> unlockedGods) {
        this.unlockedGods = new HashSet<>(unlockedGods);
    }
    
    public static void encode(SyncUnlockedGodsPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.unlockedGods.size());
        for (GodType god : packet.unlockedGods) {
            buf.writeUtf(god.name());
        }
    }
    
    public static SyncUnlockedGodsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Set<GodType> unlockedGods = new HashSet<>();
        for (int i = 0; i < size; i++) {
            try {
                GodType god = GodType.valueOf(buf.readUtf());
                unlockedGods.add(god);
            } catch (IllegalArgumentException e) {
                // Invalid god type, skip
            }
        }
        return new SyncUnlockedGodsPacket(unlockedGods);
    }
    
    public static void handle(SyncUnlockedGodsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                    // Clear existing unlocked gods and add new ones
                    for (GodType god : packet.unlockedGods) {
                        cap.unlockGod(god);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
