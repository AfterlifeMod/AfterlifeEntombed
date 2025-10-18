package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.commands.AvatarCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles registration of custom commands
 */
@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandRegistrationHandler {
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        AvatarCommand.register(event.getDispatcher());
        AfterlifeEntombedMod.LOGGER.info("Registered /avatar command");
    }
}
