package com.dracolich777.afterlifeentombed;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dracolich777.afterlibs.AfterLibs;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
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
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.util.BlockEffectEvents;
import com.dracolich777.afterlifeentombed.util.GodstoneBrewingRecipe;
import com.dracolich777.afterlifeentombed.util.ModPotionTypes;
import com.dracolich777.afterlifeentombed.util.ModPotions;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AfterlifeEntombedMod.MOD_ID)
public class AfterlifeEntombedMod {

    public static final String MOD_ID = "afterlifeentombed";
    public static final Logger LOGGER = LogManager.getLogger();

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
        modEventBus.addListener(this::registerCapabilities);

        modEventBus.register(ModEventBusSubscribers.class);
        GodseekerSwordCapability.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(ClientControlHandler.class);
        // MinecraftForge.EVENT_BUS.register(MobDropHandler.class);
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(GodAvatarCapability.IGodAvatar.class);
    }

    @SubscribeEvent // Add this annotation
    public void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Initialize AfterLibs components
            AfterLibs.commonSetup();
            
            // Register network packets for God Avatar system
            GodAvatarPackets.register();
            
            // ModEvents.init();
            ModPotionTypes.registerPotionVariants();
            event.enqueueWork(GodstoneBrewingRecipe::registerAll);
            LOGGER.info("ModEvents maps initialized.");
            
            LOGGER.info("Afterlife Entombed mod successfully loaded!");
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
