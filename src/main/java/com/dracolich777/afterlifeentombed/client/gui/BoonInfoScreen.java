package com.dracolich777.afterlifeentombed.client.gui;

import com.dracolich777.afterlifeentombed.boons.ActiveBoon;
import com.dracolich777.afterlifeentombed.capabilities.PlayerBoonsCapability;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Screen showing all active boons and curses with descriptions.
 * Accessed via info button in inventory screen.
 */
public class BoonInfoScreen extends Screen {
    
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("minecraft", "textures/gui/demo_background.png");
    
    private final Screen previousScreen;
    private List<ActiveBoon> boons;
    
    private static final int PADDING = 10;
    private static final int ICON_SIZE = 16;
    private static final int ROW_HEIGHT = 24;
    
    public BoonInfoScreen(Screen previousScreen) {
        super(Component.literal("Divine Boons & Curses"));
        this.previousScreen = previousScreen;
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Load boons from capability
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(cap -> {
                this.boons = cap.getActiveBoons();
            });
        }
        
        // Add close button
        this.addRenderableWidget(Button.builder(
            Component.literal("Close"),
            button -> this.onClose()
        ).bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Dark background
        guiGraphics.fill(0, 0, this.width, this.height, 0xCC000000);
        
        // Title
        Component title = Component.literal("§6Divine Boons & Curses");
        guiGraphics.drawCenteredString(this.font, title, this.width / 2, 15, 0xFFFFFF);
        
        if (boons == null || boons.isEmpty()) {
            // No boons message
            Component noBoons = Component.literal("§7You have no active boons or curses");
            guiGraphics.drawCenteredString(this.font, noBoons, this.width / 2, this.height / 2, 0xAAAAAA);
        } else {
            // Render boons list
            renderBoonsList(guiGraphics, mouseX, mouseY);
        }
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private void renderBoonsList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int startX = this.width / 2 - 150;
        int startY = 40;
        
        int currentY = startY;
        
        // Separate blessings and curses
        List<ActiveBoon> blessings = boons.stream()
            .filter(b -> b.getType().isBlessing())
            .toList();
        List<ActiveBoon> curses = boons.stream()
            .filter(b -> b.getType().isCurse())
            .toList();
        
        // Render blessings section
        if (!blessings.isEmpty()) {
            Component blessingsHeader = Component.literal("§6✦ Blessings");
            guiGraphics.drawString(this.font, blessingsHeader, startX, currentY, 0xFFD700);
            currentY += 15;
            
            for (ActiveBoon blessing : blessings) {
                currentY = renderBoonRow(guiGraphics, blessing, startX, currentY, true, mouseX, mouseY);
            }
            
            currentY += 10; // Spacing between sections
        }
        
        // Render curses section
        if (!curses.isEmpty()) {
            Component cursesHeader = Component.literal("§c✦ Curses");
            guiGraphics.drawString(this.font, cursesHeader, startX, currentY, 0xFF0000);
            currentY += 15;
            
            for (ActiveBoon curse : curses) {
                currentY = renderBoonRow(guiGraphics, curse, startX, currentY, false, mouseX, mouseY);
            }
        }
    }
    
    private int renderBoonRow(GuiGraphics guiGraphics, ActiveBoon boon, int x, int y, boolean isBlessing, int mouseX, int mouseY) {
        // Icon
        ItemStack icon = new ItemStack(boon.getType().getIcon());
        guiGraphics.renderItem(icon, x, y);
        
        // Name
        String colorCode = isBlessing ? "§6" : "§c";
        Component name = Component.literal(colorCode + boon.getType().getDisplayName());
        guiGraphics.drawString(this.font, name, x + ICON_SIZE + 4, y, 0xFFFFFF);
        
        // Duration info
        long currentTime = minecraft.player.level().getGameTime();
        String durationText = getDurationText(boon, currentTime);
        Component duration = Component.literal("§7" + durationText);
        guiGraphics.drawString(this.font, duration, x + ICON_SIZE + 4, y + 10, 0xAAAAAA);
        
        // Check if mouse is hovering over this row
        boolean hovering = mouseX >= x && mouseX < x + 300 && mouseY >= y && mouseY < y + ROW_HEIGHT;
        
        if (hovering) {
            // Render tooltip with description
            Component desc = Component.literal("§7" + boon.getType().getDescription());
            guiGraphics.renderTooltip(this.font, desc, mouseX, mouseY);
        }
        
        return y + ROW_HEIGHT;
    }
    
    private String getDurationText(ActiveBoon boon, long currentTime) {
        return switch (boon.getType().getDuration()) {
            case PERMANENT -> "Permanent";
            case ONE_USE -> {
                int uses = boon.getUsesRemaining();
                yield uses > 0 ? (uses + " use" + (uses == 1 ? "" : "s") + " remaining") : "Used";
            }
            case TEMPORARY -> {
                long remaining = boon.getRemainingTime(currentTime);
                if (remaining <= 0) yield "Expired";
                long seconds = remaining / 20;
                long minutes = seconds / 60;
                long secs = seconds % 60;
                if (minutes > 0) {
                    yield minutes + "m " + secs + "s";
                } else {
                    yield seconds + "s";
                }
            }
        };
    }
    
    @Override
    public void onClose() {
        // Return to previous screen (inventory)
        if (minecraft != null) {
            minecraft.setScreen(previousScreen);
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
