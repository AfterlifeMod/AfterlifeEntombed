package com.dracolich777.afterlifeentombed.util;

import java.util.function.Function;

import com.dracolich777.afterlifeentombed.init.ModBlocks;
import com.dracolich777.afterlifeentombed.init.ModDimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.ITeleporter;

public class DuatTeleporter {

    /**
     * Teleport a player from their current portal to the corresponding portal
     * in the other dimension
     */
    public static void teleportToCorrespondingPortal(ServerPlayer player, BlockPos currentPortalPos) {
        ServerLevel currentWorld = player.serverLevel();

        if (currentWorld.dimension() == ModDimensions.DUAT_KEY) {
            // Coming FROM Duat - teleport to player's spawn point or corresponding overworld portal
            teleportToOverworld(player, currentPortalPos);
        } else {
            // Going TO Duat - teleport to corresponding portal in Duat
            teleportToDuat(player, currentPortalPos);
        }
    }

    /**
     * Teleport player from Duat to Overworld (spawn point or corresponding
     * portal)
     */
    private static void teleportToOverworld(ServerPlayer player, BlockPos duatPortalPos) {
        ServerLevel overworldWorld = player.getServer().getLevel(Level.OVERWORLD);
        if (overworldWorld == null) {
            player.sendSystemMessage(Component.literal("Could not access overworld."));
            return;
        }

        BlockPos targetPos;

        // Try to use player's spawn point first
        BlockPos spawnPos = player.getRespawnPosition();
        ServerLevel spawnWorld = overworldWorld; // Default to overworld

        if (spawnPos != null) {
            if (player.getRespawnDimension() != null) {
                ServerLevel respawnWorld = player.getServer().getLevel(player.getRespawnDimension());
                if (respawnWorld != null) {
                    spawnWorld = respawnWorld;
                    // Verify the spawn point is still valid (bed/respawn anchor exists)
                    if (!player.isRespawnForced() && !isValidSpawnPoint(spawnWorld, spawnPos)) {
                        // Spawn point invalid, use world spawn instead
                        spawnPos = spawnWorld.getSharedSpawnPos();
                    }
                } else {
                    // Respawn world doesn't exist, use overworld spawn
                    spawnPos = overworldWorld.getSharedSpawnPos();
                }
            }
            targetPos = spawnPos;
        } else {
            // No respawn position set, calculate corresponding overworld position
            // Scale coordinates: Duat to Overworld is multiply by 42
            int overworldX = duatPortalPos.getX() * 42;
            int overworldZ = duatPortalPos.getZ() * 42;
            targetPos = new BlockPos(overworldX, 64, overworldZ); // Default overworld height
        }

        // Find safe position near target
        BlockPos safePos = findSafeTeleportPosition(spawnWorld, targetPos);

        // Teleport player
        if (player.level() != spawnWorld) {
            player.changeDimension(spawnWorld, new PortalTeleporter(safePos));
        } else {
            teleportPlayerSafely(player, safePos);
        }

        player.setPortalCooldown();
        spawnWorld.playSound(null, safePos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.8F, 1.0F);
    }

    /**
     * Teleport player from Overworld to Duat
     */
    private static void teleportToDuat(ServerPlayer player, BlockPos overworldPortalPos) {
        ServerLevel duatWorld = player.getServer().getLevel(ModDimensions.DUAT_KEY);
        if (duatWorld == null) {
            player.sendSystemMessage(Component.literal("Could not access Duat dimension."));
            return;
        }

        // Scale coordinates: Overworld to Duat is divide by 42
        int duatX = overworldPortalPos.getX() / 42;
        int duatZ = overworldPortalPos.getZ() / 42;

        BlockPos duatPortalPos = new BlockPos(duatX, 101, duatZ); // Portal is at y=101, frame at y=100
        BlockPos safePos = findSafeTeleportPosition(duatWorld, duatPortalPos);

        // Create portal frame if it doesn't exist
        if (!duatWorld.getBlockState(duatPortalPos).is(ModBlocks.DUAT_PORTAL.get())) {
            createDuatPortalFrame(duatWorld, new BlockPos(duatX, 100, duatZ));
            duatWorld.setBlock(duatPortalPos, ModBlocks.DUAT_PORTAL.get().defaultBlockState(), 3);
        }

        // Teleport player
        if (player.level() != duatWorld) {
            player.changeDimension(duatWorld, new PortalTeleporter(safePos));
        } else {
            teleportPlayerSafely(player, safePos);
        }

        player.setPortalCooldown();
        duatWorld.playSound(null, safePos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.8F, 1.0F);
    }

