package com.dracolich777.afterlifeentombed.capabilities;

import com.dracolich777.afterlifeentombed.items.GodType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Capability for tracking which god a player has chosen as their avatar.
 * This is used for the Agent of Gods origin system.
 */
public class GodAvatarCapability {
    public static Capability<IGodAvatar> GOD_AVATAR_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public interface IGodAvatar {
        GodType getSelectedGod();
        void setSelectedGod(GodType god);
        
        // Unlocked gods tracking (collection system)
        Set<GodType> getUnlockedGods();
        void unlockGod(GodType god);
        void lockGod(GodType god);
        boolean hasUnlockedGod(GodType god);
        
        // God switching grace period to prevent immediate ability activation
        long getGodSwitchGracePeriod();
        void setGodSwitchGracePeriod(long endTime);
        
        // Cooldown trackers for Seth abilities
        long getOneWithChaosCooldown();
        void setOneWithChaosCooldown(long cooldown);
        int getOneWithChaosTimeUsed(); // Track time used for 2 min limit
        void setOneWithChaosTimeUsed(int time);
        boolean isOneWithChaosActive();
        void setOneWithChaosActive(boolean active);
        
        long getDamageNegationCooldown();
        void setDamageNegationCooldown(long cooldown);
        boolean isDamageNegationActive();
        void setDamageNegationActive(boolean active);
        float getStoredDamage();
        void setStoredDamage(float damage);
        
        long getDesertWalkerCooldown();
        void setDesertWalkerCooldown(long cooldown);
        boolean isDesertWalkerFlying();
        void setDesertWalkerFlying(boolean flying);
        
        long getChaosIncarnateCooldown();
        void setChaosIncarnateCooldown(long cooldown);
        boolean isChaosIncarnateActive();
        void setChaosIncarnateActive(boolean active);
        
        // Cooldown trackers for Ra abilities
        long getSolarFlareCooldown();
        void setSolarFlareCooldown(long cooldown);
        
        long getPurifyingLightCooldown();
        void setPurifyingLightCooldown(long cooldown);
        boolean isPurifyingLightActive();
        void setPurifyingLightActive(boolean active);
        long getPurifyingLightEndTime();
        void setPurifyingLightEndTime(long endTime);
        
        long getHolyInfernoCooldown();
        void setHolyInfernoCooldown(long cooldown);
        
        long getAvatarOfSunCooldown();
        void setAvatarOfSunCooldown(long cooldown);
        boolean isAvatarOfSunActive();
        void setAvatarOfSunActive(boolean active);
        
        // Cooldown trackers for Shu abilities
        long getLaunchCooldown();
        void setLaunchCooldown(long cooldown);
        
        long getAirBoostCooldown();
        void setAirBoostCooldown(long cooldown);
        
        long getWindAvatarCooldown();
        void setWindAvatarCooldown(long cooldown);
        boolean isWindAvatarActive();
        void setWindAvatarActive(boolean active);
        long getWindAvatarEndTime();
        void setWindAvatarEndTime(long endTime);
        
        int getExtraJumpsUsed();
        void setExtraJumpsUsed(int jumps);
        long getExtraJumpsCooldown();
        void setExtraJumpsCooldown(long cooldown);
        
        // Cooldown trackers for Anubis abilities
        long getUndeadCommandCooldown();
        void setUndeadCommandCooldown(long cooldown);
        
        long getLifelinkCooldown();
        void setLifelinkCooldown(long cooldown);
        boolean isLifelinkActive();
        void setLifelinkActive(boolean active);
        long getLifelinkEndTime();
        void setLifelinkEndTime(long endTime);
        
        long getSummonUndeadCooldown();
        void setSummonUndeadCooldown(long cooldown);
        
        long getAvatarOfDeathCooldown();
        void setAvatarOfDeathCooldown(long cooldown);
        boolean isAvatarOfDeathActive();
        void setAvatarOfDeathActive(boolean active);
        long getAvatarOfDeathEndTime();
        void setAvatarOfDeathEndTime(long endTime);
        
        // Cooldown trackers for Thoth abilities
        long getScholarlyTeleportCooldown();
        void setScholarlyTeleportCooldown(long cooldown);
        
        long getExperienceMultiplierCooldown();
        void setExperienceMultiplierCooldown(long cooldown);
        boolean isExperienceMultiplierActive();
        void setExperienceMultiplierActive(boolean active);
        
        long getDivineEnchantCooldown();
        void setDivineEnchantCooldown(long cooldown);
        
        long getAvatarOfWisdomCooldown();
        void setAvatarOfWisdomCooldown(long cooldown);
        boolean isAvatarOfWisdomActive();
        void setAvatarOfWisdomActive(boolean active);
        long getAvatarOfWisdomEndTime();
        void setAvatarOfWisdomEndTime(long endTime);
        
        // Workstation tracking for Thoth
        long getLastWorkstationX();
        void setLastWorkstationX(long x);
        long getLastWorkstationY();
        void setLastWorkstationY(long y);
        long getLastWorkstationZ();
        void setLastWorkstationZ(long z);
        String getLastWorkstationDimension();
        void setLastWorkstationDimension(String dimension);
        boolean hasWorkstation();
        void clearWorkstation();
        
        // Cooldown trackers for Geb abilities
        long getTelekinesisCooldown();
        void setTelekinesisCooldown(long cooldown);
        
        long getExcavationCooldown();
        void setExcavationCooldown(long cooldown);
        
        long getEarthRiseCooldown();
        void setEarthRiseCooldown(long cooldown);
        
        long getAvatarOfEarthCooldown();
        void setAvatarOfEarthCooldown(long cooldown);
        boolean isAvatarOfEarthActive();
        void setAvatarOfEarthActive(boolean active);
        long getAvatarOfEarthEndTime();
        void setAvatarOfEarthEndTime(long endTime);
        
        CompoundTag serializeNBT();
        void deserializeNBT(CompoundTag nbt);
    }

