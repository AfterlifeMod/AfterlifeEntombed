package com.dracolich777.afterlifeentombed.client;

import com.dracolich777.afterlifeentombed.init.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HolyFireOverlay {

    private static final ResourceLocation HOLY_FIRE_OVERLAY_TEXTURE =
        new ResourceLocation("afterlifeentombed", "textures/gui/fire_1.png");

    // Render our custom holy fire overlay with highest priority to override vanilla fire
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.hasEffect(ModEffects.HOLY_FIRE.get())) {
            // Only render in first person view
            if (minecraft.options.getCameraType().isFirstPerson()) /*&& 
                (minecraft.player.isOnFire() || minecraft.player.displayFireAnimation()))*/ {
                // Render our custom holy fire overlay
                renderHolyFireScreenOverlay(event.getGuiGraphics(),
                                            minecraft.getWindow().getGuiScaledWidth(),
                                            minecraft.getWindow().getGuiScaledHeight());
            }
        }
    }

    private static void renderHolyFireScreenOverlay(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, HOLY_FIRE_OVERLAY_TEXTURE);

        // Animation logic - cycle through frames
        long gameTime = Minecraft.getInstance().level.getGameTime();
        int frameCount = 32;
        int currentFrame = (int) ((gameTime / 1) % frameCount);

        // Fire overlay positioning - covers full screen like vanilla fire
        int fireHeight = screenHeight;
        int fireY = 0;

        // Calculate texture coordinates for current frame (normalized 0.0 to 1.0)
        float minV = (float) currentFrame / frameCount;
        float maxV = (float) (currentFrame + 1) / frameCount;

        // Custom quad rendering to match vanilla fire overlay appearance
        Matrix4f matrix = guiGraphics.pose().last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        // Bottom left
        bufferBuilder.vertex(matrix, 0, screenHeight, 0).uv(0.0f, maxV).endVertex();
        // Bottom right  
        bufferBuilder.vertex(matrix, screenWidth, screenHeight, 0).uv(1.0f, maxV).endVertex();
        // Top right
        bufferBuilder.vertex(matrix, screenWidth, fireY, 0).uv(1.0f, minV).endVertex();
        // Top left
        bufferBuilder.vertex(matrix, 0, fireY, 0).uv(0.0f, minV).endVertex();

        BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
        BufferUploader.drawWithShader(renderedBuffer);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}