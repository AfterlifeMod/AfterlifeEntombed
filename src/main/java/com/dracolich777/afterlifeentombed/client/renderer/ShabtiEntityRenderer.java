package com.dracolich777.afterlifeentombed.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.dracolich777.afterlifeentombed.mobs.ShabtiEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ShabtiEntityRenderer extends LivingEntityRenderer<ShabtiEntity, HumanoidModel<ShabtiEntity>> {

    public ShabtiEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);

        // Add armor layer for rendering armor
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));

        // Add item in hand layer for rendering held items
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public void render(ShabtiEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Make the entity visible
        this.model.setAllVisible(true);

        // Set the rotation to match the owner if rotation display is enabled
        if (entity.hasDisplayFlag(ShabtiEntity.SHOW_ROTATION)) {
            entity.setYRot(entity.getOwnerYaw());
            entity.setXRot(entity.getOwnerPitch());
            entity.setYHeadRot(entity.getOwnerYawHead());
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        // Render status information above the entity if any status flags are enabled
        String statusInfo = entity.getOwnerStatusInfo();
        if (!statusInfo.isEmpty()) {
            renderStatusText(entity, statusInfo, poseStack, buffer, packedLight);
        }
    }

    private void renderStatusText(ShabtiEntity entity, String statusInfo, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Position the text above the entity
        poseStack.translate(0.0D, entity.getBbHeight() + 0.8D, 0.0D);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        String[] lines = statusInfo.split("\n");
        int lineHeight = 10;
        int totalHeight = lines.length * lineHeight;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int yOffset = (i * lineHeight) - (totalHeight / 2);

            // Create a semi-transparent background
            int textWidth = this.getFont().width(line);
            this.getFont().drawInBatch(line, -textWidth / 2.0F, yOffset, 0xFFFFFF, false,
                    poseStack.last().pose(), buffer, net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0x40000000, packedLight);
        }

        poseStack.popPose();
    }

    @Override
    protected RenderType getRenderType(ShabtiEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        ResourceLocation textureLocation = getTextureLocation(entity);
        if (translucent) {
            return RenderType.entityTranslucent(textureLocation);
        } else if (bodyVisible) {
            // Make the entity slightly translucent to give it a ghostly appearance
            return RenderType.entityTranslucent(textureLocation);
        } else {
            return null;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ShabtiEntity entity) {
        UUID ownerUUID = entity.getOwnerUUID();
        if (ownerUUID != null) {
            Player owner = Minecraft.getInstance().level.getPlayerByUUID(ownerUUID);
            if (owner instanceof AbstractClientPlayer clientPlayer) {
                return clientPlayer.getSkinTextureLocation();
            }
        }
        return DefaultPlayerSkin.getDefaultSkin();
    }
}
