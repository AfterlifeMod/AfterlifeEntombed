package com.dracolich777.afterlibs.events;

import com.dracolich777.afterlibs.commands.AParticleCommand;
import com.dracolich777.afterlibs.util.VerboseLogger;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler for registering commands
 */
@Mod.EventBusSubscriber(modid = "afterlifeentombed")
public class CommandEvents {
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        VerboseLogger.info(CommandEvents.class, "Registering AfterLibs commands...");
        
        AParticleCommand.register(event.getDispatcher());
        
        VerboseLogger.info(CommandEvents.class, "Successfully registered /aparticle command");
    }
}
