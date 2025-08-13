package com.dracolich777.afterlifeentombed.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ModDimensions {
    public static final ResourceKey<Level> DUAT_KEY = ResourceKey.create(
        Registries.DIMENSION,
        new ResourceLocation("afterlifeentombed", "duat")
    );
}