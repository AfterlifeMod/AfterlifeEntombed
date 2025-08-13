package com.dracolich777.afterlifeentombed.capabilities;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.IItemHandler;

// This class is specifically for managing the registration of our capability.
public class GodseekerSwordCapability {

    // Define the Capability itself. This is what you'll query on the ItemStack.
    public static final Capability<IItemHandler> GODSTONE_INVENTORY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static void register(IEventBus eventBus) {
        // No specific event listener needed here for simple capability registration
        // The CapabilityToken handles the creation.
    }
}