    /**
     * Check if a spawn point is still valid
     */
    private static boolean isValidSpawnPoint(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        // Check for bed blocks
        if (state.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
            return true;
        }
        // Check for respawn anchor
        if (state.is(Blocks.RESPAWN_ANCHOR)) {
            return state.getValue(net.minecraft.world.level.block.RespawnAnchorBlock.CHARGE) > 0;
        }
        return false;
    }

    /**
     * Create Duat portal frame using ice-themed blocks
     */
    private static void createDuatPortalFrame(ServerLevel world, BlockPos center) {
        // In Duat dimension - use ice-themed blocks
        BlockState core = Blocks.PACKED_ICE.defaultBlockState();
        BlockState cross = Blocks.CUT_RED_SANDSTONE.defaultBlockState();
        BlockState corners = Blocks.CHISELED_RED_SANDSTONE.defaultBlockState();

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

    /**
     * Teleport a player to a specific position with safety checks
     */
    public static void teleportToPosition(ServerPlayer player, ServerLevel targetWorld, BlockPos targetPos) {
        BlockPos safePos = findSafeTeleportPosition(targetWorld, targetPos);

        if (player.level() != targetWorld) {
            // Cross-dimensional teleportation
            player.changeDimension(targetWorld, new PortalTeleporter(safePos));
        } else {
            // Same dimension teleportation
            teleportPlayerSafely(player, safePos);
        }

        player.setPortalCooldown();
        targetWorld.playSound(null, safePos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.8F, 1.0F);
    }

    /**
     * Find a safe position to teleport the player to near the portal
     */
    private static BlockPos findSafeTeleportPosition(ServerLevel world, BlockPos portalPos) {
        // Try positions around the portal
        BlockPos[] candidatePositions = {
            portalPos.above(), // Directly above portal
            portalPos.north().above(), // North of portal
            portalPos.south().above(), // South of portal
            portalPos.east().above(), // East of portal
            portalPos.west().above(), // West of portal
            portalPos.above(2), // Two blocks above portal
            portalPos.north(2).above(), // Further north
            portalPos.south(2).above(), // Further south
            portalPos.east(2).above(), // Further east
            portalPos.west(2).above() // Further west
        };

        for (BlockPos pos : candidatePositions) {
            if (isSafePosition(world, pos)) {
                return pos;
            }
        }

        // If no safe position found, return position above portal and clear the area
        BlockPos fallbackPos = portalPos.east().above();
        clearAreaForPlayer(world, fallbackPos);
        return fallbackPos;
    }

    /**
     * Check if a position is safe for player teleportation
     */
    private static boolean isSafePosition(ServerLevel world, BlockPos pos) {
        // Check if player has 2 blocks of air above a solid block
        BlockState groundState = world.getBlockState(pos.below());
        BlockState feetState = world.getBlockState(pos);
        BlockState headState = world.getBlockState(pos.above());

        return groundState.isSolidRender(world, pos.below())
                && // Solid ground
                !feetState.isSolidRender(world, pos)
                && // Air at feet level
                !headState.isSolidRender(world, pos.above())
                && // Air at head level
                !isHazardousBlock(world, pos)
                && // Not in lava/fire/etc
                !isHazardousBlock(world, pos.above()); // Head not in hazards
    }

    /**
     * Check if a block is hazardous to players
     */
    private static boolean isHazardousBlock(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() == Blocks.LAVA
                || state.getBlock() == Blocks.FIRE
                || state.getBlock() == Blocks.SOUL_FIRE
                || state.getBlock() instanceof net.minecraft.world.level.block.CactusBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.MagmaBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.CampfireBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.LiquidBlock
                || state.getFluidState().is(net.minecraft.world.level.material.Fluids.LAVA)
                || state.getFluidState().is(net.minecraft.world.level.material.Fluids.FLOWING_LAVA);
    }

    /**
     * Clear an area to make it safe for player teleportation
     */
    private static void clearAreaForPlayer(ServerLevel world, BlockPos pos) {
        // Clear 2 blocks above the position
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        world.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
    }

    /**
     * Safely teleport a player within the same dimension
     */
    private static void teleportPlayerSafely(ServerPlayer player, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;

        player.teleportTo(x, y, z);

        // Reset fall damage
        player.resetFallDistance();
    }

    /**
     * Custom teleporter for cross-dimensional travel
     */
    private static class PortalTeleporter implements ITeleporter {

        private final BlockPos targetPos;

        public PortalTeleporter(BlockPos targetPos) {
            this.targetPos = targetPos;
        }

        @Override
        public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld,
                float yaw, Function<Boolean, Entity> repositionEntity) {
            Entity teleportedEntity = repositionEntity.apply(false);

            double x = targetPos.getX() + 0.5;
            double y = targetPos.getY();
            double z = targetPos.getZ() + 0.5;

            teleportedEntity.moveTo(x, y, z, yaw, entity.getXRot());

            // Reset fall damage for players
            if (teleportedEntity instanceof ServerPlayer player) {
                player.resetFallDistance();
            }

            return teleportedEntity;
        }
    }

