package com.dracolich777.afterlifeentombed.items;

import java.util.List;

import javax.annotation.Nullable;

import com.dracolich777.afterlifeentombed.init.ModEffects;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class TokenOfHorus extends Item implements ICurioItem {
    private static final int COOLDOWN_TICKS = 6000; // 2 minutes in ticks (20 ticks per second * 120 seconds)
    private static final String COOLDOWN_TAG = "EyeOfHorusCooldown";
    
    public TokenOfHorus(Properties properties) {
        super(properties);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true; // Allow right-click equipping
    }
    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        
        // Play mystical sound when equipped
        entity.level().playSound(null, entity.blockPosition(), 
            SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 1.0f, 1.5f);
        
        // Add resistance effect
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 
            Integer.MAX_VALUE, 0, false, false, true));
    }
    
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        
        // Remove resistance effect when unequipped
        entity.removeEffect(MobEffects.DAMAGE_RESISTANCE);
    }
    
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Level level = entity.level();
        
        // Refresh resistance effect every 20 ticks (1 second) to ensure it stays active
        if (level.getGameTime() % 20 == 0) {
            if (!entity.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 
                    Integer.MAX_VALUE, 0, false, false, true));
            }
        }
        
        // Update cooldown
        updateCooldown(stack, level);
    }
    
    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        // Only allow equipping in charm slot
        return slotContext.identifier().equals("charm");
    }
    
    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true; // Can always be unequipped
    }
    
    // This method will be called when the entity takes fatal damage
    public boolean tryActivateTotem(LivingEntity entity, ItemStack stack, DamageSource damageSource) {
        if (isOnCooldown(stack, entity.level())) {
            return false; // Can't activate while on cooldown
        }
        
        // Activate the totem effect
        activateTotem(entity, stack);
        return true;
    }
    
    private void activateTotem(LivingEntity entity, ItemStack stack) {
        Level level = entity.level();
        
        // Set the entity's health to 1 (like totem of undying)
        entity.setHealth(1.0F);
        
        // Clear negative effects
        entity.removeAllEffects();
        
        // Apply totem effects
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1)); // 45 seconds of Regen II
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1)); // 5 seconds of Absorption II
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0)); // 40 seconds of Fire Resistance
        entity.addEffect(new MobEffectInstance(ModEffects.REVENGE_OF_HORUS.get(), 6000, 0));
        
        // Re-apply the resistance effect from wearing the charm
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 
            Integer.MAX_VALUE, 0, false, false, true));
        
        // Play totem sound
        level.playSound(null, entity.blockPosition(), 
            SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        // Spawn particles
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, 
                entity.getX(), entity.getY() + 1.0, entity.getZ(), 
                30, 0.5, 1.0, 0.5, 0.1);
        }
        
        // Set cooldown
        setCooldown(stack, level);
        
        // Send message to player if it's a player
        if (entity instanceof Player player) {
            player.displayClientMessage(
                Component.translatable("item.afterlifeentombed.token_of_horus.activated")
                    .withStyle(ChatFormatting.GOLD), 
                true);
        }
    }
    
    public boolean isOnCooldown(ItemStack stack, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(COOLDOWN_TAG)) {
            return false;
        }
        
        long cooldownEnd = tag.getLong(COOLDOWN_TAG);
        return level.getGameTime() < cooldownEnd;
    }
    
    private void setCooldown(ItemStack stack, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(COOLDOWN_TAG, level.getGameTime() + COOLDOWN_TICKS);
    }
    
    private void updateCooldown(ItemStack stack, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(COOLDOWN_TAG)) {
            long cooldownEnd = tag.getLong(COOLDOWN_TAG);
            if (level.getGameTime() >= cooldownEnd) {
                tag.remove(COOLDOWN_TAG);
            }
        }
    }
    
    private long getRemainingCooldown(ItemStack stack, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(COOLDOWN_TAG)) {
            return 0;
        }
        
        long cooldownEnd = tag.getLong(COOLDOWN_TAG);
        long remaining = cooldownEnd - level.getGameTime();
        return Math.max(0, remaining);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.afterlifeentombed.token_of_horus.tooltip")
            .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.afterlifeentombed.token_of_horus.resistance")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.afterlifeentombed.token_of_horus.totem_effect")
            .withStyle(ChatFormatting.AQUA));
        
        if (level != null) {
            long remainingTicks = getRemainingCooldown(stack, level);
            if (remainingTicks > 0) {
                int remainingSeconds = (int) (remainingTicks / 20);
                int minutes = remainingSeconds / 60;
                int seconds = remainingSeconds % 60;
                tooltip.add(Component.translatable("item.afterlifeentombed.token_of_horus.cooldown",
                        "%d:%02d".formatted(minutes, seconds))
                    .withStyle(ChatFormatting.RED));
            } else {
                tooltip.add(Component.translatable("item.afterlifeentombed.token_of_horus.ready")
                    .withStyle(ChatFormatting.GREEN));
            }
        }
        
        super.appendHoverText(stack, level, tooltip, flag);
    }
}