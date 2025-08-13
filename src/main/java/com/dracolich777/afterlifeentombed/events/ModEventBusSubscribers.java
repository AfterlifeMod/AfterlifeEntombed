package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod; // Your main mod class
import com.dracolich777.afterlifeentombed.init.ModEntityTypes; // Where your entity type is registered
import com.dracolich777.afterlifeentombed.mobs.GodseekerEntity; // Your entity class
import com.dracolich777.afterlifeentombed.mobs.ShabtiEntity; // Shabti entity class

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusSubscribers {

    @SubscribeEvent
    public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
        // This line hooks up your GodseekerEntity's attributes
        event.put(ModEntityTypes.GODSEEKER.get(), GodseekerEntity.createAttributes().build());
        // This line hooks up your ShabtiEntity's attributes
        event.put(ModEntityTypes.SHABTI.get(), ShabtiEntity.createAttributes().build());
    }
}
