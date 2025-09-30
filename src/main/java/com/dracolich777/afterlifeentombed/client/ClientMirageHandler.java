package com.dracolich777.afterlifeentombed.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import com.dracolich777.afterlifeentombed.effects.MirageEffect;
import com.dracolich777.afterlifeentombed.effects.MirageEffect.MirageData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn; // Corrected import for BakedModel
import net.minecraftforge.client.event.RenderLevelStageEvent; // Added import for OverlayTexture
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientMirageHandler {
    
private static final Map<BlockPos, MirageBlock> MIRAGE_BLOCKS = new HashMap<>();
private static long lastUpdateTime = 0;
    
    // Heat haze effect parameters - adjusted for smoother appearance
    private static final float HAZE_INTENSITY = 0.25f; // Reduced for subtlety
    private static final float HAZE_SPEED = 0.06f; // Slightly slower for smoother motion
    private static final int HAZE_LAYERS = 3; // Reduced layers to minimize artifacts
    private static final float HORIZON_DISTORTION_DISTANCE = 45.0f;
    private static final float MAX_DISTORTION_HEIGHT = 6.0f; // Reduced height
    
    public static class MirageBlock {
        public final BlockPos pos;
        public final BlockState blockState;
        public final int amplifier;
        public float fadeAlpha;
        public boolean isBeingLookedAt;
        public int ticksExisted;
        public float hazeOffset;
        public float hazePhase;
        public final boolean isPhantomStructure;
        public final String structureName;
        public boolean isFading; // New field for gradual fading
        public float fadeSpeed; // Speed of fading
        
        public MirageBlock(BlockPos pos, BlockState blockState, int amplifier) {
            this(pos, blockState, amplifier, false, "");
        }
        
        public MirageBlock(BlockPos pos, BlockState blockState, int amplifier, boolean isPhantomStructure, String structureName) {
            this.pos = pos;
            this.blockState = blockState;
            this.amplifier = amplifier;
            this.fadeAlpha = 1.0f;
            this.isBeingLookedAt = false;
            this.ticksExisted = 0;
            this.hazeOffset = (float)(Math.random() * Math.PI * 2);
            this.hazePhase = (float)(Math.random() * Math.PI * 2);
            this.isPhantomStructure = isPhantomStructure;
            this.structureName = structureName != null ? structureName : "";
            this.isFading = false;
            this.fadeSpeed = 0.02f; // Base fade speed
        }
        
        public void startFading() {
            this.isFading = true;
            this.fadeSpeed = 0.015f; // Slower fade for smoother transition
        }
    }
    
    public static void handleMirageData(List<MirageEffect.MirageData> mirageDataList) {
        for (MirageEffect.MirageData data : mirageDataList) {
            if (data.shouldRemove) {
                // Clear all mirages immediately
                MIRAGE_BLOCKS.clear();
            } else if (data.shouldFade) {
                // Start fading all existing mirages
                for (MirageBlock mirage : MIRAGE_BLOCKS.values()) {
                    mirage.startFading();
                }
            } else {
                // Add new mirage block with phantom structure support
                MIRAGE_BLOCKS.put(data.pos, new MirageBlock(data.pos, data.blockState, data.amplifier, data.isPhantomStructure, data.structureName));
            }
        }
    }@SubscribeEvent
public static void onRenderLevelStage(RenderLevelStageEvent event) {
if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
// Corrected method call to get MultiBufferSource for RenderLevelStageEvent in 1.20.1
// Using Minecraft.getInstance().renderBuffers().bufferSource() as a robust alternative
renderMirages(event.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(), event.getPartialTick()); 
}
}
    
