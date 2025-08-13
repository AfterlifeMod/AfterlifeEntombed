package com.dracolich777.afterlifeentombed.items;

import net.minecraft.world.item.Item;

// Base class for all godstones.
// This allows GodseekerSwordCapabilityProvider to check if an item is a godstone.
public class GodstoneItem extends Item {
    public GodstoneItem(Properties pProperties) {
        super(pProperties);
    }

    public GodType getGodType() {
        return GodType.NONE; // Default, to be overridden by specific godstone classes
    }
}
