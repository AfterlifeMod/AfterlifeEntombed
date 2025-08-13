package com.dracolich777.afterlifeentombed.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class GodsteelTemplate extends SmithingTemplateItem {
    
    private static final Component GODSTEEL_UPGRADE = Component.translatable("upgrade.afterlifeentombed.godsteel_upgrade").withStyle(ChatFormatting.GRAY);
    private static final Component GODSTEEL_UPGRADE_APPLIES_TO = Component.translatable("item.afterlifeentombed.smithing_template.godsteel_upgrade.applies_to").withStyle(ChatFormatting.BLUE);
    private static final Component GODSTEEL_UPGRADE_INGREDIENTS = Component.translatable("item.afterlifeentombed.smithing_template.godsteel_upgrade.ingredients").withStyle(ChatFormatting.BLUE);
    private static final Component GODSTEEL_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable("item.afterlifeentombed.smithing_template.godsteel_upgrade.base_slot_description");
    private static final Component GODSTEEL_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable("item.afterlifeentombed.smithing_template.godsteel_upgrade.additions_slot_description");
    
    private static final ResourceLocation EMPTY_SLOT_INGOT = new ResourceLocation("item/empty_slot_ingot");
    private static final ResourceLocation EMPTY_SLOT_REDSTONE_DUST = new ResourceLocation("item/empty_slot_redstone_dust");
    
    public GodsteelTemplate(Properties properties) {
        super(GODSTEEL_UPGRADE_APPLIES_TO, GODSTEEL_UPGRADE_INGREDIENTS, GODSTEEL_UPGRADE, 
              GODSTEEL_UPGRADE_BASE_SLOT_DESCRIPTION, GODSTEEL_UPGRADE_ADDITIONS_SLOT_DESCRIPTION, 
              createGodsteelUpgradeIconList(), createGodsteelUpgradeAdditionsList());
    }
    
    private static List<ResourceLocation> createGodsteelUpgradeIconList() {
        return List.of(EMPTY_SLOT_INGOT);
    }
    
    private static List<ResourceLocation> createGodsteelUpgradeAdditionsList() {
        return List.of(EMPTY_SLOT_REDSTONE_DUST);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        tooltip.add(Component.translatable("item.afterlifeentombed.godsteel_template.tooltip.line1")
            .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.afterlifeentombed.godsteel_template.tooltip.line2")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.afterlifeentombed.godsteel_template.tooltip.line3")
            .withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Gives enchanted glint
    }
}