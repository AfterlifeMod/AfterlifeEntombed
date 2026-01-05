package com.dracolich777.afterlifeentombed.init;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.recipes.GodforgeRecipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AfterlifeEntombedMod.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, AfterlifeEntombedMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<GodforgeRecipe>> GODFORGE_SERIALIZER =
            RECIPE_SERIALIZERS.register("godforge", GodforgeRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<GodforgeRecipe>> GODFORGE_RECIPE_TYPE =
            RECIPE_TYPES.register("godforge", () -> new RecipeType<GodforgeRecipe>() {
                @Override
                public String toString() {
                    return "godforge";
                }
            });

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}
