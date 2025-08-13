package com.dracolich777.afterlifeentombed.mixins;

import com.dracolich777.afterlifeentombed.items.GodseekerSword;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class GodseekerSwordClickMixin {
    
    @Shadow
    protected abstract Slot getSlotUnderMouse();
    
    @Shadow
    private ItemStack draggingItem;
    

    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        Slot hoveredSlot = getSlotUnderMouse();
        
        if (hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack hoveredItem = hoveredSlot.getItem();
            
            // Check if we're hovering over a Godseeker Sword
            if (hoveredItem.getItem() instanceof GodseekerSword) {
                // Calculate if the click is within the hovering slot area
                int slotX = (int) mouseX - 8;
                int slotY = (int) mouseY + 25;
                
                // Check if click is within the 18x18 slot area
                if (mouseX >= slotX && mouseX <= slotX + 18 && 
                    mouseY >= slotY && mouseY <= slotY + 18) {
                    
                    if (button == 0) { // Left click
                        handleGodseekerSlotClick(hoveredItem);
                        cir.setReturnValue(true); // Consume the click
                    } else if (button == 1) { // Right click
                        handleGodseekerSlotRightClick(hoveredItem);
                        cir.setReturnValue(true); // Consume the click
                    }
                }
            }
        }
    }
    
    private void handleGodseekerSlotClick(ItemStack swordStack) {
        ItemStack currentGodstone = GodseekerSword.getGodstoneeItem(swordStack);
        
        if (!draggingItem.isEmpty()) {
            // Player is dragging an item
            if (GodseekerSword.isValidGodstone(draggingItem)) {
                // Insert the godstone
                if (currentGodstone.isEmpty()) {
                    // Slot is empty, insert the godstone
                    ItemStack toInsert = draggingItem.copy();
                    toInsert.setCount(1);
                    GodseekerSword.setGodstoneeItem(swordStack, toInsert);
                    draggingItem.shrink(1);
                    if (draggingItem.isEmpty()) {
                        draggingItem = ItemStack.EMPTY;
                    }
                } else {
                    // Slot has a godstone, swap them
                    ItemStack temp = currentGodstone.copy();
                    ItemStack toInsert = draggingItem.copy();
                    toInsert.setCount(1);
                    GodseekerSword.setGodstoneeItem(swordStack, toInsert);
                    draggingItem.shrink(1);
                    if (draggingItem.isEmpty()) {
                        draggingItem = temp;
                    } else {
                        // Try to add the removed godstone back to inventory
                        addToPlayerInventory(temp);
                    }
                }
            }
        } else {
            // Player is not dragging anything
            if (!currentGodstone.isEmpty()) {
                // Pick up the godstone
                draggingItem = currentGodstone.copy();
                GodseekerSword.setGodstoneeItem(swordStack, ItemStack.EMPTY);
            }
        }
    }
    
    private void handleGodseekerSlotRightClick(ItemStack swordStack) {
        ItemStack currentGodstone = GodseekerSword.getGodstoneeItem(swordStack);
        
        if (!currentGodstone.isEmpty()) {
            // Remove the godstone and try to add it to inventory
            GodseekerSword.setGodstoneeItem(swordStack, ItemStack.EMPTY);
            addToPlayerInventory(currentGodstone);
        }
    }
    
    private void addToPlayerInventory(ItemStack item) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (!player.getInventory().add(item)) {
                // If inventory is full, drop the item
                player.drop(item, false);
            }
        }
    }
}