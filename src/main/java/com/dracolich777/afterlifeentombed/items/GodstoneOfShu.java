package com.dracolich777.afterlifeentombed.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class GodstoneOfShu extends GodstoneItem {
    public GodstoneOfShu(Properties properties) {
        super(properties);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.afterlifeentombed.godstone_of_shu.tooltip").withStyle(ChatFormatting.RED));
        super.appendHoverText(stack, level, tooltip, flag);
    }
    @Override
    public GodType getGodType() {
        return GodType.SHU;
    }
}
