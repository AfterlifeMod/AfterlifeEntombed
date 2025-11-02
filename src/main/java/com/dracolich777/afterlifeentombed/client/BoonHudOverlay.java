package com.dracolich777.afterlifeentombed.client;

import com.dracolich777.afterlifeentombed.boons.ActiveBoon;
import com.dracolich777.afterlifeentombed.capabilities.PlayerBoonsCapability;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * HUD overlay that renders active boon and curse icons above the hunger bar
 */
@Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class BoonHudOverlay {
    
    // Icons for blessed and cursed status
    private static final ResourceLocation BLESSED_ICON = new ResourceLocation("afterlifeentombed", "textures/gui/boon.png");
    private static final ResourceLocation CURSED_ICON = new ResourceLocation("afterlifeentombed", "textures/gui/curse.png");
    
    private static final int ICON_SIZE = 9; // Small icon size
    private static final int ICON_SPACING = 10; // Space between icons
    
    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        // Render after the food bar
        if (event.getOverlay() != VanillaGuiOverlay.FOOD_LEVEL.type()) {
            return;
        }
        
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        
        if (player == null) return;
        
        // Don't render in creative or spectator
        if (minecraft.gameMode.getPlayerMode() == net.minecraft.world.level.GameType.CREATIVE ||
            minecraft.gameMode.getPlayerMode() == net.minecraft.world.level.GameType.SPECTATOR) {
            return;
        }
        
        // Get active boons and curses
        player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(cap -> {
            List<ActiveBoon> blessings = cap.getBlessings();
            List<ActiveBoon> curses = cap.getCurses();
            
            if (blessings.isEmpty() && curses.isEmpty()) return;
            
            GuiGraphics guiGraphics = event.getGuiGraphics();
            
            // Get screen dimensions
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();
            
            // Position above hunger bar (same X as hunger bar)
            int startX = screenWidth / 2 + 10; // Right side (hunger bar position)
            int startY = screenHeight - 49; // Just above hunger bar
            
            // Render icons
            renderBoonIcons(guiGraphics, startX, startY, blessings, curses);
        });
    }
    
    private static void renderBoonIcons(GuiGraphics guiGraphics, int startX, int startY, 
                                       List<ActiveBoon> blessings, List<ActiveBoon> curses) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        int currentX = startX;
        
        // Render blessing icons (golden)
        for (ActiveBoon blessing : blessings) {
            guiGraphics.blit(BLESSED_ICON, currentX, startY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            currentX += ICON_SPACING;
        }
        
        // Render curse icons (red)
        for (ActiveBoon curse : curses) {
            guiGraphics.blit(CURSED_ICON, currentX, startY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            currentX += ICON_SPACING;
        }
        
        RenderSystem.disableBlend();
    }
}
