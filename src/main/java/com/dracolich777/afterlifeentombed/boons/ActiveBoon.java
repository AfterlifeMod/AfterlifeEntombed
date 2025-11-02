package com.dracolich777.afterlifeentombed.boons;

import net.minecraft.nbt.CompoundTag;

/**
 * Represents an active boon or curse currently affecting a player
 */
public class ActiveBoon {
    private final BoonType type;
    private long expirationTime; // Game time when it expires (0 = permanent)
    private int usesRemaining; // For ONE_USE boons (0 = used, -1 = unlimited)
    private boolean activated; // Whether it has been triggered

    public ActiveBoon(BoonType type, long currentGameTime) {
        this.type = type;
        this.activated = false;
        
        // Set expiration based on duration type
        switch (type.getDuration()) {
            case PERMANENT -> {
                this.expirationTime = 0; // 0 means permanent
                this.usesRemaining = -1; // Unlimited
            }
            case ONE_USE -> {
                this.expirationTime = 0; // Doesn't expire by time
                this.usesRemaining = 1; // One use
            }
            case TEMPORARY -> {
                this.expirationTime = currentGameTime + getTickDuration(type);
                this.usesRemaining = -1; // Unlimited during duration
            }
        }
    }

    /**
     * Private constructor for NBT deserialization
     */
    private ActiveBoon(BoonType type, long expirationTime, int usesRemaining, boolean activated) {
        this.type = type;
        this.expirationTime = expirationTime;
        this.usesRemaining = usesRemaining;
        this.activated = activated;
    }

    /**
     * Get the duration in ticks for a temporary boon based on its description
     * This parses the description text to determine duration
     */
    private static long getTickDuration(BoonType type) {
        String desc = type.getDescription();
        
        // Parse duration from description (e.g., "for 10 minutes", "for 5 minutes", etc.)
        if (desc.contains("30 minutes")) return 36000; // 30 min * 60 sec * 20 ticks
        if (desc.contains("20 minutes")) return 24000;
        if (desc.contains("15 minutes")) return 18000;
        if (desc.contains("10 minutes")) return 12000;
        if (desc.contains("8 minutes")) return 9600;
        if (desc.contains("5 minutes")) return 6000;
        if (desc.contains("3 minutes")) return 3600;
        if (desc.contains("2 minutes")) return 2400;
        if (desc.contains("1 minute")) return 1200;
        if (desc.contains("30 seconds")) return 600;
        if (desc.contains("3 seconds")) return 60;
        if (desc.contains("3 nights")) return 72000; // ~60 min (3 Minecraft days)
        
        // Default to 10 minutes if not specified
        return 12000;
    }

    public BoonType getType() {
        return type;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public int getUsesRemaining() {
        return usesRemaining;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    /**
     * Consume one use of this boon
     * @return true if there are uses remaining, false if depleted
     */
    public boolean consumeUse() {
        if (usesRemaining > 0) {
            usesRemaining--;
            return usesRemaining > 0;
        }
        return usesRemaining == -1; // Unlimited uses
    }

    /**
     * Check if this boon has expired
     */
    public boolean isExpired(long currentGameTime) {
        // Permanent boons never expire by time
        if (expirationTime == 0) {
            // For ONE_USE boons, check if uses are depleted
            if (type.getDuration() == BoonType.BoonDuration.ONE_USE) {
                return usesRemaining <= 0;
            }
            return false; // Permanent
        }
        
        // Temporary boons expire when time is reached
        return currentGameTime >= expirationTime;
    }

    /**
     * Get remaining time in ticks
     */
    public long getRemainingTime(long currentGameTime) {
        if (expirationTime == 0) return -1; // Permanent or one-use
        return Math.max(0, expirationTime - currentGameTime);
    }

    /**
     * Serialize to NBT
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", type.name());
        tag.putLong("ExpirationTime", expirationTime);
        tag.putInt("UsesRemaining", usesRemaining);
        tag.putBoolean("Activated", activated);
        return tag;
    }

    /**
     * Deserialize from NBT
     */
    public static ActiveBoon deserializeNBT(CompoundTag tag) {
        try {
            BoonType type = BoonType.valueOf(tag.getString("Type"));
            long expirationTime = tag.getLong("ExpirationTime");
            int usesRemaining = tag.getInt("UsesRemaining");
            boolean activated = tag.getBoolean("Activated");
            return new ActiveBoon(type, expirationTime, usesRemaining, activated);
        } catch (IllegalArgumentException e) {
            // Invalid boon type, return null
            return null;
        }
    }

    @Override
    public String toString() {
        return "ActiveBoon{" +
                "type=" + type.name() +
                ", expires=" + expirationTime +
                ", uses=" + usesRemaining +
                ", activated=" + activated +
                '}';
    }
}
