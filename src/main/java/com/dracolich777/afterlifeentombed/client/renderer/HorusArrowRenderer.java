package com.dracolich777.afterlifeentombed.client.renderer;

import com.dracolich777.afterlifeentombed.mobs.HorusArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class HorusArrowRenderer extends ArrowRenderer<HorusArrowEntity> {

    public static final ResourceLocation ARROW_LOCATION = new ResourceLocation("textures/entity/projectiles/arrow.png");

    public HorusArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HorusArrowEntity arrow) {
        return ARROW_LOCATION;
    }
}