    /**
     * Emergency teleportation method - finds any safe location if portal system
     * fails
     */
    public static void emergencyTeleport(ServerPlayer player, ServerLevel targetWorld) {
        BlockPos spawnPos = targetWorld.getSharedSpawnPos();
        BlockPos safePos = findSafeTeleportPosition(targetWorld, spawnPos);

        teleportToPosition(player, targetWorld, safePos);

        player.sendSystemMessage(Component.literal("Emergency teleportation to safe location."));
    }

    /**
     * Validate that a teleportation can be performed safely
     */
    public static boolean canTeleportSafely(ServerPlayer player, BlockPos portalPos) {
        ServerLevel currentWorld = player.serverLevel();
        ServerLevel targetWorld;

        if (currentWorld.dimension() == ModDimensions.DUAT_KEY) {
            // Going to overworld
            targetWorld = player.getServer().getLevel(Level.OVERWORLD);
        } else {
            // Going to Duat
            targetWorld = player.getServer().getLevel(ModDimensions.DUAT_KEY);
        }

        if (targetWorld == null) {
            return false;
        }

        // Calculate target position based on coordinate scaling
        BlockPos targetPos;
        if (currentWorld.dimension() == ModDimensions.DUAT_KEY) {
            // Duat to Overworld: multiply by 42
            int overworldX = portalPos.getX() * 42;
            int overworldZ = portalPos.getZ() * 42;
            targetPos = new BlockPos(overworldX, 64, overworldZ);
        } else {
            // Overworld to Duat: divide by 42
            int duatX = portalPos.getX() / 42;
            int duatZ = portalPos.getZ() / 42;
            targetPos = new BlockPos(duatX, 101, duatZ);
        }

        // Check if target area is loaded
        if (!targetWorld.isLoaded(targetPos)) {
            return false;
        }

        return true;
    }

    /**
     * Get teleportation status information
     */
    public static Component getTeleportationStatus(ServerPlayer player, BlockPos portalPos) {
        ServerLevel currentWorld = player.serverLevel();

        if (currentWorld.dimension() == ModDimensions.DUAT_KEY) {
            return Component.literal("Portal leads to Overworld (spawn point or coordinates "
                    + (portalPos.getX() * 42) + ", " + (portalPos.getZ() * 42) + ")");
        } else {
            return Component.literal("Portal leads to Duat (coordinates "
                    + (portalPos.getX() / 42) + ", " + (portalPos.getZ() / 42) + ")");
        }
    }
}
