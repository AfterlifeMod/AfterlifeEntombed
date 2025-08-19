// package com.dracolich777.afterlifeentombed.blockentities;

// import java.util.UUID;

// import com.dracolich777.afterlifeentombed.init.ModBlockEntities;
// import com.dracolich777.afterlifeentombed.menu.GodHoldMenu;
// import com.dracolich777.afterlifeentombed.util.GodHoldInventory;

// import net.minecraft.core.BlockPos;
// import net.minecraft.core.NonNullList;
// import net.minecraft.nbt.CompoundTag;
// import net.minecraft.network.chat.Component;
// import net.minecraft.server.level.ServerLevel;
// import net.minecraft.world.Containers;
// import net.minecraft.world.MenuProvider;
// import net.minecraft.world.entity.player.Inventory;
// import net.minecraft.world.entity.player.Player;
// import net.minecraft.world.inventory.AbstractContainerMenu;
// import net.minecraft.world.item.ItemStack;
// import net.minecraft.world.level.Level;
// import net.minecraft.world.level.block.entity.BlockEntity;
// import net.minecraft.world.level.block.state.BlockState;

// public class GodHoldBlockEntity extends BlockEntity implements MenuProvider {
//     private UUID inventoryId;
//     private boolean hasInventoryId = false;
    
//     public GodHoldBlockEntity(BlockPos pPos, BlockState pBlockState) {
//         super(ModBlockEntities.GODHOLD_BLOCK_ENTITY.get(), pPos, pBlockState);
//     }
    
//     @Override
//     public Component getDisplayName() {
//         return Component.translatable("container.afterlifeentombed.godhold");
//     }
    
//     @Override
//     public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
//         return new GodHoldMenu(pContainerId, pPlayerInventory, this);
//     }
    
//     public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
//         if (pLevel.isClientSide()) {
//             return;
//         }
        
//         // Ensure we have an inventory ID
//         if (!hasInventoryId) {
//             ensureInventoryId((ServerLevel) pLevel);
//         }
//     }
    
//     private void ensureInventoryId(ServerLevel level) {
//         if (inventoryId == null) {
//             GodHoldInventory data = GodHoldInventory.get(level);
//             inventoryId = data.createNewInventory();
//             hasInventoryId = true;
//             setChanged();
//         }
//     }
    
//     public void setInventoryIdFromItem(UUID id) {
//         if (!hasInventoryId && inventoryId == null) {
//             this.inventoryId = id;
//             this.hasInventoryId = true;
//             setChanged();
//         }
//     }
    
//     public UUID getInventoryId() {
//         return inventoryId;
//     }
    
//     public void setInventoryId(UUID inventoryId) {
//         this.inventoryId = inventoryId;
//         this.hasInventoryId = true;
//         setChanged();
//     }
    
//     public NonNullList<ItemStack> getItems() {
//         if (level instanceof ServerLevel serverLevel) {
//             if (inventoryId != null) {
//                 return GodHoldInventory.get(serverLevel).getInventory(inventoryId);
//             }
//         }
//         return NonNullList.withSize(GodHoldInventory.getInventorySize(), ItemStack.EMPTY);
//     }
    
//     public void setItems(NonNullList<ItemStack> items) {
//         if (level instanceof ServerLevel serverLevel) {
//             if (inventoryId != null) {
//                 GodHoldInventory.get(serverLevel).setInventory(inventoryId, items);
//             }
//         }
//     }
    
//     public ItemStack getItem(int index) {
//         NonNullList<ItemStack> items = getItems();
//         if (items == null || index < 0 || index >= items.size()) {
//             return ItemStack.EMPTY;
//         }
//         ItemStack result = items.get(index);
//         return result != null ? result : ItemStack.EMPTY;
//     }
    
//     public void setItem(int index, ItemStack stack) {
//         NonNullList<ItemStack> items = getItems();
//         if (items != null && index >= 0 && index < items.size()) {
//             items.set(index, stack != null ? stack : ItemStack.EMPTY);
//             setItems(items);
//             setChanged();
//         }
//     }
    
//     public ItemStack removeItem(int index, int count) {
//         NonNullList<ItemStack> items = getItems();
//         if (items != null && index >= 0 && index < items.size() && !items.get(index).isEmpty() && count > 0) {
//             ItemStack result = items.get(index).split(count);
//             setItems(items);
//             setChanged();
//             return result;
//         }
//         return ItemStack.EMPTY;
//     }
    
//     public ItemStack removeItemNoUpdate(int index) {
//         NonNullList<ItemStack> items = getItems();
//         if (items != null && index >= 0 && index < items.size()) {
//             ItemStack result = items.get(index);
//             items.set(index, ItemStack.EMPTY);
//             setItems(items);
//             return result;
//         }
//         return ItemStack.EMPTY;
//     }
    
//     public boolean isEmpty() {
//         NonNullList<ItemStack> items = getItems();
//         for (ItemStack stack : items) {
//             if (!stack.isEmpty()) {
//                 return false;
//             }
//         }
//         return true;
//     }
    
//     public void clearContent() {
//         NonNullList<ItemStack> items = NonNullList.withSize(GodHoldInventory.getInventorySize(), ItemStack.EMPTY);
//         setItems(items);
//         setChanged();
//     }
    
//     public int getContainerSize() {
//         return GodHoldInventory.getInventorySize();
//     }
    
//     public void dropContents() {
//         if (level != null && !level.isClientSide()) {
//             NonNullList<ItemStack> items = getItems();
//             for (ItemStack stack : items) {
//                 if (!stack.isEmpty()) {
//                     Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
//                 }
//             }
//             clearContent();
//         }
//     }
    
//     @Override
//     protected void saveAdditional(CompoundTag pTag) {
//         super.saveAdditional(pTag);
//         if (inventoryId != null) {
//             pTag.putString("InventoryId", inventoryId.toString());
//         }
//         pTag.putBoolean("HasInventoryId", hasInventoryId);
//     }
    
//     @Override
//     public void load(CompoundTag pTag) {
//         super.load(pTag);
//         if (pTag.contains("InventoryId")) {
//             inventoryId = UUID.fromString(pTag.getString("InventoryId"));
//         }
//         hasInventoryId = pTag.getBoolean("HasInventoryId");
//     }
// }
