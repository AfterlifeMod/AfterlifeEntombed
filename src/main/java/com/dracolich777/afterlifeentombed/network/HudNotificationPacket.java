package com.dracolich777.afterlifeentombed.network;

import java.util.function.Supplier;

import com.dracolich777.afterlifeentombed.client.hud.GodAvatarHudOverlay;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

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
            // Use DistExecutor to safely call client-only code
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> ClientHandler.showNotification(message, color, durationTicks));
        });
        ctx.get().setPacketHandled(true);
    }

    // Separate client handler to avoid loading client classes on server
    public static class ClientHandler {

        @OnlyIn(Dist.CLIENT)
        public static void showNotification(String message, int color, int durationTicks) {
            GodAvatarHudOverlay.showNotification(message, color, durationTicks);
        }
    }
}
