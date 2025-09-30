package com.dracolich777.afterlifeentombed.init;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.effects.RevengeOfSethEffect;
import com.dracolich777.afterlifeentombed.effects.HolyFireEffect;
import com.dracolich777.afterlifeentombed.effects.JudgedUnworthyEffect;
import com.dracolich777.afterlifeentombed.effects.JudgedWorthyEffect;
import com.dracolich777.afterlifeentombed.effects.RevengeOfAnubisEffect;
import com.dracolich777.afterlifeentombed.effects.RevengeOfHorusEffect;
import com.dracolich777.afterlifeentombed.effects.RevengeOfThothEffect;
import com.dracolich777.afterlifeentombed.effects.RevengeOfShuEffect;
import com.dracolich777.afterlifeentombed.effects.RevengeOfIsisEffect;
import com.dracolich777.afterlifeentombed.effects.RevengeOfHorusEffect;
import com.dracolich777.afterlifeentombed.effects.SwarmedEffect;
import com.dracolich777.afterlifeentombed.effects.RevengeOfGebEffect;
import com.dracolich777.afterlifeentombed.effects.MirageEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = 
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "afterlifeentombed");
    
    public static final RegistryObject<MobEffect> REVENGE_OF_SETH = 
        MOB_EFFECTS.register("revenge_of_seth", RevengeOfSethEffect::new);

        public static final RegistryObject<MobEffect> HOLY_FIRE = 
        MOB_EFFECTS.register("holy_fire", () -> new HolyFireEffect(MobEffectCategory.HARMFUL, 0xFCFF99));

        public static final RegistryObject<MobEffect> REVENGE_OF_THOTH = 
        MOB_EFFECTS.register("revenge_of_thoth", () -> new RevengeOfThothEffect(MobEffectCategory.HARMFUL, 0x1e00ff));

        public static final RegistryObject<MobEffect> REVENGE_OF_SHU = 
        MOB_EFFECTS.register("revenge_of_shu", () -> new RevengeOfShuEffect(MobEffectCategory.HARMFUL, 0x87CEEB));

        public static final RegistryObject<MobEffect> REVENGE_OF_ISIS = 
        MOB_EFFECTS.register("revenge_of_isis", () -> new RevengeOfIsisEffect(MobEffectCategory.HARMFUL, 0x800080));

        public static final RegistryObject<MobEffect> REVENGE_OF_GEB = MOB_EFFECTS.register("revenge_of_geb",
            () -> new RevengeOfGebEffect(MobEffectCategory.HARMFUL, 0x6B8E23));

        public static final RegistryObject<MobEffect> REVENGE_OF_HORUS = 
            MOB_EFFECTS.register("revenge_of_horus", () -> new RevengeOfHorusEffect(MobEffectCategory.HARMFUL, 0x008f66));

        public static final RegistryObject<MobEffect> REVENGE_OF_ANUBIS = 
            MOB_EFFECTS.register("revenge_of_anubis", () -> new RevengeOfAnubisEffect(MobEffectCategory.HARMFUL, 0x2F1B14));

        public static final RegistryObject<MobEffect> JUDGED_WORTHY =
        MOB_EFFECTS.register("judged_worthy", () -> new JudgedWorthyEffect(MobEffectCategory.BENEFICIAL, 0xFFD700));

        public static final RegistryObject<MobEffect> JUDGED_UNWORTHY =
        MOB_EFFECTS.register("judged_unworthy", () -> new JudgedUnworthyEffect(MobEffectCategory.HARMFUL, 0x000000));

        public static final RegistryObject<MobEffect> SWARMED = 
        MOB_EFFECTS.register("swarmed_by_locusts", SwarmedEffect::new);

        public static final RegistryObject<MobEffect> MIRAGE =
        MOB_EFFECTS.register("mirage", () -> new MirageEffect(MobEffectCategory.HARMFUL, 0xfff3a3));

    
    
    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
        // Apply the effect to the target entity.
        // Parameters for MobEffectInstance:
        // 1. MobEffect: The actual effect to apply (ModEffects.REVENGE_OF_GEB.get())
        // 2. Duration (ticks): How long the effect lasts. 20 ticks = 1 second.
        //    Adjust 100 (5 seconds) as needed.
        // 3. Amplifier (0-based): The strength of the effect. 0 is level I, 1 is level II, etc.
        //    Adjust 0 as needed.
        // 4. Ambient: Whether the effect particles are less visible (true) or normal (false).
        // 5. Show Particles: Whether particles are shown around the entity (true) or hidden (false).
        // 6. Show Icon: Whether the effect icon is shown in the HUD (true) or hidden (false).