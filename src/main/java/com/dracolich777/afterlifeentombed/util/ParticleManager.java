package com.dracolich777.afterlifeentombed.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.network.NetworkHandler;
import com.dracolich777.afterlifeentombed.network.ParticleEffectPacket;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

/**
 * Programmatic particle manager for AAA particles in Afterlife: Entombed
 */
public class ParticleManager {

    private static final Logger LOGGER = LogManager.getLogger();

    // Registry of all available particles
    private static final Map<String, ParticleEmitterInfo> PARTICLES = new HashMap<>();

    // Initialize all particles
    static {
        registerParticle("seth_dissolve", "seth_crown_disolve");
        registerParticle("seth_appear", "seth_crown_appear");
        registerParticle("ra_ring", "ra_ring");
        registerParticle("horus_shield", "horus_shield");
        registerParticle("sky", "shu_jump2");
        registerParticle("haze", "haze");
        registerParticle("haze2", "haze2");
        registerParticle("haze3", "haze3");
        registerParticle("shu", "haze_flash");
        LOGGER.info("ParticleManager initialized with {} particles", PARTICLES.size());
    }

    /**
     * Register a particle with the manager
     * 
     * @param name         The logical name for the particle (used in commands/code)
     * @param resourcePath The actual resource path for the .efkefc file
     */
    private static void registerParticle(String name, String resourcePath) {
        try {
            ResourceLocation location = new ResourceLocation(AfterlifeEntombedMod.MOD_ID, resourcePath);
            ParticleEmitterInfo particle = new ParticleEmitterInfo(location);
            PARTICLES.put(name, particle);
            LOGGER.info("Registered particle '{}' with resource '{}'", name, resourcePath);
        } catch (Exception e) {
            LOGGER.error("Failed to register particle '{}' with resource '{}': {}", name, resourcePath, e.getMessage());
        }
    }

