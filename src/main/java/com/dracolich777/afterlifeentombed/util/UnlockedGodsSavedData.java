package com.dracolich777.afterlifeentombed.util;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.items.GodType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side persistent storage for unlocked gods per player
 * This ensures unlocked gods persist across sessions even if NBT data is lost
 */
public class UnlockedGodsSavedData extends SavedData {
    private static final String DATA_NAME = AfterlifeEntombedMod.MOD_ID + "_unlocked_gods";
    
    // Map of player UUID to their unlocked gods
    private final Map<UUID, Set<GodType>> playerUnlockedGods = new HashMap<>();
    
    public UnlockedGodsSavedData() {
        super();
    }
    
    /**
     * Get the saved data instance for a server
     */
    public static UnlockedGodsSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);
        if (overworld == null) {
            AfterlifeEntombedMod.LOGGER.error("Cannot access overworld for saved data!");
            return new UnlockedGodsSavedData();
        }
        
        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(
            UnlockedGodsSavedData::load,
            UnlockedGodsSavedData::new,
            DATA_NAME
        );
    }
    
    /**
     * Load data from NBT
     */
    public static UnlockedGodsSavedData load(CompoundTag nbt) {
        UnlockedGodsSavedData data = new UnlockedGodsSavedData();
        
        // Read number of players
        int playerCount = nbt.getInt("PlayerCount");
        
        for (int i = 0; i < playerCount; i++) {
            String playerKey = "Player_" + i;
            if (nbt.contains(playerKey)) {
                CompoundTag playerTag = nbt.getCompound(playerKey);
                UUID playerUUID = playerTag.getUUID("UUID");
                String godsStr = playerTag.getString("UnlockedGods");
                
                Set<GodType> unlockedGods = new HashSet<>();
                if (!godsStr.isEmpty()) {
                    String[] godNames = godsStr.split(",");
                    for (String godName : godNames) {
                        try {
                            GodType god = GodType.valueOf(godName.trim());
                            if (god != GodType.NONE) {
                                unlockedGods.add(god);
                            }
                        } catch (IllegalArgumentException e) {
                            AfterlifeEntombedMod.LOGGER.warn("Invalid god name in saved data: {}", godName);
                        }
                    }
                }
                
                data.playerUnlockedGods.put(playerUUID, unlockedGods);
            }
        }
        
        AfterlifeEntombedMod.LOGGER.info("Loaded unlocked gods data for {} players", playerCount);
        return data;
    }
    
    /**
     * Save data to NBT
     */
    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("PlayerCount", playerUnlockedGods.size());
        
        int index = 0;
        for (Map.Entry<UUID, Set<GodType>> entry : playerUnlockedGods.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("UUID", entry.getKey());
            
            // Serialize unlocked gods as comma-separated string
            StringBuilder godsStr = new StringBuilder();
            for (GodType god : entry.getValue()) {
                if (godsStr.length() > 0) {
                    godsStr.append(",");
                }
                godsStr.append(god.name());
            }
            playerTag.putString("UnlockedGods", godsStr.toString());
            
            nbt.put("Player_" + index, playerTag);
            index++;
        }
        
        return nbt;
    }
    
    /**
     * Get unlocked gods for a player
     */
    public Set<GodType> getUnlockedGods(UUID playerUUID) {
        return new HashSet<>(playerUnlockedGods.getOrDefault(playerUUID, new HashSet<>()));
    }
    
    /**
     * Unlock a god for a player
     */
    public void unlockGod(UUID playerUUID, GodType god) {
        if (god == GodType.NONE) {
            return;
        }
        
        Set<GodType> unlocked = playerUnlockedGods.computeIfAbsent(playerUUID, k -> new HashSet<>());
        boolean wasNew = unlocked.add(god);
        
        if (wasNew) {
            setDirty(); // Mark as needing save
            AfterlifeEntombedMod.LOGGER.info("Unlocked god {} for player {}", god, playerUUID);
        }
    }
    
    /**
     * Check if a player has unlocked a god
     */
    public boolean hasUnlockedGod(UUID playerUUID, GodType god) {
        Set<GodType> unlocked = playerUnlockedGods.get(playerUUID);
        return unlocked != null && unlocked.contains(god);
    }
    
    /**
     * Set all unlocked gods for a player (for syncing from capability)
     */
    public void setUnlockedGods(UUID playerUUID, Set<GodType> gods) {
        Set<GodType> filtered = new HashSet<>();
        for (GodType god : gods) {
            if (god != GodType.NONE) {
                filtered.add(god);
            }
        }
        
        playerUnlockedGods.put(playerUUID, filtered);
        setDirty();
        AfterlifeEntombedMod.LOGGER.info("Set {} unlocked gods for player {}", filtered.size(), playerUUID);
    }
}
