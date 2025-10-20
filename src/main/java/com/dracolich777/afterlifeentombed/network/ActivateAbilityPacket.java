package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.events.GebAvatarAbilities;
import com.dracolich777.afterlifeentombed.events.SethAvatarAbilities;
import com.dracolich777.afterlifeentombed.items.GodType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/**
 * Packet sent from client to server when player activates an ability
 */
public class ActivateAbilityPacket {
    private final int abilityId;

    public ActivateAbilityPacket(int abilityId) {
        this.abilityId = abilityId;
    }

    public static void encode(ActivateAbilityPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.abilityId);
    }

    public static ActivateAbilityPacket decode(FriendlyByteBuf buf) {
        return new ActivateAbilityPacket(buf.readInt());
    }

    public static void handle(ActivateAbilityPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // Route to correct god's ability handler
                player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                    GodType god = cap.getSelectedGod();
                    switch (god) {
                        case SETH -> SethAvatarAbilities.activateAbility(player, packet.abilityId);
                        case GEB -> GebAvatarAbilities.activateAbility(player, packet.abilityId);
                        // Add other gods as they are implemented
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
