package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.client.OreArrowRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Packet to sync ore positions with ore type from server to client for arrow rendering
 */
public class SyncOreMarkersPacket {
    private final Map<BlockPos, String> oreData; // Position -> ore block ID
    private final boolean clear;
    
    public SyncOreMarkersPacket(Map<BlockPos, String> oreData) {
        this.oreData = new HashMap<>(oreData);
        this.clear = false;
    }
    
    public SyncOreMarkersPacket(boolean clear) {
        this.oreData = new HashMap<>();
        this.clear = clear;
    }
    
    public static void encode(SyncOreMarkersPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.clear);
        buf.writeInt(packet.oreData.size());
        for (Map.Entry<BlockPos, String> entry : packet.oreData.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            buf.writeUtf(entry.getValue());
        }
    }
    
    public static SyncOreMarkersPacket decode(FriendlyByteBuf buf) {
        boolean clear = buf.readBoolean();
        if (clear) {
            return new SyncOreMarkersPacket(true);
        }
        
        int size = buf.readInt();
        Map<BlockPos, String> oreData = new HashMap<>();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buf.readBlockPos();
            String oreId = buf.readUtf();
            oreData.put(pos, oreId);
        }
        return new SyncOreMarkersPacket(oreData);
    }
    
    public static void handle(SyncOreMarkersPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side only
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                if (packet.clear) {
                    OreArrowRenderer.clearArrows();
                } else {
                    OreArrowRenderer.setOreData(packet.oreData);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
