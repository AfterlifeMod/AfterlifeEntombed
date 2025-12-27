package com.dracolich777.afterlifeentombed.client;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientInit {

    public static void registerClient(IEventBus modEventBus) {
        // Register client-only event handlers
        MinecraftForge.EVENT_BUS.register(ClientControlHandler.class);

        // Add client setup listener if needed
        modEventBus.addListener(ClientInit::clientSetup);

        AfterlifeEntombedMod.LOGGER.info("Client-side initialization complete");
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        // Any additional client setup code goes here
    }
}
