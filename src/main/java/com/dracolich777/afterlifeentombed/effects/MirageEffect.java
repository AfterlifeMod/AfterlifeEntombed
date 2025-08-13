// package com.dracolich777.afterlifeentombed.effects;

// import net.minecraft.world.effect.MobEffect;
// import com.dracolich777.afterlifeentombed.client.ClientMirageHandler;
// import net.minecraft.world.effect.MobEffectCategory;
// import net.minecraft.world.entity.LivingEntity;
// import net.minecraft.world.entity.player.Player;
// import net.minecraft.world.level.Level;
// import net.minecraft.world.level.block.Blocks;
// import net.minecraft.world.level.block.state.BlockState;
// import net.minecraft.core.BlockPos;
// import net.minecraft.util.RandomSource;
// import net.minecraft.world.level.biome.Biome;
// import net.minecraft.world.level.biome.Biomes;
// import net.minecraft.server.level.ServerPlayer;
// import net.minecraft.world.phys.Vec3;
// import net.minecraft.network.FriendlyByteBuf;
// import net.minecraft.resources.ResourceLocation;
// import net.minecraft.world.level.block.Block;
// import net.minecraftforge.network.NetworkDirection;
// import net.minecraftforge.network.NetworkEvent;
// import net.minecraftforge.network.NetworkRegistry;
// import net.minecraftforge.network.simple.SimpleChannel;

// import java.util.*;
// import java.util.function.Supplier;

// public class MirageEffect extends MobEffect {
    
//     // Network channel for client-server communication
//     private static final String PROTOCOL_VERSION = "1";
//     public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
//         new ResourceLocation("afterlifeentombed", "mirage_effect"),
//         () -> PROTOCOL_VERSION,
//         PROTOCOL_VERSION::equals,
//         PROTOCOL_VERSION::equals
//     );

//     // Store player's last position to detect movement
//     private static final Map<Player, BlockPos> LAST_PLAYER_POS = new HashMap<>();
    
//     // Store player's last look direction to detect view changes
//     private static final Map<Player, Vec3> LAST_LOOK_DIRECTION = new HashMap<>();

//     // Only precious ores for mirages
//     private static final Map<BlockState, Integer> TREASURE_BLOCKS = new HashMap<>();

//     // Desert-specific blocks (only for amplifier 1)
//     private static final Map<BlockState, Integer> DESERT_BLOCKS = new HashMap<>();

//     static {
//         // Only diamond and emerald ore
//         TREASURE_BLOCKS.put(Blocks.DIAMOND_ORE.defaultBlockState(), 1);
//         TREASURE_BLOCKS.put(Blocks.EMERALD_ORE.defaultBlockState(), 1);

//         // Desert-specific blocks (only for amplifier 1 in desert)
//         DESERT_BLOCKS.put(Blocks.CACTUS.defaultBlockState(), 25);
//         DESERT_BLOCKS.put(Blocks.GRASS_BLOCK.defaultBlockState(), 30);
//         DESERT_BLOCKS.put(Blocks.OAK_LOG.defaultBlockState(), 10);
//         DESERT_BLOCKS.put(Blocks.OAK_LEAVES.defaultBlockState(), 15);
//         DESERT_BLOCKS.put(Blocks.SANDSTONE.defaultBlockState(), 20);
//         DESERT_BLOCKS.put(Blocks.CHISELED_SANDSTONE.defaultBlockState(), 15);
//         DESERT_BLOCKS.put(Blocks.DEAD_BUSH.defaultBlockState(), 20);
//         DESERT_BLOCKS.put(Blocks.TERRACOTTA.defaultBlockState(), 15);
//         DESERT_BLOCKS.put(Blocks.BROWN_TERRACOTTA.defaultBlockState(), 12);
//         DESERT_BLOCKS.put(Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 12);
//     }

//     // Mirage distance - at edge of default render distance
//     private static final int MIRAGE_DISTANCE = 50;

