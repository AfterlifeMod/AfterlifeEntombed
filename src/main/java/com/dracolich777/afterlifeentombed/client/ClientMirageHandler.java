// package com.dracolich777.afterlifeentombed.client;

// import java.util.HashMap;
// import java.util.Iterator;
// import java.util.List;
// import java.util.Map;

// import org.joml.Matrix4f;

// import com.dracolich777.afterlifeentombed.effects.MirageEffect;
// import com.dracolich777.afterlifeentombed.effects.MirageEffect.MirageData;
// import com.mojang.blaze3d.vertex.PoseStack;
// import com.mojang.blaze3d.vertex.VertexConsumer;

// import net.minecraft.client.Minecraft;
// import net.minecraft.client.player.LocalPlayer;
// import net.minecraft.client.renderer.MultiBufferSource;
// import net.minecraft.client.renderer.RenderType;
// import net.minecraft.client.renderer.block.BlockRenderDispatcher;
// import net.minecraft.client.renderer.texture.OverlayTexture;
// import net.minecraft.client.resources.model.BakedModel;
// import net.minecraft.core.BlockPos;
// import net.minecraft.world.level.Level;
// import net.minecraft.world.level.block.state.BlockState;
// import net.minecraft.world.phys.Vec3;
// import net.minecraftforge.api.distmarker.Dist;
// import net.minecraftforge.api.distmarker.OnlyIn; // Corrected import for BakedModel
// import net.minecraftforge.client.event.RenderLevelStageEvent; // Added import for OverlayTexture
// import net.minecraftforge.eventbus.api.SubscribeEvent;
// import net.minecraftforge.fml.common.Mod;

// @OnlyIn(Dist.CLIENT)
// @Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
// public class ClientMirageHandler {
    
//     private static final Map<BlockPos, MirageBlock> MIRAGE_BLOCKS = new HashMap<>();
//     private static long lastUpdateTime = 0;
    
//     // Heat haze effect parameters
//     private static final float HAZE_INTENSITY = 0.3f;
//     private static final float HAZE_SPEED = 0.05f;
//     private static final int HAZE_LAYERS = 3;
    
//     public static class MirageBlock {
//         public final BlockPos pos;
//         public final BlockState blockState;
//         public final int amplifier;
//         public float fadeAlpha;
//         public boolean isBeingLookedAt;
//         public int ticksExisted;
//         public float hazeOffset;
//         public float hazePhase;
        
//         public MirageBlock(BlockPos pos, BlockState blockState, int amplifier) {
//             this.pos = pos;
//             this.blockState = blockState;
//             this.amplifier = amplifier;
//             this.fadeAlpha = 1.0f;
//             this.isBeingLookedAt = false;
//             this.ticksExisted = 0;
//             this.hazeOffset = (float)(Math.random() * Math.PI * 2);
//             this.hazePhase = (float)(Math.random() * Math.PI * 2);
//         }
//     }
    
//     public static void handleMirageData(List<MirageEffect.MirageData> mirageDataList) {
//         for (MirageEffect.MirageData data : mirageDataList) {
//             if (data.shouldRemove) {
//                 // Clear all mirages
//                 MIRAGE_BLOCKS.clear();
//             } else {
//                 // Add new mirage block
//                 MIRAGE_BLOCKS.put(data.pos, new MirageBlock(data.pos, data.blockState, data.amplifier));
//             }
//         }
//     }
    
//     @SubscribeEvent
//     public static void onRenderLevelStage(RenderLevelStageEvent event) {
//         if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
//             // Corrected method call to get MultiBufferSource for RenderLevelStageEvent in 1.20.1
//             // Using Minecraft.getInstance().renderBuffers().bufferSource() as a robust alternative
//             renderMirages(event.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(), event.getPartialTick()); 
//         }
//     }
    
//     private static void renderMirages(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
//         Minecraft minecraft = Minecraft.getInstance();
//         LocalPlayer player = minecraft.player;
        
