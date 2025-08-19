package com.dracolich777.afterlifeentombed.init;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
// import com.dracolich777.afterlifeentombed.blockentities.GodHoldBlockEntity;
// import com.dracolich777.afterlifeentombed.menu.GodHoldMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, AfterlifeEntombedMod.MOD_ID);

    // public static final RegistryObject<MenuType<GodHoldMenu>> GODHOLD_MENU =
    //         MENUS.register("godhold_menu", () ->
    //                 IForgeMenuType.create((windowId, inv, data) -> {
    //                     BlockPos pos = data.readBlockPos();
    //                     BlockEntity blockEntity = inv.player.level().getBlockEntity(pos);
    //                     if (blockEntity instanceof GodHoldBlockEntity) {
    //                         return new GodHoldMenu(windowId, inv, (GodHoldBlockEntity) blockEntity);
    //                     }
    //                     throw new IllegalStateException("Block entity at " + pos + " is not a GodHoldBlockEntity");
    //                 }));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
