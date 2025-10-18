package com.dracolich777.afterlifeentombed.util;

import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;

/**
 * Local particle utility for AfterlifeEntombed mod.
 * This directly uses AAA Particles to spawn effects without external dependencies.
 */
public class ParticleUtil {
    
    /**
     * Spawn an afterlife particle effect
     * @param level The level to spawn in
     * @param particleName The name of the particle effect
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param scale Scale of the effect
     * @return true if successful
     */
    public static boolean spawnAfterlifeParticle(Level level, String particleName, 
                                               double x, double y, double z, float scale) {
        if (level == null) {
            return false;
        }
        
        if (!level.isClientSide()) {
            // Server-side: This should be handled by the client receiving the event
            // For now, we'll just return true
            return true;
        }
        
        try {
            // Create the particle emitter info
            ResourceLocation particleLocation = new ResourceLocation("afterlibs", particleName);
            ParticleEmitterInfo particle = new ParticleEmitterInfo(particleLocation);
            
            // Spawn the particle on client side
            AAALevel.addParticle(
                level,
                particle
                    .position(x, y, z)
                    .scale(scale, scale, scale)
            );
            
            return true;
        } catch (Exception e) {
            System.err.println("Failed to spawn particle " + particleName + ": " + e.getMessage());
            return false;
        }
    }
}