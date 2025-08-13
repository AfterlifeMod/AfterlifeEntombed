package com.dracolich777.afterlifeentombed.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class GodsteelIngotOfRa extends Item {
    
    public GodsteelIngotOfRa(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        tooltip.add(Component.translatable("item.afterlifeentombed.godsteel_ingot_of_ra.tooltip.line1")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.afterlifeentombed.godsteel_ingot_of_ra.tooltip.line2")
            .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.afterlifeentombed.godsteel_ingot_of_ra.tooltip.line3")
            .withStyle(ChatFormatting.RED));
    }
}