private static void renderMirages(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
Minecraft minecraft = Minecraft.getInstance();
LocalPlayer player = minecraft.player;
        
if (player == null || MIRAGE_BLOCKS.isEmpty()) return;
        
Level level = player.level();
Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
Vec3 playerEyePos = player.position().add(0, player.getEyeHeight(), 0);
Vec3 lookDirection = player.getLookAngle();
        
        // Update mirage states
        updateMirageStates(player, lookDirection, playerEyePos, partialTick);
        
        // Disabled horizon heat distortion to reduce visual clutter
        // renderHorizonDistortion(poseStack, bufferSource, player, lookDirection, playerEyePos, cameraPos, partialTick);// Render each mirage block with heat haze effect
Iterator<Map.Entry<BlockPos, MirageBlock>> iterator = MIRAGE_BLOCKS.entrySet().iterator();
while (iterator.hasNext()) {
Map.Entry<BlockPos, MirageBlock> entry = iterator.next();
MirageBlock mirage = entry.getValue();
            
// Remove completely faded mirages or very old ones
if (mirage.fadeAlpha <= 0.01f || mirage.ticksExisted > 8000) { // Increased lifetime for smoother experience
iterator.remove();
continue;
}
            
// Check if block position is still air
if (!level.getBlockState(mirage.pos).isAir()) {
iterator.remove();
continue;
}
            
// Render mirage block with effects
renderMirageBlock(poseStack, bufferSource, mirage, cameraPos, partialTick);
            
// Render heat haze around the mirage (more intense for phantom structures)
if (mirage.isPhantomStructure) {
renderPhantomStructureEffects(poseStack, bufferSource, mirage, cameraPos, playerEyePos, partialTick);
} else {
            renderHeatHaze(poseStack, bufferSource, mirage, cameraPos, playerEyePos, partialTick);
}
        }
    }
    
    private static void renderHorizonDistortion(PoseStack poseStack, MultiBufferSource bufferSource, LocalPlayer player, Vec3 lookDirection, Vec3 playerEyePos, Vec3 cameraPos, float partialTick) {
        // Only render horizon distortion if player has mirage effect
        if (!player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS) && !MIRAGE_BLOCKS.isEmpty()) {
            Level level = player.level();
            
            // Create heat distortion effect on the horizon
            for (int angle = 0; angle < 360; angle += 20) { // Increased angle step to reduce artifacts
                double radians = Math.toRadians(angle);
                
                // Position distortion at horizon distance
                double distortionX = playerEyePos.x + Math.cos(radians) * HORIZON_DISTORTION_DISTANCE;
                double distortionZ = playerEyePos.z + Math.sin(radians) * HORIZON_DISTORTION_DISTANCE;
                double distortionY = playerEyePos.y;
                
                // Check if this direction is roughly towards the horizon (not too high or low)
                Vec3 toDistortion = new Vec3(distortionX - playerEyePos.x, 0, distortionZ - playerEyePos.z).normalize();
                double dotProduct = lookDirection.dot(toDistortion);
                
                // Only render distortion in peripheral and forward vision
                if (dotProduct > -0.2) { // Narrower range for less artifacts
                    renderHorizonHaze(poseStack, bufferSource, new Vec3(distortionX, distortionY, distortionZ), cameraPos, partialTick);
                }
            }
        }
    }
    
    private static void renderHorizonHaze(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 position, Vec3 cameraPos, float partialTick) {
        poseStack.pushPose();
        
        // Translate to distortion position
        poseStack.translate(position.x - cameraPos.x, position.y - cameraPos.y, position.z - cameraPos.z);
        
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();
        
        // Create swimming heat effect with smooth continuous waves
        long gameTime = System.currentTimeMillis();
        float time = (gameTime % 10000) / 1000.0f;
        
        // Render smooth heat distortion with overlapping circular waves
        int rings = 6;
        float maxRadius = 15.0f;
        
        for (int ring = 0; ring < rings; ring++) {
            float ringRadius = (ring + 1) * (maxRadius / rings);
            float ringAlpha = HAZE_INTENSITY * 0.3f / (ring + 1);
            
            // Create smooth circular distortion
            int segments = Math.max(16, (int)(ringRadius * 2)); // More segments for larger rings
            
            for (int i = 0; i < segments; i++) {
                float angle1 = (float)(i * 2 * Math.PI / segments);
                float angle2 = (float)((i + 1) * 2 * Math.PI / segments);
                
                // Multiple wave frequencies for complex heat shimmer
                float wave1 = 0.08f * (float)Math.sin(time * 1.5f + ringRadius * 0.1f + angle1 * 3);
                float wave2 = 0.05f * (float)Math.sin(time * 2.8f + ringRadius * 0.15f + angle1 * 5);
                float wave3 = 0.03f * (float)Math.sin(time * 4.2f + ringRadius * 0.2f + angle1 * 7);
                float totalWave = wave1 + wave2 + wave3;
                
                // Create smooth radial distortion
                float innerRadius = ringRadius - 1.0f + totalWave;
                float outerRadius = ringRadius + 1.0f + totalWave * 0.8f;
                
                float x1 = (float)Math.cos(angle1);
                float z1 = (float)Math.sin(angle1);
                float x2 = (float)Math.cos(angle2);
                float z2 = (float)Math.sin(angle2);
                
                // Variable height distortion for more realistic heat haze
                float baseHeight = MAX_DISTORTION_HEIGHT * 0.4f;
                float heightVariation = baseHeight * 0.2f * (float)Math.sin(time * 1.2f + ringRadius * 0.1f);
                float topHeight = baseHeight + heightVariation;
                float bottomHeight = -baseHeight * 0.5f + heightVariation * 0.3f;
                
                // Subtle color variation to break up uniformity
                float heatIntensity = 0.9f + 0.1f * (float)Math.sin(time * 3.0f + angle1 + ringRadius * 0.1f);
                float r = 0.98f + 0.02f * heatIntensity;
                float g = 0.99f + 0.01f * heatIntensity;
                float b = 1.0f;
                
                // Smooth alpha gradients to eliminate hard edges
                float centerAlpha = ringAlpha * 0.8f;
                float edgeAlpha = ringAlpha * 0.1f;
                
                // Inner ring
                consumer.vertex(matrix, x1 * innerRadius, bottomHeight, z1 * innerRadius)
                    .color(r, g, b, edgeAlpha).uv(0, 1).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
                consumer.vertex(matrix, x2 * innerRadius, bottomHeight, z2 * innerRadius)
                    .color(r, g, b, edgeAlpha).uv(1, 1).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
                consumer.vertex(matrix, x2 * outerRadius, topHeight, z2 * outerRadius)
                    .color(r, g, b, centerAlpha).uv(1, 0).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
                consumer.vertex(matrix, x1 * outerRadius, topHeight, z1 * outerRadius)
                    .color(r, g, b, centerAlpha).uv(0, 0).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
            }
        }
        
        poseStack.popPose();
    }
    
    private static void renderPhantomStructureEffects(PoseStack poseStack, MultiBufferSource bufferSource, MirageBlock mirage, Vec3 cameraPos, Vec3 playerEyePos, float partialTick) {
        // Render enhanced effects for phantom structures
        poseStack.pushPose();
        
        Vec3 blockPos = Vec3.atLowerCornerOf(mirage.pos);
        poseStack.translate(blockPos.x - cameraPos.x, blockPos.y - cameraPos.y, blockPos.z - cameraPos.z);
        
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();
        
        // Create ethereal glow effect around phantom structures
        long gameTime = System.currentTimeMillis();
        float time = (gameTime % 8000) / 1000.0f; // Slower, more mysterious animation
        
        // Create smooth concentric rings of ethereal energy
        int rings = 3; // Reduced rings to minimize artifacts
        for (int ring = 0; ring < rings; ring++) {
            float ringRadius = 1.2f + ring * 0.6f;
            float ringAlpha = (HAZE_INTENSITY * mirage.fadeAlpha * 0.6f) / (ring + 1);
            
            // Smooth rotating ethereal energy
            float rotationOffset = time * 0.5f + ring * 0.8f;
            int segments = 20 + ring * 6; // More segments for smoother circles
            
            for (int i = 0; i < segments; i++) {
                float angle1 = (float)(i * 2 * Math.PI / segments) + rotationOffset;
                float angle2 = (float)((i + 1) * 2 * Math.PI / segments) + rotationOffset;
                
                // Create smooth pulsing ethereal wisps
                float pulse = 0.9f + 0.1f * (float)Math.sin(time * 2.0f + ring + i * 0.15f);
                float wispHeight = 2.0f + ring * 0.2f;
                
                // Smooth wave distortion
                float waveOffset = 0.05f * (float)Math.sin(time * 3.0f + angle1 * 4 + ring);
                
                float x1 = (float)(Math.cos(angle1) * (ringRadius + waveOffset) * pulse);
                float z1 = (float)(Math.sin(angle1) * (ringRadius + waveOffset) * pulse);
                float x2 = (float)(Math.cos(angle2) * (ringRadius + waveOffset) * pulse);
                float z2 = (float)(Math.sin(angle2) * (ringRadius + waveOffset) * pulse);
                
                // Phantom structure glow color (subtle ethereal blue-white)
                float colorVariation = 0.05f * (float)Math.sin(time * 1.5f + ring + angle1);
                float r = 0.85f + colorVariation;
                float g = 0.92f + colorVariation * 0.5f;
                float b = 0.98f + colorVariation * 0.2f;
                
                // Smooth alpha gradients for seamless blending
                float bottomAlpha = ringAlpha * 0.6f;
                float topAlpha = ringAlpha * 0.1f;
                
                // Bottom of wisp
                consumer.vertex(matrix, x1, 0, z1).color(r, g, b, bottomAlpha).uv(0, 1).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
                consumer.vertex(matrix, x2, 0, z2).color(r, g, b, bottomAlpha).uv(1, 1).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
                
                // Top of wisp with smooth upward flow
                float topX1 = x1 * 0.85f + 0.05f * (float)Math.sin(time * 2.5f + i);
                float topZ1 = z1 * 0.85f + 0.05f * (float)Math.cos(time * 2.5f + i);
                float topX2 = x2 * 0.85f + 0.05f * (float)Math.sin(time * 2.5f + i + 0.3f);
                float topZ2 = z2 * 0.85f + 0.05f * (float)Math.cos(time * 2.5f + i + 0.3f);
                
                consumer.vertex(matrix, topX2, wispHeight, topZ2).color(r, g, b, topAlpha).uv(1, 0).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
                consumer.vertex(matrix, topX1, wispHeight, topZ1).color(r, g, b, topAlpha).uv(0, 0).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
            }
        }
        
        poseStack.popPose();
    }private static void updateMirageStates(LocalPlayer player, Vec3 lookDirection, Vec3 playerEyePos, float partialTick) {
long currentTime = System.currentTimeMillis();
        
for (MirageBlock mirage : MIRAGE_BLOCKS.values()) {
mirage.ticksExisted++;
            
// Calculate if player is looking directly at the mirage
Vec3 mirageCenter = Vec3.atCenterOf(mirage.pos);
Vec3 toMirage = mirageCenter.subtract(playerEyePos).normalize();
double dotProduct = lookDirection.dot(toMirage);
double distance = playerEyePos.distanceTo(mirageCenter);
            
boolean currentlyLookingDirectly = dotProduct > 0.9 && distance < 55;
            
if (currentlyLookingDirectly) {
mirage.isBeingLookedAt = true;
mirage.fadeAlpha = Math.max(0, mirage.fadeAlpha - 0.04f); // Faster fade when looked at
} else {
mirage.isBeingLookedAt = false;
// Handle different fading scenarios
if (mirage.isFading) {
// Gradually fade out when moving
mirage.fadeAlpha = Math.max(0, mirage.fadeAlpha - mirage.fadeSpeed);
} else {
// Restore opacity when not being looked at directly
mirage.fadeAlpha = Math.min(1.0f, mirage.fadeAlpha + 0.015f); // Slow restoration
}
}
            
// Update haze animation
mirage.hazePhase += HAZE_SPEED * partialTick;
if (mirage.hazePhase > Math.PI * 2) {
mirage.hazePhase -= Math.PI * 2;
}
}
}
    