//         if (player == null || MIRAGE_BLOCKS.isEmpty()) return;
        
//         Level level = player.level();
//         Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
//         Vec3 playerEyePos = player.position().add(0, player.getEyeHeight(), 0);
//         Vec3 lookDirection = player.getLookAngle();
        
//         // Update mirage states
//         updateMirageStates(player, lookDirection, playerEyePos, partialTick);
        
//         // Render each mirage block with heat haze effect
//         Iterator<Map.Entry<BlockPos, MirageBlock>> iterator = MIRAGE_BLOCKS.entrySet().iterator();
//         while (iterator.hasNext()) {
//             Map.Entry<BlockPos, MirageBlock> entry = iterator.next();
//             MirageBlock mirage = entry.getValue();
            
//             // Remove faded mirages
//             if (mirage.fadeAlpha <= 0 || mirage.ticksExisted > 6000) {
//                 iterator.remove();
//                 continue;
//             }
            
//             // Check if block position is still air
//             if (!level.getBlockState(mirage.pos).isAir()) {
//                 iterator.remove();
//                 continue;
//             }
            
//             // Render mirage block with effects
//             renderMirageBlock(poseStack, bufferSource, mirage, cameraPos, partialTick);
            
//             // Render heat haze around the mirage
//             renderHeatHaze(poseStack, bufferSource, mirage, cameraPos, playerEyePos, partialTick);
//         }
//     }
    
//     private static void updateMirageStates(LocalPlayer player, Vec3 lookDirection, Vec3 playerEyePos, float partialTick) {
//         long currentTime = System.currentTimeMillis();
        
//         for (MirageBlock mirage : MIRAGE_BLOCKS.values()) {
//             mirage.ticksExisted++;
            
//             // Calculate if player is looking directly at the mirage
//             Vec3 mirageCenter = Vec3.atCenterOf(mirage.pos);
//             Vec3 toMirage = mirageCenter.subtract(playerEyePos).normalize();
//             double dotProduct = lookDirection.dot(toMirage);
//             double distance = playerEyePos.distanceTo(mirageCenter);
            
//             boolean currentlyLookingDirectly = dotProduct > 0.9 && distance < 55;
            
//             if (currentlyLookingDirectly) {
//                 mirage.isBeingLookedAt = true;
//                 mirage.fadeAlpha = Math.max(0, mirage.fadeAlpha - 0.05f);
//             } else {
//                 mirage.isBeingLookedAt = false;
//                 mirage.fadeAlpha = Math.min(1.0f, mirage.fadeAlpha + 0.02f);
//             }
            
//             // Update haze animation
//             mirage.hazePhase += HAZE_SPEED * partialTick;
//             if (mirage.hazePhase > Math.PI * 2) {
//                 mirage.hazePhase -= Math.PI * 2;
//             }
//         }
//     }
    
//     private static void renderMirageBlock(PoseStack poseStack, MultiBufferSource bufferSource, MirageBlock mirage, Vec3 cameraPos, float partialTick) {
//         BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        
//         poseStack.pushPose();
        
//         // Translate to block position relative to camera
//         Vec3 blockPos = Vec3.atLowerCornerOf(mirage.pos);
//         poseStack.translate(blockPos.x - cameraPos.x, blockPos.y - cameraPos.y, blockPos.z - cameraPos.z);
        
//         // Apply fading and shimmering effects
//         float shimmerIntensity = 0.1f + 0.05f * (float)Math.sin(mirage.hazePhase * 2);
//         float alpha = mirage.fadeAlpha * (0.7f + shimmerIntensity);
        
//         // Slight wavering motion
//         float waveX = 0.02f * (float)Math.sin(mirage.hazePhase + mirage.hazeOffset);
//         float waveZ = 0.02f * (float)Math.cos(mirage.hazePhase * 1.3f + mirage.hazeOffset);
//         poseStack.translate(waveX, 0, waveZ);
        
