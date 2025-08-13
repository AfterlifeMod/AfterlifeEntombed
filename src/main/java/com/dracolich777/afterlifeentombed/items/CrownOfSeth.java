package com.dracolich777.afterlifeentombed.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
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
        
        // Refresh invisibility effect every 20 ticks (1 second) to ensure it stays active
        if (level.getGameTime() % 20 == 0) {
            if (!entity.hasEffect(MobEffects.INVISIBILITY)) {
                entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 
                    Integer.MAX_VALUE, 0, false, false, true));
                entity.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 
                    80, 0, false, false, true));
            }
            
        }
    }
    
    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }
    
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        
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