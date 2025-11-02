package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.boons.BoonType;
import com.dracolich777.afterlifeentombed.client.gui.BoonSelectionScreen;
import com.dracolich777.afterlifeentombed.items.GodType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from server to client to open the boon selection screen
 */
public class OpenBoonSelectionPacket {
    private final GodType god;
    private final BoonType[] choices;
    private final boolean isBlessing;

    public OpenBoonSelectionPacket(GodType god, BoonType[] choices, boolean isBlessing) {
        this.god = god;
        this.choices = choices;
        this.isBlessing = isBlessing;
    }

    public static void encode(OpenBoonSelectionPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.god.name());
        buf.writeBoolean(packet.isBlessing);
        buf.writeInt(packet.choices.length);
        for (BoonType choice : packet.choices) {
            buf.writeUtf(choice.name());
        }
    }

    public static OpenBoonSelectionPacket decode(FriendlyByteBuf buf) {
        GodType god = GodType.valueOf(buf.readUtf());
        boolean isBlessing = buf.readBoolean();
        int count = buf.readInt();
        BoonType[] choices = new BoonType[count];
        for (int i = 0; i < count; i++) {
            choices[i] = BoonType.valueOf(buf.readUtf());
        }
        return new OpenBoonSelectionPacket(god, choices, isBlessing);
    }

    public static void handle(OpenBoonSelectionPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side: Open the boon selection screen
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(new BoonSelectionScreen(packet.god, packet.choices, packet.isBlessing));
        });
        ctx.get().setPacketHandled(true);
    }
}
