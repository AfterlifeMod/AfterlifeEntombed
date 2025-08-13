package com.dracolich777.afterlifeentombed.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.util.ModPotions;

public class ModPotionTypes {

    public static void registerPotionVariants() {
        registerPotionVariant(ModPotions.REVENGE_OF_SETH.get());
        registerPotionVariant(ModPotions.HOLY_FIRE.get());
        registerPotionVariant(ModPotions.REVENGE_OF_THOTH.get());
        registerPotionVariant(ModPotions.REVENGE_OF_SHU.get());
        registerPotionVariant(ModPotions.REVENGE_OF_ISIS.get());
        registerPotionVariant(ModPotions.REVENGE_OF_GEB.get());
        registerPotionVariant(ModPotions.REVENGE_OF_HORUS.get());
        registerPotionVariant(ModPotions.REVENGE_OF_ANUBIS.get());
    }

    private static void registerPotionVariant(Potion potion) {
        // Splash Potion
        ItemStack splashPotion = PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion);

        // Lingering Potion
        ItemStack lingeringPotion = PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion);

        // Tipped Arrow
        ItemStack tippedArrow = PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), potion);

    }
}
