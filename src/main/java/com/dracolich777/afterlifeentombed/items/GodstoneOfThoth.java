package com.dracolich777.afterlifeentombed.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

// Ensure this extends GodstoneItem, which in turn extends Item
public class GodstoneOfThoth extends GodstoneItem { // <--- ENSURE THIS IS GodstoneItem

    public GodstoneOfThoth(Properties properties) { // Properties is now imported
        super(properties);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.afterlifeentombed.godstone_of_thoth.tooltip").withStyle(ChatFormatting.RED));
        // Call super.appendHoverText() AFTER adding your own tooltip, as per common practice
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public GodType getGodType() {
        return GodType.THOTH;
    }
}