package com.dracolich777.afterlifeentombed.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.dracolich777.afterlifeentombed.client.ClientMirageHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class MirageEffect extends MobEffect {
    
    // Network channel for client-server communication
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("afterlifeentombed", "mirage_effect"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    // Store player's last position to detect movement
    private static final Map<Player, BlockPos> LAST_PLAYER_POS = new HashMap<>();

    // Store player's last look direction to detect view changes
    private static final Map<Player, Vec3> LAST_LOOK_DIRECTION = new HashMap<>();

    // Only precious ores for mirages
    private static final Map<BlockState, Integer> TREASURE_BLOCKS = new HashMap<>();

    // Desert-specific blocks (only for amplifier 1)
    private static final Map<BlockState, Integer> DESERT_BLOCKS = new HashMap<>();

    // Phantom structure definition
    public static class PhantomStructure {

        public final String name;
        public final List<BlockPos> relativePositions;
        public final List<BlockState> blocks;
        public final int width, height, depth;

        public PhantomStructure(String name, List<BlockPos> positions, List<BlockState> blocks, int width, int height, int depth) {
            this.name = name;
            this.relativePositions = positions;
            this.blocks = blocks;
            this.width = width;
            this.height = height;
            this.depth = depth;
        }
    }

    // Phantom structure templates
    private static final List<PhantomStructure> PHANTOM_STRUCTURES = new ArrayList<>();

    static {
        // Only diamond and emerald ore - much rarer now
        TREASURE_BLOCKS.put(Blocks.DIAMOND_ORE.defaultBlockState(), 1); // Very rare
        TREASURE_BLOCKS.put(Blocks.EMERALD_ORE.defaultBlockState(), 1); // Very rare

        // Add more common desert blocks to treasure set to dilute valuable ores
        TREASURE_BLOCKS.put(Blocks.SANDSTONE.defaultBlockState(), 15);
        TREASURE_BLOCKS.put(Blocks.SAND.defaultBlockState(), 20);
        TREASURE_BLOCKS.put(Blocks.SMOOTH_SANDSTONE.defaultBlockState(), 12);
        TREASURE_BLOCKS.put(Blocks.CUT_SANDSTONE.defaultBlockState(), 10);

        // Desert-specific blocks (only for amplifier 1 in desert)
        DESERT_BLOCKS.put(Blocks.CACTUS.defaultBlockState(), 25);
        DESERT_BLOCKS.put(Blocks.GRASS_BLOCK.defaultBlockState(), 30);
        DESERT_BLOCKS.put(Blocks.OAK_LOG.defaultBlockState(), 10);
        DESERT_BLOCKS.put(Blocks.OAK_LEAVES.defaultBlockState(), 15);
        DESERT_BLOCKS.put(Blocks.SANDSTONE.defaultBlockState(), 20);
        DESERT_BLOCKS.put(Blocks.CHISELED_SANDSTONE.defaultBlockState(), 15);
        DESERT_BLOCKS.put(Blocks.DEAD_BUSH.defaultBlockState(), 20);
        DESERT_BLOCKS.put(Blocks.TERRACOTTA.defaultBlockState(), 15);
        DESERT_BLOCKS.put(Blocks.BROWN_TERRACOTTA.defaultBlockState(), 12);
        DESERT_BLOCKS.put(Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 12);

        // Initialize phantom structures
        initializePhantomStructures();
    }

    private static void initializePhantomStructures() {
        // Village house structure
        List<BlockPos> villagePositions = new ArrayList<>();
        List<BlockState> villageBlocks = new ArrayList<>();

        // Simple village house
        for (int x = 0; x <= 6; x++) {
            for (int z = 0; z <= 6; z++) {
                for (int y = 0; y <= 4; y++) {
                    // Foundation
                    if (y == 0) {
                        villagePositions.add(new BlockPos(x, y, z));
                        villageBlocks.add(Blocks.COBBLESTONE.defaultBlockState());
                    } // Walls
                    else if (y <= 3 && (x == 0 || x == 6 || z == 0 || z == 6)) {
                        villagePositions.add(new BlockPos(x, y, z));
                        villageBlocks.add(Blocks.OAK_PLANKS.defaultBlockState());
                    } // Roof
                    else if (y == 4 && x >= 1 && x <= 5 && z >= 1 && z <= 5) {
                        villagePositions.add(new BlockPos(x, y, z));
                        villageBlocks.add(Blocks.OAK_STAIRS.defaultBlockState());
                    }
                }
            }
        }
        // Add door
        villagePositions.add(new BlockPos(3, 1, 0));
        villageBlocks.add(Blocks.OAK_DOOR.defaultBlockState());
        villagePositions.add(new BlockPos(3, 2, 0));
        villageBlocks.add(Blocks.OAK_DOOR.defaultBlockState());

        PHANTOM_STRUCTURES.add(new PhantomStructure("village_house", villagePositions, villageBlocks, 7, 5, 7));

        // Pillager outpost tower
        List<BlockPos> outpostPositions = new ArrayList<>();
        List<BlockState> outpostBlocks = new ArrayList<>();

        // Tower base
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 8; y++) {
                    if (x == 0 && z == 0) {
                        // Central pillar
                        outpostPositions.add(new BlockPos(x, y, z));
                        outpostBlocks.add(Blocks.DARK_OAK_LOG.defaultBlockState());
                    } else if ((Math.abs(x) == 1 || Math.abs(z) == 1) && y <= 6) {
                        // Outer frame
                        outpostPositions.add(new BlockPos(x, y, z));
                        outpostBlocks.add(Blocks.COBBLESTONE.defaultBlockState());
                    }
                }
            }
        }
        // Top platform
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    outpostPositions.add(new BlockPos(x, 9, z));
                    outpostBlocks.add(Blocks.DARK_OAK_PLANKS.defaultBlockState());
                }
            }
        }

        PHANTOM_STRUCTURES.add(new PhantomStructure("pillager_outpost", outpostPositions, outpostBlocks, 5, 10, 5));

        // Desert temple (simplified)
        List<BlockPos> templePositions = new ArrayList<>();
        List<BlockState> templeBlocks = new ArrayList<>();

        // Temple base
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                for (int y = 0; y <= 6; y++) {
                    // Outer walls
                    if ((x == 0 || x == 8 || z == 0 || z == 8) && y <= 4) {
                        templePositions.add(new BlockPos(x, y, z));
                        templeBlocks.add(Blocks.SANDSTONE.defaultBlockState());
                    } // Towers at corners
                    else if (((x <= 1 || x >= 7) && (z <= 1 || z >= 7)) && y <= 6) {
                        templePositions.add(new BlockPos(x, y, z));
                        templeBlocks.add(Blocks.SANDSTONE.defaultBlockState());
                    }
                }
            }
        }

        PHANTOM_STRUCTURES.add(new PhantomStructure("desert_temple", templePositions, templeBlocks, 9, 7, 9));

        // Witch hut
        List<BlockPos> hutPositions = new ArrayList<>();
        List<BlockState> hutBlocks = new ArrayList<>();

        // Hut on stilts
        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                for (int y = 0; y <= 5; y++) {
                    // Support pillars
                    if ((x == 0 || x == 4) && (z == 0 || z == 4) && y <= 3) {
                        hutPositions.add(new BlockPos(x, y, z));
                        hutBlocks.add(Blocks.SPRUCE_LOG.defaultBlockState());
                    } // Platform
                    else if (y == 3) {
                        hutPositions.add(new BlockPos(x, y, z));
                        hutBlocks.add(Blocks.SPRUCE_PLANKS.defaultBlockState());
                    } // Walls
                    else if (y == 4 && (x == 0 || x == 4 || z == 0 || z == 4)) {
                        hutPositions.add(new BlockPos(x, y, z));
                        hutBlocks.add(Blocks.SPRUCE_PLANKS.defaultBlockState());
                    } // Roof
                    else if (y == 5 && x >= 1 && x <= 3 && z >= 1 && z <= 3) {
                        hutPositions.add(new BlockPos(x, y, z));
                        hutBlocks.add(Blocks.SPRUCE_STAIRS.defaultBlockState());
                    }
                }
            }
        }

        PHANTOM_STRUCTURES.add(new PhantomStructure("witch_hut", hutPositions, hutBlocks, 5, 6, 5));

        // Ruined portal
        List<BlockPos> portalPositions = new ArrayList<>();
        List<BlockState> portalBlocks = new ArrayList<>();

        // Portal frame (broken)
        for (int x = 0; x <= 3; x++) {
            for (int y = 0; y <= 4; y++) {
                if ((x == 0 || x == 3) && y >= 1 && y <= 3) {
                    portalPositions.add(new BlockPos(x, y, 0));
                    if (y == 2 && x == 0) {
                        // Missing block
                        portalBlocks.add(Blocks.AIR.defaultBlockState());
                    } else {
                        portalBlocks.add(Blocks.OBSIDIAN.defaultBlockState());
                    }
                }
                if ((y == 0 || y == 4) && x >= 1 && x <= 2) {
                    portalPositions.add(new BlockPos(x, y, 0));
                    portalBlocks.add(Blocks.OBSIDIAN.defaultBlockState());
                }
            }
        }
        // Add some crying obsidian
        portalPositions.add(new BlockPos(1, 0, 0));
        portalBlocks.add(Blocks.CRYING_OBSIDIAN.defaultBlockState());

        PHANTOM_STRUCTURES.add(new PhantomStructure("ruined_portal", portalPositions, portalBlocks, 4, 5, 1));
    }// Mirage distance - at edge of default render distance
    private static final int MIRAGE_DISTANCE = 50;

