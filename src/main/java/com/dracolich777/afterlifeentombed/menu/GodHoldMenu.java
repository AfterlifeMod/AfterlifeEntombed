package com.dracolich777.afterlifeentombed.menu;

import com.dracolich777.afterlifeentombed.blockentities.GodHoldBlockEntity;
import com.dracolich777.afterlifeentombed.init.ModMenuTypes;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GodHoldMenu extends AbstractContainerMenu {
    private final GodHoldBlockEntity blockEntity;
    private final ContainerLevelAccess levelAccess;
    
    // Chest-like layout: 9 columns, 6 rows = 54 slots
    public static final int CHEST_ROWS = 6;
    public static final int CHEST_COLS = 9;
    public static final int CONTAINER_SIZE = CHEST_ROWS * CHEST_COLS;
    
    public GodHoldMenu(int pContainerId, Inventory playerInventory, GodHoldBlockEntity blockEntity) {
        super(ModMenuTypes.GODHOLD_MENU.get(), pContainerId);
        this.blockEntity = blockEntity;
        this.levelAccess = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        
        // Add chest slots (6 rows of 9)
        for (int row = 0; row < CHEST_ROWS; row++) {
            for (int col = 0; col < CHEST_COLS; col++) {
                this.addSlot(new GodHoldSlot(blockEntity, col + row * CHEST_COLS, 8 + col * 18, 18 + row * 18));
            }
        }
        
        // Add player inventory slots
        int inventoryStartY = 18 + CHEST_ROWS * 18 + 14; // Chest + gap
        
        // Player inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, inventoryStartY + row * 18));
            }
        }
        
        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, inventoryStartY + 58));
        }
    }
    
    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            if (pIndex < CONTAINER_SIZE) {
                // Moving from chest to player inventory
                if (!this.moveItemStackTo(itemstack1, CONTAINER_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to chest
                if (!this.moveItemStackTo(itemstack1, 0, CONTAINER_SIZE, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        
        return itemstack;
    }
    
    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(levelAccess, pPlayer, blockEntity.getBlockState().getBlock());
    }
    
    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        // Ensure container state is properly synchronized when closing
        if (!pPlayer.level().isClientSide()) {
            blockEntity.setChanged();
        }
    }
    
    private static class GodHoldSlot extends Slot {
        private final GodHoldBlockEntity blockEntity;
        
        public GodHoldSlot(GodHoldBlockEntity blockEntity, int pSlot, int pX, int pY) {
            super(new GodHoldContainer(blockEntity), pSlot, pX, pY);
            this.blockEntity = blockEntity;
        }
        
        @Override
        public int getMaxStackSize() {
            return 64;
        }
        
        @Override
        public boolean mayPlace(ItemStack pStack) {
            return true;
        }
        
        @Override
        public void setChanged() {
            blockEntity.setChanged();
            super.setChanged();
        }
    }
    
    private static class GodHoldContainer implements Container {
        private final GodHoldBlockEntity blockEntity;
        
        public GodHoldContainer(GodHoldBlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }
        
        @Override
        public int getContainerSize() {
            return blockEntity.getContainerSize();
        }
        
        @Override
        public boolean isEmpty() {
            return blockEntity.isEmpty();
        }
        
        @Override
        public ItemStack getItem(int pSlot) {
            return blockEntity.getItem(pSlot);
        }
        
        @Override
        public ItemStack removeItem(int pSlot, int pAmount) {
            return blockEntity.removeItem(pSlot, pAmount);
        }
        
        @Override
        public ItemStack removeItemNoUpdate(int pSlot) {
            return blockEntity.removeItemNoUpdate(pSlot);
        }
        
        @Override
        public void setItem(int pSlot, ItemStack pStack) {
            blockEntity.setItem(pSlot, pStack);
        }
        
        @Override
        public void setChanged() {
            blockEntity.setChanged();
        }
        
        @Override
        public boolean stillValid(Player pPlayer) {
            return true; // Always valid since it's quantum linked
        }
        
        @Override
        public void clearContent() {
            blockEntity.clearContent();
        }
    }
}
