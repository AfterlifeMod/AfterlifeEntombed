
// package com.dracolich777.afterlifeentombed.init;

// import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;

// import net.minecraft.world.inventory.MenuType;
// import net.minecraftforge.common.extensions.IForgeMenuType;
// import net.minecraftforge.eventbus.api.IEventBus;
// import net.minecraftforge.registries.DeferredRegister;
// import net.minecraftforge.registries.ForgeRegistries;
// import net.minecraftforge.registries.RegistryObject;

// public class ModContainers {
//     public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, AfterlifeEntombedMod.MOD_ID);

//     public static final RegistryObject<MenuType<GodseekerSwordContainer>> GODSEEKER_SWORD_CONTAINER = 
//             MENU_TYPES.register("godseeker_sword_container", 
//                     () -> IForgeMenuType.create((windowId, inv, data) -> {
//                         // The data should contain the sword slot index
//                         int swordSlotIndex = data.readInt();
//                         return new GodseekerSwordContainer(windowId, inv, swordSlotIndex);
//                     }));

//     public static void register(IEventBus eventBus) {
//         MENU_TYPES.register(eventBus);
//     }
// }