//     // Maximum blocks per mirage formation
//     private static final int MAX_BLOCKS_PER_FORMATION = 15;

//     // Minimum distance between blocks (3 blocks apart)
//     private static final int MIN_BLOCK_SPACING = 3;

//     // Peripheral vision and fading thresholds
//     private static final double PERIPHERAL_CREATE_THRESHOLD = 1.0;
//     private static final double DIRECT_LOOK_THRESHOLD = 0.9;

//     // Track if network is initialized
//     private static boolean networkInitialized = false;

//     public MirageEffect(MobEffectCategory pCategory, int pColor) {
//         super(pCategory, pColor);
//         initializeNetwork();
//     }

//     // Initialize network only once
//     private static synchronized void initializeNetwork() {
//         if (!networkInitialized) {
//             try {
//                 NETWORK.registerMessage(0, MirageDataPacket.class, MirageDataPacket::encode, MirageDataPacket::decode, MirageDataPacket::handle);
//                 networkInitialized = true;
//             } catch (Exception e) {
//                 System.err.println("Failed to initialize MirageEffect network: " + e.getMessage());
//             }
//         }
//     }

//     // Data structure for network communication
//     public static class MirageData {
//         public final BlockPos pos;
//         public final BlockState blockState;
//         public final int amplifier;
//         public final boolean shouldRemove;

//         public MirageData(BlockPos pos, BlockState blockState, int amplifier, boolean shouldRemove) {
//             this.pos = pos;
//             this.blockState = blockState;
//             this.amplifier = amplifier;
//             this.shouldRemove = shouldRemove;
//         }
//     }

//     // Network packet for sending mirage data to client
//     public static class MirageDataPacket {
//         private final List<MirageData> mirageData;

//         public MirageDataPacket(List<MirageData> mirageData) {
//             this.mirageData = mirageData != null ? mirageData : new ArrayList<>();
//         }

//         public static void encode(MirageDataPacket packet, FriendlyByteBuf buf) {
//             try {
//                 buf.writeInt(packet.mirageData.size());
//                 for (MirageData data : packet.mirageData) {
//                     buf.writeBlockPos(data.pos);
//                     buf.writeInt(Block.getId(data.blockState));
//                     buf.writeInt(data.amplifier);
//                     buf.writeBoolean(data.shouldRemove);
//                 }
//             } catch (Exception e) {
//                 System.err.println("Error encoding MirageDataPacket: " + e.getMessage());
//             }
//         }

//         public static MirageDataPacket decode(FriendlyByteBuf buf) {
//             try {
//                 int size = buf.readInt();
//                 List<MirageData> mirageData = new ArrayList<>();
//                 for (int i = 0; i < size; i++) {
//                     BlockPos pos = buf.readBlockPos();
//                     BlockState blockState = Block.stateById(buf.readInt());
//                     int amplifier = buf.readInt();
//                     boolean shouldRemove = buf.readBoolean();
//                     mirageData.add(new MirageData(pos, blockState, amplifier, shouldRemove));
//                 }
//                 return new MirageDataPacket(mirageData);
//             } catch (Exception e) {
//                 System.err.println("Error decoding MirageDataPacket: " + e.getMessage());
//                 return new MirageDataPacket(new ArrayList<>());
//             }
//         }

//         public static void handle(MirageDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
//             try {
//                 ctx.get().enqueueWork(() -> {
//                     // This will be handled on the client side
//                     if (ctx.get().getDirection().getReceptionSide().isClient()) {
//                         try {
//                             ClientMirageHandler.handleMirageData(packet.mirageData);
//                         } catch (Exception e) {
//                             System.err.println("Error handling mirage data on client: " + e.getMessage());
//                         }
//                     }
//                 });
//                 ctx.get().setPacketHandled(true);
//             } catch (Exception e) {
//                 System.err.println("Error handling MirageDataPacket: " + e.getMessage());
//             }
//         }
//     }

