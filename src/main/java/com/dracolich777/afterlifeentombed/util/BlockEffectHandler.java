package com.dracolich777.afterlifeentombed.util;

import com.dracolich777.afterlifeentombed.init.ModBlocks;
import com.dracolich777.afterlifeentombed.init.ModEffects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEffectHandler {

    public static void handlePlayerBlockEffects(Player player) {
        Level level = player.level();
        BlockPos posBelow = player.blockPosition().below();
        BlockState blockState = level.getBlockState(posBelow);

        if (blockState.getBlock() == ModBlocks.DUSKSAND.get()) {
            applyDarkness(player);
        }
        if (blockState.getBlock() == ModBlocks.SETH_CURSE_BLOCK.get()) {
            applySethCurse(player);
        }
        if (blockState.getBlock() == ModBlocks.GEB_CURSE_BLOCK.get()) {
            applyGebCurse(player);
        }
        if (blockState.getBlock() == ModBlocks.THOTH_CURSE_BLOCK.get()) {
            applyThothCurse(player);
        }
    }

    private static void applyDarkness(Player player) {
        int duration = 60; // 3 seconds (20 ticks per second)
        int amplifier = 0;

        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, duration, amplifier, false, true, true));
    }
    private static void applySethCurse(Player player) {
        int duration = 240; // 3 seconds (20 ticks per second)
        int amplifier = 0;

        player.addEffect(new MobEffectInstance(ModEffects.REVENGE_OF_SETH.get(), duration, amplifier, false, true, true));
    }
    private static void applyGebCurse(Player player) {
        int duration = 240; // 3 seconds (20 ticks per second)
        int amplifier = 0;

        player.addEffect(new MobEffectInstance(ModEffects.REVENGE_OF_GEB.get(), duration, amplifier, false, true, true));
    }
    private static void applyThothCurse(Player player) {
        int duration = 600; // 3 seconds (20 ticks per second)
        int amplifier = 0;

        player.addEffect(new MobEffectInstance(ModEffects.REVENGE_OF_THOTH.get(), duration, amplifier, false, true, true));
    }
}
