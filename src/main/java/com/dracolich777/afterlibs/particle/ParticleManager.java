package com.dracolich777.afterlibs.particle;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.dracolich777.afterlibs.util.VerboseLogger;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Automatic particle manager for AAA Particles integration.
 * Automatically discovers and registers particles from the effeks folder.
 */
public class ParticleManager {
    
    private static final Map<String, ParticleEmitterInfo> PARTICLES = new HashMap<>();
    private static final String EFFEKS_PATH = "/assets/afterlibs/effeks/";
    private static final String EFFEKS_EXTENSION = ".efkefc";
    
    static {
        // Auto-discover and register all particles
        discoverAndRegisterParticles();
        
        VerboseLogger.info(ParticleManager.class, "Auto-registered {} particles from effeks folder", PARTICLES.size());
        if (!PARTICLES.isEmpty()) {
            VerboseLogger.info(ParticleManager.class, "Available particles: {}", PARTICLES.keySet());
        }
    }
    
    /**
     * Automatically discover and register all .efkefc files in the effeks folder
     */
    private static void discoverAndRegisterParticles() {
        boolean autoDiscoverySucceeded = false;
        
        try {
            // Get the effeks folder as a resource
            java.net.URL effeksUrl = ParticleManager.class.getResource(EFFEKS_PATH);
            if (effeksUrl == null) {
                VerboseLogger.warn(ParticleManager.class, "Could not find effeks resource path: {}", EFFEKS_PATH);
            } else {
                URI effeksUri = effeksUrl.toURI();
                Path effeksPath;
                FileSystem fileSystem = null;
                boolean needsClosing = false;
                
                try {
                    if (effeksUri.getScheme().equals("jar")) {
                        // Running from JAR file
                        fileSystem = FileSystems.newFileSystem(effeksUri, Collections.emptyMap());
                        effeksPath = fileSystem.getPath(EFFEKS_PATH);
                        needsClosing = true;
                        VerboseLogger.info(ParticleManager.class, "Discovering particles from JAR: {}", effeksUri);
                    } else {
                        // Running from file system (development)
                        effeksPath = Paths.get(effeksUri);
                        VerboseLogger.info(ParticleManager.class, "Discovering particles from filesystem: {}", effeksPath);
                    }
                    
                    // Scan for .efkefc files
                    try (Stream<Path> files = Files.walk(effeksPath)) {
                        long particleCount = files.filter(Files::isRegularFile)
                                                 .filter(path -> path.toString().endsWith(EFFEKS_EXTENSION))
                                                 .peek(ParticleManager::registerParticleFromPath)
                                                 .count();
                        
                        if (particleCount > 0) {
                            autoDiscoverySucceeded = true;
                            VerboseLogger.info(ParticleManager.class, "Auto-discovery completed. Found {} particles via filesystem scanning", particleCount);
                        }
                    }
                    
                } finally {
                    if (needsClosing && fileSystem != null) {
                        fileSystem.close();
                    }
                }
            }
            
        } catch (Exception e) {
            VerboseLogger.error(ParticleManager.class, "Failed to auto-discover particles: {} - {}", 
                e.getClass().getSimpleName(), e.getMessage());
            VerboseLogger.error(ParticleManager.class, "Exception details: ", e);
        }
        
        // If auto-discovery failed, fall back to manual registration
        if (!autoDiscoverySucceeded) {
            VerboseLogger.info(ParticleManager.class, "Auto-discovery failed, falling back to manual registration of known particles");
            registerFallbackParticles();
        }
    }
    
    /**
     * Register a particle from a discovered file path
     */
    private static void registerParticleFromPath(Path filePath) {
        try {
            // Get the filename without extension
            String fileName = filePath.getFileName().toString();
            if (fileName.endsWith(EFFEKS_EXTENSION)) {
                String particleName = fileName.substring(0, fileName.length() - EFFEKS_EXTENSION.length());
                
                // Register the particle
                ResourceLocation location = new ResourceLocation("afterlibs", particleName);
                ParticleEmitterInfo particle = new ParticleEmitterInfo(location);
                PARTICLES.put(particleName, particle);
                
                VerboseLogger.info(ParticleManager.class, "Auto-registered particle '{}' from file: {}", 
                    particleName, fileName);
            }
        } catch (Exception e) {
            VerboseLogger.error(ParticleManager.class, "Failed to register particle from path {}: {} - {}", 
                filePath, e.getClass().getSimpleName(), e.getMessage());
        }
    }
    
