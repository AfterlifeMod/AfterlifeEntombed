package com.dracolich777.afterlifeentombed.client.gui;

import com.dracolich777.afterlifeentombed.boons.BoonType;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.SelectBoonPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Screen for selecting a blessing or curse from a god.
 * Displays 3 choices in a triangle formation with hotbar-style slots.
 */
public class BoonSelectionScreen extends Screen {
    
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    
    private final GodType god;
    private final BoonType[] choices;
    private final boolean isBlessing;
    
    // Slot positions (triangle formation)
    private BoonSlot[] slots;
    private BoonSlot hoveredSlot = null;
    
    // Slot size (matching hotbar slot)
    private static final int SLOT_SIZE = 20;
    private static final int SLOT_SPACING = 30;
    
    public BoonSelectionScreen(GodType god, BoonType[] choices, boolean isBlessing) {
        super(Component.literal(isBlessing ? "Divine Blessing" : "Divine Curse"));
        this.god = god;
        this.choices = choices;
        this.isBlessing = isBlessing;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Create 3 slots in triangle formation
        // Top slot
        int topX = centerX - SLOT_SIZE / 2;
        int topY = centerY - 60;
        
        // Bottom left slot
        int bottomLeftX = centerX - SLOT_SPACING - SLOT_SIZE / 2;
        int bottomLeftY = centerY + 20;
        
        // Bottom right slot
        int bottomRightX = centerX + SLOT_SPACING - SLOT_SIZE / 2;
        int bottomRightY = centerY + 20;
        
        slots = new BoonSlot[Math.min(3, choices.length)];
        
        if (choices.length > 0) {
            slots[0] = new BoonSlot(topX, topY, choices[0]);
        }
        if (choices.length > 1) {
            slots[1] = new BoonSlot(bottomLeftX, bottomLeftY, choices[1]);
        }
        if (choices.length > 2) {
            slots[2] = new BoonSlot(bottomRightX, bottomRightY, choices[2]);
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Dark semi-transparent background
        guiGraphics.fill(0, 0, this.width, this.height, 0xCC000000);
        
        int centerX = this.width / 2;
        int titleY = this.height / 2 - 100;
        
        // Title: "You have received a blessing from [GOD]" in gold
        String titleText = isBlessing ? 
            "You have received a blessing from " + god.name() :
            "You have been cursed by " + god.name();
        Component title = Component.literal("§6" + titleText);
        guiGraphics.drawCenteredString(this.font, title, centerX, titleY, 0xFFFFFF);
        
        // Subtitle
        Component subtitle = Component.literal("§7Choose wisely...");
        guiGraphics.drawCenteredString(this.font, subtitle, centerX, titleY + 15, 0xAAAAAA);
        
        // Determine hovered slot
        hoveredSlot = null;
        for (BoonSlot slot : slots) {
            if (slot != null && slot.isMouseOver(mouseX, mouseY)) {
                hoveredSlot = slot;
                break;
            }
        }
        
        // Render slots
        for (BoonSlot slot : slots) {
            if (slot != null) {
                slot.render(guiGraphics, slot == hoveredSlot);
            }
        }
        
        // Render tooltip for hovered slot
        if (hoveredSlot != null) {
            renderBoonTooltip(guiGraphics, hoveredSlot.boon, mouseX, mouseY);
        }
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            for (BoonSlot slot : slots) {
                if (slot != null && slot.isMouseOver((int) mouseX, (int) mouseY)) {
                    selectBoon(slot.boon);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    /**
     * Handle boon selection
     */
    private void selectBoon(BoonType boon) {
        // Send packet to server
        GodAvatarPackets.INSTANCE.sendToServer(new SelectBoonPacket(boon));
        
        // Close this screen
        this.onClose();
    }
    
    /**
     * Render tooltip for a boon
     */
    private void renderBoonTooltip(GuiGraphics guiGraphics, BoonType boon, int mouseX, int mouseY) {
        Component name = Component.literal("§" + (isBlessing ? "6" : "c") + boon.getDisplayName());
        Component desc = Component.literal("§7" + boon.getDescription());
        
        guiGraphics.renderTooltip(this.font, java.util.List.of(name, desc), java.util.Optional.empty(), mouseX, mouseY);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false; // Don't pause the game
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        // Only allow ESC to close for blessings, not curses
        return isBlessing;
    }
    
    /**
     * Represents a boon selection slot
     */
    private class BoonSlot {
        private final int x;
        private final int y;
        private final BoonType boon;
        private final ItemStack icon;
        
        public BoonSlot(int x, int y, BoonType boon) {
            this.x = x;
            this.y = y;
            this.boon = boon;
            this.icon = new ItemStack(boon.getIcon());
        }
        
        public boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX < x + SLOT_SIZE &&
                   mouseY >= y && mouseY < y + SLOT_SIZE;
        }
        
        public void render(GuiGraphics guiGraphics, boolean hovered) {
            // Draw slot background (from widgets texture)
            RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
            
            // Hotbar slot is at UV (0, 0) in widgets.png, size 22x22
            // We'll draw slightly smaller (20x20)
            int u = 0;
            int v = 0;
            
            if (hovered) {
                // Render glow effect when hovered
                guiGraphics.fill(x - 2, y - 2, x + SLOT_SIZE + 2, y + SLOT_SIZE + 2, 
                    isBlessing ? 0xFFFFD700 : 0xFFFF0000);
            }
            
            // Draw slot background
            guiGraphics.blit(WIDGETS_LOCATION, x - 1, y - 1, u, v, 22, 22);
            
            // Draw icon
            guiGraphics.renderItem(icon, x + 2, y + 2);
            
            // Draw enchantment glint if permanent
            if (boon.getDuration() == BoonType.BoonDuration.PERMANENT) {
                guiGraphics.renderItemDecorations(font, icon, x + 2, y + 2);
            }
        }
    }
}