//     // NEW METHOD: Remove all mirages for a player
//     public static void removeAllMirages(Player player) {
//         if (player instanceof ServerPlayer serverPlayer) {
//             try {
//                 // Send clear message to client to remove all mirages
//                 List<MirageData> clearData = new ArrayList<>();
//                 clearData.add(new MirageData(BlockPos.ZERO, Blocks.AIR.defaultBlockState(), 0, true));
//                 NETWORK.sendTo(new MirageDataPacket(clearData), serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
//             } catch (Exception e) {
//                 System.err.println("Error removing mirages for player: " + e.getMessage());
//             }
//         }
        
//         // Clean up stored player data
//         cleanupPlayer(player);
//     }

//     @Override
//     public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
//         // Safety check to prevent render thread issues
//         if (pLivingEntity == null || pLivingEntity.level() == null) {
//             return;
//         }

//         // Only process on server side
//         if (pLivingEntity.level().isClientSide()) {
//             return;
//         }

//         if (pLivingEntity instanceof ServerPlayer serverPlayer) {
//             try {
//                 Level level = serverPlayer.level();
//                 if (level == null) return;

//                 RandomSource random = level.random;

//                 // Check if player moved significantly
//                 BlockPos currentPos = serverPlayer.blockPosition();
//                 BlockPos lastPos = LAST_PLAYER_POS.get(serverPlayer);

//                 boolean playerMoved = lastPos == null || currentPos.distSqr(lastPos) > 4;

//                 if (playerMoved) {
//                     // Send clear message to client
//                     List<MirageData> clearData = new ArrayList<>();
//                     clearData.add(new MirageData(BlockPos.ZERO, Blocks.AIR.defaultBlockState(), pAmplifier, true));
//                     NETWORK.sendTo(new MirageDataPacket(clearData), serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                    
//                     LAST_PLAYER_POS.put(serverPlayer, currentPos);
//                 }

//                 // Update look direction
//                 Vec3 lookAngle = serverPlayer.getLookAngle();
//                 if (lookAngle != null) {
//                     LAST_LOOK_DIRECTION.put(serverPlayer, lookAngle);
//                 }

//                 // At amplifier 1, only work in desert biomes. Otherwise work anywhere.
//                 boolean shouldCreateMirages = (pAmplifier == 1) ? isInDesertBiome(serverPlayer) : true;

//                 if (shouldCreateMirages) {
//                     // Create new mirages periodically or when player moves
//                     if (random.nextInt(40) == 0 || playerMoved) {
//                         createMirage(serverPlayer, random, pAmplifier);
//                     }
//                 }
//             } catch (Exception e) {
//                 System.err.println("Error in MirageEffect.applyEffectTick: " + e.getMessage());
//             }
//         }
//     }

//     private void createMirage(ServerPlayer player, RandomSource random, int amplifier) {
//         try {
//             Level level = player.level();
//             if (level == null) return;

//             BlockPos playerBlockPos = player.blockPosition();
//             Vec3 playerEyePos = player.position().add(0, player.getEyeHeight(), 0);
//             Vec3 playerLookDirection = player.getLookAngle();

//             if (playerLookDirection == null) return;

//             // Create 1-3 mirage formations based on amplifier
//             int formationCount = Math.min(3, 1 + amplifier);
//             List<MirageData> mirageDataList = new ArrayList<>();

//             for (int formation = 0; formation < formationCount; formation++) {
//                 // Pick a direction around the player
//                 double angle = (formation * 2.0 * Math.PI) / formationCount + random.nextDouble() * 0.5;

//                 // Calculate mirage position at fixed distance
//                 int mirageX = playerBlockPos.getX() + (int)(Math.cos(angle) * MIRAGE_DISTANCE);
//                 int mirageZ = playerBlockPos.getZ() + (int)(Math.sin(angle) * MIRAGE_DISTANCE);

