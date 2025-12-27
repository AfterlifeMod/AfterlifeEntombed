package com.dracolich777.afterlifeentombed.items;

import com.dracolich777.afterlifeentombed.init.ModEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem; 

import java.util.List;
import javax.annotation.Nullable;

public class CollarOfAnubis extends Item implements ICurioItem {

    public CollarOfAnubis() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        
        // Only run on server side
        if (!entity.level().isClientSide) {
            // Apply Judged Worthy effect continuously (short duration so it needs constant renewal)
            if (!entity.hasEffect(ModEffects.JUDGED_WORTHY.get())) {
                entity.addEffect(new MobEffectInstance(
                    ModEffects.JUDGED_WORTHY.get(),
                    40, // 2 seconds duration, refreshed every tick
                    0,  // Level 0 (level I)
                    false, // Not ambient
                    true,  // Show particles
                    true   // Show icon
                ));
            }
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        // Only allow equipping from use if the slot is 'necklace'
        return "necklace".equals(slotContext.identifier());
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        
        // Immediately apply Judged Worthy effect when equipped
        if (!entity.level().isClientSide) {
            entity.addEffect(new MobEffectInstance(
                ModEffects.JUDGED_WORTHY.get(),
                1, // 1 tick duration to trigger the conversion
                0,
                false,
                true,
                true
            ));
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        
        // Remove Judged Worthy effect when unequipped
        if (!entity.level().isClientSide) {
            entity.removeEffect(ModEffects.JUDGED_WORTHY.get());
        }
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        // Only allow equipping in the 'necklace' curios slot
        return "necklace".equals(slotContext.identifier());
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true; // Always has enchantment glint
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.afterlifeentombed.collar_of_anubis.tooltip1")
            .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.afterlifeentombed.collar_of_anubis.tooltip2")
            .withStyle(ChatFormatting.GOLD));

        
        super.appendHoverText(stack, level, tooltip, flag);
    }
}