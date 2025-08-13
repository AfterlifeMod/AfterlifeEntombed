package com.dracolich777.afterlifeentombed.mixins;

import com.dracolich777.afterlifeentombed.items.GodseekerSword;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class GodseekerSwordRenderMixin {
    
    @Shadow
    protected abstract Slot getSlotUnderMouse();
    
    @Shadow
    protected int leftPos;
    
    @Shadow
    protected int topPos;
    
    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/inventory.png");
    
    // Try different method signatures for render
    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("TAIL"))
    private void renderGodseekerSlot(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        Slot hoveredSlot = getSlotUnderMouse();
        
        if (hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack hoveredItem = hoveredSlot.getItem();
            
            // Check if the hovered item is a Godseeker Sword
            if (hoveredItem.getItem() instanceof GodseekerSword) {
                // Calculate position for the hovering slot (below the tooltip area)
                int slotX = mouseX - 8;
                int slotY = mouseY + 25; // Offset below tooltip
                
                // Make sure the slot stays within screen bounds
                if (slotX < 0) slotX = 0;
                if (slotY < 0) slotY = 0;
                
                // Draw the slot background
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, SLOT_TEXTURE);
                
                // Draw slot background (18x18 slot from inventory texture)
                guiGraphics.blit(SLOT_TEXTURE, slotX, slotY, 7, 83, 18, 18);
                
                // Get the current godstone from the sword
                ItemStack currentGodstone = GodseekerSword.getGodstoneeItem(hoveredItem);
                
                // Render the godstone item if it exists
                if (!currentGodstone.isEmpty()) {
                    guiGraphics.renderItem(currentGodstone, slotX + 1, slotY + 1);
                    guiGraphics.renderItemDecorations(((AbstractContainerScreen<?>) (Object) this).getMinecraft().font, 
                        currentGodstone, slotX + 1, slotY + 1);
                }
                
                // Draw a subtle border to indicate it's interactive
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 1, 0xFFFFFFFF); // Top
                guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 18, 0xFFFFFFFF); // Left
                guiGraphics.fill(slotX + 17, slotY, slotX + 18, slotY + 18, 0xFFFFFFFF); // Right
                guiGraphics.fill(slotX, slotY + 17, slotX + 18, slotY + 18, 0xFFFFFFFF); // Bottom
            }
        }
    }
}