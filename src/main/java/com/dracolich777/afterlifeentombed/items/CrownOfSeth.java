package com.dracolich777.afterlifeentombed.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;

import javax.annotation.Nullable;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.util.ParticleManager;

import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.SlotContext; 
import top.theillusivec4.curios.api.type.capability.ICurioItem; 
import net.minecraft.sounds.SoundEvents; 
import net.minecraft.sounds.SoundSource;

public class CrownOfSeth extends Item implements ICurioItem {

    public CrownOfSeth(Properties properties) {
        super(properties);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Level level = entity.level();
        
        if (!level.isClientSide()) {
            CompoundTag entityData = entity.getPersistentData();
            
            // Check if invisibility is pending and enough time has passed
            if (entityData.getBoolean("crown_invisibility_pending")) {
                long equipTime = entityData.getLong("crown_equip_time");
                long currentTime = level.getGameTime();
                
                // Apply invisibility after 60 ticks (3 seconds) to allow particle effect to show
                if (currentTime - equipTime >= 60) {
                    entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 
                        Integer.MAX_VALUE, 0, false, false, true));
                    entityData.putBoolean("crown_invisibility_pending", false);
                    AfterlifeEntombedMod.LOGGER.info("Crown invisibility applied after dissolve delay for: {}", entity);
                }
            }
            
            // Refresh effects every 20 ticks (1 second) to ensure they stay active
            if (level.getGameTime() % 20 == 0) {
                // Only refresh invisibility if it's not pending (meaning delay has already passed and it was applied)
                if (!entityData.getBoolean("crown_invisibility_pending")) {
                    // Only add invisibility if it's not already active
                    if (!entity.hasEffect(MobEffects.INVISIBILITY)) {
                        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 
                            Integer.MAX_VALUE, 0, false, false, true));
                    }
                }
                // Always refresh bad omen effect
                if (!entity.hasEffect(MobEffects.BAD_OMEN)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 
                        80, 0, false, false, true));
                }
            }
        }
    }
    
    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }
    
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Level level = entity.level();
        
        AfterlifeEntombedMod.LOGGER.info("CrownOfSeth onEquip called! Entity: {}, ClientSide: {}", entity, level.isClientSide());
        
        // Play sound effect immediately (works on both sides)
        level.playSound(null, entity.blockPosition(), 
            SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 0.5f, 1.5f);
        
        // Apply bad omen effect immediately (server side)
        if (!level.isClientSide()) {
            entity.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 
                80, 0, false, false, true));
        }
        
        // Send particle effect to client if on server side
        if (!level.isClientSide() && entity instanceof Player player) {
            AfterlifeEntombedMod.LOGGER.info("SERVER: Sending seth_dissolve particle to player");
            // Use the new ParticleManager server-side helper
            ParticleManager.sendParticleToPlayer(
                player,
                "seth_dissolve",
                entity.getX(), 
                entity.getY() + entity.getBbHeight() * 0.8, // Higher up, at head level
                entity.getZ(),
                0.7f, 0.7f, 0.7f  // Slightly smaller scale for dissolve effect
            );
            
            // Schedule invisibility effect after a delay (60 ticks = 3 seconds)
            // This allows the dissolve particle to play while the player is still visible
            level.scheduleTick(entity.blockPosition(), net.minecraft.world.level.block.Blocks.AIR, 60);
            
            // Use a different approach - store the delay in NBT and handle it in curioTick
            CompoundTag entityData = entity.getPersistentData();
            entityData.putLong("crown_equip_time", level.getGameTime());
            entityData.putBoolean("crown_invisibility_pending", true);
        }
    }
    
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Level level = entity.level();
        
        AfterlifeEntombedMod.LOGGER.info("CrownOfSeth onUnequip called! Entity: {}, ClientSide: {}", entity, level.isClientSide());
        
        // Send particle effect to client if on server side
        if (!level.isClientSide() && entity instanceof Player player) {
            AfterlifeEntombedMod.LOGGER.info("SERVER: Sending seth_appear particle to player");
            // Use the new ParticleManager server-side helper
            ParticleManager.sendParticleToPlayer(
                player,
                "seth_appear",
                entity.getX(), 
                entity.getY() + entity.getBbHeight() * 0.8, // Higher up, at head level
                entity.getZ(),
                0.8f, 0.8f, 0.8f  // Slightly larger scale for appear effect
            );
        }
        
        // Clean up timing data
        if (!level.isClientSide()) {
            CompoundTag entityData = entity.getPersistentData();
            entityData.remove("crown_equip_time");
            entityData.remove("crown_invisibility_pending");
        }
        
        // Remove effects
        entity.removeEffect(MobEffects.BAD_OMEN);
        entity.removeEffect(MobEffects.INVISIBILITY);
        entity.level().playSound(null, entity.blockPosition(), 
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 2.0f);
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player) {
            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.getBoolean("CrownPickedUp")) {
                tag.putBoolean("CrownPickedUp", true);
                stack.setTag(tag);
            }
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.afterlifeentombed.crown_of_seth.tooltip").withStyle(ChatFormatting.RED));

        boolean pickedUp = stack.getOrCreateTag().getBoolean("CrownPickedUp");

        if (pickedUp) {
            tooltip.add(Component.translatable("item.afterlifeentombed.crown_of_seth.tooltip.invis")
                .withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            tooltip.add(Component.translatable("item.afterlifeentombed.crown_of_seth.tooltip.invis")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.OBFUSCATED));
        }
    }
}