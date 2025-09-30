package com.dracolich777.afterlifeentombed.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;

import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;

@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModParticles {
    
    private static final Logger LOGGER = LogManager.getLogger();

    static {
        LOGGER.info("ModParticles class loaded - AAA particles initialized");
    }

    // Seth's Crown dissolve effect - using .efkefc file
    public static final ParticleEmitterInfo SETH_DISSOLVE = new ParticleEmitterInfo(new ResourceLocation("afterlifeentombed", "seth_crown_disolve"));
    
    // Seth's Crown appear effect - using .efkefc file
    public static final ParticleEmitterInfo SETH_APPEAR = new ParticleEmitterInfo(new ResourceLocation("afterlifeentombed", "seth_crown_appear"));
    
    // Ra's Ring following effect - using .efkefc file
    public static final ParticleEmitterInfo RA_RING = new ParticleEmitterInfo(new ResourceLocation("afterlifeentombed", "ra_ring"));

}