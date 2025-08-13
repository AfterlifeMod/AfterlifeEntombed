package com.dracolich777.afterlifeentombed.items;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.dracolich777.afterlifeentombed.init.ModEffects;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth; // Import Mth for utility functions like wrapDegrees
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SethsTrickery extends SwordItem {

    public SethsTrickery(Item.Properties properties) {
        super(Tiers.NETHERITE, 4 - 1, -2.4F, properties); // 8 damage
    }

    @Override
public boolean canBeDepleted() {
    return false;
}

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(ModEffects.REVENGE_OF_SETH.get(), 100, 0)); // 5 seconds, amplifier 0
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            Optional<LivingEntity> targetEntity = getEntityPlayerIsLookingAt(player, level, 20.0);

            if (targetEntity.isPresent()) {
                LivingEntity target = targetEntity.get();

                // Use the entity's head yaw (which should update when they look around)
                float entityYaw = target.yHeadRot;
                
                // Convert yaw to direction vector (Minecraft yaw: 0 = north, 90 = east, 180 = south, 270 = west)
                double yawRadians = Math.toRadians(entityYaw);
                double dirX = -Math.sin(yawRadians);
                double dirZ = Math.cos(yawRadians);
                Vec3 entityFacingDirection = new Vec3(dirX, 0, dirZ).normalize();

                double distanceBehind = 1.5;

                // Position behind the entity based on their actual yaw
                Vec3 teleportPos = target.position().subtract(entityFacingDirection.scale(distanceBehind));

                // Match entity's vertical level (target's feet)
                teleportPos = new Vec3(teleportPos.x, target.getY(), teleportPos.z);

                // Teleport player first
                player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);

                // Now calculate facing direction from the player's new position
                Vec3 targetCenter = target.position().add(0, target.getBbHeight() / 2, 0); // Target center
                Vec3 playerEye = player.getEyePosition(); // Player's actual eye position after teleport
                Vec3 lookDirection = targetCenter.subtract(playerEye).normalize();
                
                // Convert look direction to yaw and pitch
                float yaw = (float) Math.toDegrees(Math.atan2(-lookDirection.x, lookDirection.z));
                float pitch = (float) Math.toDegrees(Math.asin(-lookDirection.y));
                
                player.setYRot(yaw);
                player.setXRot(pitch);

                // Cooldown to prevent spamming
                player.getCooldowns().addCooldown(this, 60);

                return InteractionResultHolder.success(player.getItemInHand(hand));
            }
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }
    
    /**
     * Helper method to find the LivingEntity the player is looking at.
     * (No changes here, this method was already robust)
     */
    private Optional<LivingEntity> getEntityPlayerIsLookingAt(Player player, Level level, double range) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.scale(range));
        
        AABB searchBox = new AABB(eyePos, endPos).inflate(1.0); 
        
        LivingEntity closestEntity = null;
        double closestDistance = range + 1;
        
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchBox);
        
        for (LivingEntity entity : entities) {
            if (entity == player) continue;
            
            AABB entityBox = entity.getBoundingBox().inflate(0.3); 
            
            Optional<Vec3> hitVec = entityBox.clip(eyePos, endPos);
            
            if (hitVec.isPresent()) {
                double distance = eyePos.distanceTo(hitVec.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }
        
        return Optional.ofNullable(closestEntity);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.afterlifeentombed.seths_trickery.tooltip1")
                .withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("tooltip.afterlifeentombed.seths_trickery.tooltip2")
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.afterlifeentombed.seths_trickery.tooltip3")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}