//         // Render block with translucency
//         VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        
//         // Custom rendering to support alpha
//         renderBlockWithAlpha(poseStack, consumer, mirage.blockState, alpha);
        
//         poseStack.popPose();
//     }
    
//     private static void renderBlockWithAlpha(PoseStack poseStack, VertexConsumer consumer, BlockState blockState, float alpha) {
//         BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        
//         // Get the block model and render it with custom alpha
//         try {
//             BakedModel model = blockRenderer.getBlockModel(blockState);
            
//             // Create a custom vertex consumer that applies alpha
//             VertexConsumer alphaConsumer = new AlphaVertexConsumer(consumer, alpha);
            
//             // Render the model
//             blockRenderer.getModelRenderer().renderModel(
//                 poseStack.last(),
//                 alphaConsumer,
//                 blockState,
//                 model,
//                 1.0f, 1.0f, 1.0f, // RGB tint
//                 0xF000F0, // Full brightness (packed light)
//                 OverlayTexture.NO_OVERLAY // No overlay (packed overlay)
//             );
//         } catch (Exception e) {
//             // Fallback: render as a simple translucent cube
//             renderSimpleCube(poseStack, consumer, alpha);
//         }
//     }
    
//     private static void renderSimpleCube(PoseStack poseStack, VertexConsumer consumer, float alpha) {
//         Matrix4f matrix = poseStack.last().pose();
        
//         // Simple cube vertices with alpha
//         int light = 0xF000F0;
//         float r = 0.8f, g = 0.8f, b = 0.8f;
        
//         // Front face
//         consumer.vertex(matrix, 0, 0, 1).color(r, g, b, alpha).uv(0, 1).overlayCoords(0).uv2(light).normal(0, 0, 1).endVertex();
//         consumer.vertex(matrix, 1, 0, 1).color(r, g, b, alpha).uv(1, 1).overlayCoords(0).uv2(light).normal(0, 0, 1).endVertex();
//         consumer.vertex(matrix, 1, 1, 1).color(r, g, b, alpha).uv(1, 0).overlayCoords(0).uv2(light).normal(0, 0, 1).endVertex();
//         consumer.vertex(matrix, 0, 1, 1).color(r, g, b, alpha).uv(0, 0).overlayCoords(0).uv2(light).normal(0, 0, 1).endVertex();
        
//         // Add other faces as needed...
//         // Back face
//         consumer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha).uv(0, 1).overlayCoords(0).uv2(light).normal(0, 0, -1).endVertex();
//         consumer.vertex(matrix, 0, 1, 0).color(r, g, b, alpha).uv(0, 0).overlayCoords(0).uv2(light).normal(0, 0, -1).endVertex();
//         consumer.vertex(matrix, 1, 1, 0).color(r, g, b, alpha).uv(1, 0).overlayCoords(0).uv2(light).normal(0, 0, -1).endVertex();
//         consumer.vertex(matrix, 1, 0, 0).color(r, g, b, alpha).uv(1, 1).overlayCoords(0).uv2(light).normal(0, 0, -1).endVertex();

//         // Right face
//         consumer.vertex(matrix, 1, 0, 0).color(r, g, b, alpha).uv(0, 1).overlayCoords(0).uv2(light).normal(1, 0, 0).endVertex();
//         consumer.vertex(matrix, 1, 1, 0).color(r, g, b, alpha).uv(0, 0).overlayCoords(0).uv2(light).normal(1, 0, 0).endVertex();
//         consumer.vertex(matrix, 1, 1, 1).color(r, g, b, alpha).uv(1, 0).overlayCoords(0).uv2(light).normal(1, 0, 0).endVertex();
//         consumer.vertex(matrix, 1, 0, 1).color(r, g, b, alpha).uv(1, 1).overlayCoords(0).uv2(light).normal(1, 0, 0).endVertex();