private static void renderMirageBlock(PoseStack poseStack, MultiBufferSource bufferSource, MirageBlock mirage, Vec3 cameraPos, float partialTick) {
BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        
poseStack.pushPose();
        
// Translate to block position relative to camera
Vec3 blockPos = Vec3.atLowerCornerOf(mirage.pos);
poseStack.translate(blockPos.x - cameraPos.x, blockPos.y - cameraPos.y, blockPos.z - cameraPos.z);
        
        // Apply fading and shimmering effects
        float shimmerIntensity = 0.1f + 0.05f * (float)Math.sin(mirage.hazePhase * 2);
        float alpha = mirage.fadeAlpha * (0.7f + shimmerIntensity);
        
        // Phantom structures are more ethereal and translucent
        if (mirage.isPhantomStructure) {
            alpha *= 0.6f; // Make phantom structures more transparent
            shimmerIntensity *= 1.5f; // More shimmer for phantom structures
        }// Slight wavering motion
float waveX = 0.02f * (float)Math.sin(mirage.hazePhase + mirage.hazeOffset);
float waveZ = 0.02f * (float)Math.cos(mirage.hazePhase * 1.3f + mirage.hazeOffset);
poseStack.translate(waveX, 0, waveZ);
        
// Render block with translucency
VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        
// Custom rendering to support alpha
renderBlockWithAlpha(poseStack, consumer, mirage.blockState, alpha);
        
poseStack.popPose();
}
    
