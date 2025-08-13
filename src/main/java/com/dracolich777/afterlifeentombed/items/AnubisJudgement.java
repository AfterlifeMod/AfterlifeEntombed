package com.dracolich777.afterlifeentombed.items;

import java.util.List;

import javax.annotation.Nullable;

import com.dracolich777.afterlifeentombed.init.ModEffects;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity; // Import Mth for utility functions like wrapDegrees
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class AnubisJudgement extends SwordItem {

    public AnubisJudgement(Item.Properties properties) {
        super(Tiers.NETHERITE, 2 - 1, -2.4F, properties); // 8 damage
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(ModEffects.REVENGE_OF_ANUBIS.get(), 300, 2));
        target.addEffect(new MobEffectInstance(ModEffects.SWARMED.get(), 100, 4));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.afterlifeentombed.anubis_judgement.tooltip1")
                .withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("tooltip.afterlifeentombed.anubis_judgement.tooltip2")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.afterlifeentombed.anubis_judgement.tooltip3")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
