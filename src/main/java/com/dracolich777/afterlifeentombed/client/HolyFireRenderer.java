// package com.dracolich777.afterlifeentombed.client;

// import com.mojang.blaze3d.vertex.PoseStack;
// import com.mojang.blaze3d.vertex.VertexConsumer;
// import net.minecraft.client.renderer.MultiBufferSource;
// import net.minecraft.client.renderer.RenderType;
// import net.minecraft.client.renderer.texture.OverlayTexture;
// import net.minecraft.resources.ResourceLocation;
// import net.minecraft.util.Mth;
// import net.minecraft.world.entity.LivingEntity;
// import net.minecraftforge.api.distmarker.Dist;
// import net.minecraftforge.client.event.RenderLivingEvent;
// import net.minecraftforge.eventbus.api.SubscribeEvent;
// import net.minecraftforge.fml.common.Mod;

// @Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
// public class HolyFireRenderer {

//     // Your custom holy fire textures. These should be fire-like textures.
//     // Ensure these textures are located in src/main/resources/assets/afterlifeentombed/textures/gui/
//     private static final ResourceLocation CUSTOM_FIRE_TEXTURE_0 = new ResourceLocation("afterlifeentombed", "textures/gui/fire_0.png");
//     private static final ResourceLocation CUSTOM_FIRE_TEXTURE_1 = new ResourceLocation("afterlifeentombed", "textures/gui/fire_1.png");

//     /**
//      * This event is fired after a living entity and its layers (including vanilla fire) have been rendered.
//      * We use RenderLivingEvent.Post to add our custom fire overlay.
//      * Note: This will render your custom fire *on top of* the vanilla fire, as directly
//      * replacing vanilla fire without affecting the main model render is complex with events alone.
//      *
//      * @param event The RenderLivingEvent.Post event instance.
//      */
//     @SubscribeEvent
//     public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) { // Changed from Pre to Post
//         LivingEntity entity = event.getEntity();

//         // Only render if the entity is currently on fire (using the vanilla 'onFire' status)
//         if (entity.isOnFire()) {
//             // We do NOT call event.setCanceled(true) here, as we want the player model
//             // and vanilla fire to render first, and then we add our custom fire.
//             renderCustomFireOverlay(event.getPoseStack(), event.getMultiBufferSource(),
//                                     event.getPackedLight(), entity);
//         }
//     }

//     /**
//      * Renders a custom fire overlay on the given living entity.
//      * This method draws two animated, rotating quads with custom textures around the entity.
//      * The transformations are adjusted to ensure the fire is correctly positioned, scaled, and centered.
//      *
//      * @param poseStack The current PoseStack for applying transformations.
//      * @param buffer The MultiBufferSource to get the VertexConsumer for rendering.
//      * @param packedLight The packed light value for lighting the rendered fire.
//      * @param entity The LivingEntity on which the fire is to be rendered.
//      */
//     private static void renderCustomFireOverlay(PoseStack poseStack, MultiBufferSource buffer,
//                                                int packedLight, LivingEntity entity) {

//         // Determine which custom fire texture to use based on game ticks for a simple animation.
//         // This alternates between CUSTOM_FIRE_TEXTURE_0 and CUSTOM_FIRE_TEXTURE_1 every 2 ticks.
//         ResourceLocation texture = (entity.tickCount / 2) % 2 == 0
//             ? CUSTOM_FIRE_TEXTURE_0 : CUSTOM_FIRE_TEXTURE_1;

//         poseStack.pushPose(); // Save the current transformation state before applying new ones.

//         float entityHeight = entity.getBbHeight();
//         float scaleFactor = entityHeight * 1.4F; // Overall scale for the fire effect

//         // --- Transformation Order for Correct Fire Appearance ---
//         // 1. Vertical offset: Lift the fire slightly above the entity's feet.
//         //    The entity's origin (0,0,0) in its local space is typically at its feet, horizontally centered.
//         //    A small positive Y translation lifts the base of the fire.
//         poseStack.translate(0.0F, 0.1F, 0.0F); // Lifts the fire by 0.1 blocks from the entity's base

