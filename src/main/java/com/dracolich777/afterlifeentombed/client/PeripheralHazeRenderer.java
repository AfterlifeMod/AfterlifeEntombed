// package com.dracolich777.afterlifeentombed.client;

// import com.mojang.blaze3d.systems.RenderSystem;
// import com.mojang.blaze3d.vertex.BufferBuilder;
// import com.mojang.blaze3d.vertex.BufferUploader;
// import com.mojang.blaze3d.vertex.DefaultVertexFormat;
// import com.mojang.blaze3d.vertex.PoseStack;
// import com.mojang.blaze3d.vertex.Tesselator;
// import com.mojang.blaze3d.vertex.VertexFormat;
// import org.joml.Matrix4f; // Corrected import for Matrix4f in 1.20.1
// import net.minecraft.client.Minecraft;
// // import net.minecraft.client.gui.GuiComponent; // Removed unused import
// import net.minecraft.client.player.LocalPlayer;
// import net.minecraft.client.renderer.GameRenderer;
// import net.minecraft.util.Mth;
// import net.minecraft.world.effect.MobEffectInstance;
// import net.minecraftforge.api.distmarker.Dist;
// import net.minecraftforge.api.distmarker.OnlyIn;
// import net.minecraftforge.client.event.RenderGuiOverlayEvent;
// import net.minecraftforge.eventbus.api.SubscribeEvent;
// import net.minecraftforge.fml.common.Mod; // Added import for @Mod annotation
// import com.dracolich777.afterlifeentombed.init.ModEffects;
// import com.dracolich777.afterlifeentombed.effects.MirageEffect;
// import net.minecraft.client.gui.GuiGraphics; // Added import for GuiGraphics

// @OnlyIn(Dist.CLIENT)
// @Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
// public class PeripheralHazeRenderer {
    
//     private static float hazeTime = 0;
//     private static final float HAZE_INTENSITY = 0.4f;
//     private static final float HAZE_SPEED = 0.03f;
//     private static final int HAZE_BANDS = 8;
    
//     @SubscribeEvent
//     public static void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {
//         Minecraft minecraft = Minecraft.getInstance();
//         LocalPlayer player = minecraft.player;
        
//         if (player == null) return;
        
//         // Check if player has mirage effect
//         MobEffectInstance mirageEffect = player.getEffect(ModEffects.MIRAGE.get());
//         if (mirageEffect == null) return;
        
//         // Update haze animation time
//         hazeTime += HAZE_SPEED;
//         if (hazeTime > Math.PI * 2) {
//             hazeTime -= Math.PI * 2;
//         }
        
//         // Render peripheral heat haze
//         // Changed to use GuiGraphics from the event
//         renderPeripheralHaze(event.getGuiGraphics(), minecraft.getWindow().getGuiScaledWidth(), 
//                              minecraft.getWindow().getGuiScaledHeight(), mirageEffect.getAmplifier());
//     }
    
//     // Changed parameter from PoseStack to GuiGraphics
//     public static void renderPeripheralHaze(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int amplifier) {
//         // Set up rendering state
//         RenderSystem.setShader(GameRenderer::getPositionColorShader);
//         RenderSystem.enableBlend();
//         RenderSystem.defaultBlendFunc();
//         RenderSystem.disableDepthTest();
        
//         Tesselator tesselator = Tesselator.getInstance();
//         BufferBuilder buffer = tesselator.getBuilder();
//         // Get the Matrix4f from GuiGraphics
//         Matrix4f matrix = guiGraphics.pose().last().pose(); 
        
//         // Calculate haze intensity based on amplifier
//         float intensity = HAZE_INTENSITY * Math.min(1.0f, (amplifier + 1) * 0.3f);
        
//         // Render left side haze
//         renderSideHaze(buffer, matrix, 0, 0, screenWidth * 0.25f, screenHeight, intensity, true);
        
//         // Render right side haze
//         renderSideHaze(buffer, matrix, screenWidth * 0.75f, 0, screenWidth * 0.25f, screenHeight, intensity, false);
        
//         // Render top haze
//         renderTopBottomHaze(buffer, matrix, 0, 0, screenWidth, screenHeight * 0.2f, intensity, true);
        