private static void renderBlockWithAlpha(PoseStack poseStack, VertexConsumer consumer, BlockState blockState, float alpha) {
BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        
// Get the block model and render it with custom alpha
try {
BakedModel model = blockRenderer.getBlockModel(blockState);
            
// Create a custom vertex consumer that applies alpha
VertexConsumer alphaConsumer = new AlphaVertexConsumer(consumer, alpha);
            
// Render the model
blockRenderer.getModelRenderer().renderModel(
poseStack.last(),
alphaConsumer,
blockState,
model,
1.0f, 1.0f, 1.0f, // RGB tint
0xF000F0, // Full brightness (packed light)
OverlayTexture.NO_OVERLAY // No overlay (packed overlay)
);
} catch (Exception e) {
// Fallback: render as a simple translucent cube
renderSimpleCube(poseStack, consumer, alpha);
}
}
    
private static void renderSimpleCube(PoseStack poseStack, VertexConsumer consumer, float alpha) {
Matrix4f matrix = poseStack.last().pose();
        
// Simple cube vertices with alpha
int light = 0xF000F0;
float r = 0.8f, g = 0.8f, b = 0.8f;
        
// Front face
consumer.vertex(matrix, 0, 0, 1).color(r, g, b, alpha).uv(0, 1).overlayCoords(0).uv2(light).normal(0, 0, 1).endVertex();
consumer.vertex(matrix, 1, 0, 1).color(r, g, b, alpha).uv(1, 1).overlayCoords(0).uv2(light).normal(0, 0, 1).endVertex();
consumer.vertex(matrix, 1, 1, 1).color(r, g, b, alpha).uv(1, 0).overlayCoords(0).uv2(light).normal(0, 0, 1).endVertex();
consumer.vertex(matrix, 0, 1, 1).color(r, g, b, alpha).uv(0, 0).overlayCoords(0).uv2(light).normal(0, 0, 1).endVertex();
        
// Add other faces as needed...
// Back face
consumer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha).uv(0, 1).overlayCoords(0).uv2(light).normal(0, 0, -1).endVertex();
consumer.vertex(matrix, 0, 1, 0).color(r, g, b, alpha).uv(0, 0).overlayCoords(0).uv2(light).normal(0, 0, -1).endVertex();
consumer.vertex(matrix, 1, 1, 0).color(r, g, b, alpha).uv(1, 0).overlayCoords(0).uv2(light).normal(0, 0, -1).endVertex();
consumer.vertex(matrix, 1, 0, 0).color(r, g, b, alpha).uv(1, 1).overlayCoords(0).uv2(light).normal(0, 0, -1).endVertex();

