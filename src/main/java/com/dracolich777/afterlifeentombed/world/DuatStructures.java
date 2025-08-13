// package com.dracolich777.afterlifeentombed.world;

// import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
// import net.minecraft.world.level.levelgen.structure.StructureType;
// import net.minecraft.resources.ResourceLocation;
// import net.minecraftforge.registries.DeferredRegister;
// import net.minecraftforge.registries.ForgeRegistries;
// import net.minecraftforge.registries.RegistryObject;
// import net.minecraftforge.eventbus.api.IEventBus;

// public class DuatStructures {

//     public static final DeferredRegister<StructureType<?>> STRUCTURES = DeferredRegister.create(
//             ForgeRegistries.STRUCTURE_TYPES,
//             AfterlifeEntombedMod.MODID
//     );

//     public static final RegistryObject<StructureType<?>> RED_OBELISK = STRUCTURES.register("red_obelisk",
//             () -> () -> new ResourceLocation(AfterlifeEntombedMod.MODID, "red_obelisk"));

//     public static final RegistryObject<StructureType<?>> OBELISK = STRUCTURES.register("obelisk",
//             () -> () -> new ResourceLocation(AfterlifeEntombedMod.MODID, "obelisk"));

//     public static void register(IEventBus bus) {
//         STRUCTURES.register(bus);
//     }
// }