// Maximum blocks per mirage formation
    private static final int MAX_BLOCKS_PER_FORMATION = 15;

// Minimum distance between blocks (3 blocks apart)
    private static final int MIN_BLOCK_SPACING = 3;

    // Peripheral vision and fading thresholds - adjusted for more peripheral spawning
    private static final double PERIPHERAL_CREATE_THRESHOLD = 0.7; // Reduced from 1.0 to make more peripheral// Track if network is initialized
    private static boolean networkInitialized = false;

    public MirageEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        initializeNetwork();
    }

// Initialize network only once
    private static synchronized void initializeNetwork() {
        if (!networkInitialized) {
            try {
                NETWORK.registerMessage(0, MirageDataPacket.class, MirageDataPacket::encode, MirageDataPacket::decode, MirageDataPacket::handle);
                networkInitialized = true;
            } catch (Exception e) {
                System.err.println("Failed to initialize MirageEffect network: " + e.getMessage());
            }
        }
    }

    // Data structure for network communication
    public static class MirageData {

        public final BlockPos pos;
        public final BlockState blockState;
        public final int amplifier;
        public final boolean shouldRemove;
        public final boolean isPhantomStructure;
        public final String structureName;
        public final boolean shouldFade; // New field for gradual fading

        public MirageData(BlockPos pos, BlockState blockState, int amplifier, boolean shouldRemove) {
            this(pos, blockState, amplifier, shouldRemove, false, "", false);
        }

        public MirageData(BlockPos pos, BlockState blockState, int amplifier, boolean shouldRemove, boolean isPhantomStructure, String structureName) {
            this(pos, blockState, amplifier, shouldRemove, isPhantomStructure, structureName, false);
        }

        public MirageData(BlockPos pos, BlockState blockState, int amplifier, boolean shouldRemove, boolean isPhantomStructure, String structureName, boolean shouldFade) {
            this.pos = pos;
            this.blockState = blockState;
            this.amplifier = amplifier;
            this.shouldRemove = shouldRemove;
            this.isPhantomStructure = isPhantomStructure;
            this.structureName = structureName != null ? structureName : "";
            this.shouldFade = shouldFade;
        }
    }// Network packet for sending mirage data to client

    public static class MirageDataPacket {

        private final List<MirageData> mirageData;

        public MirageDataPacket(List<MirageData> mirageData) {
            this.mirageData = mirageData != null ? mirageData : new ArrayList<>();
        }

        public static void encode(MirageDataPacket packet, FriendlyByteBuf buf) {
            try {
                buf.writeInt(packet.mirageData.size());
                for (MirageData data : packet.mirageData) {
                    buf.writeBlockPos(data.pos);
                    buf.writeInt(Block.getId(data.blockState));
                    buf.writeInt(data.amplifier);
                    buf.writeBoolean(data.shouldRemove);
                    buf.writeBoolean(data.isPhantomStructure);
                    buf.writeUtf(data.structureName);
                    buf.writeBoolean(data.shouldFade);
                }
            } catch (Exception e) {
                System.err.println("Error encoding MirageDataPacket: " + e.getMessage());
            }
        }

        public static MirageDataPacket decode(FriendlyByteBuf buf) {
            try {
                int size = buf.readInt();
                List<MirageData> mirageData = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    BlockPos pos = buf.readBlockPos();
                    BlockState blockState = Block.stateById(buf.readInt());
                    int amplifier = buf.readInt();
                    boolean shouldRemove = buf.readBoolean();
                    boolean isPhantomStructure = buf.readBoolean();
                    String structureName = buf.readUtf();
                    boolean shouldFade = buf.readBoolean();
                    mirageData.add(new MirageData(pos, blockState, amplifier, shouldRemove, isPhantomStructure, structureName, shouldFade));
                }
                return new MirageDataPacket(mirageData);
            } catch (Exception e) {
                System.err.println("Error decoding MirageDataPacket: " + e.getMessage());
                return new MirageDataPacket(new ArrayList<>());
            }
        }

        public static void handle(MirageDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
            try {
                ctx.get().enqueueWork(() -> {
// This will be handled on the client side
                    if (ctx.get().getDirection().getReceptionSide().isClient()) {
                        try {
                            ClientMirageHandler.handleMirageData(packet.mirageData);
                        } catch (Exception e) {
                            System.err.println("Error handling mirage data on client: " + e.getMessage());
                        }
                    }
                });
                ctx.get().setPacketHandled(true);
            } catch (Exception e) {
                System.err.println("Error handling MirageDataPacket: " + e.getMessage());
            }
        }
    }