// Right face
consumer.vertex(matrix, 1, 0, 0).color(r, g, b, alpha).uv(0, 1).overlayCoords(0).uv2(light).normal(1, 0, 0).endVertex();
consumer.vertex(matrix, 1, 1, 0).color(r, g, b, alpha).uv(0, 0).overlayCoords(0).uv2(light).normal(1, 0, 0).endVertex();
consumer.vertex(matrix, 1, 1, 1).color(r, g, b, alpha).uv(1, 0).overlayCoords(0).uv2(light).normal(1, 0, 0).endVertex();
consumer.vertex(matrix, 1, 0, 1).color(r, g, b, alpha).uv(1, 1).overlayCoords(0).uv2(light).normal(1, 0, 0).endVertex();

// Left face
consumer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha).uv(0, 1).overlayCoords(0).uv2(light).normal(-1, 0, 0).endVertex();
consumer.vertex(matrix, 0, 0, 1).color(r, g, b, alpha).uv(1, 1).overlayCoords(0).uv2(light).normal(-1, 0, 0).endVertex();
consumer.vertex(matrix, 0, 1, 1).color(r, g, b, alpha).uv(1, 0).overlayCoords(0).uv2(light).normal(-1, 0, 0).endVertex();
consumer.vertex(matrix, 0, 1, 0).color(r, g, b, alpha).uv(0, 0).overlayCoords(0).uv2(light).normal(-1, 0, 0).endVertex();

