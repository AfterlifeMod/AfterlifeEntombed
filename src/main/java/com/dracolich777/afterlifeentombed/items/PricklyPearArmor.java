package com.dracolich777.afterlifeentombed.items;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.ChatFormatting;
import java.util.List;

public class PricklyPearArmor extends ArmorItem {
    
    public PricklyPearArmor(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        // Add thorns effect tooltip
        tooltip.add(Component.translatable("tooltip.afterlifeentombed.thorns_effect")
            .withStyle(ChatFormatting.DARK_GREEN));
        
        // Add stored potion effect tooltip
        if (hasStoredEffect(stack)) {
            CompoundTag nbt = stack.getTag();
            if (nbt != null) {
                String effectId = nbt.getString("StoredEffect");
                int duration = nbt.getInt("EffectDuration");
                int amplifier = nbt.getInt("EffectAmplifier");
                
                MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(effectId));
                if (effect != null) {
                    String effectName = effect.getDisplayName().getString();
                    int durationSeconds = duration / 20; // Convert ticks to seconds
                    int minutes = durationSeconds / 60;
                    int seconds = durationSeconds % 60;
                    
                    String amplifierText = amplifier > 0 ? " " + (amplifier + 1) : "";
                    String durationText = String.format("%d:%02d", minutes, seconds);
                    
                    tooltip.add(Component.translatable("tooltip.afterlifeentombed.stored_effect", 
                        effectName + amplifierText, durationText)
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }
        } else {
            tooltip.add(Component.translatable("tooltip.afterlifeentombed.no_effect")
                .withStyle(ChatFormatting.GRAY));
        }
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }
    
    // Called when the wearer takes damage
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide && player.hurtTime > 0) {
            // Apply stored potion effect to attacker if they exist
            LivingEntity lastHurtBy = player.getLastHurtByMob();
            if (lastHurtBy != null) {
                applyStoredEffect(stack, lastHurtBy);
                
                // Small thorns damage (1 heart)
                lastHurtBy.hurt(level.damageSources().thorns(player), 2.0F);
            }
        }
    }
    
    private void applyStoredEffect(ItemStack stack, LivingEntity target) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("StoredEffect")) {
            String effectId = nbt.getString("StoredEffect");
            int duration = nbt.getInt("EffectDuration");
            int amplifier = nbt.getInt("EffectAmplifier");
            
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(effectId));
            if (effect != null) {
                target.addEffect(new MobEffectInstance(effect, duration, amplifier));
            }
        }
    }
    
    // Store potion effect in armor
    public static void storeEffect(ItemStack stack, MobEffect effect, int duration, int amplifier) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putString("StoredEffect", BuiltInRegistries.MOB_EFFECT.getKey(effect).toString());
        nbt.putInt("EffectDuration", duration);
        nbt.putInt("EffectAmplifier", amplifier);
    }
    
    // Check if armor has stored effect
    public static boolean hasStoredEffect(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        return nbt != null && nbt.contains("StoredEffect");
    }
    
    // Get stored effect name for tooltip
    public static String getStoredEffectName(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("StoredEffect")) {
            String effectId = nbt.getString("StoredEffect");
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(effectId));
            if (effect != null) {
                return effect.getDisplayName().getString();
            }
        }
        return "";
    }
}