// NEW METHOD: Remove all mirages for a player
    public static void removeAllMirages(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            try {
// Send clear message to client to remove all mirages
                List<MirageData> clearData = new ArrayList<>();
                clearData.add(new MirageData(BlockPos.ZERO, Blocks.AIR.defaultBlockState(), 0, true));
                NETWORK.sendTo(new MirageDataPacket(clearData), serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            } catch (Exception e) {
                System.err.println("Error removing mirages for player: " + e.getMessage());
            }
        }

// Clean up stored player data
        cleanupPlayer(player);
    }

    @Override
    public void applyEffectTick(@Nonnull LivingEntity pLivingEntity, int pAmplifier) {
// Safety check to prevent render thread issues
        if (pLivingEntity == null || pLivingEntity.level() == null) {
            return;
        }

// Only process on server side
        if (pLivingEntity.level().isClientSide()) {
            return;
        }

        if (pLivingEntity instanceof ServerPlayer serverPlayer) {
            try {
                Level level = serverPlayer.level();
                if (level == null) {
                    return;
                }

                RandomSource random = level.random;

                // Check if player moved significantly
                BlockPos currentPos = serverPlayer.blockPosition();
                BlockPos lastPos = LAST_PLAYER_POS.get(serverPlayer);

                boolean playerMoved = lastPos == null || currentPos.distSqr(lastPos) > 9; // Increased threshold for less frequent clearing

                if (playerMoved) {
                    // Send fade message to client instead of immediate clear
                    List<MirageData> fadeData = new ArrayList<>();
                    fadeData.add(new MirageData(BlockPos.ZERO, Blocks.AIR.defaultBlockState(), pAmplifier, false, false, "", true));
                    NETWORK.sendTo(new MirageDataPacket(fadeData), serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);

                    LAST_PLAYER_POS.put(serverPlayer, currentPos);
                }// Update look direction
                Vec3 lookAngle = serverPlayer.getLookAngle();
                if (lookAngle != null) {
                    LAST_LOOK_DIRECTION.put(serverPlayer, lookAngle);
                }

// At amplifier 1, only work in desert biomes. Otherwise work anywhere.
    boolean shouldCreateMirages = (pAmplifier == 1) ? isInDesertBiome(serverPlayer) : true;

                if (shouldCreateMirages) {
// Create new mirages periodically or when player moves
                    if (random.nextInt(60) == 0 || (playerMoved && random.nextInt(100) == 0)) {
                        createMirage(serverPlayer, random, pAmplifier);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in MirageEffect.applyEffectTick: " + e.getMessage());
            }
        }
    }

    private void createMirage(ServerPlayer player, RandomSource random, int amplifier) {
        try {
            Level level = player.level();
            if (level == null)
                return;

    BlockPos playerBlockPos = player.blockPosition();
    Vec3 playerEyePos = player.position().add(0, player.getEyeHeight(), 0);
    Vec3 playerLookDirection = player.getLookAngle();

    if (playerLookDirection == null)
        return;

    // Create only 1 mirage structure at a time to reduce spawn frequency
    int formationCount = 1; // Reduced from Math.min(3, 1 + amplifier)
    List<MirageData> mirageDataList = new ArrayList<>();
    for (int formation = 0; formation < formationCount; formation++) {
// Pick a direction around the player
        double angle = (formation * 2.0 * Math.PI) / formationCount + random.nextDouble() * 0.5;

// Calculate mirage position at fixed distance
    int mirageX = playerBlockPos.getX() + (int) (Math.cos(angle) * MIRAGE_DISTANCE);
    int mirageZ = playerBlockPos.getZ() + (int) (Math.sin(angle) * MIRAGE_DISTANCE);

// Keep mirage near horizon (player's Y level)
    int mirageY = playerBlockPos.getY() + random.nextInt(3) - 1;

    BlockPos potentialMirageBlockPos = new BlockPos(mirageX, mirageY, mirageZ);
    Vec3 potentialMirageCenter = Vec3.atCenterOf(potentialMirageBlockPos);

// Calculate dot product for peripheral vision check
    Vec3 toPotentialMirage = potentialMirageCenter.subtract(playerEyePos).normalize();
    double dotProduct = playerLookDirection.dot(toPotentialMirage);

// Only create if it's in peripheral vision
        if (dotProduct < PERIPHERAL_CREATE_THRESHOLD && dotProduct > 0) {
// Only create phantom structures - no individual blocks
            if (!PHANTOM_STRUCTURES.isEmpty()) {
                PhantomStructure structure = selectBiomeAppropriateStructure(player, level, random);
                List<MirageData> structureData = createPhantomStructure(player, potentialMirageBlockPos, structure, random, amplifier);
                mirageDataList.addAll(structureData);
            }
        }
    }

// Send mirage data to client
            if (!mirageDataList.isEmpty()) {
                NETWORK.sendTo(new MirageDataPacket(mirageDataList), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        } catch (Exception e) {
            System.err.println("Error creating mirage: " + e.getMessage());
        }
    }

    private Map<BlockState, Integer> getAppropriateBlocks(Player player, Level level, int amplifier) {
        Map<BlockState, Integer> blocks = new HashMap<>();

// Always include precious ores only
    blocks.putAll(TREASURE_BLOCKS);

// Add ancient debris if in the nether
    if (level.dimension() == Level.NETHER) {
        blocks.put(Blocks.ANCIENT_DEBRIS.defaultBlockState(), 1);
    }

// Add desert blocks only if amplifier is 1 (desert-only mode)
    if (amplifier == 1) {
        blocks.putAll(DESERT_BLOCKS);
    }

        return blocks;
    }

    private List<MirageData> createSparseFormation(ServerPlayer player, BlockPos centerPos, Map<BlockState, Integer> blockSet, RandomSource random, int amplifier) {
        List<MirageData> mirageDataList = new ArrayList<>();

    try {
        Level level = player.level();
        if (level == null)
            return mirageDataList;

// Find solid ground for the mirage
    BlockPos groundPos = findSolidGround(level, centerPos);
    if (groundPos == null)
        return mirageDataList;

// Create a sparse pattern with up to MAX_BLOCKS_PER_FORMATION blocks
    List<BlockPos> positions = new ArrayList<>();

// Generate positions ensuring they have at least MIN_BLOCK_SPACING blocks between them
    int maxAttempts = 200;
    int attempts = 0;

    while (positions.size() < MAX_BLOCKS_PER_FORMATION && attempts < maxAttempts) {
        int searchRadius = 15 + (positions.size() / 3);
        int dx = random.nextInt(searchRadius * 2 + 1) - searchRadius;
        int dz = random.nextInt(searchRadius * 2 + 1) - searchRadius;

    BlockPos candidateBase = groundPos.offset(dx, 0, dz);
    BlockPos candidatePos = findNonFloatingPosition(level, candidateBase);

    if (candidatePos != null) {
        boolean validPosition = true;
        for (BlockPos existingPos : positions) {
            double distance = Math.sqrt(candidatePos.distSqr(existingPos));
            if (distance < MIN_BLOCK_SPACING) {
                validPosition = false;
                break;
            }
        }

            if (validPosition) {
                positions.add(candidatePos);
            }
        }
        attempts++;
    }

// Create mirage data for each position
        for (BlockPos pos : positions) {
            BlockState block = getRandomMirageBlock(blockSet, random);
            if (level.getBlockState(pos).isAir()) {
                mirageDataList.add(new MirageData(pos, block, amplifier, false));
            }
        }
    } catch (Exception e) {
        System.err.println("Error creating sparse formation: " + e.getMessage());
    }

        return mirageDataList;
    }

    private List<MirageData> createPhantomStructure(ServerPlayer player, BlockPos centerPos, PhantomStructure structure, RandomSource random, int amplifier) {
        List<MirageData> structureData = new ArrayList<>();

    try {
        Level level = player.level();
        if (level == null)
            return structureData;

        // Find solid ground for the structure
        BlockPos groundPos = findSolidGround(level, centerPos);
        if (groundPos == null)
            return structureData;

        // Place the structure blocks
        for (int i = 0; i < structure.relativePositions.size() && i < structure.blocks.size(); i++) {
            BlockPos relativePos = structure.relativePositions.get(i);
            BlockState blockState = structure.blocks.get(i);

            BlockPos absolutePos = groundPos.offset(relativePos);

            // Only place blocks in air
            if (level.getBlockState(absolutePos).isAir()) {
                structureData.add(new MirageData(absolutePos, blockState, amplifier, false, true, structure.name));
            }
        }
    } catch (Exception e) {
        System.err.println("Error creating phantom structure: " + e.getMessage());
    }

        return structureData;
    }

    private BlockPos findSolidGround(Level level, BlockPos startPos) {
        try {
            for (int y = startPos.getY(); y > level.getMinBuildHeight() + 5; y--) {
                BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
                BlockState blockBelow = level.getBlockState(checkPos.below());
                BlockState blockAt = level.getBlockState(checkPos);

                if (!blockBelow.isAir() && blockAt.isAir()) {
                    return checkPos;
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding solid ground: " + e.getMessage());
        }
        return null;
    }

    private BlockPos findNonFloatingPosition(Level level, BlockPos startPos) {
        try {
            BlockPos groundPos = findSolidGround(level, startPos);
            if (groundPos == null)
                return null;

    for (int dy = 0; dy <= 5; dy++) {
        BlockPos checkPos = groundPos.offset(0, dy, 0);
        BlockState blockAt = level.getBlockState(checkPos);
        BlockState blockBelow = level.getBlockState(checkPos.below());

        if (blockAt.isAir() && !blockBelow.isAir()) {
            return checkPos;
        }
    }

            return groundPos;
        } catch (Exception e) {
            System.err.println("Error finding non-floating position: " + e.getMessage());
            return null;
        }
    }

    private BlockState getRandomMirageBlock(Map<BlockState, Integer> blockSet, RandomSource random) {
        try {
            if (blockSet.isEmpty()) {
                return Blocks.DIAMOND_ORE.defaultBlockState();
            }

    int totalWeight = blockSet.values().stream().mapToInt(Integer::intValue).sum();
    if (totalWeight <= 0) {
        return Blocks.DIAMOND_ORE.defaultBlockState();
    }

    int randomValue = random.nextInt(totalWeight);

        int currentWeight = 0;
        for (Map.Entry<BlockState, Integer> entry : blockSet.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue < currentWeight) {
                return entry.getKey();
            }
        }
    } catch (Exception e) {
        System.err.println("Error getting random mirage block: " + e.getMessage());
    }

        return Blocks.DIAMOND_ORE.defaultBlockState();
    }

    private boolean isInDesertBiome(Player player) {
        try {
            Level level = player.level();
            if (level == null)
                return false;

    BlockPos pos = player.blockPosition();
    Biome biome = level.getBiome(pos).value();

            return biome == level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                    .get(Biomes.DESERT)
                    || biome == level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                            .get(Biomes.BADLANDS)
                    || biome == level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                            .get(Biomes.ERODED_BADLANDS)
                    || biome == level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                            .get(Biomes.WOODED_BADLANDS);
        } catch (Exception e) {
            System.err.println("Error checking desert biome: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    public static void cleanupPlayer(Player player) {
        try {
            LAST_PLAYER_POS.remove(player);
            LAST_LOOK_DIRECTION.remove(player);
        } catch (Exception e) {
            System.err.println("Error cleaning up player data: " + e.getMessage());
        }
    }

    private PhantomStructure selectBiomeAppropriateStructure(Player player, Level level, RandomSource random) {
        Biome biome = level.getBiome(player.blockPosition()).value();

        // Get the biome registry key for more reliable detection
        String biomeKey = "unknown";
        try {
            var biomeRegistry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
            var resourceLocation = biomeRegistry.getKey(biome);
            if (resourceLocation != null) {
                biomeKey = resourceLocation.toString().toLowerCase();
            }
        } catch (Exception e) {
            System.err.println("Error getting biome key: " + e.getMessage());
        }

        List<PhantomStructure> appropriateStructures = new ArrayList<>();

        // Debug output to see what biome we're detecting
        System.out.println("Debug: Player in biome: " + biomeKey);

        // Select structures based on biome
        if (biomeKey.contains("desert")) {
            appropriateStructures.add(getStructureByName("desert_temple"));
            appropriateStructures.add(getStructureByName("ruined_portal"));
            System.out.println("Debug: Selected desert structures");
        } else if (biomeKey.contains("plains") || biomeKey.contains("grassland")) {
            appropriateStructures.add(getStructureByName("village_house"));
            appropriateStructures.add(getStructureByName("pillager_outpost"));
            System.out.println("Debug: Selected plains structures");
        } else if (biomeKey.contains("swamp") || biomeKey.contains("marsh")) {
            appropriateStructures.add(getStructureByName("witch_hut"));
            appropriateStructures.add(getStructureByName("ruined_portal"));
            System.out.println("Debug: Selected swamp structures");
        } else if (biomeKey.contains("forest") || biomeKey.contains("taiga") || biomeKey.contains("birch") || biomeKey.contains("dark_forest")) {
            appropriateStructures.add(getStructureByName("pillager_outpost"));
            appropriateStructures.add(getStructureByName("witch_hut"));
            System.out.println("Debug: Selected forest structures");
        } else {
            // Default fallback for other biomes
            appropriateStructures.add(getStructureByName("ruined_portal"));
            appropriateStructures.add(getStructureByName("village_house"));
            System.out.println("Debug: Selected fallback structures for biome: " + biomeKey);
        }

        return appropriateStructures.get(random.nextInt(appropriateStructures.size()));
    }

    private PhantomStructure getStructureByName(String name) {
        return PHANTOM_STRUCTURES.stream()
                .filter(structure -> structure.name.equals(name))
                .findFirst()
                .orElse(PHANTOM_STRUCTURES.get(0)); // Fallback to first structure
    }
}
