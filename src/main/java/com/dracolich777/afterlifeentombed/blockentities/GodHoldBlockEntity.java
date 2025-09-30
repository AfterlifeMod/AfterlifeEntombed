package com.dracolich777.afterlifeentombed.blockentities;

import com.dracolich777.afterlifeentombed.init.ModBlockEntities;
import com.dracolich777.afterlifeentombed.menu.GodHoldMenu;
import com.dracolich777.afterlifeentombed.util.GodHoldInventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class GodHoldBlockEntity extends BlockEntity implements MenuProvider {
    
    private ItemStackHandler itemHandler;
    private LazyOptional<ItemStackHandler> lazyItemHandler = LazyOptional.empty();
    
    public GodHoldBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.GODHOLD_BLOCK_ENTITY.get(), pPos, pBlockState);
        this.itemHandler = new ItemStackHandler(GodHoldInventory.getInventorySize()) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                // Sync changes to the shared inventory
                if (level instanceof ServerLevel serverLevel) {
                    GodHoldInventory sharedInv = GodHoldInventory.get(serverLevel);
                    sharedInv.setItem(slot, getStackInSlot(slot));
                }
            }
            
            @Override
            public ItemStack getStackInSlot(int slot) {
                // Get item from shared inventory
                if (level instanceof ServerLevel serverLevel) {
                    GodHoldInventory sharedInv = GodHoldInventory.get(serverLevel);
                    return sharedInv.getItem(slot);
                }
                return super.getStackInSlot(slot);
            }
            
            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                // Set item in shared inventory
                if (level instanceof ServerLevel serverLevel) {
                    GodHoldInventory sharedInv = GodHoldInventory.get(serverLevel);
                    sharedInv.setItem(slot, stack);
                } else {
                    super.setStackInSlot(slot, stack);
                }
            }
            
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                // Extract item from shared inventory
                if (level instanceof ServerLevel serverLevel) {
                    GodHoldInventory sharedInv = GodHoldInventory.get(serverLevel);
                    ItemStack currentStack = sharedInv.getItem(slot);
                    
                    if (currentStack.isEmpty() || amount <= 0) {
                        return ItemStack.EMPTY;
                    }
                    
                    int extractAmount = Math.min(amount, currentStack.getCount());
                    ItemStack result = currentStack.copy();
                    result.setCount(extractAmount);
                    
                    if (!simulate) {
                        ItemStack remaining = currentStack.copy();
                        remaining.shrink(extractAmount);
                        sharedInv.setItem(slot, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
                    }
                    
                    return result;
                } else {
                    return super.extractItem(slot, amount, simulate);
                }
            }
            
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                // Insert item into shared inventory
                if (level instanceof ServerLevel serverLevel) {
                    GodHoldInventory sharedInv = GodHoldInventory.get(serverLevel);
                    ItemStack currentStack = sharedInv.getItem(slot);
                    
                    if (stack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                    
                    if (!currentStack.isEmpty() && !ItemStack.isSameItemSameTags(currentStack, stack)) {
                        return stack;
                    }
                    
                    int currentCount = currentStack.isEmpty() ? 0 : currentStack.getCount();
                    int maxStackSize = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
                    int insertAmount = Math.min(stack.getCount(), maxStackSize - currentCount);
                    
                    if (insertAmount <= 0) {
                        return stack;
                    }
                    
                    if (!simulate) {
                        ItemStack newStack = stack.copy();
                        newStack.setCount(currentCount + insertAmount);
                        sharedInv.setItem(slot, newStack);
                    }
                    
                    ItemStack remainder = stack.copy();
                    remainder.shrink(insertAmount);
                    return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
                } else {
                    return super.insertItem(slot, stack, simulate);
                }
            }
        };
        this.lazyItemHandler = LazyOptional.of(() -> this.itemHandler);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.afterlifeentombed.godhold");
    }
    
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @Nonnull Inventory pPlayerInventory, @Nonnull Player pPlayer) {
        return new GodHoldMenu(pContainerId, pPlayerInventory, this);
    }
    
    // Get the shared inventory from the world's saved data
    private GodHoldInventory getSharedInventory() {
        if (level instanceof ServerLevel serverLevel) {
            return GodHoldInventory.get(serverLevel);
        }
        return null;
    }
    
    public NonNullList<ItemStack> getItems() {
        GodHoldInventory sharedInv = getSharedInventory();
        if (sharedInv != null) {
            return sharedInv.getSharedInventory();
        }
        return NonNullList.withSize(GodHoldInventory.getInventorySize(), ItemStack.EMPTY);
    }
    
    public void setItems(NonNullList<ItemStack> items) {
        GodHoldInventory sharedInv = getSharedInventory();
        if (sharedInv != null) {
            sharedInv.setSharedInventory(items);
        }
    }
    
    public ItemStack getItem(int index) {
        GodHoldInventory sharedInv = getSharedInventory();
        if (sharedInv != null) {
            return sharedInv.getItem(index);
        }
        return ItemStack.EMPTY;
    }
    
    public void setItem(int index, ItemStack stack) {
        GodHoldInventory sharedInv = getSharedInventory();
        if (sharedInv != null) {
            sharedInv.setItem(index, stack);
            setChanged();
        }
    }
    
    public ItemStack removeItem(int index, int count) {
        GodHoldInventory sharedInv = getSharedInventory();
        if (sharedInv != null) {
            ItemStack result = sharedInv.removeItem(index, count);
            setChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }
    
    public ItemStack removeItemNoUpdate(int index) {
        GodHoldInventory sharedInv = getSharedInventory();
        if (sharedInv != null) {
            ItemStack result = sharedInv.removeItemNoUpdate(index);
            return result;
        }
        return ItemStack.EMPTY;
    }
    
    public boolean isEmpty() {
        GodHoldInventory sharedInv = getSharedInventory();
        if (sharedInv != null) {
            return sharedInv.isEmpty();
        }
        return true;
    }
    
    public void clearContent() {
        GodHoldInventory sharedInv = getSharedInventory();
        if (sharedInv != null) {
            sharedInv.clearContent();
            setChanged();
        }
    }
    
    public int getContainerSize() {
        return GodHoldInventory.getInventorySize();
    }
    
    public void dropContents() {
        var currentLevel = level;
        if (currentLevel != null && !currentLevel.isClientSide()) {
            NonNullList<ItemStack> items = getItems();
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(currentLevel, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                }
            }
            clearContent();
        }
    }
    
    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }
    
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }
}