    /**
     * Spawn a particle in the world with default settings
     * 
     * @param level        The level to spawn in
     * @param particleName The name of the particle to spawn
     * @param x            X coordinate
     * @param y            Y coordinate
     * @param z            Z coordinate
     * @return true if particle was spawned successfully
     */
    public static boolean spawnParticle(Level level, String particleName, double x, double y, double z) {
        return spawnParticle(level, particleName, x, y, z, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Spawn a particle in the world with custom scale
     * 
     * @param level        The level to spawn in
     * @param particleName The name of the particle to spawn
     * @param x            X coordinate
     * @param y            Y coordinate
     * @param z            Z coordinate
     * @param scaleX       X scale factor
     * @param scaleY       Y scale factor
     * @param scaleZ       Z scale factor
     * @return true if particle was spawned successfully
     */
    public static boolean spawnParticle(Level level, String particleName, double x, double y, double z,
            float scaleX, float scaleY, float scaleZ) {
        return spawnParticle(level, particleName, x, y, z, scaleX, scaleY, scaleZ, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Spawn a particle in the world with full customization
     * 
     * @param level        The level to spawn in
     * @param particleName The name of the particle to spawn
     * @param x            X coordinate
     * @param y            Y coordinate
     * @param z            Z coordinate
     * @param scaleX       X scale factor
     * @param scaleY       Y scale factor
     * @param scaleZ       Z scale factor
     * @param yaw          Yaw rotation in degrees
     * @param pitch        Pitch rotation in degrees
     * @param roll         Roll rotation in degrees
     * @return true if particle was spawned successfully
     */
    public static boolean spawnParticle(Level level, String particleName, double x, double y, double z,
            float scaleX, float scaleY, float scaleZ,
            float yaw, float pitch, float roll) {
        if (level == null) {
            LOGGER.warn("Cannot spawn particle '{}': level is null", particleName);
            return false;
        }

        if (!level.isClientSide()) {
            LOGGER.warn("Cannot spawn particle '{}': must be called on client side", particleName);
            return false;
        }

        ParticleEmitterInfo particle = PARTICLES.get(particleName);
        if (particle == null) {
            LOGGER.warn("Unknown particle name: '{}'. Available particles: {}", particleName, getAvailableParticles());
            return false;
        }

        try {
            // Convert degrees to radians
            float yawRad = (float) Math.toRadians(yaw);
            float pitchRad = (float) Math.toRadians(pitch);
            float rollRad = (float) Math.toRadians(roll);

            AAALevel.addParticle(
                    level,
                    particle
                            .position(x, y, z)
                            .rotation(yawRad, pitchRad, rollRad)
                            .scale(scaleX, scaleY, scaleZ));

            LOGGER.info("Successfully spawned particle '{}' at ({}, {}, {}) with scale ({}, {}, {})",
                    particleName, x, y, z, scaleX, scaleY, scaleZ);
            return true;

        } catch (Exception e) {
            LOGGER.error("Failed to spawn particle '{}': {}", particleName, e.getMessage());
            return false;
        }
    }

    /**
     * Check if a particle exists
     * 
     * @param particleName The name of the particle
     * @return true if the particle exists
     */
    public static boolean hasParticle(String particleName) {
        return PARTICLES.containsKey(particleName);
    }

    /**
     * Get all available particle names
     * 
     * @return Set of all particle names
     */
    public static Set<String> getAvailableParticles() {
        return PARTICLES.keySet();
    }

    /**
     * Get the particle emitter info for a given name
     * 
     * @param particleName The name of the particle
     * @return ParticleEmitterInfo or null if not found
     */
    public static ParticleEmitterInfo getParticle(String particleName) {
        return PARTICLES.get(particleName);
    }

    /**
     * Server-side helper to send a particle to a specific player
     * 
     * @param player       The player to send the particle to
     * @param particleName The name of the particle to spawn
     * @param x            X coordinate
     * @param y            Y coordinate
     * @param z            Z coordinate
     * @param scaleX       X scale factor
     * @param scaleY       Y scale factor
     * @param scaleZ       Z scale factor
     * @return true if packet was sent successfully
     */
    public static boolean sendParticleToPlayer(Player player, String particleName,
            double x, double y, double z,
            float scaleX, float scaleY, float scaleZ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            LOGGER.warn("Cannot send particle to non-server player: {}", player);
            return false;
        }

        if (!hasParticle(particleName)) {
            LOGGER.warn("Cannot send unknown particle '{}' to player {}", particleName, player.getName().getString());
            return false;
        }

        try {
            ParticleEffectPacket packet = new ParticleEffectPacket(x, y, z, particleName, scaleX, scaleY, scaleZ);
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
            LOGGER.info("SERVER: Sent particle '{}' to player {} at ({}, {}, {}) with scale ({}, {}, {})",
                    particleName, player.getName().getString(), x, y, z, scaleX, scaleY, scaleZ);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to send particle '{}' to player {}: {}", particleName, player.getName().getString(),
                    e.getMessage());
            return false;
        }
    }

    /**
     * Server-side helper to send a particle to a specific player with uniform scale
     * 
     * @param player       The player to send the particle to
     * @param particleName The name of the particle to spawn
     * @param x            X coordinate
     * @param y            Y coordinate
     * @param z            Z coordinate
     * @param scale        Uniform scale factor
     * @return true if packet was sent successfully
     */
    public static boolean sendParticleToPlayer(Player player, String particleName,
            double x, double y, double z, float scale) {
        return sendParticleToPlayer(player, particleName, x, y, z, scale, scale, scale);
    }

    /**
     * Server-side helper to send a particle to a specific player with default scale
     * 
     * @param player       The player to send the particle to
     * @param particleName The name of the particle to spawn
     * @param x            X coordinate
     * @param y            Y coordinate
     * @param z            Z coordinate
     * @return true if packet was sent successfully
     */
    public static boolean sendParticleToPlayer(Player player, String particleName,
            double x, double y, double z) {
        return sendParticleToPlayer(player, particleName, x, y, z, 1.0f, 1.0f, 1.0f);
    }
}