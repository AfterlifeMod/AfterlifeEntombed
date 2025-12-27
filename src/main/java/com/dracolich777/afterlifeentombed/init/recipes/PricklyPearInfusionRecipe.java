package com.dracolich777.afterlifeentombed.init.recipes;

import java.util.List;

import com.dracolich777.afterlifeentombed.init.ModRecipes;
import com.dracolich777.afterlifeentombed.items.PricklyPearArmor;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class PricklyPearInfusionRecipe extends CustomRecipe {
    
    public PricklyPearInfusionRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }
    
    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack armor = ItemStack.EMPTY;
        ItemStack potion = ItemStack.EMPTY;
        
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof PricklyPearArmor) {
                    if (!armor.isEmpty()) return false; // Multiple armor pieces
                    armor = stack;
                } else if (stack.getItem() instanceof PotionItem) {
                    if (!potion.isEmpty()) return false; // Multiple potions
                    potion = stack;
                }
            }
        }
        
        return !armor.isEmpty() && !potion.isEmpty() && 
               !PotionUtils.getMobEffects(potion).isEmpty();
    }
    
    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack armor = ItemStack.EMPTY;
        ItemStack potion = ItemStack.EMPTY;
        
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof PricklyPearArmor) {
                    armor = stack.copy();
                } else if (stack.getItem() instanceof PotionItem) {
                    potion = stack;
                }
            }
        }
        
        if (!armor.isEmpty() && !potion.isEmpty()) {
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(potion);
            if (!effects.isEmpty()) {
                MobEffectInstance effect = effects.get(0); // Use first effect (Java 17 compatible)
                PricklyPearArmor.storeEffect(armor, effect.getEffect(), 
                    effect.getDuration(), effect.getAmplifier());
            }
        }
        
        return armor;
    }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.PRICKLY_PEAR_INFUSION.get();
    }
}