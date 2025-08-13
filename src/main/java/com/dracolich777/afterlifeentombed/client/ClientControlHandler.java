package com.dracolich777.afterlifeentombed.client;

import com.dracolich777.afterlifeentombed.init.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "afterlifeentombed", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientControlHandler {
    
    private static boolean wasEffectActive = false;
    private static double originalMouseSensitivity = -1;
    private static boolean originalInvertYMouse = false;
    
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        
        MobEffectInstance effect = player.getEffect(ModEffects.REVENGE_OF_SETH.get());
        if (effect == null) return;
        
        // Get the key mappings
        var keyMappings = Minecraft.getInstance().options;
        
        // Reverse movement controls
        if (event.getKey() == keyMappings.keyUp.getKey().getValue()) {
            // W pressed, simulate S
            keyMappings.keyDown.setDown(event.getAction() != GLFW.GLFW_RELEASE);
            keyMappings.keyUp.setDown(false);
        } else if (event.getKey() == keyMappings.keyDown.getKey().getValue()) {
            // S pressed, simulate W
            keyMappings.keyUp.setDown(event.getAction() != GLFW.GLFW_RELEASE);
            keyMappings.keyDown.setDown(false);
        } else if (event.getKey() == keyMappings.keyLeft.getKey().getValue()) {
            // A pressed, simulate D
            keyMappings.keyRight.setDown(event.getAction() != GLFW.GLFW_RELEASE);
            keyMappings.keyLeft.setDown(false);
        } else if (event.getKey() == keyMappings.keyRight.getKey().getValue()) {
            // D pressed, simulate A
            keyMappings.keyLeft.setDown(event.getAction() != GLFW.GLFW_RELEASE);
            keyMappings.keyRight.setDown(false);
        } else if (event.getKey() == keyMappings.keyShift.getKey().getValue()) {
            // Shift pressed, simulate Space
            keyMappings.keyJump.setDown(event.getAction() != GLFW.GLFW_RELEASE);
            keyMappings.keyShift.setDown(false);
        } else if (event.getKey() == keyMappings.keyJump.getKey().getValue()) {
            // Space pressed, simulate Shift
            keyMappings.keyShift.setDown(event.getAction() != GLFW.GLFW_RELEASE);
            keyMappings.keyJump.setDown(false);
        }
    }
    
    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        
        MobEffectInstance effect = player.getEffect(ModEffects.REVENGE_OF_SETH.get());
        if (effect == null) return;
        
        // Reverse mouse buttons
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            // Left click becomes right click
            Minecraft.getInstance().options.keyAttack.setDown(false);
            Minecraft.getInstance().options.keyUse.setDown(event.getAction() != GLFW.GLFW_RELEASE);
        } else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            // Right click becomes left click
            Minecraft.getInstance().options.keyUse.setDown(false);
            Minecraft.getInstance().options.keyAttack.setDown(event.getAction() != GLFW.GLFW_RELEASE);
        }
    }
    
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        
        MobEffectInstance effect = player.getEffect(ModEffects.REVENGE_OF_SETH.get());
        if (effect == null) return;
        
        // Cancel the original scroll event
        event.setCanceled(true);
        
        // Simulate reversed scroll by directly manipulating the selected slot
        // Reverse the scroll direction
        double scrollDelta = event.getScrollDelta();
        if (scrollDelta > 0) {
            // Scroll up becomes scroll down
            player.getInventory().swapPaint(1);
        } else if (scrollDelta < 0) {
            // Scroll down becomes scroll up  
            player.getInventory().swapPaint(-1);
        }
    }
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        
        boolean hasEffect = player.hasEffect(ModEffects.REVENGE_OF_SETH.get());
        var options = Minecraft.getInstance().options;
        
        // Check if effect status changed
        if (hasEffect != wasEffectActive) {
            if (hasEffect) {
                // Effect just became active - store original settings and reverse them
                if (originalMouseSensitivity == -1) {
                    originalMouseSensitivity = options.sensitivity().get();
                    originalInvertYMouse = options.invertYMouse().get();
                }
                
                // Apply reversed settings
                // Negative sensitivity simulates reversed X-axis
                options.sensitivity().set(-Math.abs(originalMouseSensitivity));
                // Toggle Y-axis inversion
                options.invertYMouse().set(!originalInvertYMouse);
                
            } else {
                // Effect just became inactive - restore original settings
                if (originalMouseSensitivity != -1) {
                    options.sensitivity().set(Math.abs(originalMouseSensitivity));
                    options.invertYMouse().set(originalInvertYMouse);
                }
            }
            wasEffectActive = hasEffect;
        }
    }
}