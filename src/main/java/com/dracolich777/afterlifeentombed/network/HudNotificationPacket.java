package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.client.hud.GodAvatarHudOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet to send HUD notifications from server to client
 */
public class HudNotificationPacket {
    private final String message;
    private final int color;
    private final int durationTicks;
    
    public HudNotificationPacket(String message, int color, int durationTicks) {
        this.message = message;
        this.color = color;
        this.durationTicks = durationTicks;
    }
    
    public HudNotificationPacket(FriendlyByteBuf buf) {
        this.message = buf.readUtf();
        this.color = buf.readInt();
        this.durationTicks = buf.readInt();
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(message);
        buf.writeInt(color);
        buf.writeInt(durationTicks);
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // This runs on the client side
            GodAvatarHudOverlay.showNotification(message, color, durationTicks);
        });
        ctx.get().setPacketHandled(true);
    }
}
