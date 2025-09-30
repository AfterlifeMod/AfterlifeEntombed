package com.dracolich777.afterlifeentombed.util;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;

public class GodHoldInventory extends SavedData {
    private static final String DATA_NAME = AfterlifeEntombedMod.MOD_ID + "_godhold_shared_inventory";
    private static final int INVENTORY_SIZE = 9; // Dispenser-like 3x3 grid
    
    // Single shared inventory for all Godhold blocks
    private NonNullList<ItemStack> sharedInventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    
    public static GodHoldInventory get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(GodHoldInventory::load, GodHoldInventory::new, DATA_NAME);
    }
    
    public static GodHoldInventory load(CompoundTag tag) {
        GodHoldInventory data = new GodHoldInventory();
        
        if (tag.contains("SharedInventory")) {
            ListTag itemsTag = tag.getList("SharedInventory", 10);
            for (int i = 0; i < itemsTag.size(); i++) {
                CompoundTag itemTag = itemsTag.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < data.sharedInventory.size()) {
                    data.sharedInventory.set(slot, ItemStack.of(itemTag));
                }
            }
        }
        
        return data;
    }
    
    @Override
    public CompoundTag save(@Nonnull CompoundTag tag) {
        ListTag itemsTag = new ListTag();
        
        for (int i = 0; i < sharedInventory.size(); i++) {
            ItemStack stack = sharedInventory.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stack.save(itemTag);
                itemsTag.add(itemTag);
            }
        }
        
        tag.put("SharedInventory", itemsTag);
        return tag;
    }
    
    public NonNullList<ItemStack> getSharedInventory() {
        return sharedInventory;
    }
    
    public void setSharedInventory(NonNullList<ItemStack> items) {
        this.sharedInventory = items;
        setDirty();
    }
    
    public ItemStack getItem(int slot) {
        if (slot >= 0 && slot < sharedInventory.size()) {
            return sharedInventory.get(slot);
        }
        return ItemStack.EMPTY;
    }
    
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < sharedInventory.size()) {
            sharedInventory.set(slot, stack);
            setDirty();
        }
    }
    
    public ItemStack removeItem(int slot, int count) {
        if (slot >= 0 && slot < sharedInventory.size() && !sharedInventory.get(slot).isEmpty() && count > 0) {
            ItemStack result = sharedInventory.get(slot).split(count);
            if (sharedInventory.get(slot).isEmpty()) {
                sharedInventory.set(slot, ItemStack.EMPTY);
            }
            setDirty();
            return result;
        }
        return ItemStack.EMPTY;
    }
    
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot >= 0 && slot < sharedInventory.size()) {
            ItemStack result = sharedInventory.get(slot);
            sharedInventory.set(slot, ItemStack.EMPTY);
            setDirty();
            return result;
        }
        return ItemStack.EMPTY;
    }
    
    public boolean isEmpty() {
        for (ItemStack stack : sharedInventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public void clearContent() {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            sharedInventory.set(i, ItemStack.EMPTY);
        }
        setDirty();
    }
    
    public static int getInventorySize() {
        return INVENTORY_SIZE;
    }
}
