package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ParticleEffectPacket {
    private final double x, y, z;
    private final String effectType;
    private final float scaleX, scaleY, scaleZ;

    public ParticleEffectPacket(double x, double y, double z, String effectType) {
        this(x, y, z, effectType, 1.0f, 1.0f, 1.0f);
    }
    
    public ParticleEffectPacket(double x, double y, double z, String effectType, float scaleX, float scaleY, float scaleZ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.effectType = effectType;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public static void encode(ParticleEffectPacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
        buf.writeUtf(packet.effectType);
        buf.writeFloat(packet.scaleX);
        buf.writeFloat(packet.scaleY);
        buf.writeFloat(packet.scaleZ);
    }

    public static ParticleEffectPacket decode(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        String effectType = buf.readUtf();
        float scaleX = buf.readFloat();
        float scaleY = buf.readFloat();
        float scaleZ = buf.readFloat();
        return new ParticleEffectPacket(x, y, z, effectType, scaleX, scaleY, scaleZ);
    }

    public static void handle(ParticleEffectPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            try {
                // This runs on the client side
                Level level = Minecraft.getInstance().level;
                
                if (level != null && level.isClientSide()) {
                    // Use the new ParticleManager to spawn particles
                    boolean success = com.dracolich777.afterlifeentombed.util.ParticleManager.spawnParticle(
                        level, packet.effectType, packet.x, packet.y, packet.z, 
                        packet.scaleX, packet.scaleY, packet.scaleZ
                    );
                    
                    if (success) {
                        AfterlifeEntombedMod.LOGGER.info("CLIENT: Successfully spawned particle '{}' at ({}, {}, {}) with scale ({}, {}, {})", 
                            packet.effectType, packet.x, packet.y, packet.z, packet.scaleX, packet.scaleY, packet.scaleZ);
                    } else {
                        AfterlifeEntombedMod.LOGGER.warn("CLIENT: Failed to spawn particle '{}'", packet.effectType);
                    }
                }
            } catch (Exception e) {
                AfterlifeEntombedMod.LOGGER.error("Failed to spawn particle effect from packet: {}", e.getMessage());
            }
        });
        context.get().setPacketHandled(true);
    }
}