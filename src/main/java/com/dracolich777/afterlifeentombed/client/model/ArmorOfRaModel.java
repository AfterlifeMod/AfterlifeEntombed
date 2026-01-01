package com.dracolich777.afterlifeentombed.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public class ArmorOfRaModel extends HumanoidModel<LivingEntity> {
    
    public ArmorOfRaModel(ModelPart root) {
        super(root);
    }
    
    public static LayerDefinition createArmorLayer() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshDefinition.getRoot();
        
        // HEAD - Pharaonic headdress with sun disk
        PartDefinition head = partDefinition.getChild("head");
        
        // Nemes headdress base
        head.addOrReplaceChild("headdress_front", 
            CubeListBuilder.create()
                .texOffs(0, 32).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.3F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Side flaps of headdress
        head.addOrReplaceChild("headdress_left", 
            CubeListBuilder.create()
                .texOffs(36, 32).addBox(3.5F, -1.0F, -2.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        head.addOrReplaceChild("headdress_right", 
            CubeListBuilder.create()
                .texOffs(36, 32).addBox(-5.5F, -1.0F, -2.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Sun disk on top
        head.addOrReplaceChild("sun_disk", 
            CubeListBuilder.create()
                .texOffs(48, 32).addBox(-2.0F, -12.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Uraeus (cobra) on forehead
        head.addOrReplaceChild("uraeus", 
            CubeListBuilder.create()
                .texOffs(48, 44).addBox(-0.5F, -9.5F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // BODY - Golden chest plate with solar motifs
        PartDefinition body = partDefinition.getChild("body");
        
        // Main chest plate (pectoral)
        body.addOrReplaceChild("chest_pectoral", 
            CubeListBuilder.create()
                .texOffs(0, 16).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 6.0F, 1.0F, new CubeDeformation(0.6F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Solar disk centerpiece
        body.addOrReplaceChild("chest_sun", 
            CubeListBuilder.create()
                .texOffs(24, 16).addBox(-1.5F, 2.0F, -3.8F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Back plate
        body.addOrReplaceChild("back_plate", 
            CubeListBuilder.create()
                .texOffs(0, 23).addBox(-4.0F, 0.0F, 2.0F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.4F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Waist belt/girdle
        body.addOrReplaceChild("waist_belt", 
            CubeListBuilder.create()
                .texOffs(32, 16).addBox(-4.5F, 10.0F, -2.5F, 9.0F, 2.0F, 5.0F, new CubeDeformation(0.3F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // ARMS - Ornate bracers with wing motifs
        PartDefinition leftArm = partDefinition.getChild("left_arm");
        
        // Left shoulder pauldron (larger, more ornate)
        leftArm.addOrReplaceChild("left_pauldron", 
            CubeListBuilder.create()
                .texOffs(0, 50).addBox(-1.5F, -3.0F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.5F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Left upper arm band
        leftArm.addOrReplaceChild("left_armband", 
            CubeListBuilder.create()
                .texOffs(22, 50).addBox(-1.0F, 2.0F, -2.5F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.4F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        PartDefinition rightArm = partDefinition.getChild("right_arm");
        
        // Right shoulder pauldron
        rightArm.addOrReplaceChild("right_pauldron", 
            CubeListBuilder.create()
                .texOffs(0, 50).addBox(-3.5F, -3.0F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.5F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Right upper arm band
        rightArm.addOrReplaceChild("right_armband", 
            CubeListBuilder.create()
                .texOffs(22, 50).addBox(-3.0F, 2.0F, -2.5F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.4F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // LEGS - Golden greaves and shin guards
        PartDefinition leftLeg = partDefinition.getChild("left_leg");
        
        // Left thigh armor
        leftLeg.addOrReplaceChild("left_thigh", 
            CubeListBuilder.create()
                .texOffs(40, 50).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.3F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Left shin guard
        leftLeg.addOrReplaceChild("left_shin", 
            CubeListBuilder.create()
                .texOffs(40, 61).addBox(-2.3F, 6.0F, -2.8F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        PartDefinition rightLeg = partDefinition.getChild("right_leg");
        
        // Right thigh armor
        rightLeg.addOrReplaceChild("right_thigh", 
            CubeListBuilder.create()
                .texOffs(40, 50).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.3F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Right shin guard
        rightLeg.addOrReplaceChild("right_shin", 
            CubeListBuilder.create()
                .texOffs(40, 61).addBox(-1.7F, 6.0F, -2.8F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}