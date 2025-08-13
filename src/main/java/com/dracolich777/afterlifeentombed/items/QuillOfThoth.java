package com.dracolich777.afterlifeentombed.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;

public class QuillOfThoth extends Item implements ICurioItem {
    
    public QuillOfThoth(Properties properties) {
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
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        
        // Play experience orb sound when equipped
        entity.level().playSound(null, entity.blockPosition(), 
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 0.5f);
    }
    
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        
        entity.removeEffect(MobEffects.CONDUIT_POWER);
        entity.level().playSound(null, entity.blockPosition(), 
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 2.0f);
    }
    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }
     @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Level level = entity.level();
        
        // Refresh fire resistance effect every 20 ticks (1 second) to ensure it stays active
        if (level.getGameTime() % 20 == 0) {
            if (!entity.hasEffect(MobEffects.CONDUIT_POWER)) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 
                    Integer.MAX_VALUE, 0, false, false, true));
            }
        }
        }
    
    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        // Only allow equipping in charm slot
        return slotContext.identifier().equals("charm");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.afterlifeentombed.quill_of_thoth.tooltip")
            .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.afterlifeentombed.quill_of_thoth.effect1")
            .withStyle(ChatFormatting.GREEN));
        
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
