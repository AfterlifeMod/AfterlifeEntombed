package com.dracolich777.afterlibs.api;

import com.dracolich777.afterlibs.network.NetworkHandler;
import com.dracolich777.afterlibs.particle.ParticleManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Set;

/**
 * Public API for AfterLibs particle system.
 * This class provides a clean interface for other mods to spawn particles.
 */
public class AfterLibsAPI {
    
    /**
     * Spawn an AfterLife particle with the specified parameters.
     * This method handles both client-side and server-side spawning automatically.
     * 
     * @param level The level/world to spawn the particle in
     * @param particleName The name of the particle to spawn (e.g., "horus_shield")
     * @param x X coordinate
     * @param y Y coordinate  
     * @param z Z coordinate
     * @param scale Uniform scale factor (1.0 = normal size, 2.0 = double size, etc.)
     * @return true if the particle was spawned successfully, false otherwise
     */
    public static boolean spawnAfterlifeParticle(Level level, String particleName, 
                                                double x, double y, double z, float scale) {
        if (level == null) {
            return false;
        }
        
        if (level.isClientSide()) {
            // Client-side: spawn directly
            return ParticleManager.spawnParticle(level, particleName, x, y, z, scale);
        } else {
            // Server-side: send to all players in dimension
            if (level instanceof ServerLevel serverLevel) {
                NetworkHandler.sendParticleToAll(serverLevel, particleName, x, y, z, scale);
                return true;
            }
            return false;
        }
    }
    
    /**
     * Spawn an AfterLife particle with custom scale per axis.
     * 
     * @param level The level/world to spawn the particle in
     * @param particleName The name of the particle to spawn (e.g., "horus_shield")
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param scaleX X-axis scale factor
     * @param scaleY Y-axis scale factor
     * @param scaleZ Z-axis scale factor
     * @return true if the particle was spawned successfully, false otherwise
     */
    public static boolean spawnAfterlifeParticle(Level level, String particleName, 
                                                double x, double y, double z, 
                                                float scaleX, float scaleY, float scaleZ) {
        if (level == null) {
            return false;
        }
        
        if (level.isClientSide()) {
            // Client-side: spawn directly
            return ParticleManager.spawnParticle(level, particleName, x, y, z, scaleX, scaleY, scaleZ, 0.0f, 0.0f, 0.0f);
        } else {
            // Server-side: send to all players in dimension
            if (level instanceof ServerLevel serverLevel) {
                NetworkHandler.sendParticleToAll(serverLevel, particleName, x, y, z, scaleX, scaleY, scaleZ);
                return true;
            }
            return false;
        }
    }
    
    /**
     * Spawn an AfterLife particle for a specific player only.
     * This method only works on the server side.
     * 
     * @param player The player to show the particle to
     * @param particleName The name of the particle to spawn
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param scale Uniform scale factor
     * @return true if the particle was sent successfully, false otherwise
     */
    public static boolean spawnAfterlifeParticleForPlayer(ServerPlayer player, String particleName,
                                                         double x, double y, double z, float scale) {
        if (player == null) {
            return false;
        }
        
        NetworkHandler.sendParticleToPlayer(player, particleName, x, y, z, scale);
        return true;
    }
    
    /**
     * Spawn an AfterLife particle for players near a specific location.
     * This method only works on the server side.
     * 
     * @param level The server level
     * @param particleName The name of the particle to spawn
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param scale Uniform scale factor
     * @param range Range in blocks to show the particle to players
     * @return true if the particle was sent successfully, false otherwise
     */
    public static boolean spawnAfterlifeParticleNearby(ServerLevel level, String particleName,
                                                      double x, double y, double z, float scale, double range) {
        if (level == null) {
            return false;
        }
        
        NetworkHandler.sendParticleToNearby(level, particleName, x, y, z, scale, range);
        return true;
    }
    
    /**
     * Check if a particle with the given name is available.
     * 
     * @param particleName The name of the particle to check
     * @return true if the particle exists and can be spawned
     */
    public static boolean isParticleAvailable(String particleName) {
        return ParticleManager.hasParticle(particleName);
    }
    
    /**
     * Get a list of all available particle names.
     * 
     * @return Set of available particle names
     */
    public static Set<String> getAvailableParticles() {
        return ParticleManager.getAvailableParticles();
    }
    
    /**
     * Get the total number of available particles.
     * 
     * @return Number of registered particles
     */
    public static int getParticleCount() {
        return getAvailableParticles().size();
    }
}