    public static class GodAvatar implements IGodAvatar {
        private GodType selectedGod = GodType.NONE; // Default to NONE until player selects a god
        
        // Unlocked gods collection
        private Set<GodType> unlockedGods = new HashSet<>();
        
        // God switching grace period to prevent immediate ability activation after switching
        private long godSwitchGracePeriod = 0;
        
        // Cooldown tracking (using game time in ticks)
        private long oneWithChaosCooldown = 0;
        private int oneWithChaosTimeUsed = 0;
        private boolean oneWithChaosActive = false;
        
        private long damageNegationCooldown = 0;
        private boolean damageNegationActive = false;
        private float storedDamage = 0.0f;
        
        private long desertWalkerCooldown = 0;
        private boolean desertWalkerFlying = false;
        
        private long chaosIncarnateCooldown = 0;
        private boolean chaosIncarnateActive = false;
        
        // Ra ability tracking
        private long solarFlareCooldown = 0;
        
        private long purifyingLightCooldown = 0;
        private boolean purifyingLightActive = false;
        private long purifyingLightEndTime = 0;
        
        private long holyInfernoCooldown = 0;
        
        private long avatarOfSunCooldown = 0;
        private boolean avatarOfSunActive = false;
        
        // Shu ability tracking
        private long launchCooldown = 0;
        private long airBoostCooldown = 0;
        private long windAvatarCooldown = 0;
        private boolean windAvatarActive = false;
        private long windAvatarEndTime = 0;
        private int extraJumpsUsed = 0;
        private long extraJumpsCooldown = 0;
        
        // Anubis ability tracking
        private long undeadCommandCooldown = 0;
        private long lifelinkCooldown = 0;
        private boolean lifelinkActive = false;
        private long lifelinkEndTime = 0;
        private long summonUndeadCooldown = 0;
        private long avatarOfDeathCooldown = 0;
        private boolean avatarOfDeathActive = false;
        private long avatarOfDeathEndTime = 0;
        
        // Thoth ability tracking
        private long scholarlyTeleportCooldown = 0;
        private long experienceMultiplierCooldown = 0;
        private boolean experienceMultiplierActive = false;
        private long divineEnchantCooldown = 0;
        private long avatarOfWisdomCooldown = 0;
        private boolean avatarOfWisdomActive = false;
        private long avatarOfWisdomEndTime = 0;
        
        // Workstation tracking for Thoth
        private long lastWorkstationX = 0;
        private long lastWorkstationY = -65; // Invalid Y position to indicate no workstation
        private long lastWorkstationZ = 0;
        private String lastWorkstationDimension = "";
        
        // Geb ability tracking
        private long telekinesisCooldown = 0;
        private long excavationCooldown = 0;
        private long earthRiseCooldown = 0;
        private long avatarOfEarthCooldown = 0;
        private boolean avatarOfEarthActive = false;
        private long avatarOfEarthEndTime = 0;