// Top face
consumer.vertex(matrix, 0, 1, 0).color(r, g, b, alpha).uv(0, 1).overlayCoords(0).uv2(light).normal(0, 1, 0).endVertex();
consumer.vertex(matrix, 0, 1, 1).color(r, g, b, alpha).uv(0, 0).overlayCoords(0).uv2(light).normal(0, 1, 0).endVertex();
consumer.vertex(matrix, 1, 1, 1).color(r, g, b, alpha).uv(1, 0).overlayCoords(0).uv2(light).normal(0, 1, 0).endVertex();
consumer.vertex(matrix, 1, 1, 0).color(r, g, b, alpha).uv(1, 1).overlayCoords(0).uv2(light).normal(0, 1, 0).endVertex();

// Bottom face
consumer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha).uv(0, 1).overlayCoords(0).uv2(light).normal(0, -1, 0).endVertex();
consumer.vertex(matrix, 1, 0, 0).color(r, g, b, alpha).uv(1, 1).overlayCoords(0).uv2(light).normal(0, -1, 0).endVertex();
consumer.vertex(matrix, 1, 0, 1).color(r, g, b, alpha).uv(1, 0).overlayCoords(0).uv2(light).normal(0, -1, 0).endVertex();
consumer.vertex(matrix, 0, 0, 1).color(r, g, b, alpha).uv(0, 0).overlayCoords(0).uv2(light).normal(0, -1, 0).endVertex();
}
    
