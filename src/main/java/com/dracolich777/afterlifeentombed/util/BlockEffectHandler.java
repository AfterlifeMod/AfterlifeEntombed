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
        if (blockState.getBlock() == ModBlocks.RA_CURSE_BLOCK.get()) {
            applyRaCurse(player);
        }
        if (blockState.getBlock() == ModBlocks.HORUS_CURSE_BLOCK.get()) {
            applyHorusCurse(player);
        }
        if (blockState.getBlock() == ModBlocks.SHU_CURSE_BLOCK.get()) {
            applyShuCurse(player);
        }
        if (blockState.getBlock() == ModBlocks.ISIS_CURSE_BLOCK.get()) {
            applyIsisCurse(player);
        }
        if (blockState.getBlock() == ModBlocks.ANUBIS_CURSE_BLOCK.get()) {
            applyAnubisCurse(player);
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

    private static void applyRaCurse(Player player) {
        int duration = 240; // 12 seconds (20 ticks per second)
        int amplifier = 0;

        player.addEffect(new MobEffectInstance(ModEffects.HOLY_FIRE.get(), duration, amplifier, false, true, true));
    }

    private static void applyHorusCurse(Player player) {
        int duration = 240; // 12 seconds (20 ticks per second)
        int amplifier = 0;

        player.addEffect(new MobEffectInstance(ModEffects.REVENGE_OF_HORUS.get(), duration, amplifier, false, true, true));
    }

    private static void applyShuCurse(Player player) {
        int duration = 240; // 12 seconds (20 ticks per second)
        int amplifier = 0;

        player.addEffect(new MobEffectInstance(ModEffects.REVENGE_OF_SHU.get(), duration, amplifier, false, true, true));
    }

    private static void applyIsisCurse(Player player) {
        int duration = 240; // 12 seconds (20 ticks per second)
        int amplifier = 0;

        player.addEffect(new MobEffectInstance(ModEffects.REVENGE_OF_ISIS.get(), duration, amplifier, false, true, true));
    }

    private static void applyAnubisCurse(Player player) {
        int duration = 240; // 12 seconds (20 ticks per second)
        int amplifier = 0;

        player.addEffect(new MobEffectInstance(ModEffects.REVENGE_OF_ANUBIS.get(), duration, amplifier, false, true, true));
    }
}
