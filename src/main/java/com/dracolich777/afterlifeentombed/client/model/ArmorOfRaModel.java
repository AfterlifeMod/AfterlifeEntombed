package com.dracolich777.afterlifeentombed.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class ArmorOfRaModel extends HumanoidModel<LivingEntity> {
    
    public ArmorOfRaModel(ModelPart root) {
        super(root);
    }
    
    public static LayerDefinition createArmorLayer() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshDefinition.getRoot();
        
        // Start with basic armor and add custom elements
        PartDefinition body = partDefinition.getChild("body");
        
        // Add custom chest armor elements
        body.addOrReplaceChild("chest_main", 
            CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.5F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Add shoulder pads to arms
        PartDefinition leftArm = partDefinition.getChild("left_arm");
        leftArm.addOrReplaceChild("left_shoulder", 
            CubeListBuilder.create()
                .texOffs(24, 22).addBox(2.0F, -6.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        PartDefinition rightArm = partDefinition.getChild("right_arm");
        rightArm.addOrReplaceChild("right_shoulder", 
            CubeListBuilder.create()
                .texOffs(24, 22).addBox(-4.0F, -6.0F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}