private static void renderHeatHaze(PoseStack poseStack, MultiBufferSource bufferSource, MirageBlock mirage, Vec3 cameraPos, Vec3 playerEyePos, float partialTick) {
// Only render heat haze for desert mirages (amplifier 1) or when close to any mirage
double distanceToMirage = playerEyePos.distanceTo(Vec3.atCenterOf(mirage.pos));
        
if (mirage.amplifier != 1 && distanceToMirage > 30) return;
        
poseStack.pushPose();
        
Vec3 blockPos = Vec3.atLowerCornerOf(mirage.pos);
poseStack.translate(blockPos.x - cameraPos.x, blockPos.y - cameraPos.y, blockPos.z - cameraPos.z);
        
// Render multiple layers of subtle distortion around the mirage
for (int layer = 0; layer < HAZE_LAYERS; layer++) {
float layerOffset = (layer + 1) * 0.5f;
float layerAlpha = HAZE_INTENSITY * mirage.fadeAlpha * (1.0f - layer * 0.3f);
            
renderHazeLayer(poseStack, bufferSource, mirage, layerOffset, layerAlpha, partialTick);
}
        
poseStack.popPose();
}
    
    private static void renderHazeLayer(PoseStack poseStack, MultiBufferSource bufferSource, MirageBlock mirage, float offset, float alpha, float partialTick) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();
        
        // Create smooth shimmering air effect around the mirage
        float time = mirage.hazePhase + mirage.hazeOffset;
        int segments = 16; // Consistent segment count for smooth circles
        float radius = 1.0f + offset;
        float height = 2.0f;
        
        for (int i = 0; i < segments; i++) {
            float angle1 = (float)(i * 2 * Math.PI / segments);
            float angle2 = (float)((i + 1) * 2 * Math.PI / segments);
            
            // Create subtle wavering effect
            float waveOffset1 = 0.05f * (float)Math.sin(time * 1.8f + angle1 * 2);
            float waveOffset2 = 0.05f * (float)Math.sin(time * 1.8f + angle2 * 2);
            
            // Calculate positions with subtle wave distortion
            float x1 = (float)(Math.cos(angle1) * (radius + waveOffset1));
            float z1 = (float)(Math.sin(angle1) * (radius + waveOffset1));
            float x2 = (float)(Math.cos(angle2) * (radius + waveOffset2));
            float z2 = (float)(Math.sin(angle2) * (radius + waveOffset2));
            
            // Subtle color variation to break up uniformity
            float colorShift = 0.02f * (float)Math.sin(time * 0.8f + angle1);
            float r = 0.96f + colorShift;
            float g = 0.98f + colorShift * 0.5f;
            float b = 1.0f;
            
            // Smooth alpha gradients to reduce hard edges
            float bottomAlpha = alpha * 0.4f;
            float topAlpha = alpha * 0.05f;
            
            // Bottom vertices with smoother gradients
            consumer.vertex(matrix, x1, 0, z1).color(r, g, b, bottomAlpha).uv(0, 1).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
            consumer.vertex(matrix, x2, 0, z2).color(r, g, b, bottomAlpha).uv(1, 1).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
            
            // Top vertices with minimal distortion for smoother appearance
            float topWave1 = 0.03f * (float)Math.sin(time * 2.2f + angle1 * 1.5f);
            float topWave2 = 0.03f * (float)Math.sin(time * 2.2f + angle2 * 1.5f);
            
            consumer.vertex(matrix, x2 + topWave2, height, z2 + topWave2).color(r, g, b, topAlpha).uv(1, 0).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
            consumer.vertex(matrix, x1 + topWave1, height, z1 + topWave1).color(r, g, b, topAlpha).uv(0, 0).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
        }
    }// Custom vertex consumer that applies alpha blending
private static class AlphaVertexConsumer implements VertexConsumer {
private final VertexConsumer delegate;
private final float alpha;
        
public AlphaVertexConsumer(VertexConsumer delegate, float alpha) {
this.delegate = delegate;
this.alpha = alpha;
}
        
@Override
public VertexConsumer vertex(double x, double y, double z) {
return delegate.vertex(x, y, z);
}
        
@Override
public VertexConsumer color(int red, int green, int blue, int alpha) {
return delegate.color(red, green, blue, (int)(alpha * this.alpha));
}
        
@Override
public VertexConsumer color(float red, float green, float blue, float alpha) {
return delegate.color(red, green, blue, alpha * this.alpha);
}
        
@Override
public VertexConsumer uv(float u, float v) {
return delegate.uv(u, v);
}
        
@Override
public VertexConsumer overlayCoords(int u, int v) {
return delegate.overlayCoords(u, v);
}
        
@Override
public VertexConsumer uv2(int u, int v) {
return delegate.uv2(u, v);
}
        
@Override
public VertexConsumer normal(float x, float y, float z) {
return delegate.normal(x, y, z);
}
        
@Override
public void endVertex() {
delegate.endVertex();
}
        
@Override
public void defaultColor(int red, int green, int blue, int alpha) {
delegate.defaultColor(red, green, blue, (int)(alpha * this.alpha));
}
        
@Override
public void unsetDefaultColor() {
delegate.unsetDefaultColor();
}
}
    
public static void clearAllMirages() {
MIRAGE_BLOCKS.clear();
}
}
