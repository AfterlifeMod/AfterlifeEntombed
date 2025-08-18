
package com.dracolich777.afterlifeentombed.client; // Adjust package as necessary

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
// import com.dracolich777.afterlifeentombed.client.particles.RitualCircleParticle;
import com.dracolich777.afterlifeentombed.client.renderer.GodseekerRenderer;
import com.dracolich777.afterlifeentombed.client.renderer.HorusArrowRenderer;
import com.dracolich777.afterlifeentombed.client.renderer.ShabtiEntityRenderer;
import com.dracolich777.afterlifeentombed.init.ModEntityTypes;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntityTypes.GODSEEKER.get(), GodseekerRenderer::new);
            // MenuScreens.register(ModContainers.GODSEEKER_SWORD_CONTAINER.get(), GodseekerSwordScreen::new);
            EntityRenderers.register(ModEntityTypes.SHABTI.get(), ShabtiEntityRenderer::new);
            EntityRenderers.register(ModEntityTypes.HORUS_ARROW.get(), HorusArrowRenderer::new);
            

        });
    }
    
    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        // event.registerSpriteSet(ModParticles.RITUAL_CIRCLE.get(), RitualCircleParticle.Provider::new);
}


}