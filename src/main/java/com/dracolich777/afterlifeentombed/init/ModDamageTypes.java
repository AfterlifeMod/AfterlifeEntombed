package com.dracolich777.afterlifeentombed.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {
    
    public static final ResourceKey<DamageType> HOLY_FIRE = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        new ResourceLocation("afterlifeentombed", "holy_fire")
    );
    
    public static final ResourceKey<DamageType> HORUS_DAMAGE = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        new ResourceLocation("afterlifeentombed", "horus_damage")
    );
    
    public static void bootstrap(BootstapContext<DamageType> context) {
        context.register(HOLY_FIRE, new DamageType("holy_fire_damage", 0.1F));
        context.register(HORUS_DAMAGE, new DamageType("horus_revenge_damage", 0.1F));
    }
    
    // Alternative method with more descriptive name
    public static DamageSource createHorusDamage(ServerLevel level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(HORUS_DAMAGE));
    }
    
    // Method to create Holy Fire damage source
    public static DamageSource holyFire(ServerLevel level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(HOLY_FIRE));
    }
}