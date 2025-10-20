package com.dracolich777.afterlifeentombed.client;

import java.util.Set;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.client.gui.GodSelectionScreen;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side handler for opening the god selection GUI
 */
@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class GodSelectionKeyHandler {
    
    private static KeyMapping openGodSelectionKey;
    private static boolean wasKeyDown = false;
    
    @Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            openGodSelectionKey = new KeyMapping(
                "key.afterlifeentombed.open_god_selection",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_G, // Default to 'G' key
                "key.categories.afterlifeentombed"
            );
            event.register(openGodSelectionKey);
        }
    }
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        // Check if key exists
        if (openGodSelectionKey == null) return;
        
        boolean isKeyDown = openGodSelectionKey.isDown();
        
        // Track if our screen is actually open
        boolean isOurScreenOpen = mc.screen instanceof GodSelectionScreen;
        
        // Key just pressed - toggle the GUI
        if (isKeyDown && !wasKeyDown) {
            if (isOurScreenOpen) {
                // Already open - close it
                AfterlifeEntombedMod.LOGGER.info("God Selection Key pressed - closing open screen");
                mc.setScreen(null);
            } else if (mc.screen == null) {
                // Not open - open it
                AfterlifeEntombedMod.LOGGER.info("God Selection Key pressed - attempting to open");
                
                // Get unlocked gods from capability
                mc.player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                    Set<GodType> unlockedGods = cap.getUnlockedGods();
                    GodType currentGod = cap.getSelectedGod();
                    
                    // Always open the screen, even if no gods are unlocked
                    AfterlifeEntombedMod.LOGGER.info("Opening god selection screen with {} unlocked gods", unlockedGods.size());
                    mc.setScreen(new GodSelectionScreen(unlockedGods, currentGod));
                });
            }
        }
        
        wasKeyDown = isKeyDown;
    }
}
