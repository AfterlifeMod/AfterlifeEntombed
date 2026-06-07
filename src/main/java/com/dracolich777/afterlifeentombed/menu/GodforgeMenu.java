package com.dracolich777.afterlifeentombed.menu;

import com.dracolich777.afterlifeentombed.blockentities.GodforgeBlockEntity;
import com.dracolich777.afterlifeentombed.init.ModMenuTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

import com.dracolich777.afterlifeentombed.init.ModBlocks;

import javax.annotation.Nonnull;

public class GodforgeMenu extends AbstractContainerMenu {
    private final GodforgeBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    
    // Client constructor
    public GodforgeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(2));
    }
    
    // Server constructor
    public GodforgeMenu(int containerId, Inventory playerInventory, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.GODFORGE_MENU.get(), containerId);
        checkContainerSize(playerInventory, 5);
        this.blockEntity = ((GodforgeBlockEntity) entity);
        this.level = playerInventory.player.level();
        this.data = data;
        
        // Add player hotbar first (slots 0-8)
        addPlayerHotbar(playerInventory);
        // Add player inventory second (slots 9-35)
        addPlayerInventory(playerInventory);
        
        // Add Godforge inventory last (slots 36-40)
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // Fuel slot (top-left) - blaze powder slot for future use
            this.addSlot(new SlotItemHandler(handler, GodforgeBlockEntity.FUEL_SLOT, 17, 17));
            
            // Red slot (top-center) - item to be modified/receive enchantment
            this.addSlot(new SlotItemHandler(handler, GodforgeBlockEntity.TARGET_ITEM_SLOT, 79, 17));
            
            // Purple slot (right-top) - named book with enchantment name
            this.addSlot(new SlotItemHandler(handler, GodforgeBlockEntity.BOOK_SLOT, 102, 29));
            
            // Purple slot (right-bottom) - enchanted item to consume
            this.addSlot(new SlotItemHandler(handler, GodforgeBlockEntity.SOURCE_ENCHANTED_SLOT, 102, 52));
            
            // Gold slot (bottom-center) - output slot
            this.addSlot(new SlotItemHandler(handler, GodforgeBlockEntity.OUTPUT_SLOT, 79, 58) {
                @Override
                public boolean mayPlace(@Nonnull ItemStack stack) {
                    return false; // Cannot place items in output slot
                }
            });
        });
        
        addDataSlots(data);
    }
    
    // Slot layout constants
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 5;
    
    @Override
    public @Nonnull ItemStack quickMoveStack(@Nonnull Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();
        
        // Check if the slot clicked is one of the vanilla container slots
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory (excluding output)
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, 
                    TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT - 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, 
                    VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }
    
    @Override
    public boolean stillValid(@Nonnull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.GODFORGE.get());
    }
    
    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }
    
    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
    
    public int getProgress() {
        return data.get(0);
    }
    
    public int getMaxProgress() {
        return data.get(1);
    }
    
    public boolean isCrafting() {
        return data.get(0) > 0 && data.get(1) > 0;
    }
}
