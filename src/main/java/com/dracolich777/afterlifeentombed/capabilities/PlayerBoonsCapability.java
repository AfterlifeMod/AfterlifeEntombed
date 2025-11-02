package com.dracolich777.afterlifeentombed.capabilities;

import com.dracolich777.afterlifeentombed.boons.ActiveBoon;
import com.dracolich777.afterlifeentombed.boons.BoonType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Capability for tracking active boons and curses on a player
 */
public class PlayerBoonsCapability {
    public static Capability<IPlayerBoons> PLAYER_BOONS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public interface IPlayerBoons {
        List<ActiveBoon> getActiveBoons();
        void addBoon(ActiveBoon boon);
        void removeBoon(ActiveBoon boon);
        void clearExpiredBoons(long currentGameTime);
        boolean hasBoon(BoonType type);
        ActiveBoon getBoon(BoonType type);
        
        // NBT serialization methods
        CompoundTag serializeNBT();
        void deserializeNBT(CompoundTag nbt);
        
        /**
         * Get only the blessings (positive boons)
         */
        default List<ActiveBoon> getBlessings() {
            return getActiveBoons().stream()
                .filter(boon -> boon.getType().isBlessing())
                .collect(Collectors.toList());
        }
        
        /**
         * Get only the curses (negative boons)
         */
        default List<ActiveBoon> getCurses() {
            return getActiveBoons().stream()
                .filter(boon -> boon.getType().isCurse())
                .collect(Collectors.toList());
        }
    }

    public static class PlayerBoons implements IPlayerBoons {
        private final List<ActiveBoon> activeBoons = new ArrayList<>();

        @Override
        public List<ActiveBoon> getActiveBoons() {
            return activeBoons;
        }

        @Override
        public void addBoon(ActiveBoon boon) {
            // Remove existing boon of same type if present
            activeBoons.removeIf(b -> b.getType() == boon.getType());
            activeBoons.add(boon);
        }

        @Override
        public void removeBoon(ActiveBoon boon) {
            activeBoons.remove(boon);
        }

        @Override
        public void clearExpiredBoons(long currentGameTime) {
            activeBoons.removeIf(boon -> boon.isExpired(currentGameTime));
        }

        @Override
        public boolean hasBoon(BoonType type) {
            return activeBoons.stream().anyMatch(b -> b.getType() == type);
        }

        @Override
        public ActiveBoon getBoon(BoonType type) {
            return activeBoons.stream()
                .filter(b -> b.getType() == type)
                .findFirst()
                .orElse(null);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            ListTag boonsList = new ListTag();
            
            for (ActiveBoon boon : activeBoons) {
                boonsList.add(boon.serializeNBT());
            }
            
            tag.put("ActiveBoons", boonsList);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            activeBoons.clear();
            
            if (nbt.contains("ActiveBoons", Tag.TAG_LIST)) {
                ListTag boonsList = nbt.getList("ActiveBoons", Tag.TAG_COMPOUND);
                
                for (int i = 0; i < boonsList.size(); i++) {
                    CompoundTag boonTag = boonsList.getCompound(i);
                    ActiveBoon boon = ActiveBoon.deserializeNBT(boonTag);
                    if (boon != null) {
                        activeBoons.add(boon);
                    }
                }
            }
        }
    }

    public static class PlayerBoonsProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final PlayerBoons playerBoons = new PlayerBoons();
        private final LazyOptional<IPlayerBoons> optional = LazyOptional.of(() -> playerBoons);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == PLAYER_BOONS_CAPABILITY) {
                return optional.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return playerBoons.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            try {
                playerBoons.deserializeNBT(nbt);
            } catch (Exception e) {
                System.err.println("Failed to deserialize PlayerBoonsCapability: " + e.getMessage());
                e.printStackTrace();
            }
        }

        public void invalidate() {
            optional.invalidate();
        }
    }
}
