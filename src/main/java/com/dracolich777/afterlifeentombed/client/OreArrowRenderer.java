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
    
    private static final Map<BlockPos, String> ORE_DATA = new HashMap<>();
    private static final int MAX_ARROWS = 10; // Show arrows to 10 nearest ores
    private static final float ARROW_DISTANCE = 2.5f; // Distance from player to arrow
    private static final float ARROW_SIZE = 0.4f;
    
    /**
     * Update the ore data to display arrows for (with colors)
     */
    public static void setOreData(Map<BlockPos, String> oreData) {
        ORE_DATA.clear();
        if (oreData != null) {
            ORE_DATA.putAll(oreData);
        }
    }
    
    /**
     * Clear all ore arrows
     */
    public static void clearArrows() {
        ORE_DATA.clear();
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
        
        if (player == null || ORE_DATA.isEmpty()) return;
        
        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
        BlockPos playerPos = player.blockPosition();
        
        // Find the nearest ores
        List<Map.Entry<BlockPos, String>> nearestOres = ORE_DATA.entrySet().stream()
            .sorted(Comparator.comparingDouble(entry -> entry.getKey().distSqr(playerPos)))
            .limit(MAX_ARROWS)
            .toList();
        
        // Render arrow for each nearest ore
        for (Map.Entry<BlockPos, String> entry : nearestOres) {
            renderArrowToOre(poseStack, bufferSource, cameraPos, player, entry.getKey(), entry.getValue(), partialTick);
        }
    }
    
    private static void renderArrowToOre(PoseStack poseStack, MultiBufferSource bufferSource, 
                                         Vec3 cameraPos, LocalPlayer player, BlockPos orePos, String oreId, float partialTick) {
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
        
        // Calculate rotation to point toward ore more accurately
        // Use the actual direction from arrow to ore
        double dx = oreCenter.x - arrowPos.x;
        double dy = oreCenter.y - arrowPos.y;
        double dz = oreCenter.z - arrowPos.z;
        
        // Normalize the direction vector
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length > 0.001) {
            dx /= length;
            dy /= length;
            dz /= length;
        }
        
        // Calculate yaw (rotation around Y axis)
        float yaw = (float)Math.atan2(-dx, dz);
        
        // Calculate pitch (rotation around X axis) - negate for proper up/down pointing
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float pitch = (float)Math.atan2(dy, horizontalDistance);
        
        // Apply rotations in correct order: yaw first, then pitch
        poseStack.mulPose(new org.joml.Quaternionf().rotationY(yaw));
        poseStack.mulPose(new org.joml.Quaternionf().rotationX(-pitch));
        
        // Add bobbing animation
        float time = (player.level().getGameTime() + partialTick) * 0.15f;
        float bob = (float)Math.sin(time) * 0.08f;
        poseStack.translate(0, bob, 0);
        
        // Get color for this ore type
        Vector3f color = getOreColor(oreId);
        
        // Render the arrow
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();
        
        // Draw arrow shaft (line pointing forward in +Z direction)
        float shaftLength = ARROW_SIZE;
        addColoredLine(consumer, matrix, 0, 0, 0, 0, 0, shaftLength, color.x, color.y, color.z, 1.0f);
        
        // Draw arrow head (cone shape with lines)
        float headSize = ARROW_SIZE * 0.35f;
        float headBase = shaftLength * 0.7f; // Head starts 70% along shaft
        
        // Arrow head - 4 lines forming a pyramid pointing forward
        addColoredLine(consumer, matrix, 0, headSize, headBase, 0, 0, shaftLength, color.x, color.y, color.z, 1.0f);
        addColoredLine(consumer, matrix, 0, -headSize, headBase, 0, 0, shaftLength, color.x, color.y, color.z, 1.0f);
        addColoredLine(consumer, matrix, headSize, 0, headBase, 0, 0, shaftLength, color.x, color.y, color.z, 1.0f);
        addColoredLine(consumer, matrix, -headSize, 0, headBase, 0, 0, shaftLength, color.x, color.y, color.z, 1.0f);
        
        // Connect arrow head base points (cross pattern)
        addColoredLine(consumer, matrix, 0, headSize, headBase, headSize, 0, headBase, color.x, color.y, color.z, 1.0f);
        addColoredLine(consumer, matrix, headSize, 0, headBase, 0, -headSize, headBase, color.x, color.y, color.z, 1.0f);
        addColoredLine(consumer, matrix, 0, -headSize, headBase, -headSize, 0, headBase, color.x, color.y, color.z, 1.0f);
        addColoredLine(consumer, matrix, -headSize, 0, headBase, 0, headSize, headBase, color.x, color.y, color.z, 1.0f);
        
        poseStack.popPose();
    }
    
    /**
     * Get color based on ore type
     */
    private static Vector3f getOreColor(String oreId) {
        String oreLower = oreId.toLowerCase();
        
        // Coal - dark gray/black
        if (oreLower.contains("coal")) {
            return new Vector3f(0.2f, 0.2f, 0.2f);
        }
        // Iron - light gray/tan
        else if (oreLower.contains("iron")) {
            return new Vector3f(0.8f, 0.7f, 0.6f);
        }
        // Copper - orange/copper
        else if (oreLower.contains("copper")) {
            return new Vector3f(0.9f, 0.5f, 0.3f);
        }
        // Gold - bright yellow/gold
        else if (oreLower.contains("gold")) {
            return new Vector3f(1.0f, 0.85f, 0.0f);
        }
        // Lapis - deep blue
        else if (oreLower.contains("lapis")) {
            return new Vector3f(0.2f, 0.4f, 0.9f);
        }
        // Redstone - bright red
        else if (oreLower.contains("redstone")) {
            return new Vector3f(1.0f, 0.0f, 0.0f);
        }
        // Diamond - cyan/light blue
        else if (oreLower.contains("diamond")) {
            return new Vector3f(0.3f, 0.9f, 0.9f);
        }
        // Emerald - bright green
        else if (oreLower.contains("emerald")) {
            return new Vector3f(0.2f, 1.0f, 0.3f);
        }
        // Quartz - white
        else if (oreLower.contains("quartz")) {
            return new Vector3f(1.0f, 1.0f, 1.0f);
        }
        // Ancient Debris - dark purple/brown
        else if (oreLower.contains("debris") || oreLower.contains("ancient")) {
            return new Vector3f(0.4f, 0.2f, 0.3f);
        }
        // Default - golden color for unknown ores
        else {
            return new Vector3f(1.0f, 0.8f, 0.2f);
        }
    }
    
    private static void addColoredLine(VertexConsumer consumer, Matrix4f matrix, 
                                       float x1, float y1, float z1, 
                                       float x2, float y2, float z2,
                                       float r, float g, float b, float a) {
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(0, 1, 0).endVertex();
    }
}
