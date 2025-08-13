package com.dracolich777.afterlifeentombed.client;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.init.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.Color; // <--- ADD THIS IMPORT!

import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, value = Dist.CLIENT)
public class DevWrenchHudOverlay {

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        boolean isDevWrenchEquipped = CuriosApi.getCuriosHelper().findFirstCurio(player, ModItems.DEV_WRENCH.get()).isPresent();

        if (isDevWrenchEquipped) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            int x = 5;
            int y = 5;
            int lineHeight = 10;

            String coords = String.format("Coords: X:%.0f Y:%.0f Z:%.0f", player.getX(), player.getY(), player.getZ());
            guiGraphics.drawString(minecraft.font, Component.literal(coords), x, y, Color.CYAN.getRGB(), false);
            y += lineHeight;

            HitResult hitResult = minecraft.hitResult;
            if (hitResult != null) {
                if (hitResult.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                    if (entityHitResult.getEntity() instanceof LivingEntity targetEntity) {
                        String targetInfo = String.format("Target: %s Health: %.1f/%.1f",
                                targetEntity.getName().getString(), targetEntity.getHealth(), targetEntity.getMaxHealth());
                        guiGraphics.drawString(minecraft.font, Component.literal(targetInfo), x, y, Color.RED.getRGB(), false);
                    }
                } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    String blockInfo = String.format("Block: %s Pos: %.0f, %.0f, %.0f",
                            player.level().getBlockState(blockHitResult.getBlockPos()).getBlock().getName().getString(),
                            (double)blockHitResult.getBlockPos().getX(), (double)blockHitResult.getBlockPos().getY(), (double)blockHitResult.getBlockPos().getZ());
                    guiGraphics.drawString(minecraft.font, Component.literal(blockInfo), x, y, Color.GREEN.getRGB(), false);
                }
            }
        }
    }
}