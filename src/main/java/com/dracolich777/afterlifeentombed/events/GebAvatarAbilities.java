
package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlibs.api.AfterLibsAPI;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.items.GodstoneItem;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.SyncGodAvatarPacket;
import com.dracolich777.afterlifeentombed.client.hud.GodAvatarHudHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

/**
 * Event handlers for Geb Avatar abilities - God of Earth & Stone
 * 
 * Passives:
 * - Knockback immunity when standing on stone/dirt/grass (any natural ground)
 * - Slowness I at all times
 * - Night Vision V and Regeneration III when underground
 * 
 * Ability 1: Telekinesis - Move naturally generated blocks
 * Ability 2: Excavation - Mine 16x16x16 area or reveal ore locations
 * Ability 3: Earth Rise - Teleport to highest safe block at current X/Z
 * Ability 4: Avatar of Earth - Ultimate transformation (60 seconds, 5 min cooldown)
 */
@Mod.EventBusSubscriber
public class GebAvatarAbilities {
    
    // Ability IDs
    public static final int ABILITY_TELEKINESIS = 1;
    public static final int ABILITY_EXCAVATION = 2;
    public static final int ABILITY_EARTH_RISE = 3;
    public static final int ABILITY_AVATAR_OF_EARTH = 4;
    
    // Track telekinesis active blocks: player UUID -> BlockPos and BlockState
    private static final Map<UUID, TelekinesisData> TELEKINESIS_ACTIVE = new HashMap<>();
    
    // Track excavation timer: player UUID -> end time
    private static final Map<UUID, Long> EXCAVATION_TIMER = new HashMap<>();
    
    // Track ore locations for arrow display: player UUID -> Set of BlockPos
    private static final Map<UUID, Set<BlockPos>> ORE_MARKERS = new HashMap<>();
    
    // Cooldown constants
    private static final long TELEKINESIS_COOLDOWN = 600; // 30 seconds
    private static final long EXCAVATION_COOLDOWN = 1200; // 60 seconds
    private static final long EARTH_RISE_COOLDOWN = 400; // 20 seconds
    private static final long AVATAR_OF_EARTH_COOLDOWN = 6000; // 5 minutes
    private static final long AVATAR_OF_EARTH_DURATION = 1200; // 60 seconds
    
    private static final long EXCAVATION_MINING_WINDOW = 400; // 20 seconds to mine
    
