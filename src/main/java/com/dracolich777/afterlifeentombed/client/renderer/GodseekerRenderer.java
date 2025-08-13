package com.dracolich777.afterlifeentombed.client.renderer;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.mobs.GodseekerEntity;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

import net.minecraft.resources.ResourceLocation;

public class GodseekerRenderer extends MobRenderer<GodseekerEntity, VillagerModel<GodseekerEntity>> {

    private static final ResourceLocation GODSEEKER_TEXTURE =
            new ResourceLocation(AfterlifeEntombedMod.MOD_ID, "textures/entity/godseeker.png");

    public GodseekerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);

        this.addLayer(new CrossedArmsItemLayer<>(
                this,
                context.getItemInHandRenderer()
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(GodseekerEntity entity) {
        return GODSEEKER_TEXTURE;
    }
}
