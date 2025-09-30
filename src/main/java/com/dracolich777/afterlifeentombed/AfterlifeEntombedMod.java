package com.dracolich777.afterlifeentombed;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dracolich777.afterlifeentombed.capabilities.GodseekerSwordCapability;
import com.dracolich777.afterlifeentombed.client.ClientControlHandler;
import com.dracolich777.afterlifeentombed.events.ModEventBusSubscribers;
import com.dracolich777.afterlifeentombed.events.PortalActivationHandler;
import com.dracolich777.afterlifeentombed.init.ModBlockEntities;
import com.dracolich777.afterlifeentombed.init.ModBlocks;
import com.dracolich777.afterlifeentombed.init.ModEffects;
import com.dracolich777.afterlifeentombed.init.ModEntityTypes;
import com.dracolich777.afterlifeentombed.init.ModItems;
import com.dracolich777.afterlifeentombed.init.ModMenuTypes;
import com.dracolich777.afterlifeentombed.init.ModRecipeTypes;
import com.dracolich777.afterlifeentombed.network.NetworkHandler;
import com.dracolich777.afterlifeentombed.util.BlockEffectEvents;
import com.dracolich777.afterlifeentombed.util.GodstoneBrewingRecipe;
import com.dracolich777.afterlifeentombed.util.ModPotionTypes;
import com.dracolich777.afterlifeentombed.util.ModPotions;

import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.resources.ResourceLocation; // Make sure this is imported
import net.minecraftforge.api.distmarker.Dist; // Make sure this is imported
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AfterlifeEntombedMod.MOD_ID)
public class AfterlifeEntombedMod {

    public static final String MOD_ID = "afterlifeentombed";
    public static final Logger LOGGER = LogManager.getLogger();
    
    // Public static particle emitter for SETH_DISSOLVE
    public static ParticleEmitterInfo SETH_DISSOLVE = null;
    
    // Public static particle emitter for SETH_APPEAR
    public static ParticleEmitterInfo SETH_APPEAR = null;
    
    // Public static particle emitter for RA_RING
    public static ParticleEmitterInfo RA_RING = null;

    public AfterlifeEntombedMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEffects.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        // ModContainers.register(modEventBus);
        ModPotions.POTIONS.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipeTypes.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(BlockEffectEvents.class);
        MinecraftForge.EVENT_BUS.register(PortalActivationHandler.class);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);  // Register client setup listener

        modEventBus.register(ModEventBusSubscribers.class);
        GodseekerSwordCapability.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(ClientControlHandler.class);
        // MinecraftForge.EVENT_BUS.register(MobDropHandler.class);
    }

    @SubscribeEvent // Add this annotation
    public void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Initialize network handler
            NetworkHandler.register();
            LOGGER.info("Network handler initialized.");
            
            // ModEvents.init();
            ModPotionTypes.registerPotionVariants();
            event.enqueueWork(GodstoneBrewingRecipe::registerAll);
            LOGGER.info("ModEvents maps initialized.");
            
            // Force initialization of ModParticles class
            try {
                Class.forName("com.dracolich777.afterlifeentombed.init.ModParticles");
                LOGGER.info("ModParticles class loaded successfully");
            } catch (ClassNotFoundException e) {
                LOGGER.error("Failed to load ModParticles class: {}", e.getMessage());
            }
            
            // Force initialization of ParticleManager class to trigger static block
            try {
                Class.forName("com.dracolich777.afterlifeentombed.util.ParticleManager");
                LOGGER.info("ParticleManager class loaded successfully");
                // Test if particles are registered
                LOGGER.info("Available particles: {}", com.dracolich777.afterlifeentombed.util.ParticleManager.getAvailableParticles());
            } catch (ClassNotFoundException e) {
                LOGGER.error("Failed to load ParticleManager class: {}", e.getMessage());
            }
            
            LOGGER.info("Afterlife Entombed mod successfully loaded with AAA particles support!");
        });
    }

    @SubscribeEvent // Add this annotation
    @OnlyIn(Dist.CLIENT) // Ensures this method is only loaded/run on the client side
    public void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Initialize AAA particles directly here
            try {
                // Create the SETH_DISSOLVE particle info using the .efkefc file
                ResourceLocation sethParticleLocation = new ResourceLocation(MOD_ID, "seth_crown_disolve");
                SETH_DISSOLVE = new ParticleEmitterInfo(sethParticleLocation);
                LOGGER.info("Successfully created SETH_DISSOLVE particle: {} with ResourceLocation: {}", SETH_DISSOLVE, sethParticleLocation);
                LOGGER.info("Expected particle file path: assets/{}/effeks/{}.efkefc", sethParticleLocation.getNamespace(), sethParticleLocation.getPath());
                
                // Create the SETH_APPEAR particle info using the .efkefc file
                ResourceLocation sethAppearParticleLocation = new ResourceLocation(MOD_ID, "seth_crown_appear");
                SETH_APPEAR = new ParticleEmitterInfo(sethAppearParticleLocation);
                LOGGER.info("Successfully created SETH_APPEAR particle: {} with ResourceLocation: {}", SETH_APPEAR, sethAppearParticleLocation);
                LOGGER.info("Expected particle file path: assets/{}/effeks/{}.efkefc", sethAppearParticleLocation.getNamespace(), sethAppearParticleLocation.getPath());
                
                // Create the RA_RING particle info using the .efkefc file
                ResourceLocation raParticleLocation = new ResourceLocation(MOD_ID, "ra_ring");
                RA_RING = new ParticleEmitterInfo(raParticleLocation);
                LOGGER.info("Successfully created RA_RING particle: {} with ResourceLocation: {}", RA_RING, raParticleLocation);
                LOGGER.info("Expected particle file path: assets/{}/effeks/{}.efkefc", raParticleLocation.getNamespace(), raParticleLocation.getPath());
                
                // Force ModParticles class loading
                Class.forName("com.dracolich777.afterlifeentombed.init.ModParticles");
                LOGGER.info("ModParticles class initialized in client setup");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize particles in client setup: {}", e.getMessage());
                e.printStackTrace();
            }
        });
    }
}

/*
To Do:
Register recipes and remove unused ones -done
Register loot tables -think im done
Make godseeker spawn/summon through ritual -done
Fix isis charm picking creative only blocks -done
Implement kev textures
figure out how to turn this into a java file
add prevention for equipping more than one charm
fix godseeker trades to not have missing items, and to require 10 diamonds + some other item related to that god -done
Make horus token take durability, smite you when broken, and recharge using totems -failed, reverting
fix tooltips and polish
FIX THE FUCKING WAND -DONE

FIX REVENGE OF HORUS -DONE
FIX CACTUS ARMOR <----
*/
