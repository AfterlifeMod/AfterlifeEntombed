package com.dracolich777.afterlifeentombed.init;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.init.recipes.PricklyPearInfusionRecipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = 
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AfterlifeEntombedMod.MOD_ID);
        
    public static final RegistryObject<RecipeSerializer<PricklyPearInfusionRecipe>> PRICKLY_PEAR_INFUSION = 
        RECIPE_SERIALIZERS.register("prickly_pear_infusion", 
            () -> new SimpleCraftingRecipeSerializer<>(PricklyPearInfusionRecipe::new));

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
