package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.init.ModBlocks;
import com.dracolich777.afterlifeentombed.init.ModDimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "afterlifeentombed")
public class PortalActivationHandler {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!(event.getLevel() instanceof Level world)) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState placedState = event.getPlacedBlock();

        if (placedState.is(ModBlocks.DUSKSTEEL.get())) {
            if (isValidPortalPattern(world, pos)) {
                activatePortal(world, pos);
            }
        }

        if (placedState.is(Blocks.FIRE)) {
            BlockPos belowPos = pos.below();
            if (world.getBlockState(belowPos).is(ModBlocks.DUSKSTEEL.get()) && isValidPortalPattern(world, belowPos)) {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                activatePortal(world, belowPos);
            }
        }
    }

    private static boolean isValidPortalPattern(Level world, BlockPos center) {
        if (!world.getBlockState(center).is(ModBlocks.DUSKSTEEL.get())) {
            return false;
        }

        BlockPos[] goldPositions = {center.north(), center.south(), center.east(), center.west()};
        for (BlockPos pos : goldPositions) {
            if (!world.getBlockState(pos).is(Blocks.GOLD_BLOCK)) {
                return false;
            }
        }

        BlockPos[] cornerPositions = {
            center.north().east(), center.north().west(),
            center.south().east(), center.south().west()
        };

        for (BlockPos pos : cornerPositions) {
            if (!world.getBlockState(pos).is(Blocks.CHISELED_SANDSTONE)) {
                return false;
            }
        }

        return true;
    }

    private static void activatePortal(Level world, BlockPos center) {
        // Prevent portal activation in Nether and End
        if (world.dimension() == Level.NETHER || world.dimension() == Level.END) {
            return;
        }

        BlockPos portalPos = center.above();

        // Only place portal if it doesn't already exist
        if (!world.getBlockState(portalPos).is(ModBlocks.DUAT_PORTAL.get())) {
            world.setBlock(portalPos, ModBlocks.DUAT_PORTAL.get().defaultBlockState(), 3);
        }

        // Create corresponding portal in the Duat dimension if we're in the Overworld
        if (world.dimension() == Level.OVERWORLD) {
            createDuatPortal(world, center);
        }

        world.playSound(null, center, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0F, 1.0F);
        world.playSound(null, center, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static void createDuatPortal(Level world, BlockPos originalPos) {
        if (world.isClientSide) {
            return;
        }

        ServerLevel duatWorld = world.getServer().getLevel(ModDimensions.DUAT_KEY);
        if (duatWorld == null) {
            return;
        }

        // Scale coordinates: 1 block in Duat = 42 blocks in Overworld
        // So Overworld to Duat is divide by 42
        int duatX = originalPos.getX() / 42;
        int duatZ = originalPos.getZ() / 42;
        
        BlockPos duatFramePos = new BlockPos(duatX, 100, duatZ);
        BlockPos duatPortalPos = duatFramePos.above();

        // Only create frame and portal if they don't already exist
        if (!duatWorld.getBlockState(duatPortalPos).is(ModBlocks.DUAT_PORTAL.get())) {
            createPortalFrame(duatWorld, duatFramePos);
            duatWorld.setBlock(duatPortalPos, ModBlocks.DUAT_PORTAL.get().defaultBlockState(), 3);
        }
    }

    private static void createPortalFrame(Level world, BlockPos center) {
        BlockState core, cross, corners;
        
        if (world.dimension() == ModDimensions.DUAT_KEY) {
            // In Duat dimension - use ice-themed blocks
            core = Blocks.PACKED_ICE.defaultBlockState();
            cross = Blocks.CUT_RED_SANDSTONE.defaultBlockState();
            corners = Blocks.CHISELED_RED_SANDSTONE.defaultBlockState();
        } else {
            // In Overworld - use standard blocks
            core = ModBlocks.DUSKSTEEL.get().defaultBlockState();
            cross = Blocks.GOLD_BLOCK.defaultBlockState();
            corners = Blocks.CHISELED_SANDSTONE.defaultBlockState();
        }

        // Place cross pattern
        BlockPos[] crossPositions = {
            center.north(), center.south(), center.east(), center.west()
        };
        for (BlockPos pos : crossPositions) {
            world.setBlock(pos, cross, 3);
        }

        // Place corners
        BlockPos[] cornerPositions = {
            center.north().east(), center.north().west(),
            center.south().east(), center.south().west()
        };
        for (BlockPos pos : cornerPositions) {
            world.setBlock(pos, corners, 3);
        }

        // Place center
        world.setBlock(center, core, 3);
    }
}