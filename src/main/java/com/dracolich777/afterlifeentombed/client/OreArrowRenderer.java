package com.dracolich777.afterlifeentombed.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

/**
 * Renders 3D arrows in world space pointing to ore locations for Geb's Excavation ability
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class OreArrowRenderer {
    
    private static final Set<BlockPos> ORE_POSITIONS = new HashSet<>();
    private static final int MAX_ARROWS = 5; // Show arrows to 5 nearest ores
    private static final float ARROW_DISTANCE = 3.0f; // Distance from player to arrow
    private static final float ARROW_SIZE = 0.5f;
    
    /**
     * Update the ore positions to display arrows for
     */
    public static void setOrePositions(Set<BlockPos> ores) {
        ORE_POSITIONS.clear();
        if (ores != null) {
            ORE_POSITIONS.addAll(ores);
        }
    }
    
    /**
     * Clear all ore arrows
     */
    public static void clearArrows() {
        ORE_POSITIONS.clear();
    }
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            renderOreArrows(event.getPoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(), event.getPartialTick());
        }
    }
    
    private static void renderOreArrows(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        
        if (player == null || ORE_POSITIONS.isEmpty()) return;
        
        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
        BlockPos playerPos = player.blockPosition();
        
        // Find the nearest ores
        List<BlockPos> nearestOres = ORE_POSITIONS.stream()
            .filter(pos -> player.level().getBlockState(pos).is(net.minecraft.tags.BlockTags.create(
                new net.minecraft.resources.ResourceLocation("forge", "ores"))))
            .sorted(Comparator.comparingDouble(pos -> pos.distSqr(playerPos)))
            .limit(MAX_ARROWS)
            .toList();
        
        // Render arrow for each nearest ore
        for (BlockPos orePos : nearestOres) {
            renderArrowToOre(poseStack, bufferSource, cameraPos, player, orePos, partialTick);
        }
    }
    
    private static void renderArrowToOre(PoseStack poseStack, MultiBufferSource bufferSource, 
                                         Vec3 cameraPos, LocalPlayer player, BlockPos orePos, float partialTick) {
        Vec3 playerEyePos = player.getEyePosition(partialTick);
        Vec3 oreCenter = Vec3.atCenterOf(orePos);
        Vec3 directionToOre = oreCenter.subtract(playerEyePos).normalize();
        
        // Position arrow at fixed distance from player in direction of ore
        Vec3 arrowPos = playerEyePos.add(directionToOre.scale(ARROW_DISTANCE));
        
        // Convert to render coordinates (relative to camera)
        double renderX = arrowPos.x - cameraPos.x;
        double renderY = arrowPos.y - cameraPos.y;
        double renderZ = arrowPos.z - cameraPos.z;
        
        poseStack.pushPose();
        poseStack.translate(renderX, renderY, renderZ);
        
        // Calculate rotation to point at ore
        double dx = oreCenter.x - arrowPos.x;
        double dy = oreCenter.y - arrowPos.y;
        double dz = oreCenter.z - arrowPos.z;
        
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)Math.atan2(dx, dz);
        float pitch = (float)Math.atan2(-dy, horizontalDistance);
        
        // Apply rotation
        org.joml.Quaternionf yawRotation = new org.joml.Quaternionf().rotationY((float)(-yaw));
        org.joml.Quaternionf pitchRotation = new org.joml.Quaternionf().rotationX((float)(pitch));
        poseStack.mulPose(yawRotation);
        poseStack.mulPose(pitchRotation);
        
        // Add bobbing animation
        float time = (player.level().getGameTime() + partialTick) * 0.1f;
        float bob = (float)Math.sin(time) * 0.1f;
        poseStack.translate(0, bob, 0);
        
        // Render the arrow
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();
        
        // Draw arrow shaft (line pointing forward)
        float shaftLength = ARROW_SIZE;
        addColoredLine(consumer, matrix, 0, 0, 0, 0, 0, shaftLength, 1.0f, 0.8f, 0.0f, 1.0f);
        
        // Draw arrow head (cone shape with lines)
        float headSize = ARROW_SIZE * 0.3f;
        float headStart = shaftLength;
        
        // Arrow head - 4 lines forming a pyramid
        addColoredLine(consumer, matrix, 0, headSize, headStart, 0, 0, shaftLength, 1.0f, 0.8f, 0.0f, 1.0f);
        addColoredLine(consumer, matrix, 0, -headSize, headStart, 0, 0, shaftLength, 1.0f, 0.8f, 0.0f, 1.0f);
        addColoredLine(consumer, matrix, headSize, 0, headStart, 0, 0, shaftLength, 1.0f, 0.8f, 0.0f, 1.0f);
        addColoredLine(consumer, matrix, -headSize, 0, headStart, 0, 0, shaftLength, 1.0f, 0.8f, 0.0f, 1.0f);
        
        // Connect arrow head points
        addColoredLine(consumer, matrix, 0, headSize, headStart, headSize, 0, headStart, 1.0f, 0.8f, 0.0f, 1.0f);
        addColoredLine(consumer, matrix, headSize, 0, headStart, 0, -headSize, headStart, 1.0f, 0.8f, 0.0f, 1.0f);
        addColoredLine(consumer, matrix, 0, -headSize, headStart, -headSize, 0, headStart, 1.0f, 0.8f, 0.0f, 1.0f);
        addColoredLine(consumer, matrix, -headSize, 0, headStart, 0, headSize, headStart, 1.0f, 0.8f, 0.0f, 1.0f);
        
        poseStack.popPose();
    }
    
    private static void addColoredLine(VertexConsumer consumer, Matrix4f matrix, 
                                       float x1, float y1, float z1, 
                                       float x2, float y2, float z2,
                                       float r, float g, float b, float a) {
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(0, 1, 0).endVertex();
    }
}
