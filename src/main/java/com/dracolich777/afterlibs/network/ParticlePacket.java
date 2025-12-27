package com.dracolich777.afterlibs.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.function.Supplier;

/**
 * Network packet for spawning particles on the client side
 */
public class ParticlePacket {
    
    private final double x, y, z;
    private final String particleName;
    private final float scaleX, scaleY, scaleZ;
    private final float yaw, pitch, roll;
    
    public ParticlePacket(double x, double y, double z, String particleName, float scale) {
        this(x, y, z, particleName, scale, scale, scale, 0.0f, 0.0f, 0.0f);
    }
    
    public ParticlePacket(double x, double y, double z, String particleName, 
                         float scaleX, float scaleY, float scaleZ) {
        this(x, y, z, particleName, scaleX, scaleY, scaleZ, 0.0f, 0.0f, 0.0f);
    }
    
    public ParticlePacket(double x, double y, double z, String particleName, 
                         float scaleX, float scaleY, float scaleZ,
                         float yaw, float pitch, float roll) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.particleName = particleName;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }
    
    public static void encode(ParticlePacket packet, FriendlyByteBuf buf) {
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
        buf.writeUtf(packet.particleName);
        buf.writeFloat(packet.scaleX);
        buf.writeFloat(packet.scaleY);
        buf.writeFloat(packet.scaleZ);
        buf.writeFloat(packet.yaw);
        buf.writeFloat(packet.pitch);
        buf.writeFloat(packet.roll);
    }
    
    public static ParticlePacket decode(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        String particleName = buf.readUtf();
        float scaleX = buf.readFloat();
        float scaleY = buf.readFloat();
        float scaleZ = buf.readFloat();
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        float roll = buf.readFloat();
        return new ParticlePacket(x, y, z, particleName, scaleX, scaleY, scaleZ, yaw, pitch, roll);
    }
    
    public static void handle(ParticlePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // Use DistExecutor to only run client code on the client
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ()
                    -> ClientPacketHandler.handleClientSide(packet)
            );
        });
        context.get().setPacketHandled(true);
    }

    // Separate class to hold client-only code
    private static class ClientPacketHandler {

        static void handleClientSide(ParticlePacket packet) {
            // Import moved inside method to avoid loading on server
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            net.minecraft.world.level.Level level = minecraft.level;
            
            if (level != null && level.isClientSide()) {
                com.dracolich777.afterlibs.particle.ParticleManager.spawnParticle(
                    level, packet.particleName, 
                    packet.x, packet.y, packet.z,
                    packet.scaleX, packet.scaleY, packet.scaleZ,
                    packet.yaw, packet.pitch, packet.roll
                );
            }
        }
    }
}