//                 // Keep mirage near horizon (player's Y level)
//                 int mirageY = playerBlockPos.getY() + random.nextInt(3) - 1;

//                 BlockPos potentialMirageBlockPos = new BlockPos(mirageX, mirageY, mirageZ);
//                 Vec3 potentialMirageCenter = Vec3.atCenterOf(potentialMirageBlockPos);

//                 // Calculate dot product for peripheral vision check
//                 Vec3 toPotentialMirage = potentialMirageCenter.subtract(playerEyePos).normalize();
//                 double dotProduct = playerLookDirection.dot(toPotentialMirage);

//                 // Only create if it's in peripheral vision
//                 if (dotProduct < PERIPHERAL_CREATE_THRESHOLD && dotProduct > 0) {
//                     // Get the appropriate block set
//                     Map<BlockState, Integer> blockSet = getAppropriateBlocks(player, level, amplifier);

//                     // Create formation data
//                     List<MirageData> formationData = createSparseFormation(player, potentialMirageBlockPos, blockSet, random, amplifier);
//                     mirageDataList.addAll(formationData);
//                 }
//             }

//             // Send mirage data to client
//             if (!mirageDataList.isEmpty()) {
//                 NETWORK.sendTo(new MirageDataPacket(mirageDataList), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
//             }
//         } catch (Exception e) {
//             System.err.println("Error creating mirage: " + e.getMessage());
//         }
//     }

//     private Map<BlockState, Integer> getAppropriateBlocks(Player player, Level level, int amplifier) {
//         Map<BlockState, Integer> blocks = new HashMap<>();

//         // Always include precious ores only
//         blocks.putAll(TREASURE_BLOCKS);

//         // Add ancient debris if in the nether
//         if (level.dimension() == Level.NETHER) {
//             blocks.put(Blocks.ANCIENT_DEBRIS.defaultBlockState(), 1);
//         }

//         // Add desert blocks only if amplifier is 1 (desert-only mode)
//         if (amplifier == 1) {
//             blocks.putAll(DESERT_BLOCKS);
//         }

//         return blocks;
//     }

//     private List<MirageData> createSparseFormation(ServerPlayer player, BlockPos centerPos, Map<BlockState, Integer> blockSet, RandomSource random, int amplifier) {
//         List<MirageData> mirageDataList = new ArrayList<>();
        
//         try {
//             Level level = player.level();
//             if (level == null) return mirageDataList;

//             // Find solid ground for the mirage
//             BlockPos groundPos = findSolidGround(level, centerPos);
//             if (groundPos == null) return mirageDataList;

//             // Create a sparse pattern with up to MAX_BLOCKS_PER_FORMATION blocks
//             List<BlockPos> positions = new ArrayList<>();

//             // Generate positions ensuring they have at least MIN_BLOCK_SPACING blocks between them
//             int maxAttempts = 200;
//             int attempts = 0;

//             while (positions.size() < MAX_BLOCKS_PER_FORMATION && attempts < maxAttempts) {
//                 int searchRadius = 15 + (positions.size() / 3);
//                 int dx = random.nextInt(searchRadius * 2 + 1) - searchRadius;
//                 int dz = random.nextInt(searchRadius * 2 + 1) - searchRadius;

//                 BlockPos candidateBase = groundPos.offset(dx, 0, dz);
//                 BlockPos candidatePos = findNonFloatingPosition(level, candidateBase);

//                 if (candidatePos != null) {
//                     boolean validPosition = true;
//                     for (BlockPos existingPos : positions) {
//                         double distance = Math.sqrt(candidatePos.distSqr(existingPos));
//                         if (distance < MIN_BLOCK_SPACING) {
//                             validPosition = false;
//                             break;
//                         }
//                     }

//                     if (validPosition) {
//                         positions.add(candidatePos);
//                     }
//                 }
//                 attempts++;
//             }

