package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.client.OreArrowRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Packet to sync ore positions from server to client for arrow rendering
 */
public class SyncOreMarkersPacket {
    private final Set<BlockPos> orePositions;
    private final boolean clear;
    
    public SyncOreMarkersPacket(Set<BlockPos> orePositions) {
        this.orePositions = new HashSet<>(orePositions);
        this.clear = false;
    }
    
    public SyncOreMarkersPacket(boolean clear) {
        this.orePositions = new HashSet<>();
        this.clear = clear;
    }
    
    public static void encode(SyncOreMarkersPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.clear);
        buf.writeInt(packet.orePositions.size());
        for (BlockPos pos : packet.orePositions) {
            buf.writeBlockPos(pos);
        }
    }
    
    public static SyncOreMarkersPacket decode(FriendlyByteBuf buf) {
        boolean clear = buf.readBoolean();
        if (clear) {
            return new SyncOreMarkersPacket(true);
        }
        
        int size = buf.readInt();
        Set<BlockPos> positions = new HashSet<>();
        for (int i = 0; i < size; i++) {
            positions.add(buf.readBlockPos());
        }
        return new SyncOreMarkersPacket(positions);
    }
    
    public static void handle(SyncOreMarkersPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side only
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                if (packet.clear) {
                    OreArrowRenderer.clearArrows();
                } else {
                    OreArrowRenderer.setOrePositions(packet.orePositions);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