//         // 2. Apply rotation: Rotate the fire around the Y-axis (vertical axis of the entity).
//         //    This rotation is applied to the coordinate system after the vertical offset.
//         float rotation = (entity.tickCount + Mth.lerp(1.0F, entity.yRotO, entity.getYRot())) * 2.0F;
//         poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-rotation));

//         // 3. Apply scaling: Scale the entire coordinate system.
//         //    This makes the fire larger or smaller based on the entity's height.
//         poseStack.scale(scaleFactor, scaleFactor, scaleFactor);

//         // 4. Horizontal centering: Translate the quad's local origin to be centered horizontally.
//         //    Since `renderFireQuad` draws a quad from (0,0) to (1,1) in its local UV space,
//         //    translating by -0.5 on X and Z centers it within the scaled space.
//         //    The Y=0 here refers to the base of the *scaled* fire, which is already lifted by step 1.
//         poseStack.translate(-0.5F, 0.0F, -0.5F);

//         // Get the VertexConsumer for rendering.
//         // RenderType.entityCutoutNoCull is suitable for fire textures as they are typically
//         // transparent and should not be culled (i.e., visible from all angles).
//         VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));

//         PoseStack.Pose pose = poseStack.last(); // Get the current transformation matrix from the PoseStack.

//         // Render the first fire quad.
//         // The parameters define the local coordinates (x0, y0, x1, y1, y2) of the quad.
//         // Here, it creates a quad from (0,0) to (1,1) in UV space, mapped to the transformed 3D space.
//         renderFireQuad(pose, vertexConsumer, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, packedLight);

//         // Render a second fire layer, rotated 45 degrees relative to the first.
//         // This creates a fuller, more volumetric fire effect around the entity.
//         poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(45.0F));
//         pose = poseStack.last(); // Get the updated transformation matrix for the second quad.
//         renderFireQuad(pose, vertexConsumer, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, packedLight);

//         poseStack.popPose(); // Restore the previous transformation state.
//     }

//     /**
//      * Renders a single quad (rectangle) for the fire effect.
//      * This method defines the vertices of a textured quad in 3D space, applying
//      * the current transformations from the PoseStack.
//      *
//      * @param pose The current transformation matrix (obtained from PoseStack.last()).
//      * @param consumer The VertexConsumer to add the quad's vertices to.
//      * @param x0 The local x-coordinate of the bottom-left corner of the quad.
//      * @param y0 The local y-coordinate of the bottom-left corner of the quad.
//      * @param x1 The local x-coordinate of the top-right corner of the quad.
//      * @param y1 The local y-coordinate of the top-right corner of the quad (often used for height).
//      * @param y2 The local y-coordinate of the top-right corner of the quad (can be same as y1).
//      * @param packedLight The packed light value for lighting calculations.
//      */
//     private static void renderFireQuad(PoseStack.Pose pose, VertexConsumer consumer,
//                                        float x0, float y0, float x1, float y1, float y2, int packedLight) {
//         // The UV coordinates (0.0F, 0.0F) to (1.0F, 1.0F) map the entire texture onto this quad.
//         // OverlayTexture.NO_OVERLAY indicates no special overlay (e.g., hurt tint).
//         // The normal (0.0F, 1.0F, 0.0F) indicates the quad is oriented upwards along the Y-axis.
//         // The color (255, 255, 255, 255) means full white, allowing the texture's colors to show through.

//         // Vertices are defined in a counter-clockwise order for proper rendering and culling.
//         // Top-right vertex
//         consumer.vertex(pose.pose(), x1, y2, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
//         // Top-left vertex
//         consumer.vertex(pose.pose(), x0, y2, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
//         // Bottom-left vertex
//         consumer.vertex(pose.pose(), x0, y0, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
//         // Bottom-right vertex
//         consumer.vertex(pose.pose(), x1, y0, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
//     }
// }