//         // Render bottom haze
//         renderTopBottomHaze(buffer, matrix, 0, screenHeight * 0.8f, screenWidth, screenHeight * 0.2f, intensity, false);
        
//         // Finish rendering - Corrected BufferUploader usage
//         BufferUploader.draw(buffer.end());
        
//         // Restore rendering state
//         RenderSystem.enableDepthTest();
//         RenderSystem.disableBlend();
//     }
    
//     private static void renderSideHaze(BufferBuilder buffer, Matrix4f matrix, float x, float y, float width, float height, float intensity, boolean leftSide) {
//         buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
//         // Create wavering bands of distortion
//         for (int band = 0; band < HAZE_BANDS; band++) {
//             float bandY = y + (height / HAZE_BANDS) * band;
//             float bandHeight = height / HAZE_BANDS;
            
//             // Calculate wave distortion
//             float wavePhase = hazeTime * 2 + band * 0.5f;
//             float waveOffset = 5 * (float)Math.sin(wavePhase);
            
//             // Different wave patterns for left and right
//             if (!leftSide) {
//                 waveOffset = -waveOffset;
//             }
            
//             // Color with slight tint and varying alpha
//             float alpha = intensity * (0.1f + 0.05f * (float)Math.sin(wavePhase * 1.3f));
//             float r = 1.0f + 0.1f * (float)Math.sin(wavePhase * 0.7f);
//             float g = 0.95f + 0.05f * (float)Math.cos(wavePhase * 0.9f);
//             float b = 0.9f + 0.1f * (float)Math.sin(wavePhase * 1.1f);
            
//             // Gradient from edge to center
//             float innerAlpha = alpha * 0.2f;
//             float outerAlpha = alpha;
            
//             if (leftSide) {
//                 // Left side: fade from left edge to center
//                 buffer.vertex(matrix, x + waveOffset, bandY, 0).color(r, g, b, outerAlpha).endVertex();
//                 buffer.vertex(matrix, x + width, bandY, 0).color(r, g, b, innerAlpha).endVertex();
//                 buffer.vertex(matrix, x + width, bandY + bandHeight, 0).color(r, g, b, innerAlpha).endVertex();
//                 buffer.vertex(matrix, x + waveOffset, bandY + bandHeight, 0).color(r, g, b, outerAlpha).endVertex();
//             } else {
//                 // Right side: fade from center to right edge
//                 buffer.vertex(matrix, x, bandY, 0).color(r, g, b, innerAlpha).endVertex();
//                 buffer.vertex(matrix, x + width + waveOffset, bandY, 0).color(r, g, b, outerAlpha).endVertex();
//                 buffer.vertex(matrix, x + width + waveOffset, bandY + bandHeight, 0).color(r, g, b, outerAlpha).endVertex();
//                 buffer.vertex(matrix, x, bandY + bandHeight, 0).color(r, g, b, innerAlpha).endVertex();
//             }
//         }
//     }
    
//     private static void renderTopBottomHaze(BufferBuilder buffer, Matrix4f matrix, float x, float y, float width, float height, float intensity, boolean top) {
//         buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
//         // Create horizontal bands of distortion
//         for (int band = 0; band < HAZE_BANDS; band++) {
//             float bandX = x + (width / HAZE_BANDS) * band;
//             float bandWidth = width / HAZE_BANDS;
            
//             // Calculate wave distortion
//             float wavePhase = hazeTime * 1.5f + band * 0.3f;
//             float waveOffset = 3 * (float)Math.cos(wavePhase);
            
//             // Different wave patterns for top and bottom
//             if (!top) {
//                 waveOffset = -waveOffset;
//             }
            
//             // Color with slight tint and varying alpha
//             float alpha = intensity * (0.08f + 0.04f * (float)Math.sin(wavePhase * 1.2f));
//             float r = 1.0f + 0.05f * (float)Math.sin(wavePhase * 0.8f);
//             float g = 0.98f + 0.02f * (float)Math.cos(wavePhase);
//             float b = 0.95f + 0.05f * (float)Math.sin(wavePhase * 1.3f);
            
//             // Gradient from edge to center
//             float innerAlpha = alpha * 0.3f;
//             float outerAlpha = alpha;
            