        @Override
        public GodType getSelectedGod() {
            return selectedGod;
        }

        @Override
        public void setSelectedGod(GodType god) {
            this.selectedGod = god;
        }

        @Override
        public Set<GodType> getUnlockedGods() {
            return new HashSet<>(unlockedGods); // Return copy to prevent external modification
        }

        @Override
        public void unlockGod(GodType god) {
            if (god != GodType.NONE) {
                unlockedGods.add(god);
            }
        }

        @Override
        public void lockGod(GodType god) {
            unlockedGods.remove(god);
        }

        @Override
        public boolean hasUnlockedGod(GodType god) {
            return unlockedGods.contains(god);
        }

        @Override
        public long getGodSwitchGracePeriod() {
            return godSwitchGracePeriod;
        }

        @Override
        public void setGodSwitchGracePeriod(long endTime) {
            this.godSwitchGracePeriod = endTime;
        }

        @Override
        public long getOneWithChaosCooldown() {
            return oneWithChaosCooldown;
        }

        @Override
        public void setOneWithChaosCooldown(long cooldown) {
            this.oneWithChaosCooldown = cooldown;
        }

        @Override
        public int getOneWithChaosTimeUsed() {
            return oneWithChaosTimeUsed;
        }

        @Override
        public void setOneWithChaosTimeUsed(int time) {
            this.oneWithChaosTimeUsed = time;
        }

        @Override
        public boolean isOneWithChaosActive() {
            return oneWithChaosActive;
        }

        @Override
        public void setOneWithChaosActive(boolean active) {
            this.oneWithChaosActive = active;
        }

        @Override
        public long getDamageNegationCooldown() {
            return damageNegationCooldown;
        }

        @Override
        public void setDamageNegationCooldown(long cooldown) {
            this.damageNegationCooldown = cooldown;
        }

        @Override
        public boolean isDamageNegationActive() {
            return damageNegationActive;
        }

        @Override
        public void setDamageNegationActive(boolean active) {
            this.damageNegationActive = active;
        }

        @Override
        public float getStoredDamage() {
            return storedDamage;
        }

        @Override
        public void setStoredDamage(float damage) {
            this.storedDamage = damage;
        }

        @Override
        public long getDesertWalkerCooldown() {
            return desertWalkerCooldown;
        }

        @Override
        public void setDesertWalkerCooldown(long cooldown) {
            this.desertWalkerCooldown = cooldown;
        }

        @Override
        public boolean isDesertWalkerFlying() {
            return desertWalkerFlying;
        }

        @Override
        public void setDesertWalkerFlying(boolean flying) {
            this.desertWalkerFlying = flying;
        }

        @Override
        public long getChaosIncarnateCooldown() {
            return chaosIncarnateCooldown;
        }

        @Override
        public void setChaosIncarnateCooldown(long cooldown) {
            this.chaosIncarnateCooldown = cooldown;
        }

        @Override
        public boolean isChaosIncarnateActive() {
            return chaosIncarnateActive;
        }

        @Override
        public void setChaosIncarnateActive(boolean active) {
            this.chaosIncarnateActive = active;
        }

        // Ra ability getters/setters
        @Override
        public long getSolarFlareCooldown() {
            return solarFlareCooldown;
        }

        @Override
        public void setSolarFlareCooldown(long cooldown) {
            this.solarFlareCooldown = cooldown;
        }

        @Override
        public long getPurifyingLightCooldown() {
            return purifyingLightCooldown;
        }

        @Override
        public void setPurifyingLightCooldown(long cooldown) {
            this.purifyingLightCooldown = cooldown;
        }

        @Override
        public boolean isPurifyingLightActive() {
            return purifyingLightActive;
        }

        @Override
        public void setPurifyingLightActive(boolean active) {
            this.purifyingLightActive = active;
        }

        @Override
        public long getPurifyingLightEndTime() {
            return purifyingLightEndTime;
        }

        @Override
        public void setPurifyingLightEndTime(long endTime) {
            this.purifyingLightEndTime = endTime;
        }

        @Override
        public long getHolyInfernoCooldown() {
            return holyInfernoCooldown;
        }

        @Override
        public void setHolyInfernoCooldown(long cooldown) {
            this.holyInfernoCooldown = cooldown;
        }

