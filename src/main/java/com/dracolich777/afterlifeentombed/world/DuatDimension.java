// package com.dracolich777.afterlifeentombed.world;

// import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
// import net.minecraft.core.registries.Registries;
// import net.minecraft.resources.ResourceKey;
// import net.minecraft.resources.ResourceLocation;
// import net.minecraft.world.level.dimension.LevelStem;
// import net.minecraftforge.registries.DeferredRegister;
// import net.minecraftforge.registries.RegistryObject;
// import net.minecraftforge.eventbus.api.IEventBus;

// public class DuatDimension {
//     public static final ResourceKey<LevelStem> DUAT_DIMENSION = ResourceKey.create(
//             Registries.LEVEL_STEM,
//             new ResourceLocation(AfterlifeEntombedMod.MODID, "the_duat")
//     );

//     public static final DeferredRegister<LevelStem> DIMENSIONS = DeferredRegister.create(
//             Registries.LEVEL_STEM,
//             AfterlifeEntombedMod.MODID
//     );

//     public static final RegistryObject<LevelStem> DUAT = DIMENSIONS.register("the_duat",
//             () -> new LevelStem(
//                     () -> DuatWorldgen.createBiomeSource(),
//                     DuatWorldgen.createChunkGenerator()
//             )
//     );

//     public static void register(IEventBus bus) {
//         DIMENSIONS.register(bus);
//     }
// }
