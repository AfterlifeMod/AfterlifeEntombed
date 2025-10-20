package com.dracolich777.afterlifeentombed.client.gui;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.init.ModItems;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.SwitchGodPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Radial menu for selecting which unlocked god to transform into
 * Appears while holding the selection key
 */
public class GodSelectionScreen extends Screen {
    
    private final Set<GodType> unlockedGods;
    private final GodType currentGod;
    private final List<RadialOption> radialOptions = new ArrayList<>();
    private RadialOption hoveredOption = null;
    
    // Radial menu configuration
    private static final int RADIUS = 80;
    private static final int ICON_SIZE = 32;
    private static final int CENTER_DEADZONE = 20;
    
    public GodSelectionScreen(Set<GodType> unlockedGods, GodType currentGod) {
        super(Component.literal("Select Your Avatar"));
        this.unlockedGods = unlockedGods;
        this.currentGod = currentGod;
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Clear previous options
        radialOptions.clear();
        
        // Create radial options for each unlocked god
        List<GodType> sortedGods = new ArrayList<>(unlockedGods);
        sortedGods.sort(Comparator.comparing(Enum::name));
        
        int count = sortedGods.size();
        double angleStep = 360.0 / count;
        double startAngle = -90.0; // Start at top
        
        for (int i = 0; i < count; i++) {
            GodType god = sortedGods.get(i);
            double angle = Math.toRadians(startAngle + i * angleStep);
            
            int x = (int) (Math.cos(angle) * RADIUS);
            int y = (int) (Math.sin(angle) * RADIUS);
            
            ItemStack godstone = getGodstoneForGod(god);
            radialOptions.add(new RadialOption(god, x, y, godstone, angle));
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Semi-transparent dark overlay
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // If no gods are unlocked, show message and return
        if (unlockedGods.isEmpty()) {
            Component title = Component.literal("§cNo Gods Unlocked");
            Component subtitle = Component.literal("§7Consume a godstone to unlock a god");
            guiGraphics.drawCenteredString(this.font, title, centerX, centerY - 10, 0xFFFFFF);
            guiGraphics.drawCenteredString(this.font, subtitle, centerX, centerY + 5, 0xFFFFFF);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }
        
        // Calculate cursor position relative to center
        int cursorX = mouseX - centerX;
        int cursorY = mouseY - centerY;
        double cursorDistance = Math.sqrt(cursorX * cursorX + cursorY * cursorY);
        
        // Determine hovered option based on cursor angle
        hoveredOption = null;
        if (cursorDistance > CENTER_DEADZONE && !radialOptions.isEmpty()) {
            double cursorAngle = Math.atan2(cursorY, cursorX);
            
            // Find closest option by angle
            RadialOption closest = null;
            double minAngleDiff = Double.MAX_VALUE;
            
            for (RadialOption option : radialOptions) {
                double angleDiff = Math.abs(normalizeAngle(cursorAngle - option.angle));
                if (angleDiff < minAngleDiff) {
                    minAngleDiff = angleDiff;
                    closest = option;
                }
            }
            
            hoveredOption = closest;
        }
        
        // Draw center circle
        int centerRadius = 30;
        guiGraphics.fill(centerX - centerRadius, centerY - centerRadius, 
                        centerX + centerRadius, centerY + centerRadius, 0xDD222222);
        
        // Draw center circle border
        drawCircleOutline(guiGraphics, centerX, centerY, centerRadius, 0xFFFFFFFF);
        
        // Draw current god text in center
        Component currentText = Component.literal("§e" + currentGod.name());
        guiGraphics.drawCenteredString(this.font, currentText, centerX, centerY - 4, 0xFFFFFF);
        
        // Draw radial options
        for (RadialOption option : radialOptions) {
            boolean isHovered = option == hoveredOption;
            boolean isCurrent = option.god == currentGod;
            renderRadialOption(guiGraphics, option, centerX, centerY, isHovered, isCurrent);
        }
        
        // Draw selection indicator line to hovered option
        if (hoveredOption != null && cursorDistance > CENTER_DEADZONE) {
            int optionX = centerX + hoveredOption.x;
            int optionY = centerY + hoveredOption.y;
            
            // Draw line from center to option
            drawLine(guiGraphics, centerX, centerY, optionX, optionY, 0xAAFFFFFF);
            
            // Draw hovered god name at bottom
            Component hoveredText = Component.literal("§6§l" + hoveredOption.god.name());
            guiGraphics.drawCenteredString(this.font, hoveredText, centerX, this.height - 30, 0xFFFFFF);
        }
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private void renderRadialOption(GuiGraphics guiGraphics, RadialOption option, int centerX, int centerY, 
                                    boolean isHovered, boolean isCurrent) {
        int x = centerX + option.x;
        int y = centerY + option.y;
        
        // Determine colors
        int bgColor = isCurrent ? 0xDD4CAF50 : (isHovered ? 0xDD2196F3 : 0xDD555555);
        int borderColor = isHovered ? 0xFFFFFFFF : 0xFF888888;
        
        // Draw background circle
        int iconRadius = ICON_SIZE / 2;
        guiGraphics.fill(x - iconRadius, y - iconRadius, x + iconRadius, y + iconRadius, bgColor);
        
        // Draw border
        drawBoxOutline(guiGraphics, x - iconRadius, y - iconRadius, x + iconRadius, y + iconRadius, borderColor);
        
        // Draw godstone icon
        if (!option.godstone.isEmpty()) {
            guiGraphics.renderItem(option.godstone, x - 8, y - 8);
        }
        
        // Draw checkmark if current god
        if (isCurrent) {
            Component check = Component.literal("§a✓");
            guiGraphics.drawCenteredString(this.font, check, x, y - iconRadius - 12, 0xFFFFFF);
        }
    }
    
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        // Prevent camera rotation while radial menu is open
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredOption != null) {
            // Left click on a hovered option - select it and close
            onGodSelected(hoveredOption.god);
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    /**
     * Called by the key handler when the menu should close and select the hovered god
     */
    public void closeAndSelect() {
        // When closing, if we have a hovered option, select it
        if (hoveredOption != null && hoveredOption.god != currentGod) {
            onGodSelected(hoveredOption.god);
        }
    }
    
    @Override
    public void onClose() {
        super.onClose();
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    private void onGodSelected(GodType god) {
        if (god == currentGod) {
            return; // Already this god
        }
        
        // Send packet to server to switch god
        GodAvatarPackets.sendToServer(new SwitchGodPacket(god));
    }
    
    private ItemStack getGodstoneForGod(GodType god) {
        Item godstone = switch (god) {
            case SETH -> ModItems.GODSTONE_OF_SETH.get();
            case RA -> ModItems.GODSTONE_OF_RA.get();
            case HORUS -> ModItems.GODSTONE_OF_HORUS.get();
            case ISIS -> ModItems.GODSTONE_OF_ISIS.get();
            case GEB -> ModItems.GODSTONE_OF_GEB.get();
            case ANUBIS -> ModItems.GODSTONE_OF_ANUBIS.get();
            case SHU -> ModItems.GODSTONE_OF_SHU.get();
            case THOTH -> ModItems.GODSTONE_OF_THOTH.get();
            default -> null;
        };
        
        return godstone != null ? new ItemStack(godstone) : ItemStack.EMPTY;
    }
    
    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
    
    private void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        // Simple line drawing using filled rectangles
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = (int) Math.sqrt(dx * dx + dy * dy);
        
        for (int i = 0; i <= steps; i++) {
            int x = x1 + (dx * i / steps);
            int y = y1 + (dy * i / steps);
            guiGraphics.fill(x, y, x + 2, y + 2, color);
        }
    }
    
    private void drawCircleOutline(GuiGraphics guiGraphics, int centerX, int centerY, int radius, int color) {
        int segments = 64;
        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;
            
            int x1 = centerX + (int) (Math.cos(angle1) * radius);
            int y1 = centerY + (int) (Math.sin(angle1) * radius);
            int x2 = centerX + (int) (Math.cos(angle2) * radius);
            int y2 = centerY + (int) (Math.sin(angle2) * radius);
            
            guiGraphics.fill(x1, y1, x1 + 2, y1 + 2, color);
        }
    }
    
    private void drawBoxOutline(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        guiGraphics.fill(x1, y1, x2, y1 + 2, color); // Top
        guiGraphics.fill(x1, y2 - 2, x2, y2, color); // Bottom
        guiGraphics.fill(x1, y1, x1 + 2, y2, color); // Left
        guiGraphics.fill(x2 - 2, y1, x2, y2, color); // Right
    }
    
    /**
     * Represents a single option in the radial menu
     */
    private static class RadialOption {
        final GodType god;
        final int x;
        final int y;
        final ItemStack godstone;
        final double angle;
        
        RadialOption(GodType god, int x, int y, ItemStack godstone, double angle) {
            this.god = god;
            this.x = x;
            this.y = y;
            this.godstone = godstone;
            this.angle = angle;
        }
    }
}
