package com.dracolich777.afterlibs.api.examples;

import com.dracolich777.afterlibs.api.AfterLibsAPI;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Example usage of the AfterLibs API for spawning particles.
 * This class demonstrates how other mods can integrate with AfterLibs.
 */
public class AfterLibsAPIExamples {
    
    /**
     * Example: Simple particle spawning
     * 
     * This is the most basic way to spawn a particle - equivalent to the /aparticle command
     */
    public static void basicParticleExample(Level level, double x, double y, double z) {
        // Spawn a horus_shield particle at the specified coordinates with scale 2.0
        // This works on both client and server side automatically
        boolean success = AfterLibsAPI.spawnAfterlifeParticle(level, "horus_shield", x, y, z, 2.0f);
        
        if (success) {
            System.out.println("Successfully spawned horus_shield particle!");
        } else {
            System.out.println("Failed to spawn particle - check if particle name is valid");
        }
    }
    
    /**
     * Example: Custom scaling per axis
     * 
     * You can scale particles differently on each axis for unique effects
     */
    public static void customScaleExample(Level level, double x, double y, double z) {
        // Spawn a particle that's stretched tall and thin
        AfterLibsAPI.spawnAfterlifeParticle(level, "seth_crown_appear", x, y, z, 
            0.5f,  // scaleX - half width
            3.0f,  // scaleY - triple height
            0.5f   // scaleZ - half depth
        );
    }
    
    /**
     * Example: Player-specific particles (server-side only)
     * 
     * Sometimes you want to show a particle to only one player
     */
    public static void playerSpecificExample(ServerPlayer player, double x, double y, double z) {
        // Only this player will see the particle
        AfterLibsAPI.spawnAfterlifeParticleForPlayer(player, "haze_flash", x, y, z, 1.5f);
    }
    
    /**
     * Example: Area-based particles (server-side only)
     * 
     * Show particles to players within a certain range
     */
    public static void areaBasedExample(ServerLevel level, double x, double y, double z) {
        // Show particle to all players within 50 blocks
        AfterLibsAPI.spawnAfterlifeParticleNearby(level, "shield_wall", x, y, z, 1.0f, 50.0);
    }
    
    /**
     * Example: Checking available particles
     * 
     * You can validate particle names before trying to spawn them
     */
    public static void particleValidationExample() {
        // Check if a specific particle is available
        if (AfterLibsAPI.isParticleAvailable("horus_shield")) {
            System.out.println("Horus shield particle is available!");
        }
        
        // Get all available particles
        System.out.println("Available particles: " + AfterLibsAPI.getAvailableParticles());
        
        // Get particle count
        System.out.println("Total particles: " + AfterLibsAPI.getParticleCount());
    }
    
    /**
     * Example: Advanced particle effects
     * 
     * Create complex particle effects by spawning multiple particles
     */
    public static void advancedEffectExample(Level level, double centerX, double centerY, double centerZ) {
        // Create a circular pattern of particles
        for (int i = 0; i < 8; i++) {
            double angle = (i * Math.PI * 2) / 8; // 8 particles in a circle
            double x = centerX + Math.cos(angle) * 2; // 2 block radius
            double z = centerZ + Math.sin(angle) * 2;
            
            // Spawn particles with slight delay effect by varying Y
            AfterLibsAPI.spawnAfterlifeParticle(level, "haze", x, centerY + (i * 0.1), z, 0.8f);
        }
        
        // Add a central particle with different scale
        AfterLibsAPI.spawnAfterlifeParticle(level, "horus_shield", centerX, centerY, centerZ, 1.5f);
    }
    
    /**
     * Example: Event-driven particle spawning
     * 
     * How to integrate particles with game events
     */
    public static void eventDrivenExample(Level level, double x, double y, double z, String eventType) {
        switch (eventType) {
            case "player_death":
                AfterLibsAPI.spawnAfterlifeParticle(level, "seth_crown_disolve", x, y, z, 2.0f);
                break;
            case "player_respawn":
                AfterLibsAPI.spawnAfterlifeParticle(level, "seth_crown_appear", x, y, z, 1.5f);
                break;
            case "shield_activate":
                AfterLibsAPI.spawnAfterlifeParticle(level, "shield_wall", x, y, z, 1.0f);
                break;
            case "magic_cast":
                // Create a multi-particle effect
                AfterLibsAPI.spawnAfterlifeParticle(level, "haze_flash", x, y, z, 1.0f);
                AfterLibsAPI.spawnAfterlifeParticle(level, "haze2", x, y + 1, z, 0.7f);
                break;
            default:
                // Default particle for unknown events
                AfterLibsAPI.spawnAfterlifeParticle(level, "haze", x, y, z, 1.0f);
        }
    }
}