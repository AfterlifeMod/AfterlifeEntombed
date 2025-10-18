package com.dracolich777.afterlibs.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import com.dracolich777.afterlibs.AfterLibs;

/**
 * Network handler for AfterLibs particle system
 */
public class NetworkHandler {
    
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(AfterLibs.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    
    private static int packetId = 0;
    
    public static void register() {
        INSTANCE.messageBuilder(ParticlePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
            .decoder(ParticlePacket::decode)
            .encoder(ParticlePacket::encode)
            .consumerMainThread(ParticlePacket::handle)
            .add();
    }
    
    /**
     * Send a particle to all players in the dimension
     */
    public static void sendParticleToAll(ServerLevel level, String particleName, 
                                        double x, double y, double z, float scale) {
        ParticlePacket packet = new ParticlePacket(x, y, z, particleName, scale);
        INSTANCE.send(PacketDistributor.DIMENSION.with(() -> level.dimension()), packet);
    }
    
    /**
     * Send a particle to all players in the dimension with custom scale
     */
    public static void sendParticleToAll(ServerLevel level, String particleName, 
                                        double x, double y, double z, 
                                        float scaleX, float scaleY, float scaleZ) {
        ParticlePacket packet = new ParticlePacket(x, y, z, particleName, scaleX, scaleY, scaleZ);
        INSTANCE.send(PacketDistributor.DIMENSION.with(() -> level.dimension()), packet);
    }
    
    /**
     * Send a particle to a specific player
     */
    public static void sendParticleToPlayer(ServerPlayer player, String particleName, 
                                           double x, double y, double z, float scale) {
        ParticlePacket packet = new ParticlePacket(x, y, z, particleName, scale);
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
    
    /**
     * Send a particle to players near a location
     */
    public static void sendParticleToNearby(ServerLevel level, String particleName, 
                                           double x, double y, double z, float scale, double range) {
        ParticlePacket packet = new ParticlePacket(x, y, z, particleName, scale);
        INSTANCE.send(PacketDistributor.NEAR.with(() -> 
            new PacketDistributor.TargetPoint(x, y, z, range, level.dimension())), packet);
    }
}