        @Override
        public long getAvatarOfSunCooldown() {
            return avatarOfSunCooldown;
        }

        @Override
        public void setAvatarOfSunCooldown(long cooldown) {
            this.avatarOfSunCooldown = cooldown;
        }

        @Override
        public boolean isAvatarOfSunActive() {
            return avatarOfSunActive;
        }

        @Override
        public void setAvatarOfSunActive(boolean active) {
            this.avatarOfSunActive = active;
        }

        // Shu ability getters/setters
        @Override
        public long getLaunchCooldown() {
            return launchCooldown;
        }

        @Override
        public void setLaunchCooldown(long cooldown) {
            this.launchCooldown = cooldown;
        }

        @Override
        public long getAirBoostCooldown() {
            return airBoostCooldown;
        }

        @Override
        public void setAirBoostCooldown(long cooldown) {
            this.airBoostCooldown = cooldown;
        }

        @Override
        public long getWindAvatarCooldown() {
            return windAvatarCooldown;
        }

        @Override
        public void setWindAvatarCooldown(long cooldown) {
            this.windAvatarCooldown = cooldown;
        }

        @Override
        public boolean isWindAvatarActive() {
            return windAvatarActive;
        }

        @Override
        public void setWindAvatarActive(boolean active) {
            this.windAvatarActive = active;
        }

        @Override
        public long getWindAvatarEndTime() {
            return windAvatarEndTime;
        }

        @Override
        public void setWindAvatarEndTime(long endTime) {
            this.windAvatarEndTime = endTime;
        }

        @Override
        public int getExtraJumpsUsed() {
            return extraJumpsUsed;
        }

        @Override
        public void setExtraJumpsUsed(int jumps) {
            this.extraJumpsUsed = jumps;
        }

        @Override
        public long getExtraJumpsCooldown() {
            return extraJumpsCooldown;
        }

        @Override
        public void setExtraJumpsCooldown(long cooldown) {
            this.extraJumpsCooldown = cooldown;
        }

        @Override
        public long getUndeadCommandCooldown() {
            return undeadCommandCooldown;
        }

        @Override
        public void setUndeadCommandCooldown(long cooldown) {
            this.undeadCommandCooldown = cooldown;
        }

        @Override
        public long getLifelinkCooldown() {
            return lifelinkCooldown;
        }

        @Override
        public void setLifelinkCooldown(long cooldown) {
            this.lifelinkCooldown = cooldown;
        }

        @Override
        public boolean isLifelinkActive() {
            return lifelinkActive;
        }

        @Override
        public void setLifelinkActive(boolean active) {
            this.lifelinkActive = active;
        }

        @Override
        public long getLifelinkEndTime() {
            return lifelinkEndTime;
        }

        @Override
        public void setLifelinkEndTime(long endTime) {
            this.lifelinkEndTime = endTime;
        }

        @Override
        public long getSummonUndeadCooldown() {
            return summonUndeadCooldown;
        }

        @Override
        public void setSummonUndeadCooldown(long cooldown) {
            this.summonUndeadCooldown = cooldown;
        }

        @Override
        public long getAvatarOfDeathCooldown() {
            return avatarOfDeathCooldown;
        }

        @Override
        public void setAvatarOfDeathCooldown(long cooldown) {
            this.avatarOfDeathCooldown = cooldown;
        }

        @Override
        public boolean isAvatarOfDeathActive() {
            return avatarOfDeathActive;
        }

        @Override
        public void setAvatarOfDeathActive(boolean active) {
            this.avatarOfDeathActive = active;
        }

        @Override
        public long getAvatarOfDeathEndTime() {
            return avatarOfDeathEndTime;
        }

        @Override
        public void setAvatarOfDeathEndTime(long endTime) {
            this.avatarOfDeathEndTime = endTime;
        }
        
        // Thoth ability getters/setters
        @Override
        public long getScholarlyTeleportCooldown() {
            return scholarlyTeleportCooldown;
        }
        
        @Override
        public void setScholarlyTeleportCooldown(long cooldown) {
            this.scholarlyTeleportCooldown = cooldown;
        }
        
        @Override
        public long getExperienceMultiplierCooldown() {
            return experienceMultiplierCooldown;
        }
        
        @Override
        public void setExperienceMultiplierCooldown(long cooldown) {
            this.experienceMultiplierCooldown = cooldown;
        }
        