//             if (top) {
//                 // Top: fade from top edge to center
//                 buffer.vertex(matrix, bandX, y + waveOffset, 0).color(r, g, b, outerAlpha).endVertex();
//                 buffer.vertex(matrix, bandX + bandWidth, y + waveOffset, 0).color(r, g, b, outerAlpha).endVertex();
//                 buffer.vertex(matrix, bandX + bandWidth, y + height, 0).color(r, g, b, innerAlpha).endVertex();
//                 buffer.vertex(matrix, bandX, y + height, 0).color(r, g, b, innerAlpha).endVertex();
//             } else {
//                 // Bottom: fade from center to bottom edge
//                 buffer.vertex(matrix, bandX, y, 0).color(r, g, b, innerAlpha).endVertex();
//                 buffer.vertex(matrix, bandX + bandWidth, y, 0).color(r, g, b, innerAlpha).endVertex();
//                 buffer.vertex(matrix, bandX + bandWidth, y + height + waveOffset, 0).color(r, g, b, outerAlpha).endVertex();
//                 buffer.vertex(matrix, bandX, y + height + waveOffset, 0).color(r, g, b, outerAlpha).endVertex();
//             }
//         }
//     }
    
//     // Additional method for more intense desert haze effect
//     // Changed parameter from PoseStack to GuiGraphics
//     public static void renderDesertHaze(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float intensity) {
//         if (intensity <= 0) return;
        
//         RenderSystem.setShader(GameRenderer::getPositionColorShader);
//         RenderSystem.enableBlend();
//         RenderSystem.defaultBlendFunc();
//         RenderSystem.disableDepthTest();
        
//         Tesselator tesselator = Tesselator.getInstance();
//         BufferBuilder buffer = tesselator.getBuilder();
//         // Get the Matrix4f from GuiGraphics
//         Matrix4f matrix = guiGraphics.pose().last().pose();
        
//         buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        
//         // Create rising heat waves across the bottom third of the screen
//         int waveCount = 20;
//         for (int i = 0; i < waveCount; i++) {
//             float waveX = (screenWidth / (float)waveCount) * i;
//             float waveWidth = screenWidth / (float)waveCount;
            
//             // Multiple overlapping sine waves for complex distortion
//             float phase1 = hazeTime * 3 + i * 0.4f;
//             float phase2 = hazeTime * 2.2f + i * 0.6f;
//             float phase3 = hazeTime * 1.8f + i * 0.3f;
            
//             float waveHeight1 = 15 * (float)Math.sin(phase1);
//             float waveHeight2 = 10 * (float)Math.sin(phase2);
//             float waveHeight3 = 8 * (float)Math.cos(phase3);
            
//             float totalWaveHeight = waveHeight1 + waveHeight2 + waveHeight3;
            
//             // Heat distortion color (slightly orange/yellow tint)
//             float alpha = intensity * 0.15f * (0.7f + 0.3f * (float)Math.sin(phase1 * 1.5f));
//             float r = 1.0f + 0.1f * (float)Math.sin(phase1 * 0.9f);
//             float g = 0.95f + 0.05f * (float)Math.cos(phase2 * 0.8f);
//             float b = 0.85f + 0.15f * (float)Math.sin(phase3 * 1.2f);
            
//             // Create wavy quad from bottom of screen upward
//             float baseY = screenHeight * 0.7f;
//             float topY = screenHeight * 0.4f;
            
//             buffer.vertex(matrix, waveX, screenHeight, 0).color(r, g, b, alpha).endVertex();
//             buffer.vertex(matrix, waveX + waveWidth, screenHeight, 0).color(r, g, b, alpha).endVertex();
//             buffer.vertex(matrix, waveX + waveWidth, topY + totalWaveHeight, 0).color(r, g, b, alpha * 0.3f).endVertex();
//             buffer.vertex(matrix, waveX, topY + totalWaveHeight, 0).color(r, g, b, alpha * 0.3f).endVertex();
//         }
        
//         BufferUploader.draw(buffer.end()); // Corrected BufferUploader usage
        
//         RenderSystem.enableDepthTest();
//         RenderSystem.disableBlend();
//     }
// }
