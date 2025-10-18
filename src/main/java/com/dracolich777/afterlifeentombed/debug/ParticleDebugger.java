package com.dracolich777.afterlifeentombed.debug;

import com.dracolich777.afterlibs.particle.ParticleManager;

/**
 * Simple debug tool to test particle discovery
 */
public class ParticleDebugger {
    
    public static void main(String[] args) {
        System.out.println("=== AfterLibs Particle Discovery Test ===");
        
        // Test if we can find our particle assets
        String[] testParticles = {
            "horus_shield",
            "seth_crown_disolve", 
            "haze_flash"
        };
        
        for (String particleName : testParticles) {
            boolean exists = ParticleManager.hasParticle(particleName);
            System.out.println("Particle '" + particleName + "': " + (exists ? "FOUND" : "NOT FOUND"));
        }
        
        System.out.println("\nAvailable particles: " + ParticleManager.getAvailableParticles());
        
        // Test resource loading
        String resourcePath = "/assets/afterlifeentombed/effeks/horus_shield.efkefc";
        var resource = ParticleDebugger.class.getResourceAsStream(resourcePath);
        System.out.println("\nResource test for '" + resourcePath + "': " + (resource != null ? "FOUND" : "NOT FOUND"));
    }
}