        @Override
        public boolean isExperienceMultiplierActive() {
            return experienceMultiplierActive;
        }
        
        @Override
        public void setExperienceMultiplierActive(boolean active) {
            this.experienceMultiplierActive = active;
        }
        
        @Override
        public long getDivineEnchantCooldown() {
            return divineEnchantCooldown;
        }
        
        @Override
        public void setDivineEnchantCooldown(long cooldown) {
            this.divineEnchantCooldown = cooldown;
        }
        
        @Override
        public long getAvatarOfWisdomCooldown() {
            return avatarOfWisdomCooldown;
        }
        
        @Override
        public void setAvatarOfWisdomCooldown(long cooldown) {
            this.avatarOfWisdomCooldown = cooldown;
        }
        
        @Override
        public boolean isAvatarOfWisdomActive() {
            return avatarOfWisdomActive;
        }
        
        @Override
        public void setAvatarOfWisdomActive(boolean active) {
            this.avatarOfWisdomActive = active;
        }
        
        @Override
        public long getAvatarOfWisdomEndTime() {
            return avatarOfWisdomEndTime;
        }
        
        @Override
        public void setAvatarOfWisdomEndTime(long endTime) {
            this.avatarOfWisdomEndTime = endTime;
        }
        
        @Override
        public long getLastWorkstationX() {
            return lastWorkstationX;
        }
        
        @Override
        public void setLastWorkstationX(long x) {
            this.lastWorkstationX = x;
        }
        
        @Override
        public long getLastWorkstationY() {
            return lastWorkstationY;
        }
        
        @Override
        public void setLastWorkstationY(long y) {
            this.lastWorkstationY = y;
        }
        
        @Override
        public long getLastWorkstationZ() {
            return lastWorkstationZ;
        }
        
        @Override
        public void setLastWorkstationZ(long z) {
            this.lastWorkstationZ = z;
        }
        
        @Override
        public String getLastWorkstationDimension() {
            return lastWorkstationDimension;
        }
        
        @Override
        public void setLastWorkstationDimension(String dimension) {
            this.lastWorkstationDimension = dimension;
        }
        
        @Override
        public boolean hasWorkstation() {
            return lastWorkstationY >= -64; // Valid Y position
        }
        
        @Override
        public void clearWorkstation() {
            lastWorkstationY = -65; // Set to invalid position
        }
        
        // Geb ability getters/setters
        @Override
        public long getTelekinesisCooldown() {
            return telekinesisCooldown;
        }
        
        @Override
        public void setTelekinesisCooldown(long cooldown) {
            this.telekinesisCooldown = cooldown;
        }
        
        @Override
        public long getExcavationCooldown() {
            return excavationCooldown;
        }
        
        @Override
        public void setExcavationCooldown(long cooldown) {
            this.excavationCooldown = cooldown;
        }
        
        @Override
        public long getEarthRiseCooldown() {
            return earthRiseCooldown;
        }
        
        @Override
        public void setEarthRiseCooldown(long cooldown) {
            this.earthRiseCooldown = cooldown;
        }
        
        @Override
        public long getAvatarOfEarthCooldown() {
            return avatarOfEarthCooldown;
        }
        
        @Override
        public void setAvatarOfEarthCooldown(long cooldown) {
            this.avatarOfEarthCooldown = cooldown;
        }
        
        @Override
        public boolean isAvatarOfEarthActive() {
            return avatarOfEarthActive;
        }
        
        @Override
        public void setAvatarOfEarthActive(boolean active) {
            this.avatarOfEarthActive = active;
        }
        
        @Override
        public long getAvatarOfEarthEndTime() {
            return avatarOfEarthEndTime;
        }
        
