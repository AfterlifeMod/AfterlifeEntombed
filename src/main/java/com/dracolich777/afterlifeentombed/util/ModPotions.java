package com.dracolich777.afterlifeentombed.util;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.init.ModEffects;

public class ModPotions {

    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(
            ForgeRegistries.POTIONS, AfterlifeEntombedMod.MOD_ID);

    public static final int DURATION = 3600; // 3 minutes

    public static final RegistryObject<Potion> REVENGE_OF_SETH =
            POTIONS.register("revenge_of_seth_potion", ModPotions::createRevengeOfSethPotion);

    public static final RegistryObject<Potion> HOLY_FIRE =
            POTIONS.register("holy_fire_potion", ModPotions::createHolyFirePotion);

    public static final RegistryObject<Potion> REVENGE_OF_THOTH =
            POTIONS.register("revenge_of_thoth_potion", ModPotions::createRevengeOfThothPotion);

    public static final RegistryObject<Potion> REVENGE_OF_SHU =
            POTIONS.register("revenge_of_shu_potion", ModPotions::createRevengeOfShuPotion);

    public static final RegistryObject<Potion> REVENGE_OF_ISIS =
            POTIONS.register("revenge_of_isis_potion", ModPotions::createRevengeOfIsisPotion);

    public static final RegistryObject<Potion> REVENGE_OF_GEB =
            POTIONS.register("revenge_of_geb_potion", ModPotions::createRevengeOfGebPotion);

    public static final RegistryObject<Potion> REVENGE_OF_HORUS =
            POTIONS.register("revenge_of_horus_potion", ModPotions::createRevengeOfHorusPotion);

    public static final RegistryObject<Potion> REVENGE_OF_ANUBIS =
            POTIONS.register("revenge_of_anubis_potion", ModPotions::createRevengeOfAnubisPotion);

    // Factory methods
    private static Potion createRevengeOfSethPotion() {
        return new Potion(new MobEffectInstance(ModEffects.REVENGE_OF_SETH.get(), 1200));
    }

    private static Potion createHolyFirePotion() {
        return new Potion(new MobEffectInstance(ModEffects.HOLY_FIRE.get(), DURATION));
    }

    private static Potion createRevengeOfThothPotion() {
        return new Potion(new MobEffectInstance(ModEffects.REVENGE_OF_THOTH.get(), DURATION));
    }

    private static Potion createRevengeOfShuPotion() {
        return new Potion(new MobEffectInstance(ModEffects.REVENGE_OF_SHU.get(), DURATION));
    }

    private static Potion createRevengeOfIsisPotion() {
        return new Potion(new MobEffectInstance(ModEffects.REVENGE_OF_ISIS.get(), DURATION));
    }

    private static Potion createRevengeOfGebPotion() {
        return new Potion(new MobEffectInstance(ModEffects.REVENGE_OF_GEB.get(), 70));
    }

    private static Potion createRevengeOfHorusPotion() {
        return new Potion(new MobEffectInstance(ModEffects.REVENGE_OF_HORUS.get(), DURATION));
    }

    private static Potion createRevengeOfAnubisPotion() {
        return new Potion(new MobEffectInstance(ModEffects.REVENGE_OF_ANUBIS.get(), DURATION));
    }
}
