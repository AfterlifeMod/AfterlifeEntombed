package com.dracolich777.afterlifeentombed.init;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.mobs.GodseekerEntity;
import com.dracolich777.afterlifeentombed.mobs.ScorpionWhipProjectileEntity;
import com.dracolich777.afterlifeentombed.mobs.ShabtiEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AfterlifeEntombedMod.MOD_ID);

    public static final RegistryObject<EntityType<GodseekerEntity>> GODSEEKER =
            ENTITY_TYPES.register("godseeker",
                    () -> EntityType.Builder.of(GodseekerEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .build(AfterlifeEntombedMod.MOD_ID + ":godseeker"));

    public static final RegistryObject<EntityType<ScorpionWhipProjectileEntity>> SCORPION_WHIP_PROJECTILE =
            ENTITY_TYPES.register("scorpion_whip_projectile",
                    () -> EntityType.Builder.<ScorpionWhipProjectileEntity>of(ScorpionWhipProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(AfterlifeEntombedMod.MOD_ID + ":scorpion_whip_projectile"));

    public static final RegistryObject<EntityType<ShabtiEntity>> SHABTI =
            ENTITY_TYPES.register("shabti",
                    () -> EntityType.Builder.<ShabtiEntity>of(ShabtiEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(AfterlifeEntombedMod.MOD_ID + ":shabti"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
