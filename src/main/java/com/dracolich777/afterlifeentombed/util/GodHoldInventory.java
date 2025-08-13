package com.dracolich777.afterlifeentombed.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

public class GodHoldInventory extends SavedData {
    private static final String DATA_NAME = AfterlifeEntombedMod.MOD_ID + "_godhold_inventories";
    private static final int INVENTORY_SIZE = 54; // Double chest size
    
    // Map of inventory ID to items
    private Map<UUID, NonNullList<ItemStack>> inventories = new HashMap<>();
    
    public static GodHoldInventory get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(GodHoldInventory::load, GodHoldInventory::new, DATA_NAME);
    }
    
    public static GodHoldInventory load(CompoundTag tag) {
        GodHoldInventory data = new GodHoldInventory();
        
        CompoundTag inventoriesTag = tag.getCompound("inventories");
        for (String key : inventoriesTag.getAllKeys()) {
            UUID id = UUID.fromString(key);
            NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
            
            ListTag itemsTag = inventoriesTag.getList(key, 10);
            for (int i = 0; i < itemsTag.size(); i++) {
                CompoundTag itemTag = itemsTag.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < items.size()) {
                    items.set(slot, ItemStack.of(itemTag));
                }
            }
            data.inventories.put(id, items);
        }
        
        return data;
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag inventoriesTag = new CompoundTag();
        
        for (Map.Entry<UUID, NonNullList<ItemStack>> entry : inventories.entrySet()) {
            ListTag itemsTag = new ListTag();
            NonNullList<ItemStack> items = entry.getValue();
            
            for (int i = 0; i < items.size(); i++) {
                ItemStack stack = items.get(i);
                if (!stack.isEmpty()) {
                    CompoundTag itemTag = new CompoundTag();
                    itemTag.putInt("Slot", i);
                    stack.save(itemTag);
                    itemsTag.add(itemTag);
                }
            }
            
            inventoriesTag.put(entry.getKey().toString(), itemsTag);
        }
        
        tag.put("inventories", inventoriesTag);
        return tag;
    }
    
    public NonNullList<ItemStack> getInventory(UUID inventoryId) {
        return inventories.computeIfAbsent(inventoryId, k -> NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY));
    }
    
    public void setInventory(UUID inventoryId, NonNullList<ItemStack> items) {
        inventories.put(inventoryId, items);
        setDirty();
    }
    
    public void markDirty() {
        setDirty();
    }
    
    public UUID createNewInventory() {
        UUID newId = UUID.randomUUID();
        inventories.put(newId, NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY));
        setDirty();
        return newId;
    }
    
    public static int getInventorySize() {
        return INVENTORY_SIZE;
    }
}
