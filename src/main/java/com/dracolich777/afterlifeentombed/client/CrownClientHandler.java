package com.dracolich777.afterlifeentombed.client;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.init.ModItems;
import com.dracolich777.afterlifeentombed.items.CrownOfSeth;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CrownClientHandler {
    
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        // Get the client player (the one viewing the world)
        Player clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null) return;
        
        // Check if the player being rendered has the crown
        boolean hasCrown = CuriosApi.getCuriosHelper()
                .findEquippedCurio(ModItems.CROWN_OF_SETH.get(), player)
                .isPresent();
        
        if (hasCrown) {
            // If the player being rendered is the client player themselves
            if (player.getUUID().equals(clientPlayer.getUUID())) {
                // Make them translucent so they can see themselves but know they're invisible
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                // You can adjust this alpha value (0.3f = 30% opacity)
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.3f);
            } else {
                // For other players viewing someone with the crown, cancel rendering completely
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        Player clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null) return;
        
        // Check if the player being rendered has the crown
        boolean hasCrown = CuriosApi.getCuriosHelper()
                .findEquippedCurio(ModItems.CROWN_OF_SETH.get(), player)
                .isPresent();
        
        if (hasCrown && player.getUUID().equals(clientPlayer.getUUID())) {
            // Restore normal rendering state after rendering the translucent player
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
    }
}