//         // Left face
//         consumer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha).uv(0, 1).overlayCoords(0).uv2(light).normal(-1, 0, 0).endVertex();
//         consumer.vertex(matrix, 0, 0, 1).color(r, g, b, alpha).uv(1, 1).overlayCoords(0).uv2(light).normal(-1, 0, 0).endVertex();
//         consumer.vertex(matrix, 0, 1, 1).color(r, g, b, alpha).uv(1, 0).overlayCoords(0).uv2(light).normal(-1, 0, 0).endVertex();
//         consumer.vertex(matrix, 0, 1, 0).color(r, g, b, alpha).uv(0, 0).overlayCoords(0).uv2(light).normal(-1, 0, 0).endVertex();

//         // Top face
//         consumer.vertex(matrix, 0, 1, 0).color(r, g, b, alpha).uv(0, 1).overlayCoords(0).uv2(light).normal(0, 1, 0).endVertex();
//         consumer.vertex(matrix, 0, 1, 1).color(r, g, b, alpha).uv(0, 0).overlayCoords(0).uv2(light).normal(0, 1, 0).endVertex();
//         consumer.vertex(matrix, 1, 1, 1).color(r, g, b, alpha).uv(1, 0).overlayCoords(0).uv2(light).normal(0, 1, 0).endVertex();
//         consumer.vertex(matrix, 1, 1, 0).color(r, g, b, alpha).uv(1, 1).overlayCoords(0).uv2(light).normal(0, 1, 0).endVertex();

//         // Bottom face
//         consumer.vertex(matrix, 0, 0, 0).color(r, g, b, alpha).uv(0, 1).overlayCoords(0).uv2(light).normal(0, -1, 0).endVertex();
//         consumer.vertex(matrix, 1, 0, 0).color(r, g, b, alpha).uv(1, 1).overlayCoords(0).uv2(light).normal(0, -1, 0).endVertex();
//         consumer.vertex(matrix, 1, 0, 1).color(r, g, b, alpha).uv(1, 0).overlayCoords(0).uv2(light).normal(0, -1, 0).endVertex();
//         consumer.vertex(matrix, 0, 0, 1).color(r, g, b, alpha).uv(0, 0).overlayCoords(0).uv2(light).normal(0, -1, 0).endVertex();
//     }
    
//     private static void renderHeatHaze(PoseStack poseStack, MultiBufferSource bufferSource, MirageBlock mirage, Vec3 cameraPos, Vec3 playerEyePos, float partialTick) {
//         // Only render heat haze for desert mirages (amplifier 1) or when close to any mirage
//         double distanceToMirage = playerEyePos.distanceTo(Vec3.atCenterOf(mirage.pos));
        
//         if (mirage.amplifier != 1 && distanceToMirage > 30) return;
        
//         poseStack.pushPose();
        
//         Vec3 blockPos = Vec3.atLowerCornerOf(mirage.pos);
//         poseStack.translate(blockPos.x - cameraPos.x, blockPos.y - cameraPos.y, blockPos.z - cameraPos.z);
        
//         // Render multiple layers of subtle distortion around the mirage
//         for (int layer = 0; layer < HAZE_LAYERS; layer++) {
//             float layerOffset = (layer + 1) * 0.5f;
//             float layerAlpha = HAZE_INTENSITY * mirage.fadeAlpha * (1.0f - layer * 0.3f);
            
//             renderHazeLayer(poseStack, bufferSource, mirage, layerOffset, layerAlpha, partialTick);
//         }
        
//         poseStack.popPose();
//     }
    
//     private static void renderHazeLayer(PoseStack poseStack, MultiBufferSource bufferSource, MirageBlock mirage, float offset, float alpha, float partialTick) {
//         VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
//         Matrix4f matrix = poseStack.last().pose();
        
//         // Create shimmering air effect around the mirage
//         float time = mirage.hazePhase + mirage.hazeOffset;
//         int segments = 16;
        