    /**
     * Fallback method to register known particles if auto-discovery fails
     */
    private static void registerFallbackParticles() {
        String[] knownParticles = {
            "horus_shield", "seth_crown_disolve", "seth_crown_appear", 
            "haze", "haze2", "haze3", "haze_flash", "shield_wall", "shu_jump2"
        };
        
        for (String particleName : knownParticles) {
            try {
                // Test if the particle resource exists
                String resourcePath = EFFEKS_PATH + particleName + EFFEKS_EXTENSION;
                InputStream testStream = ParticleManager.class.getResourceAsStream(resourcePath);
                
                if (testStream != null) {
                    testStream.close();
                    
                    // Resource exists, register it
                    ResourceLocation location = new ResourceLocation("afterlibs", particleName);
                    ParticleEmitterInfo particle = new ParticleEmitterInfo(location);
                    PARTICLES.put(particleName, particle);
                    
                    VerboseLogger.info(ParticleManager.class, "Fallback registered particle: {}", particleName);
                } else {
                    VerboseLogger.warn(ParticleManager.class, "Fallback particle not found: {}", particleName);
                }
            } catch (Exception e) {
                VerboseLogger.error(ParticleManager.class, "Failed to fallback register particle '{}': {} - {}", 
                    particleName, e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
    
    /**
     * Spawn a particle in the world with default settings
     * 
     * @param level The level to spawn in (must be client-side)
     * @param particleName The name of the particle to spawn
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return true if particle was spawned successfully
     */
    public static boolean spawnParticle(Level level, String particleName, double x, double y, double z) {
        return spawnParticle(level, particleName, x, y, z, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
    }
    
    /**
     * Spawn a particle in the world with custom scale
     * 
     * @param level The level to spawn in (must be client-side)
     * @param particleName The name of the particle to spawn
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param scale Uniform scale factor
     * @return true if particle was spawned successfully
     */
    public static boolean spawnParticle(Level level, String particleName, double x, double y, double z, float scale) {
        return spawnParticle(level, particleName, x, y, z, scale, scale, scale, 0.0f, 0.0f, 0.0f);
    }
    
    /**
     * Spawn a particle in the world with full customization
     * 
     * @param level The level to spawn in (must be client-side)
     * @param particleName The name of the particle to spawn
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param scaleX X scale factor
     * @param scaleY Y scale factor
     * @param scaleZ Z scale factor
     * @param yaw Yaw rotation in degrees
     * @param pitch Pitch rotation in degrees
     * @param roll Roll rotation in degrees
     * @return true if particle was spawned successfully
     */
    public static boolean spawnParticle(Level level, String particleName, double x, double y, double z,
                                       float scaleX, float scaleY, float scaleZ,
                                       float yaw, float pitch, float roll) {
        
        if (level == null) {
            VerboseLogger.warn(ParticleManager.class, "Cannot spawn particle '{}': level is null", particleName);
            return false;
        }
        
        if (!level.isClientSide()) {
            VerboseLogger.warn(ParticleManager.class, "Cannot spawn particle '{}': must be called on client side", particleName);
            return false;
        }
        
        ParticleEmitterInfo particle = PARTICLES.get(particleName);
        if (particle == null) {
            VerboseLogger.warn(ParticleManager.class, "Unknown particle name: '{}'. Available particles: {}", 
                particleName, getAvailableParticles());
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
                    .scale(scaleX, scaleY, scaleZ)
            );
            
            VerboseLogger.info(ParticleManager.class, "Successfully spawned particle '{}' at ({}, {}, {}) with scale ({}, {}, {})",
                particleName, x, y, z, scaleX, scaleY, scaleZ);
            return true;
            
        } catch (Exception e) {
            VerboseLogger.error(ParticleManager.class, "Failed to spawn particle '{}': {}", particleName, e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a particle exists
     * 
     * @param particleName The name of the particle
     * @return true if the particle is registered
     */
    public static boolean hasParticle(String particleName) {
        return PARTICLES.containsKey(particleName);
    }
    
    /**
     * Get all available particles
     * 
     * @return Set of available particle names
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
     * Get the number of registered particles
     * 
     * @return Number of particles
     */
    public static int getParticleCount() {
        return PARTICLES.size();
    }
}