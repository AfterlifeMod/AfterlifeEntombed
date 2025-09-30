package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.commands.ParticleCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommandEvents {
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ParticleCommand.register(event.getDispatcher());
    }
}