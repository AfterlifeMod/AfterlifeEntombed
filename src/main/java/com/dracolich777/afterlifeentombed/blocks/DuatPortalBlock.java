package com.dracolich777.afterlifeentombed.blocks;

import com.dracolich777.afterlifeentombed.init.ModBlocks;
import com.dracolich777.afterlifeentombed.init.ModDimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class DuatPortalBlock extends Block {

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public DuatPortalBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .lightLevel((state) -> 15)
                .noCollission());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide && !entity.isPassenger() && !entity.isVehicle() && entity.canChangeDimensions()) {
            if (entity instanceof ServerPlayer player) {
                if (player.isOnPortalCooldown()) {
                    return;
                }

                world.playSound(null, pos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.5F,
                        world.random.nextFloat() * 0.4F + 0.8F);

                ServerLevel serverWorld = (ServerLevel) world;

                if (serverWorld.dimension() == ModDimensions.DUAT_KEY) {
                    // Coming FROM Duat - teleport to player's spawn point
                    teleportToPlayerSpawn(player);
                } else {
                    // Going TO Duat - teleport to corresponding portal in Duat
                    teleportToDuat(player, pos);
                }
            }
        }
    }

    private void teleportToPlayerSpawn(ServerPlayer player) {
        ServerLevel overworldWorld = player.getServer().getLevel(Level.OVERWORLD);
        if (overworldWorld == null) {
            player.sendSystemMessage(Component.literal("Could not access overworld."));
            return;
        }

        BlockPos spawnPos = player.getRespawnPosition();
        ServerLevel spawnWorld = overworldWorld; // Default to overworld

        // If player has a respawn position, try to use it
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
        } else {
            // No respawn position set, use world spawn
            spawnPos = spawnWorld.getSharedSpawnPos();
        }

        // Find safe position near spawn
        BlockPos safePos = findSafeTeleportPosition(spawnWorld, spawnPos);

        // Teleport player
        if (player.level() != spawnWorld) {
            player.changeDimension(spawnWorld, new SpawnTeleporter(safePos));
        } else {
            teleportPlayerSafely(player, safePos);
        }

        player.setPortalCooldown();
        spawnWorld.playSound(null, safePos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.8F, 1.0F);
    }

    private void teleportToDuat(ServerPlayer player, BlockPos portalPos) {
        ServerLevel duatWorld = player.getServer().getLevel(ModDimensions.DUAT_KEY);
        if (duatWorld == null) {
            player.sendSystemMessage(Component.literal("Could not access Duat dimension."));
            return;
        }

        // Scale coordinates: Overworld to Duat is divide by 42
        int duatX = portalPos.getX() / 42;
        int duatZ = portalPos.getZ() / 42;
        
        BlockPos duatPortalPos = new BlockPos(duatX, 101, duatZ); // Portal is at y=101, frame at y=100
        BlockPos safePos = findSafeTeleportPosition(duatWorld, duatPortalPos);

        // Create portal frame if it doesn't exist
        if (!duatWorld.getBlockState(duatPortalPos).is(ModBlocks.DUAT_PORTAL.get())) {
            createDuatPortalFrame(duatWorld, new BlockPos(duatX, 100, duatZ));
            duatWorld.setBlock(duatPortalPos, ModBlocks.DUAT_PORTAL.get().defaultBlockState(), 3);
        }

        // Teleport player
        if (player.level() != duatWorld) {
            player.changeDimension(duatWorld, new SpawnTeleporter(safePos));
        } else {
            teleportPlayerSafely(player, safePos);
        }

        player.setPortalCooldown();
        duatWorld.playSound(null, safePos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.8F, 1.0F);
    }

    private boolean isValidSpawnPoint(ServerLevel world, BlockPos pos) {
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

    private void createDuatPortalFrame(ServerLevel world, BlockPos center) {
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

    private BlockPos findSafeTeleportPosition(ServerLevel world, BlockPos portalPos) {
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

    private boolean isSafePosition(ServerLevel world, BlockPos pos) {
        // Check if player has 2 blocks of air above a solid block
        BlockState groundState = world.getBlockState(pos.below());
        BlockState feetState = world.getBlockState(pos);
        BlockState headState = world.getBlockState(pos.above());

        return groundState.isSolidRender(world, pos.below())
                && !feetState.isSolidRender(world, pos)
                && !headState.isSolidRender(world, pos.above())
                && !isHazardousBlock(world, pos)
                && !isHazardousBlock(world, pos.above());
    }

    private boolean isHazardousBlock(ServerLevel world, BlockPos pos) {
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

    private void clearAreaForPlayer(ServerLevel world, BlockPos pos) {
        // Clear 2 blocks above the position
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        world.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
    }

    private void teleportPlayerSafely(ServerPlayer player, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;

        player.teleportTo(x, y, z);
        player.resetFallDistance();
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!isValidPortalFrame(world, pos)) {
            // Break the portal if frame is invalid
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            world.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    public static boolean isValidPortalFrame(Level world, BlockPos portalPos) {
        // Portal is one block above the frame center
        BlockPos frameCenter = portalPos.below();

        // Check for valid center blocks
        BlockState centerState = world.getBlockState(frameCenter);
        if (!centerState.is(ModBlocks.DUSKSTEEL.get()) && !centerState.is(Blocks.PACKED_ICE)) {
            return false;
        }

        // Check cross pattern around frame center
        BlockPos[] crossPositions = {
            frameCenter.north(), frameCenter.south(), frameCenter.east(), frameCenter.west()
        };

        for (BlockPos pos : crossPositions) {
            BlockState state = world.getBlockState(pos);
            if (!state.is(Blocks.GOLD_BLOCK) && !state.is(Blocks.CUT_RED_SANDSTONE)) {
                return false;
            }
        }

        // Check corners around frame center
        BlockPos[] cornerPositions = {
            frameCenter.north().east(), frameCenter.north().west(),
            frameCenter.south().east(), frameCenter.south().west()
        };

        for (BlockPos pos : cornerPositions) {
            BlockState state = world.getBlockState(pos);
            if (!state.is(Blocks.CHISELED_SANDSTONE) && !state.is(Blocks.CHISELED_RED_SANDSTONE)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Custom teleporter for cross-dimensional travel
     */
    private static class SpawnTeleporter implements ITeleporter {
        private final BlockPos targetPos;

        public SpawnTeleporter(BlockPos targetPos) {
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

            if (teleportedEntity instanceof ServerPlayer player) {
                player.resetFallDistance();
            }

            return teleportedEntity;
        }
    }
}