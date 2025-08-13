package com.dracolich777.afterlifeentombed.client.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public class ModModelLayers {
    public static final ModelLayerLocation ARMOR_OF_RA_HELMET = 
        new ModelLayerLocation(new ResourceLocation("afterlifeentombed", "armor_of_ra"), "helmet");
    public static final ModelLayerLocation ARMOR_OF_RA_CHESTPLATE = 
        new ModelLayerLocation(new ResourceLocation("afterlifeentombed", "armor_of_ra"), "chestplate");
    public static final ModelLayerLocation ARMOR_OF_RA_LEGGINGS = 
        new ModelLayerLocation(new ResourceLocation("afterlifeentombed", "armor_of_ra"), "leggings");
    public static final ModelLayerLocation ARMOR_OF_RA_BOOTS = 
        new ModelLayerLocation(new ResourceLocation("afterlifeentombed", "armor_of_ra"), "boots");
    
    public static ModelLayerLocation getArmorLayer(EquipmentSlot slot) {
        switch (slot) {
            case HEAD:
                return ARMOR_OF_RA_HELMET;
            case CHEST:
                return ARMOR_OF_RA_CHESTPLATE;
            case LEGS:
                return ARMOR_OF_RA_LEGGINGS;
            case FEET:
                return ARMOR_OF_RA_BOOTS;
            default:
                return ARMOR_OF_RA_HELMET;
        }
    }
}
