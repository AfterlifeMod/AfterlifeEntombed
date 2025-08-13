package com.dracolich777.afterlifeentombed.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GodseekerSword extends SwordItem {
    
    // Map godstone items to their effect names
    private static final Map<String, String> GODSTONE_EFFECTS = new HashMap<>();
    
    static {
        GODSTONE_EFFECTS.put("afterlifeentombed:godstone_of_ra", "holy_fire");
        GODSTONE_EFFECTS.put("afterlifeentombed:godstone_of_anubis", "revenge_of_anubis");
        GODSTONE_EFFECTS.put("afterlifeentombed:godstone_of_isis", "revenge_of_isis");
        GODSTONE_EFFECTS.put("afterlifeentombed:godstone_of_seth", "revenge_of_seth");
        GODSTONE_EFFECTS.put("afterlifeentombed:godstone_of_thoth", "revenge_of_thoth");
        GODSTONE_EFFECTS.put("afterlifeentombed:godstone_of_horus", "revenge_of_horus");
        GODSTONE_EFFECTS.put("afterlifeentombed:godstone_of_geb", "revenge_of_geb");
        GODSTONE_EFFECTS.put("afterlifeentombed:godstone_of_shu", "revenge_of_shu");
    }
    
    public GodseekerSword(Properties pProperties) {
        super(Tiers.NETHERITE, 3, -2.4F, pProperties);
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        boolean result = super.hurtEnemy(pStack, pTarget, pAttacker);
        
        // Apply godstone effect if one is present
        ItemStack godstoneeItem = getGodstoneeItem(pStack);
        if (!godstoneeItem.isEmpty()) {
            String effectName = getEffectFromGodstone(godstoneeItem);
            if (effectName != null) {
                applyGodstoneeEffect(pTarget, effectName);
            }
        }
        
        return result;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        ItemStack godstoneeItem = getGodstoneeItem(pStack);
        
        if (!godstoneeItem.isEmpty()) {
            String effectName = getEffectFromGodstone(godstoneeItem);
            if (effectName != null) {
                String displayName = getEffectDisplayName(effectName);
                pTooltipComponents.add(Component.literal("Power: " + displayName)
                    .withStyle(ChatFormatting.GOLD));
            }
        } else {
            pTooltipComponents.add(Component.literal("No godstone inserted")
                .withStyle(ChatFormatting.GRAY));
        }
        
        pTooltipComponents.add(Component.literal("Hover to insert godstone")
            .withStyle(ChatFormatting.AQUA));
        
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    // Get the godstone item from the sword's NBT
    public static ItemStack getGodstoneeItem(ItemStack swordStack) {
        CompoundTag tag = swordStack.getOrCreateTag();
        if (tag.contains("GodstoneeItem")) {
            CompoundTag godstoneeTag = tag.getCompound("GodstoneeItem");
            return ItemStack.of(godstoneeTag);
        }
        return ItemStack.EMPTY;
    }

    // Set the godstone item in the sword's NBT
    public static void setGodstoneeItem(ItemStack swordStack, ItemStack godstoneeItem) {
        CompoundTag tag = swordStack.getOrCreateTag();
        if (godstoneeItem.isEmpty()) {
            tag.remove("GodstoneeItem");
        } else {
            CompoundTag godstoneeTag = new CompoundTag();
            godstoneeItem.save(godstoneeTag);
            tag.put("GodstoneeItem", godstoneeTag);
        }
    }

    // Check if an item is a valid godstone
    public static boolean isValidGodstone(ItemStack item) {
        if (item.isEmpty()) return false;
        String itemKey = ForgeRegistries.ITEMS.getKey(item.getItem()).toString();
        return GODSTONE_EFFECTS.containsKey(itemKey);
    }

    // Get the effect name from a godstone item
    private String getEffectFromGodstone(ItemStack godstoneeItem) {
        String itemKey = ForgeRegistries.ITEMS.getKey(godstoneeItem.getItem()).toString();
        return GODSTONE_EFFECTS.get(itemKey);
    }

    // Convert internal effect name to display name
    private String getEffectDisplayName(String effectName) {
        switch (effectName) {
            case "holy_fire": return "Holy Fire of Ra";
            case "revenge_of_anubis": return "Revenge of Anubis";
            case "revenge_of_isis": return "Revenge of Isis";
            case "revenge_of_osiris": return "Revenge of Osiris";
            case "revenge_of_thoth": return "Revenge of Thoth";
            case "revenge_of_horus": return "Revenge of Horus";
            case "revenge_of_sobek": return "Revenge of Sobek";
            case "revenge_of_bastet": return "Revenge of Bastet";
            default: return "Unknown Power";
        }
    }

    // Apply the appropriate effect based on the godstone
    private void applyGodstoneeEffect(LivingEntity target, String effectName) {
        var effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("afterlifeentombed", effectName));
        
        if (effect != null) {
            target.addEffect(new MobEffectInstance(effect, 200, 0));
        }
    }
}