//         for (int i = 0; i < segments; i++) {
//             float angle1 = (float)(i * 2 * Math.PI / segments);
//             float angle2 = (float)((i + 1) * 2 * Math.PI / segments);
            
//             // Create wavering effect
//             float waveOffset1 = 0.1f * (float)Math.sin(time * 2 + angle1 * 3);
//             float waveOffset2 = 0.1f * (float)Math.sin(time * 2 + angle2 * 3);
            
//             float radius = 1.0f + offset;
//             float height = 2.0f;
            
//             // Calculate positions with wave distortion
//             float x1 = (float)(Math.cos(angle1) * (radius + waveOffset1));
//             float z1 = (float)(Math.sin(angle1) * (radius + waveOffset1));
//             float x2 = (float)(Math.cos(angle2) * (radius + waveOffset2));
//             float z2 = (float)(Math.sin(angle2) * (radius + waveOffset2));
            
//             // Render vertical strips of distorted air
//             float r = 0.9f + 0.1f * (float)Math.sin(time + angle1);
//             float g = 0.95f + 0.05f * (float)Math.cos(time * 1.3f + angle1);
//             float b = 1.0f;
            
//             // Bottom vertices
//             consumer.vertex(matrix, x1, 0, z1).color(r, g, b, alpha * 0.3f).uv(0, 1).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
//             consumer.vertex(matrix, x2, 0, z2).color(r, g, b, alpha * 0.3f).uv(1, 1).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
            
//             // Top vertices with more distortion
//             float topWave1 = 0.15f * (float)Math.sin(time * 3 + angle1 * 2);
//             float topWave2 = 0.15f * (float)Math.sin(time * 3 + angle2 * 2);
            
//             consumer.vertex(matrix, x2 + topWave2, height, z2 + topWave2).color(r, g, b, alpha * 0.1f).uv(1, 0).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
//             consumer.vertex(matrix, x1 + topWave1, height, z1 + topWave1).color(r, g, b, alpha * 0.1f).uv(0, 0).overlayCoords(0).uv2(0xF000F0).normal(0, 1, 0).endVertex();
//         }
//     }
    
//     // Custom vertex consumer that applies alpha blending
//     private static class AlphaVertexConsumer implements VertexConsumer {
//         private final VertexConsumer delegate;
//         private final float alpha;
        
//         public AlphaVertexConsumer(VertexConsumer delegate, float alpha) {
//             this.delegate = delegate;
//             this.alpha = alpha;
//         }
        
//         @Override
//         public VertexConsumer vertex(double x, double y, double z) {
//             return delegate.vertex(x, y, z);
//         }
        
//         @Override
//         public VertexConsumer color(int red, int green, int blue, int alpha) {
//             return delegate.color(red, green, blue, (int)(alpha * this.alpha));
//         }
        
//         @Override
//         public VertexConsumer color(float red, float green, float blue, float alpha) {
//             return delegate.color(red, green, blue, alpha * this.alpha);
//         }
        
//         @Override
//         public VertexConsumer uv(float u, float v) {
//             return delegate.uv(u, v);
//         }
        
//         @Override
//         public VertexConsumer overlayCoords(int u, int v) {
//             return delegate.overlayCoords(u, v);
//         }
        
//         @Override
//         public VertexConsumer uv2(int u, int v) {
//             return delegate.uv2(u, v);
//         }
        
//         @Override
//         public VertexConsumer normal(float x, float y, float z) {
//             return delegate.normal(x, y, z);
//         }
        
//         @Override
//         public void endVertex() {
//             delegate.endVertex();
//         }
        
//         @Override
//         public void defaultColor(int red, int green, int blue, int alpha) {
//             delegate.defaultColor(red, green, blue, (int)(alpha * this.alpha));
//         }
        
//         @Override
//         public void unsetDefaultColor() {
//             delegate.unsetDefaultColor();
//         }
//     }
    
//     public static void clearAllMirages() {
//         MIRAGE_BLOCKS.clear();
//     }
// }