//             // Create mirage data for each position
//             for (BlockPos pos : positions) {
//                 BlockState block = getRandomMirageBlock(blockSet, random);
//                 if (level.getBlockState(pos).isAir()) {
//                     mirageDataList.add(new MirageData(pos, block, amplifier, false));
//                 }
//             }
//         } catch (Exception e) {
//             System.err.println("Error creating sparse formation: " + e.getMessage());
//         }

//         return mirageDataList;
//     }

//     private BlockPos findSolidGround(Level level, BlockPos startPos) {
//         try {
//             for (int y = startPos.getY(); y > level.getMinBuildHeight() + 5; y--) {
//                 BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
//                 BlockState blockBelow = level.getBlockState(checkPos.below());
//                 BlockState blockAt = level.getBlockState(checkPos);

//                 if (!blockBelow.isAir() && blockAt.isAir()) {
//                     return checkPos;
//                 }
//             }
//         } catch (Exception e) {
//             System.err.println("Error finding solid ground: " + e.getMessage());
//         }
//         return null;
//     }

//     private BlockPos findNonFloatingPosition(Level level, BlockPos startPos) {
//         try {
//             BlockPos groundPos = findSolidGround(level, startPos);
//             if (groundPos == null) return null;

//             for (int dy = 0; dy <= 5; dy++) {
//                 BlockPos checkPos = groundPos.offset(0, dy, 0);
//                 BlockState blockAt = level.getBlockState(checkPos);
//                 BlockState blockBelow = level.getBlockState(checkPos.below());

//                 if (blockAt.isAir() && !blockBelow.isAir()) {
//                     return checkPos;
//                 }
//             }

//             return groundPos;
//         } catch (Exception e) {
//             System.err.println("Error finding non-floating position: " + e.getMessage());
//             return null;
//         }
//     }

//     private BlockState getRandomMirageBlock(Map<BlockState, Integer> blockSet, RandomSource random) {
//         try {
//             if (blockSet.isEmpty()) {
//                 return Blocks.DIAMOND_ORE.defaultBlockState();
//             }

//             int totalWeight = blockSet.values().stream().mapToInt(Integer::intValue).sum();
//             if (totalWeight <= 0) {
//                 return Blocks.DIAMOND_ORE.defaultBlockState();
//             }

//             int randomValue = random.nextInt(totalWeight);

//             int currentWeight = 0;
//             for (Map.Entry<BlockState, Integer> entry : blockSet.entrySet()) {
//                 currentWeight += entry.getValue();
//                 if (randomValue < currentWeight) {
//                     return entry.getKey();
//                 }
//             }
//         } catch (Exception e) {
//             System.err.println("Error getting random mirage block: " + e.getMessage());
//         }

//         return Blocks.DIAMOND_ORE.defaultBlockState();
//     }

//     private boolean isInDesertBiome(Player player) {
//         try {
//             Level level = player.level();
//             if (level == null) return false;

//             BlockPos pos = player.blockPosition();
//             Biome biome = level.getBiome(pos).value();

//             return biome == level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
//                     .get(Biomes.DESERT) ||
//                     biome == level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
//                     .get(Biomes.BADLANDS) ||
//                     biome == level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
//                     .get(Biomes.ERODED_BADLANDS) ||
//                     biome == level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
//                     .get(Biomes.WOODED_BADLANDS);
//         } catch (Exception e) {
//             System.err.println("Error checking desert biome: " + e.getMessage());
//             return false;
//         }
//     }

//     @Override
//     public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
//         return true;
//     }

//     public static void cleanupPlayer(Player player) {
//         try {
//             LAST_PLAYER_POS.remove(player);
//             LAST_LOOK_DIRECTION.remove(player);
//         } catch (Exception e) {
//             System.err.println("Error cleaning up player data: " + e.getMessage());
//         }
//     }
// }