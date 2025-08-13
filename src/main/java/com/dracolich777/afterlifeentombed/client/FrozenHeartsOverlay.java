package com.dracolich777.afterlifeentombed.client;

import com.dracolich777.afterlifeentombed.effects.RevengeOfAnubisEffect;
import com.dracolich777.afterlifeentombed.init.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class FrozenHeartsOverlay {
    
    // Create your custom frozen heart texture - place this in src/main/resources/assets/afterlifeentombed/textures/gui/
    private static final ResourceLocation FROZEN_HEART_TEXTURE = new ResourceLocation("afterlifeentombed", "textures/gui/frozen_heart_texture.png");
    
    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type()) {
            return;
        }
        
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        
        if (player == null) return;
        
        // Only render if health bar should be visible
        if (!shouldRenderHealthBar(minecraft, player)) {
            return;
        }
        
        // Check if player has the Revenge of Anubis effect
        MobEffectInstance effect = player.getEffect(ModEffects.REVENGE_OF_ANUBIS.get());
        if (effect == null) return;
        
        // Calculate frozen hearts
        int frozenHearts = RevengeOfAnubisEffect.getFrozenHearts(effect.getAmplifier());
        
        // Only render if there are actually frozen hearts
        if (frozenHearts <= 0) return;
        
        GuiGraphics guiGraphics = event.getGuiGraphics();
        
        int totalHearts = (int) Math.ceil(player.getMaxHealth() / 2.0f);
        
        // Get screen dimensions
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        // Calculate health bar position (same as vanilla)
        int healthBarX = screenWidth / 2 - 91; // Standard health bar X position
        int healthBarY = screenHeight - 39; // Standard health bar Y position
        
        // Adjust for hardcore mode (skull icon)
        if (minecraft.gameMode.canHurtPlayer()) {
            healthBarY -= 0;
        }
        
        // Render frozen heart overlays
        renderFrozenHearts(guiGraphics, healthBarX, healthBarY, totalHearts, frozenHearts);
    }
    
    private static boolean shouldRenderHealthBar(Minecraft minecraft, LocalPlayer player) {
        // Check if player is in a gamemode that shows health bar
        if (minecraft.gameMode.getPlayerMode() == net.minecraft.world.level.GameType.CREATIVE) {
            return false; // Creative mode doesn't show health bar
        }
        
        if (minecraft.gameMode.getPlayerMode() == net.minecraft.world.level.GameType.SPECTATOR) {
            return false; // Spectator mode doesn't show health bar
        }
        
        // Check if health bar is disabled by other mods or settings
        if (minecraft.options.hideGui) {
            return false; // GUI is hidden (F1 mode)
        }
        
        // Check if player has health to display
        if (player.getMaxHealth() <= 0) {
            return false;
        }
        
        // Additional check: only show if player can actually take damage
        return minecraft.gameMode.canHurtPlayer();
    }
    
    private static void renderFrozenHearts(GuiGraphics guiGraphics, int startX, int startY, int totalHearts, int frozenHearts) {
        // Enable blending for transparent textures
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        // Calculate which hearts should be frozen (from the right side)
        int firstFrozenHeart = totalHearts - frozenHearts;
        
        for (int i = 0; i < totalHearts; i++) {
            if (i >= firstFrozenHeart) {
                // This heart should be frozen
                int heartRow = i / 10; // Hearts wrap to new row after 10
                int heartCol = i % 10;
                
                // Calculate heart position
                int heartX = startX + (heartCol * 8); // Hearts are 8 pixels apart
                int heartY = startY - (heartRow * 10); // New rows are 10 pixels up
                
                // Render the frozen heart overlay
                guiGraphics.blit(FROZEN_HEART_TEXTURE, heartX, heartY, 0, 0, 9, 9, 9, 9);
            }
        }
        
        RenderSystem.disableBlend();
    }
}