package com.dracolich777.afterlifeentombed.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;

import javax.annotation.Nullable;
import java.util.List;

public class BreathOfShuItem extends Item implements ICurioItem {
    
    public BreathOfShuItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }
    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.afterlifeentombed.breath_of_shu.tooltip")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.afterlifeentombed.breath_of_shu.tooltip2")
            .withStyle(ChatFormatting.BLUE));
        super.appendHoverText(stack, level, tooltip, flag);
    }
    
    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
    
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        // Called when the item is equipped
        ICurioItem.super.onEquip(slotContext, prevStack, stack);
    }
    
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        
        entity.removeEffect(MobEffects.SLOW_FALLING);
    }
    
     @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Level level = entity.level();
        
        // Refresh fire resistance effect every 20 ticks (1 second) to ensure it stays active
        if (level.getGameTime() % 20 == 0) {
            if (!entity.hasEffect(MobEffects.SLOW_FALLING)) {
                entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 
                    Integer.MAX_VALUE, 0, false, false, true));
            }
        }
        }
    }
