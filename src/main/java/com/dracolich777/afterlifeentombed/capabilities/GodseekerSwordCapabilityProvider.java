package com.dracolich777.afterlifeentombed.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import com.dracolich777.afterlifeentombed.items.GodstoneItem; // Import your GodstoneItem

public class GodseekerSwordCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private final ItemStack stack; // The GodseekerSword ItemStack this capability is attached to
    private final ItemStackHandler itemHandler; // Our actual inventory handler
    private final LazyOptional<IItemHandler> handlerLazyOptional;

    // Constructor to create the capability provider
    public GodseekerSwordCapabilityProvider(ItemStack stack, int size) {
        this.stack = stack;
        // Create an ItemStackHandler with the specified size (e.g., 1 for one godstone)
        this.itemHandler = createHandler(size);
        this.handlerLazyOptional = LazyOptional.of(() -> itemHandler);
    }

    private ItemStackHandler createHandler(int size) {
        return new ItemStackHandler(size) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                // Only allow items that are instances of GodstoneItem
                return stack.getItem() instanceof GodstoneItem;
            }

            @Override
            protected void onContentsChanged(int slot) {
                // When the inventory contents change, save the inventory to the parent ItemStack's NBT
                // This makes sure the godstone persists when the sword is saved/loaded.
                GodseekerSwordCapabilityProvider.this.stack.getOrCreateTag().put("GodstoneInventory", serializeNBT());
            }
        };
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
        if (cap == GodseekerSwordCapability.GODSTONE_INVENTORY_CAPABILITY) {
            return handlerLazyOptional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return itemHandler.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        itemHandler.deserializeNBT(nbt);
    }
}