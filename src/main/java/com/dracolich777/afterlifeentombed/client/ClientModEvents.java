package com.dracolich777.afterlifeentombed.client;

// import com.dracolich777.afterlifeentombed.client.gui.GodHoldScreen;
import com.dracolich777.afterlifeentombed.client.model.ArmorOfRaModel;
import com.dracolich777.afterlifeentombed.client.model.ModModelLayers;
import com.dracolich777.afterlifeentombed.init.ModMenuTypes;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.ARMOR_OF_RA_HELMET, ArmorOfRaModel::createArmorLayer);
        event.registerLayerDefinition(ModModelLayers.ARMOR_OF_RA_CHESTPLATE, ArmorOfRaModel::createArmorLayer);
        event.registerLayerDefinition(ModModelLayers.ARMOR_OF_RA_LEGGINGS, ArmorOfRaModel::createArmorLayer);
        event.registerLayerDefinition(ModModelLayers.ARMOR_OF_RA_BOOTS, ArmorOfRaModel::createArmorLayer);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // event.registerEntityRenderer(ModEntityTypes.SHABTI.get(), ShabtiEntityRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        // event.enqueueWork(() -> {
        //     MenuScreens.register(ModMenuTypes.GODHOLD_MENU.get(), GodHoldScreen::new);
        // });
    }
}