        @Override
        public void setAvatarOfEarthEndTime(long endTime) {
            this.avatarOfEarthEndTime = endTime;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("SelectedGod", selectedGod.name());
            nbt.putLong("GodSwitchGracePeriod", godSwitchGracePeriod);
            
            // Serialize unlocked gods
            StringBuilder unlockedGodsStr = new StringBuilder();
            for (GodType god : unlockedGods) {
                if (unlockedGodsStr.length() > 0) {
                    unlockedGodsStr.append(",");
                }
                unlockedGodsStr.append(god.name());
            }
            nbt.putString("UnlockedGods", unlockedGodsStr.toString());
            
            nbt.putLong("OneWithChaosCooldown", oneWithChaosCooldown);
            nbt.putInt("OneWithChaosTimeUsed", oneWithChaosTimeUsed);
            nbt.putBoolean("OneWithChaosActive", oneWithChaosActive);
            nbt.putLong("DamageNegationCooldown", damageNegationCooldown);
            nbt.putBoolean("DamageNegationActive", damageNegationActive);
            nbt.putFloat("StoredDamage", storedDamage);
            nbt.putLong("DesertWalkerCooldown", desertWalkerCooldown);
            nbt.putBoolean("DesertWalkerFlying", desertWalkerFlying);
            nbt.putLong("ChaosIncarnateCooldown", chaosIncarnateCooldown);
            nbt.putBoolean("ChaosIncarnateActive", chaosIncarnateActive);
            // Ra ability fields
            nbt.putLong("SolarFlareCooldown", solarFlareCooldown);
            nbt.putLong("PurifyingLightCooldown", purifyingLightCooldown);
            nbt.putBoolean("PurifyingLightActive", purifyingLightActive);
            nbt.putLong("PurifyingLightEndTime", purifyingLightEndTime);
            nbt.putLong("HolyInfernoCooldown", holyInfernoCooldown);
            nbt.putLong("AvatarOfSunCooldown", avatarOfSunCooldown);
            nbt.putBoolean("AvatarOfSunActive", avatarOfSunActive);
            // Shu ability fields
            nbt.putLong("LaunchCooldown", launchCooldown);
            nbt.putLong("AirBoostCooldown", airBoostCooldown);
            nbt.putLong("WindAvatarCooldown", windAvatarCooldown);
            nbt.putBoolean("WindAvatarActive", windAvatarActive);
            nbt.putLong("WindAvatarEndTime", windAvatarEndTime);
            nbt.putInt("ExtraJumpsUsed", extraJumpsUsed);
            nbt.putLong("ExtraJumpsCooldown", extraJumpsCooldown);
            // Anubis ability fields
            nbt.putLong("UndeadCommandCooldown", undeadCommandCooldown);
            nbt.putLong("LifelinkCooldown", lifelinkCooldown);
            nbt.putBoolean("LifelinkActive", lifelinkActive);
            nbt.putLong("LifelinkEndTime", lifelinkEndTime);
            nbt.putLong("SummonUndeadCooldown", summonUndeadCooldown);
            nbt.putLong("AvatarOfDeathCooldown", avatarOfDeathCooldown);
            nbt.putBoolean("AvatarOfDeathActive", avatarOfDeathActive);
            nbt.putLong("AvatarOfDeathEndTime", avatarOfDeathEndTime);
            // Thoth ability fields
            nbt.putLong("ScholarlyTeleportCooldown", scholarlyTeleportCooldown);
            nbt.putLong("ExperienceMultiplierCooldown", experienceMultiplierCooldown);
            nbt.putBoolean("ExperienceMultiplierActive", experienceMultiplierActive);
            nbt.putLong("DivineEnchantCooldown", divineEnchantCooldown);
            nbt.putLong("AvatarOfWisdomCooldown", avatarOfWisdomCooldown);
            nbt.putBoolean("AvatarOfWisdomActive", avatarOfWisdomActive);
            nbt.putLong("AvatarOfWisdomEndTime", avatarOfWisdomEndTime);
            // Workstation tracking
            nbt.putLong("LastWorkstationX", lastWorkstationX);
            nbt.putLong("LastWorkstationY", lastWorkstationY);
            nbt.putLong("LastWorkstationZ", lastWorkstationZ);
            nbt.putString("LastWorkstationDimension", lastWorkstationDimension);
            // Geb ability fields
            nbt.putLong("TelekinesisCooldown", telekinesisCooldown);
            nbt.putLong("ExcavationCooldown", excavationCooldown);
            nbt.putLong("EarthRiseCooldown", earthRiseCooldown);
            nbt.putLong("AvatarOfEarthCooldown", avatarOfEarthCooldown);
            nbt.putBoolean("AvatarOfEarthActive", avatarOfEarthActive);
            nbt.putLong("AvatarOfEarthEndTime", avatarOfEarthEndTime);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            // Handle null or empty NBT gracefully
            if (nbt == null || nbt.isEmpty()) {
                // Use defaults
                selectedGod = GodType.NONE;
                return;
            }
            
            // Safely read god type with fallback
            if (nbt.contains("SelectedGod")) {
                try {
                    selectedGod = GodType.valueOf(nbt.getString("SelectedGod"));
                } catch (IllegalArgumentException e) {
                    selectedGod = GodType.NONE; // Default to NONE if invalid
                }
            } else {
                selectedGod = GodType.NONE; // No saved data, default to NONE
            }
            
            // Safely read all other values with defaults
            godSwitchGracePeriod = nbt.contains("GodSwitchGracePeriod") ? nbt.getLong("GodSwitchGracePeriod") : 0;
            
            // Deserialize unlocked gods
            unlockedGods.clear();
            if (nbt.contains("UnlockedGods")) {
                String unlockedStr = nbt.getString("UnlockedGods");
                if (!unlockedStr.isEmpty()) {
                    String[] godNames = unlockedStr.split(",");
                    for (String godName : godNames) {
                        try {
                            GodType god = GodType.valueOf(godName.trim());
                            unlockedGods.add(god);
                        } catch (IllegalArgumentException e) {
                            // Invalid god name, skip it
                        }
                    }
                }
            }
            
            oneWithChaosCooldown = nbt.contains("OneWithChaosCooldown") ? nbt.getLong("OneWithChaosCooldown") : 0;
            oneWithChaosTimeUsed = nbt.contains("OneWithChaosTimeUsed") ? nbt.getInt("OneWithChaosTimeUsed") : 0;
            oneWithChaosActive = nbt.contains("OneWithChaosActive") && nbt.getBoolean("OneWithChaosActive");
            damageNegationCooldown = nbt.contains("DamageNegationCooldown") ? nbt.getLong("DamageNegationCooldown") : 0;
            damageNegationActive = nbt.contains("DamageNegationActive") && nbt.getBoolean("DamageNegationActive");
            storedDamage = nbt.contains("StoredDamage") ? nbt.getFloat("StoredDamage") : 0.0f;
            desertWalkerCooldown = nbt.contains("DesertWalkerCooldown") ? nbt.getLong("DesertWalkerCooldown") : 0;
            desertWalkerFlying = nbt.contains("DesertWalkerFlying") && nbt.getBoolean("DesertWalkerFlying");
            chaosIncarnateCooldown = nbt.contains("ChaosIncarnateCooldown") ? nbt.getLong("ChaosIncarnateCooldown") : 0;
            chaosIncarnateActive = nbt.contains("ChaosIncarnateActive") && nbt.getBoolean("ChaosIncarnateActive");
            // Ra ability fields
            solarFlareCooldown = nbt.contains("SolarFlareCooldown") ? nbt.getLong("SolarFlareCooldown") : 0;
            purifyingLightCooldown = nbt.contains("PurifyingLightCooldown") ? nbt.getLong("PurifyingLightCooldown") : 0;
            purifyingLightActive = nbt.contains("PurifyingLightActive") && nbt.getBoolean("PurifyingLightActive");
            purifyingLightEndTime = nbt.contains("PurifyingLightEndTime") ? nbt.getLong("PurifyingLightEndTime") : 0;
            holyInfernoCooldown = nbt.contains("HolyInfernoCooldown") ? nbt.getLong("HolyInfernoCooldown") : 0;
            avatarOfSunCooldown = nbt.contains("AvatarOfSunCooldown") ? nbt.getLong("AvatarOfSunCooldown") : 0;
            avatarOfSunActive = nbt.contains("AvatarOfSunActive") && nbt.getBoolean("AvatarOfSunActive");
            // Shu ability fields
            launchCooldown = nbt.contains("LaunchCooldown") ? nbt.getLong("LaunchCooldown") : 0;
            airBoostCooldown = nbt.contains("AirBoostCooldown") ? nbt.getLong("AirBoostCooldown") : 0;
            windAvatarCooldown = nbt.contains("WindAvatarCooldown") ? nbt.getLong("WindAvatarCooldown") : 0;
            windAvatarActive = nbt.contains("WindAvatarActive") && nbt.getBoolean("WindAvatarActive");
            windAvatarEndTime = nbt.contains("WindAvatarEndTime") ? nbt.getLong("WindAvatarEndTime") : 0;
            extraJumpsUsed = nbt.contains("ExtraJumpsUsed") ? nbt.getInt("ExtraJumpsUsed") : 0;
            extraJumpsCooldown = nbt.contains("ExtraJumpsCooldown") ? nbt.getLong("ExtraJumpsCooldown") : 0;
            // Anubis ability fields
            undeadCommandCooldown = nbt.contains("UndeadCommandCooldown") ? nbt.getLong("UndeadCommandCooldown") : 0;
            lifelinkCooldown = nbt.contains("LifelinkCooldown") ? nbt.getLong("LifelinkCooldown") : 0;
            lifelinkActive = nbt.contains("LifelinkActive") && nbt.getBoolean("LifelinkActive");
            lifelinkEndTime = nbt.contains("LifelinkEndTime") ? nbt.getLong("LifelinkEndTime") : 0;
            summonUndeadCooldown = nbt.contains("SummonUndeadCooldown") ? nbt.getLong("SummonUndeadCooldown") : 0;
            avatarOfDeathCooldown = nbt.contains("AvatarOfDeathCooldown") ? nbt.getLong("AvatarOfDeathCooldown") : 0;
            avatarOfDeathActive = nbt.contains("AvatarOfDeathActive") && nbt.getBoolean("AvatarOfDeathActive");
            avatarOfDeathEndTime = nbt.contains("AvatarOfDeathEndTime") ? nbt.getLong("AvatarOfDeathEndTime") : 0;
            // Thoth ability fields
            scholarlyTeleportCooldown = nbt.contains("ScholarlyTeleportCooldown") ? nbt.getLong("ScholarlyTeleportCooldown") : 0;
            experienceMultiplierCooldown = nbt.contains("ExperienceMultiplierCooldown") ? nbt.getLong("ExperienceMultiplierCooldown") : 0;
            experienceMultiplierActive = nbt.contains("ExperienceMultiplierActive") && nbt.getBoolean("ExperienceMultiplierActive");
            divineEnchantCooldown = nbt.contains("DivineEnchantCooldown") ? nbt.getLong("DivineEnchantCooldown") : 0;
            avatarOfWisdomCooldown = nbt.contains("AvatarOfWisdomCooldown") ? nbt.getLong("AvatarOfWisdomCooldown") : 0;
            avatarOfWisdomActive = nbt.contains("AvatarOfWisdomActive") && nbt.getBoolean("AvatarOfWisdomActive");
            avatarOfWisdomEndTime = nbt.contains("AvatarOfWisdomEndTime") ? nbt.getLong("AvatarOfWisdomEndTime") : 0;
            // Workstation tracking
            lastWorkstationX = nbt.contains("LastWorkstationX") ? nbt.getLong("LastWorkstationX") : 0;
            lastWorkstationY = nbt.contains("LastWorkstationY") ? nbt.getLong("LastWorkstationY") : -65;
            lastWorkstationZ = nbt.contains("LastWorkstationZ") ? nbt.getLong("LastWorkstationZ") : 0;
            lastWorkstationDimension = nbt.contains("LastWorkstationDimension") ? nbt.getString("LastWorkstationDimension") : "";
            // Geb ability fields
            telekinesisCooldown = nbt.contains("TelekinesisCooldown") ? nbt.getLong("TelekinesisCooldown") : 0;
            excavationCooldown = nbt.contains("ExcavationCooldown") ? nbt.getLong("ExcavationCooldown") : 0;
            earthRiseCooldown = nbt.contains("EarthRiseCooldown") ? nbt.getLong("EarthRiseCooldown") : 0;
            avatarOfEarthCooldown = nbt.contains("AvatarOfEarthCooldown") ? nbt.getLong("AvatarOfEarthCooldown") : 0;
            avatarOfEarthActive = nbt.contains("AvatarOfEarthActive") && nbt.getBoolean("AvatarOfEarthActive");
            avatarOfEarthEndTime = nbt.contains("AvatarOfEarthEndTime") ? nbt.getLong("AvatarOfEarthEndTime") : 0;
        }
    }

    public static class GodAvatarProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final GodAvatar godAvatar = new GodAvatar();
        private final LazyOptional<IGodAvatar> optional = LazyOptional.of(() -> godAvatar);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == GOD_AVATAR_CAPABILITY) {
                return optional.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return godAvatar.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            try {
                godAvatar.deserializeNBT(nbt);
            } catch (Exception e) {
                // If deserialization fails, log and use defaults
                System.err.println("Failed to deserialize GodAvatarCapability: " + e.getMessage());
                e.printStackTrace();
            }
        }

        public void invalidate() {
            optional.invalidate();
        }
    }
}