    /**
     * Check if player is Geb avatar
     */
    private static boolean isGebAvatar(Player player) {
        return player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY)
                .map(cap -> cap.getSelectedGod() == GodType.GEB)
                .orElse(false);
    }
    
    /**
     * Main ability activation entry point
     */
    public static void activateAbility(ServerPlayer player, int abilityId) {
        if (!isGebAvatar(player)) return;
        
        switch (abilityId) {
            case ABILITY_TELEKINESIS -> activateTelekinesis(player);
            case ABILITY_EXCAVATION -> activateExcavation(player);
            case ABILITY_EARTH_RISE -> activateEarthRise(player);
            case ABILITY_AVATAR_OF_EARTH -> activateAvatarOfEarth(player);
        }
    }
    
    /**
     * Check if block is natural ground (stone, dirt, grass, etc.)
     */
    private static boolean isNaturalGround(BlockState state) {
        if (state.isAir()) return false;
        
        Block block = state.getBlock();
        
        // Vanilla ground blocks
        if (state.is(BlockTags.DIRT) || 
            state.is(BlockTags.STONE_ORE_REPLACEABLES) ||
            state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES) ||
            block == Blocks.GRASS_BLOCK ||
            block == Blocks.STONE ||
            block == Blocks.DEEPSLATE ||
            block == Blocks.GRANITE ||
            block == Blocks.DIORITE ||
            block == Blocks.ANDESITE ||
            block == Blocks.NETHERRACK ||
            block == Blocks.BASALT ||
            block == Blocks.BLACKSTONE ||
            block == Blocks.END_STONE ||
            block == Blocks.SAND ||
            block == Blocks.GRAVEL ||
            block == Blocks.CLAY) {
            return true;
        }
        
        // Check material/tags for modded blocks
        String blockId = block.toString().toLowerCase();
        return blockId.contains("stone") || 
               blockId.contains("dirt") || 
               blockId.contains("grass") ||
               blockId.contains("soil") ||
               blockId.contains("rock") ||
               blockId.contains("gravel") ||
               blockId.contains("sand");
    }
    
    /**
     * Check if block is excavatable (stone, dirt, gravel, etc. but not ores)
     */
    private static boolean isExcavatable(BlockState state) {
        if (state.isAir()) return false;
        
        Block block = state.getBlock();
        
        // Don't excavate ores
        if (isOre(state)) return false;
        
        // Don't excavate unbreakable blocks
        if (block == Blocks.BEDROCK || 
            block == Blocks.END_PORTAL_FRAME || 
            block == Blocks.END_PORTAL || 
            block == Blocks.END_GATEWAY ||
            block == Blocks.COMMAND_BLOCK ||
            block == Blocks.CHAIN_COMMAND_BLOCK ||
            block == Blocks.REPEATING_COMMAND_BLOCK ||
            block == Blocks.STRUCTURE_BLOCK ||
            block == Blocks.JIGSAW ||
            block == Blocks.BARRIER ||
            block == Blocks.LIGHT) {
            return false;
        }
        
        // Don't excavate extremely hard blocks (obsidian, crying obsidian, etc)
        if (block == Blocks.OBSIDIAN ||
            block == Blocks.CRYING_OBSIDIAN ||
            block == Blocks.RESPAWN_ANCHOR ||
            block == Blocks.REINFORCED_DEEPSLATE) {
            return false;
        }
        
        // Check for blocks with very high blast resistance (movement-proof blocks)
        if (state.getDestroySpeed(null, BlockPos.ZERO) < 0) {
            return false; // Unbreakable blocks
        }
        
        // Excavate natural ground blocks
        return isNaturalGround(state);
    }
    
    /**
     * Check if block is an ore (including modded)
     */
    private static boolean isOre(BlockState state) {
        Block block = state.getBlock();
        
        // Vanilla ores
        if (state.is(BlockTags.COAL_ORES) ||
            state.is(BlockTags.IRON_ORES) ||
            state.is(BlockTags.COPPER_ORES) ||
            state.is(BlockTags.GOLD_ORES) ||
            state.is(BlockTags.REDSTONE_ORES) ||
            state.is(BlockTags.EMERALD_ORES) ||
            state.is(BlockTags.LAPIS_ORES) ||
            state.is(BlockTags.DIAMOND_ORES) ||
            block == Blocks.NETHER_QUARTZ_ORE ||
            block == Blocks.NETHER_GOLD_ORE ||
            block == Blocks.ANCIENT_DEBRIS) {
            return true;
        }
        
        // Check for modded ores by name
        String blockId = block.toString().toLowerCase();
        return blockId.contains("ore") || blockId.contains("debris");
    }
    
    /**
     * Passive: Knockback immunity when standing on natural ground
     */
    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isGebAvatar(player)) return;
        
        // Check if standing on natural ground
        BlockPos belowPos = player.blockPosition().below();
        BlockState belowState = player.level().getBlockState(belowPos);
        
        if (isNaturalGround(belowState)) {
            event.setCanceled(true);
        }
    }
    
    /**
     * Passive effects: Slowness I always, Night Vision V and Regeneration III underground
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!isGebAvatar(player)) return;
        
        // Always apply Slowness I
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false));
        
        // Check if underground (below Y=62 for overworld, or any Y in nether/end with blocks above)
        boolean isUnderground = false;
        if (player.getY() < 62) {
            isUnderground = true;
        } else {
            // Check if there are solid blocks above (in a cave)
            BlockPos checkPos = player.blockPosition().above(3);
            for (int i = 0; i < 10; i++) {
                if (player.level().getBlockState(checkPos.above(i)).isSolidRender(player.level(), checkPos.above(i))) {
                    isUnderground = true;
                    break;
                }
            }
        }
        
        if (isUnderground) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 4, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 2, false, false));
        }
        
        // Handle telekinesis block following
        UUID playerId = player.getUUID();
        if (TELEKINESIS_ACTIVE.containsKey(playerId)) {
            handleTelekinesisMovement(player);
        }
        
        // Handle ore marker display (arrows to ores when excavation is active)
        if (EXCAVATION_TIMER.containsKey(playerId) && ORE_MARKERS.containsKey(playerId)) {
            displayOreMarkers(player);
        }
    }
    
    /**
     * Ability 1: Telekinesis - Pick up and move naturally generated blocks
     */
    public static void activateTelekinesis(ServerPlayer player) {
        if (!isGebAvatar(player)) return;
        
        UUID playerId = player.getUUID();
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            
            // Check if already active - deactivate
            if (TELEKINESIS_ACTIVE.containsKey(playerId)) {
                deactivateTelekinesis(player);
                return;
            }
            
            // Check cooldown
            long cooldown = cap.getTelekinesisCooldown();
            if (currentTime < cooldown) {
                long remaining = (cooldown - currentTime) / 20;
                GodAvatarHudHelper.sendCooldownMessage(player, "Telekinesis", remaining);
                return;
            }
            
            // Raycast to find target block
            Vec3 start = player.getEyePosition();
            Vec3 end = start.add(player.getLookAngle().scale(20));
            ClipContext context = new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
            BlockHitResult result = player.level().clip(context);
            
            if (result.getType() != HitResult.Type.BLOCK) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cNo block in range"), true);
                return;
            }
            
            BlockPos targetPos = result.getBlockPos();
            BlockState targetState = player.level().getBlockState(targetPos);
            
            // Check if it's a natural block or ore
            if ((!isNaturalGround(targetState) && !isOre(targetState)) || targetState.isAir()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cCan only move natural blocks and ores"), true);
                return;
            }
            
            // Store the block data with its current position
            TELEKINESIS_ACTIVE.put(playerId, new TelekinesisData(targetPos, targetState));
            
            // Remove block from world
            player.level().setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
            
            GodAvatarHudHelper.sendNotification(player, "✦ Telekinesis Active ✦", GodAvatarHudHelper.COLOR_SPECIAL, 40);
        });
    }
    
    private static void deactivateTelekinesis(ServerPlayer player) {
        UUID playerId = player.getUUID();
        TelekinesisData data = TELEKINESIS_ACTIVE.remove(playerId);
        
        if (data == null) return;
        
        // Place block at last valid position (current position in data)
        BlockPos placePos = data.currentPos;
        
        // Simply place the block back - don't drop it as an item
        if (player.level().getBlockState(placePos).isAir()) {
            player.level().setBlock(placePos, data.state, 3);
        } else {
            // Try to find a nearby air block to place it
            BlockPos nearbyAir = findClosestSurfaceAir(player.level(), placePos, placePos);
            if (nearbyAir != null && player.level().getBlockState(nearbyAir).isAir()) {
                player.level().setBlock(nearbyAir, data.state, 3);
            }
            // If no valid position, the block just disappears (doesn't drop)
        }
        
        // Set cooldown
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long cooldownTime = player.level().getGameTime() + TELEKINESIS_COOLDOWN;
            cap.setTelekinesisCooldown(cooldownTime);
        });
        
        GodAvatarHudHelper.sendDeactivationMessage(player, "Telekinesis", GodAvatarHudHelper.COLOR_SPECIAL);
    }
    
    private static void handleTelekinesisMovement(Player player) {
        UUID playerId = player.getUUID();
        TelekinesisData data = TELEKINESIS_ACTIVE.get(playerId);
        if (data == null) return;
        
        BlockPos oldPos = data.currentPos;
        BlockPos newPos;
        
        // Calculate target position based on player look direction
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.getEyePosition();
        BlockPos playerHeadPos = player.blockPosition().above(); // Player's head position
        
        // Check if player is looking down significantly (below their feet)
        if (lookVec.y < -0.5) {
            // Block should be directly beneath player's feet
            newPos = player.blockPosition().below();
        } else {
            // Block follows gaze at 3-4 blocks distance
            double distance = 3.5;
            Vec3 targetVec = playerPos.add(lookVec.scale(distance));
            newPos = new BlockPos((int)Math.floor(targetVec.x), (int)Math.floor(targetVec.y), (int)Math.floor(targetVec.z));
        }
        
        // NEVER allow block to occupy player's head position
        if (newPos.equals(playerHeadPos)) {
            // Try to place it one block away from player's head
            Direction facing = player.getDirection();
            newPos = playerHeadPos.relative(facing);
        }
        
        // Don't move if position hasn't changed
        if (newPos.equals(oldPos)) return;
        
        // Check if target position is occupied by a non-air block
        if (!player.level().getBlockState(newPos).isAir()) {
            // Find closest air block on the surface towards the old position
            BlockPos surfacePos = findClosestSurfaceAir(player.level(), newPos, oldPos);
            if (surfacePos != null && !surfacePos.equals(playerHeadPos)) {
                newPos = surfacePos;
            } else {
                // No valid position found, keep at old position
                return;
            }
        }
        
        // Move the block visually
        if (!newPos.equals(oldPos)) {
            // First check if we can place at new position
            if (player.level().getBlockState(newPos).isAir() && !newPos.equals(playerHeadPos)) {
                // Remove from old position ONLY if we can successfully place at new position
                if (player.level().getBlockState(oldPos).equals(data.state)) {
                    player.level().setBlock(oldPos, Blocks.AIR.defaultBlockState(), 3);
                }
                
                // Place at new position
                player.level().setBlock(newPos, data.state, 3);
                data.currentPos = newPos;
            }
            // If we can't place at new position, don't remove from old position
        }
    }
    
    /**
     * Find the closest air block on the surface near a blocked position
     */
    private static BlockPos findClosestSurfaceAir(net.minecraft.world.level.Level level, BlockPos blocked, BlockPos from) {
        // Check positions around the blocked position
        for (int radius = 1; radius <= 3; radius++) {
            for (Direction dir : Direction.values()) {
                BlockPos checkPos = blocked.relative(dir, radius);
                if (level.getBlockState(checkPos).isAir()) {
                    return checkPos;
                }
            }
        }
        return null;
    }
    
    /**
     * Ability 2: Excavation - Next block mined breaks 9x9x9 area, shows arrows to nearest ores
     */
    public static void activateExcavation(ServerPlayer player) {
        if (!isGebAvatar(player)) return;
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            UUID playerId = player.getUUID();
            
            // Check if already active - deactivate and cancel
            if (EXCAVATION_TIMER.containsKey(playerId)) {
                EXCAVATION_TIMER.remove(playerId);
                ORE_MARKERS.remove(playerId);
                
                // Clear arrows on client
                com.dracolich777.afterlifeentombed.network.GodAvatarPackets.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new com.dracolich777.afterlifeentombed.network.SyncOreMarkersPacket(true)
                );
                
                // Set cooldown
                long cooldownTime = currentTime + EXCAVATION_COOLDOWN;
                cap.setExcavationCooldown(cooldownTime);
                
                GodAvatarHudHelper.sendDeactivationMessage(player, "Excavation", GodAvatarHudHelper.COLOR_SPECIAL);
                return;
            }
            
            // Check cooldown
            long cooldown = cap.getExcavationCooldown();
            if (currentTime < cooldown) {
                long remaining = (cooldown - currentTime) / 20;
                GodAvatarHudHelper.sendCooldownMessage(player, "Excavation", remaining);
                return;
            }
            
            // Activate excavation - mark as ready and reveal nearby ores
            EXCAVATION_TIMER.put(player.getUUID(), -1L); // -1 means active indefinitely until used
            revealNearbyOres(player);
            
            GodAvatarHudHelper.sendNotification(player, "§6Excavation Active - Mine a block to excavate 9x9x9!", GodAvatarHudHelper.COLOR_SPECIAL, 100);
        });
    }
    
    /**
     * Handle block break for excavation and telekinesis protection
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        
        BlockPos breakPos = event.getPos();
        
        // Protect telekinesis blocks from being broken
        for (TelekinesisData data : TELEKINESIS_ACTIVE.values()) {
            if (data.currentPos.equals(breakPos)) {
                event.setCanceled(true);
                return;
            }
        }
        
        if (!isGebAvatar(player)) return;
        
        UUID playerId = player.getUUID();
        if (!EXCAVATION_TIMER.containsKey(playerId)) return;
        
        // Player mined a block within the window - excavate area
        EXCAVATION_TIMER.remove(playerId);
        
        BlockPos center = event.getPos();
        int blocksExcavated = excavateArea(player, center);
        
        // Clear ore markers after excavation is used
        ORE_MARKERS.remove(playerId);
        
        // Clear arrows on client
        com.dracolich777.afterlifeentombed.network.GodAvatarPackets.INSTANCE.send(
            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
            new com.dracolich777.afterlifeentombed.network.SyncOreMarkersPacket(true)
        );
        
        // Only set cooldown if blocks were actually excavated (more than just the one broken by hand)
        if (blocksExcavated > 0) {
            player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                long cooldownTime = player.level().getGameTime() + EXCAVATION_COOLDOWN;
                cap.setExcavationCooldown(cooldownTime);
            });
        } else {
            // No blocks excavated, just deactivate without cooldown
            GodAvatarHudHelper.sendNotification(player, "§eExcavation deactivated (no blocks to mine)", GodAvatarHudHelper.COLOR_SPECIAL, 40);
        }
    }
    
    private static int excavateArea(ServerPlayer player, BlockPos center) {
        ServerLevel level = player.serverLevel();
        int radius = 4; // 9x9x9 centered on block
        int blocksRemoved = 0;
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (pos.equals(center)) continue; // Skip the center block (already broken by player)
                    
                    BlockState state = level.getBlockState(pos);
                    
                    if (isExcavatable(state)) {
                        // Drop items
                        Block.dropResources(state, level, pos, level.getBlockEntity(pos), player, player.getMainHandItem());
                        // Remove block
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        blocksRemoved++;
                    }
                }
            }
        }
        
        if (blocksRemoved > 0) {
            GodAvatarHudHelper.sendNotification(player, 
                "§6Excavated " + blocksRemoved + " blocks!", 
                GodAvatarHudHelper.COLOR_SPECIAL, 60);
        }
        
        return blocksRemoved;
    }
    
    private static void revealNearbyOres(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        Set<BlockPos> ores = new HashSet<>();
        Map<BlockPos, String> oreData = new HashMap<>();
        
        // Search in a radius for ores
        int radius = 32;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    
                    if (isOre(state)) {
                        ores.add(pos.immutable());
                        // Store block ID for color coding
                        String blockId = state.getBlock().toString();
                        oreData.put(pos.immutable(), blockId);
                    }
                }
            }
        }
        
        if (ores.isEmpty()) {
            GodAvatarHudHelper.sendNotification(player, "§cNo ores found nearby", GodAvatarHudHelper.COLOR_SPECIAL, 60);
        } else {
            ORE_MARKERS.put(player.getUUID(), ores);
            
            // Send ore data to client for 3D arrow rendering with colors
            com.dracolich777.afterlifeentombed.network.GodAvatarPackets.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new com.dracolich777.afterlifeentombed.network.SyncOreMarkersPacket(oreData)
            );
            
            GodAvatarHudHelper.sendNotification(player, 
                "§6Found " + ores.size() + " ore blocks - 3D arrows will guide you!", 
                GodAvatarHudHelper.COLOR_SPECIAL, 100);
        }
    }
    
    private static void displayOreMarkers(ServerPlayer player) {
        Set<BlockPos> ores = ORE_MARKERS.get(player.getUUID());
        if (ores == null || ores.isEmpty()) {
            ORE_MARKERS.remove(player.getUUID());
            // Clear arrows on client
            com.dracolich777.afterlifeentombed.network.GodAvatarPackets.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new com.dracolich777.afterlifeentombed.network.SyncOreMarkersPacket(true)
            );
            return;
        }
        
        // Check if ores have been mined and remove them
        boolean oresChanged = ores.removeIf(pos -> !isOre(player.level().getBlockState(pos)));
        
        if (ores.isEmpty()) {
            ORE_MARKERS.remove(player.getUUID());
            GodAvatarHudHelper.sendNotification(player, "§6All marked ores collected!", GodAvatarHudHelper.COLOR_SPECIAL, 60);
            // Clear arrows on client
            com.dracolich777.afterlifeentombed.network.GodAvatarPackets.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new com.dracolich777.afterlifeentombed.network.SyncOreMarkersPacket(true)
            );
            return;
        }
        
        // If ores changed, update client
        if (oresChanged) {
            // Rebuild ore data map with remaining ores
            Map<BlockPos, String> oreData = new HashMap<>();
            for (BlockPos pos : ores) {
                BlockState state = player.level().getBlockState(pos);
                oreData.put(pos, state.getBlock().toString());
            }
            com.dracolich777.afterlifeentombed.network.GodAvatarPackets.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new com.dracolich777.afterlifeentombed.network.SyncOreMarkersPacket(oreData)
            );
        }
    }
    
    /**
     * Ability 3: Earth Rise - Teleport to highest safe block at current X/Z
     */
    public static void activateEarthRise(ServerPlayer player) {
        if (!isGebAvatar(player)) return;
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            
            // Check cooldown
            long cooldown = cap.getEarthRiseCooldown();
            if (currentTime < cooldown) {
                long remaining = (cooldown - currentTime) / 20;
                GodAvatarHudHelper.sendCooldownMessage(player, "Earth Rise", remaining);
                return;
            }
            
            // Find highest safe solid block
            BlockPos currentPos = player.blockPosition();
            int maxY = player.level().getMaxBuildHeight() - 10;
            BlockPos targetPos = null;
            
            for (int y = maxY; y > player.level().getMinBuildHeight(); y--) {
                BlockPos checkPos = new BlockPos(currentPos.getX(), y, currentPos.getZ());
                BlockState state = player.level().getBlockState(checkPos);
                BlockState above = player.level().getBlockState(checkPos.above());
                BlockState above2 = player.level().getBlockState(checkPos.above(2));
                
                // Safe if solid block with 2 air blocks above
                if (!state.isAir() && state.isSolidRender(player.level(), checkPos) &&
                    above.isAir() && above2.isAir()) {
                    targetPos = checkPos.above();
                    break;
                }
            }
            
            if (targetPos == null) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cNo safe location found"), true);
                return;
            }
            
            // Check if player is already at the target position (within 1 block)
            if (player.blockPosition().distSqr(targetPos) <= 1.0) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eAlready at highest point"), true);
                return; // Don't teleport or set cooldown
            }
            
            // Teleport
            player.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            
            // Set cooldown only after successful teleport
            long cooldownTime = currentTime + EARTH_RISE_COOLDOWN;
            cap.setEarthRiseCooldown(cooldownTime);
            
            GodAvatarHudHelper.sendNotification(player, "✦ Earth Rise ✦", GodAvatarHudHelper.COLOR_SPECIAL, 40);
        });
    }
    
    /**
     * Ability 4: Avatar of Earth - Ultimate transformation (60 seconds)
     */
    public static void activateAvatarOfEarth(ServerPlayer player) {
        if (!isGebAvatar(player)) return;
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();
            
            // Check if already active - deactivate early
            if (cap.isAvatarOfEarthActive()) {
                deactivateAvatarOfEarth(player);
                return;
            }
            
            // Check cooldown
            long cooldown = cap.getAvatarOfEarthCooldown();
            if (currentTime < cooldown) {
                long remaining = (cooldown - currentTime) / 20;
                GodAvatarHudHelper.sendCooldownMessage(player, "Avatar of Earth", remaining);
                return;
            }

            // Check if holding a different god's stone to switch
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof GodstoneItem godstone) {
            GodType newGod = godstone.getGodType();
            if (newGod != GodType.SETH && newGod != GodType.NONE) {
                // Switch gods!
                cap.setSelectedGod(newGod);
                
                // Consume the godstone
                mainHand.shrink(1);
                
                // Spawn swap particles based on new god
                if (player.level() instanceof ServerLevel level) {
                    String particleName = switch (newGod) {
                        case RA -> "ra_halo";
                        case SHU -> "shujump";
                        case ANUBIS -> "anubis_nuke";
                        case GEB -> "seth_fog";
                        case HORUS, ISIS, THOTH -> "seth_fog"; // Default to seth_fog for other gods
                        default -> "seth_fog";
                    };
                    AfterLibsAPI.spawnAfterlifeParticle(level, particleName, player.getX(), player.getY() + 1, player.getZ(), 2.0f);
                }
                
                // Switch to the new god's origin
                var server = player.getServer();
                if (server != null) {
                    String originId = switch (newGod) {
                        case RA -> "afterlifeentombed:avatar_of_ra";
                        case SHU -> "afterlifeentombed:avatar_of_shu";
                        case ANUBIS -> "afterlifeentombed:avatar_of_anubis";
                        case THOTH -> "afterlifeentombed:avatar_of_thoth";
                        case GEB -> "afterlifeentombed:avatar_of_geb";
                        case HORUS, ISIS -> "afterlifeentombed:avatar_of_egypt";
                        default -> null;
                    };
                    
                    if (originId != null) {
                        // Remove ALL existing avatar origins first
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_egypt"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_seth"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_ra"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_shu"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_anubis"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_thoth"
                        );
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin revoke " + player.getGameProfile().getName() + " origins:origin afterlifeentombed:avatar_of_geb"
                        );
                        
                        // Now grant the new origin
                        server.getCommands().performPrefixedCommand(
                            server.createCommandSourceStack(),
                            "origin set " + player.getGameProfile().getName() + " origins:origin " + originId
                        );
                    }
                }
                
                GodAvatarHudHelper.sendNotification(player, "Now avatar of " + newGod.name(), GodAvatarHudHelper.COLOR_SPECIAL, 60);
                // Sync to client
                GodAvatarPackets.INSTANCE.sendToServer(new SyncGodAvatarPacket(newGod));
                return;
            }
        }
            
            // Activate
            cap.setAvatarOfEarthActive(true);
            long endTime = currentTime + AVATAR_OF_EARTH_DURATION;
            cap.setAvatarOfEarthEndTime(endTime);

            // Apply standardised godly buffs for ultimate (1 minute duration)
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 254, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 6, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 6, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 254, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 254, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 6, false, false));
            // Grant creative flight
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            // Remove cooldowns on other active abilities
            cap.setNoAbilityCooldowns(true);

            // Grant the Origins toggle power
            var server = player.getServer();
            if (server != null) {
                server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    "power grant " + player.getGameProfile().getName() + " afterlifeentombed:geb_avatar_of_earth_active"
                );
            }

            GodAvatarHudHelper.sendNotification(player, "✦ AVATAR OF EARTH ✦", GodAvatarHudHelper.COLOR_SPECIAL, 60);
        });
    }
    
    private static void deactivateAvatarOfEarth(ServerPlayer player) {
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            cap.setAvatarOfEarthActive(false);
            cap.setAvatarOfEarthEndTime(0);
            
            // Remove creative flight
            if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            
            // Revoke the Origins toggle power
            var server = player.getServer();
            if (server != null) {
                server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    "power revoke " + player.getGameProfile().getName() + " afterlifeentombed:geb_avatar_of_earth_active"
                );
            }
            
            // Grant slow falling for 1 minute after ultimate ends
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 0, false, false));
            
            // Set cooldown
            long cooldownTime = player.level().getGameTime() + AVATAR_OF_EARTH_COOLDOWN;
            cap.setAvatarOfEarthCooldown(cooldownTime);
            
            GodAvatarHudHelper.sendDeactivationMessage(player, "Avatar of Earth", GodAvatarHudHelper.COLOR_SPECIAL);
        });
    }
    
    /**
     * Apply Avatar of Earth buffs
     */
    @SubscribeEvent
    public static void applyAvatarOfEarthBuffs(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!isGebAvatar(player)) return;
        
        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            if (!cap.isAvatarOfEarthActive()) return;
            
            long currentTime = player.level().getGameTime();
            long endTime = cap.getAvatarOfEarthEndTime();
            
            // Check if expired
            if (currentTime >= endTime) {
                deactivateAvatarOfEarth(player);
                // Slow falling is already applied in deactivateAvatarOfEarth
                return;
            }
            
            // Apply ultimate buffs (matching butler.instructions.md requirements)
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 6, false, false)); // Resistance VII
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 254, false, false)); // Strength 255
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 254, false, false)); // Regen 255
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 40, 254, false, false)); // Absorption 255
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 6, false, false)); // Haste VII
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 6, false, false)); // Speed VII
            
            // Creative flight
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            
            // Instant build (no cooldowns on other abilities during ultimate)
            cap.setTelekinesisCooldown(0);
            cap.setExcavationCooldown(0);
            cap.setEarthRiseCooldown(0);
            
            // Remove slowness during ultimate
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        });
    }
    
    /**
     * Data class for telekinesis
     */
    private static class TelekinesisData {
        final BlockPos originalPos;
        final BlockState state;
        BlockPos currentPos; // Current position as block follows player's gaze
        
        TelekinesisData(BlockPos pos, BlockState state) {
            this.originalPos = pos;
            this.state = state;
            this.currentPos = pos;
        }
    }
}
