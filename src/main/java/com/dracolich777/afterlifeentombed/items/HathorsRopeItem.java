package com.dracolich777.afterlifeentombed.items;

import com.dracolich777.afterlifeentombed.init.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hathor's Rope - An item that teleports the player from the Duat to the highest safe point
 * in the Overworld at their current X/Z coordinates.
 */
public class HathorsRopeItem extends Item {

    public HathorsRopeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Check if player is in the Duat
            if (level.dimension() != ModDimensions.DUAT_KEY) {
                player.sendSystemMessage(Component.literal("Hathor's Rope can only be used in the Duat!"));
                return InteractionResultHolder.fail(itemStack);
            }

            // Get the overworld
            ServerLevel overworld = serverPlayer.getServer().getLevel(Level.OVERWORLD);
            if (overworld == null) {
                player.sendSystemMessage(Component.literal("Could not access the Overworld!"));
                return InteractionResultHolder.fail(itemStack);
            }

            // Get player's current X and Z coordinates
            int x = serverPlayer.getBlockX();
            int z = serverPlayer.getBlockZ();

            // Find the highest safe point in the overworld at these coordinates
            BlockPos targetPos = findHighestSafePoint(overworld, x, z);

            if (targetPos == null) {
                player.sendSystemMessage(Component.literal("Could not find a safe location in the Overworld!"));
                return InteractionResultHolder.fail(itemStack);
            }

            // Play particle effect at player's current position (placeholder using haze particle)
            // TODO: Replace with hathorsrope particle when created
            spawnRopeParticles(level, serverPlayer.position());

            // Play particle effect at destination in overworld
            spawnRopeParticles(overworld, targetPos);

            // Play sound effect
            level.playSound(null, serverPlayer.blockPosition(), SoundEvents.PORTAL_TRAVEL, 
                SoundSource.PLAYERS, 1.0F, 1.0F);

            // Teleport the player
            serverPlayer.teleportTo(overworld, 
                targetPos.getX() + 0.5, 
                targetPos.getY(), 
                targetPos.getZ() + 0.5, 
                serverPlayer.getYRot(), 
                serverPlayer.getXRot());

            // Play sound at destination
            overworld.playSound(null, targetPos, SoundEvents.PORTAL_TRAVEL, 
                SoundSource.PLAYERS, 1.0F, 1.0F);

            player.sendSystemMessage(Component.literal("Hathor's Rope has pulled you to safety!"));
            
            // Don't consume the item
            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    /**
     * Find the highest safe point at the given X/Z coordinates
     * A safe point is defined as a solid block with 2 air blocks above it
     */
    private BlockPos findHighestSafePoint(ServerLevel world, int x, int z) {
        // Start from build height and work down
        int maxHeight = world.getMaxBuildHeight() - 3;
        int minHeight = world.getMinBuildHeight();

        for (int y = maxHeight; y >= minHeight; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState groundState = world.getBlockState(pos);
            BlockState feetState = world.getBlockState(pos.above());
            BlockState headState = world.getBlockState(pos.above(2));

            // Check if this is a safe location
            if (isSafeGround(groundState) && 
                feetState.isAir() && 
                headState.isAir() &&
                !isHazardousBlock(world, pos)) {
                return pos.above(); // Return the position where player's feet will be
            }
        }

        // If no safe point found, return world spawn
        return world.getSharedSpawnPos();
    }

    /**
     * Check if a block is safe to stand on
     */
    private boolean isSafeGround(BlockState state) {
        // Must be solid and not hazardous
        return state.isSolidRender(null, null) && 
               !state.is(Blocks.LAVA) &&
               !state.is(Blocks.MAGMA_BLOCK) &&
               !state.is(Blocks.CAMPFIRE) &&
               !state.is(Blocks.SOUL_CAMPFIRE);
    }

    /**
     * Check if a block or its surroundings are hazardous
     */
    private boolean isHazardousBlock(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        
        // Check for hazardous blocks
        return state.is(Blocks.LAVA) ||
               state.is(Blocks.FIRE) ||
               state.is(Blocks.SOUL_FIRE) ||
               state.is(Blocks.MAGMA_BLOCK) ||
               state.is(Blocks.CACTUS) ||
               state.is(Blocks.SWEET_BERRY_BUSH) ||
               state.is(Blocks.WITHER_ROSE);
    }

    /**
     * Spawn rope particle effects
     * Using placeholder haze particle until hathorsrope particle is created
     */
    private void spawnRopeParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            // TODO: Replace "sethblackhaze" with "hathorsrope" when particle is created
            com.dracolich777.afterlibs.api.AfterLibsAPI.spawnAfterlifeParticle(
                serverLevel, 
                "blood_lance", // Placeholder - replace with "hathorsrope" 
                pos.getX() + 0.5, 
                pos.getY() + 1.0, 
                pos.getZ() + 0.5, 
                1.0f
            );
        }
    }

    /**
     * Overloaded method for Vec3 positions
     */
    private void spawnRopeParticles(Level level, net.minecraft.world.phys.Vec3 pos) {
        spawnRopeParticles(level, new BlockPos((int)pos.x, (int)pos.y, (int)pos.z));
    }
}
