package com.dracolich777.afterlifeentombed.client.gui;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.menu.GodforgeMenu;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class GodforgeScreen extends AbstractContainerScreen<GodforgeMenu> {
    private static final ResourceLocation TEXTURE = 
            new ResourceLocation(AfterlifeEntombedMod.MOD_ID, "textures/gui/godforge.png");

    public GodforgeScreen(GodforgeMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageHeight = 166; // Same height as brewing stand
        this.imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // Draw the background texture
        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        
        // Draw the progress bar if crafting
        if (menu.isCrafting()) {
            int progress = menu.getProgress();
            int maxProgress = menu.getMaxProgress();
            
            if (maxProgress > 0) {
                float progressPercent = (float) progress / (float) maxProgress;
                
                // Horizontal progress bar from x=15 to x=65 (50 pixels wide), at y=65, height=2
                // Calculate how many pixels to fill based on progress
                int fillWidth = Math.min((int) (progressPercent * 50), 50);
                
                // Draw the progress bar fill overlay from texture at (176, 0)
                // Each horizontal pixel filled is a 2-pixel tall vertical stack
                if (fillWidth > 0) {
                    pGuiGraphics.blit(TEXTURE, x + 15, y + 65, 176, 0, fillWidth, 2);
                }
            }
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
