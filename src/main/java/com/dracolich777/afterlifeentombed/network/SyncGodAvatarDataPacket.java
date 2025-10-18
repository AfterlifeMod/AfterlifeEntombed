package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.items.GodType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet for syncing full god avatar capability data from server to client
 * This is needed for the HUD overlay to display correct information
 */
public class SyncGodAvatarDataPacket {
    private final String godName;
    private final boolean oneWithChaosActive;
    private final int oneWithChaosTimeUsed;
    private final long oneWithChaosCooldown;
    private final boolean damageNegationActive;
    private final float storedDamage;
    private final long damageNegationCooldown;
    private final boolean desertWalkerFlying;
    private final long desertWalkerCooldown;
    private final boolean chaosIncarnateActive;
    private final long chaosIncarnateCooldown;
    // Ra ability fields
    private final long solarFlareCooldown;
    private final long purifyingLightCooldown;
    private final boolean purifyingLightActive;
    private final long purifyingLightEndTime;
    private final long holyInfernoCooldown;
    private final long avatarOfSunCooldown;
    private final boolean avatarOfSunActive;
    // Shu ability fields
    private final long launchCooldown;
    private final long airBoostCooldown;
    private final long windAvatarCooldown;
    private final boolean windAvatarActive;
    private final long windAvatarEndTime;
    private final int extraJumpsUsed;
    private final long extraJumpsCooldown;
    // Anubis ability fields
    private final long undeadCommandCooldown;
    private final long lifelinkCooldown;
    private final boolean lifelinkActive;
    private final long lifelinkEndTime;
    private final long summonUndeadCooldown;
    private final long avatarOfDeathCooldown;
    private final boolean avatarOfDeathActive;
    private final long avatarOfDeathEndTime;

    public SyncGodAvatarDataPacket(GodType god, 
                                   boolean oneWithChaosActive, int oneWithChaosTimeUsed, long oneWithChaosCooldown,
                                   boolean damageNegationActive, float storedDamage, long damageNegationCooldown,
                                   boolean desertWalkerFlying, long desertWalkerCooldown,
                                   boolean chaosIncarnateActive, long chaosIncarnateCooldown,
                                   long solarFlareCooldown, long purifyingLightCooldown,
                                   boolean purifyingLightActive, long purifyingLightEndTime,
                                   long holyInfernoCooldown, long avatarOfSunCooldown,
                                   boolean avatarOfSunActive,
                                   long launchCooldown, long airBoostCooldown,
                                   long windAvatarCooldown, boolean windAvatarActive, long windAvatarEndTime,
                                   int extraJumpsUsed, long extraJumpsCooldown,
                                   long undeadCommandCooldown, long lifelinkCooldown,
                                   boolean lifelinkActive, long lifelinkEndTime,
                                   long summonUndeadCooldown, long avatarOfDeathCooldown,
                                   boolean avatarOfDeathActive, long avatarOfDeathEndTime) {
        this.godName = god.name();
        this.oneWithChaosActive = oneWithChaosActive;
        this.oneWithChaosTimeUsed = oneWithChaosTimeUsed;
        this.oneWithChaosCooldown = oneWithChaosCooldown;
        this.damageNegationActive = damageNegationActive;
        this.storedDamage = storedDamage;
        this.damageNegationCooldown = damageNegationCooldown;
        this.desertWalkerFlying = desertWalkerFlying;
        this.desertWalkerCooldown = desertWalkerCooldown;
        this.chaosIncarnateActive = chaosIncarnateActive;
        this.chaosIncarnateCooldown = chaosIncarnateCooldown;
        this.solarFlareCooldown = solarFlareCooldown;
        this.purifyingLightCooldown = purifyingLightCooldown;
        this.purifyingLightActive = purifyingLightActive;
        this.purifyingLightEndTime = purifyingLightEndTime;
        this.holyInfernoCooldown = holyInfernoCooldown;
        this.avatarOfSunCooldown = avatarOfSunCooldown;
        this.avatarOfSunActive = avatarOfSunActive;
        this.launchCooldown = launchCooldown;
        this.airBoostCooldown = airBoostCooldown;
        this.windAvatarCooldown = windAvatarCooldown;
        this.windAvatarActive = windAvatarActive;
        this.windAvatarEndTime = windAvatarEndTime;
        this.extraJumpsUsed = extraJumpsUsed;
        this.extraJumpsCooldown = extraJumpsCooldown;
        this.undeadCommandCooldown = undeadCommandCooldown;
        this.lifelinkCooldown = lifelinkCooldown;
        this.lifelinkActive = lifelinkActive;
        this.lifelinkEndTime = lifelinkEndTime;
        this.summonUndeadCooldown = summonUndeadCooldown;
        this.avatarOfDeathCooldown = avatarOfDeathCooldown;
        this.avatarOfDeathActive = avatarOfDeathActive;
        this.avatarOfDeathEndTime = avatarOfDeathEndTime;
    }

    public static void encode(SyncGodAvatarDataPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.godName);
        buf.writeBoolean(packet.oneWithChaosActive);
        buf.writeInt(packet.oneWithChaosTimeUsed);
        buf.writeLong(packet.oneWithChaosCooldown);
        buf.writeBoolean(packet.damageNegationActive);
        buf.writeFloat(packet.storedDamage);
        buf.writeLong(packet.damageNegationCooldown);
        buf.writeBoolean(packet.desertWalkerFlying);
        buf.writeLong(packet.desertWalkerCooldown);
        buf.writeBoolean(packet.chaosIncarnateActive);
        buf.writeLong(packet.chaosIncarnateCooldown);
        // Ra ability fields
        buf.writeLong(packet.solarFlareCooldown);
        buf.writeLong(packet.purifyingLightCooldown);
        buf.writeBoolean(packet.purifyingLightActive);
        buf.writeLong(packet.purifyingLightEndTime);
        buf.writeLong(packet.holyInfernoCooldown);
        buf.writeLong(packet.avatarOfSunCooldown);
        buf.writeBoolean(packet.avatarOfSunActive);
        // Shu ability fields
        buf.writeLong(packet.launchCooldown);
        buf.writeLong(packet.airBoostCooldown);
        buf.writeLong(packet.windAvatarCooldown);
        buf.writeBoolean(packet.windAvatarActive);
        buf.writeLong(packet.windAvatarEndTime);
        buf.writeInt(packet.extraJumpsUsed);
        buf.writeLong(packet.extraJumpsCooldown);
        // Anubis ability fields
        buf.writeLong(packet.undeadCommandCooldown);
        buf.writeLong(packet.lifelinkCooldown);
        buf.writeBoolean(packet.lifelinkActive);
        buf.writeLong(packet.lifelinkEndTime);
        buf.writeLong(packet.summonUndeadCooldown);
        buf.writeLong(packet.avatarOfDeathCooldown);
        buf.writeBoolean(packet.avatarOfDeathActive);
        buf.writeLong(packet.avatarOfDeathEndTime);
    }

    public static SyncGodAvatarDataPacket decode(FriendlyByteBuf buf) {
        String godName = buf.readUtf();
        boolean oneWithChaosActive = buf.readBoolean();
        int oneWithChaosTimeUsed = buf.readInt();
        long oneWithChaosCooldown = buf.readLong();
        boolean damageNegationActive = buf.readBoolean();
        float storedDamage = buf.readFloat();
        long damageNegationCooldown = buf.readLong();
        boolean desertWalkerFlying = buf.readBoolean();
        long desertWalkerCooldown = buf.readLong();
        boolean chaosIncarnateActive = buf.readBoolean();
        long chaosIncarnateCooldown = buf.readLong();
        // Ra ability fields
        long solarFlareCooldown = buf.readLong();
        long purifyingLightCooldown = buf.readLong();
        boolean purifyingLightActive = buf.readBoolean();
        long purifyingLightEndTime = buf.readLong();
        long holyInfernoCooldown = buf.readLong();
        long avatarOfSunCooldown = buf.readLong();
        boolean avatarOfSunActive = buf.readBoolean();
        // Shu ability fields
        long launchCooldown = buf.readLong();
        long airBoostCooldown = buf.readLong();
        long windAvatarCooldown = buf.readLong();
        boolean windAvatarActive = buf.readBoolean();
        long windAvatarEndTime = buf.readLong();
        int extraJumpsUsed = buf.readInt();
        long extraJumpsCooldown = buf.readLong();
        // Anubis ability fields
        long undeadCommandCooldown = buf.readLong();
        long lifelinkCooldown = buf.readLong();
        boolean lifelinkActive = buf.readBoolean();
        long lifelinkEndTime = buf.readLong();
        long summonUndeadCooldown = buf.readLong();
        long avatarOfDeathCooldown = buf.readLong();
        boolean avatarOfDeathActive = buf.readBoolean();
        long avatarOfDeathEndTime = buf.readLong();
        
        try {
            GodType god = GodType.valueOf(godName);
            return new SyncGodAvatarDataPacket(god, 
                oneWithChaosActive, oneWithChaosTimeUsed, oneWithChaosCooldown,
                damageNegationActive, storedDamage, damageNegationCooldown,
                desertWalkerFlying, desertWalkerCooldown,
                chaosIncarnateActive, chaosIncarnateCooldown,
                solarFlareCooldown, purifyingLightCooldown,
                purifyingLightActive, purifyingLightEndTime,
                holyInfernoCooldown, avatarOfSunCooldown,
                avatarOfSunActive,
                launchCooldown, airBoostCooldown,
                windAvatarCooldown, windAvatarActive, windAvatarEndTime,
                extraJumpsUsed, extraJumpsCooldown,
                undeadCommandCooldown, lifelinkCooldown,
                lifelinkActive, lifelinkEndTime,
                summonUndeadCooldown, avatarOfDeathCooldown,
                avatarOfDeathActive, avatarOfDeathEndTime);
        } catch (IllegalArgumentException e) {
            // Invalid god name, return NONE
            return new SyncGodAvatarDataPacket(GodType.NONE, 
                false, 0, 0,
                false, 0, 0,
                false, 0,
                false, 0,
                0, 0,
                false, 0,
                0, 0,
                false,
                0, 0,
                0, false, 0,
                0, 0,
                0, 0,
                false, 0,
                0, 0,
                false, 0);
        }
    }

    public static void handle(SyncGodAvatarDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side handling
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
                    try {
                        GodType god = GodType.valueOf(packet.godName);
                        cap.setSelectedGod(god);
                        cap.setOneWithChaosActive(packet.oneWithChaosActive);
                        cap.setOneWithChaosTimeUsed(packet.oneWithChaosTimeUsed);
                        cap.setOneWithChaosCooldown(packet.oneWithChaosCooldown);
                        cap.setDamageNegationActive(packet.damageNegationActive);
                        cap.setStoredDamage(packet.storedDamage);
                        cap.setDamageNegationCooldown(packet.damageNegationCooldown);
                        cap.setDesertWalkerFlying(packet.desertWalkerFlying);
                        cap.setDesertWalkerCooldown(packet.desertWalkerCooldown);
                        cap.setChaosIncarnateActive(packet.chaosIncarnateActive);
                        cap.setChaosIncarnateCooldown(packet.chaosIncarnateCooldown);
                        // Ra ability fields
                        cap.setSolarFlareCooldown(packet.solarFlareCooldown);
                        cap.setPurifyingLightCooldown(packet.purifyingLightCooldown);
                        cap.setPurifyingLightActive(packet.purifyingLightActive);
                        cap.setPurifyingLightEndTime(packet.purifyingLightEndTime);
                        cap.setHolyInfernoCooldown(packet.holyInfernoCooldown);
                        cap.setAvatarOfSunCooldown(packet.avatarOfSunCooldown);
                        cap.setAvatarOfSunActive(packet.avatarOfSunActive);
                        // Shu ability fields
                        cap.setLaunchCooldown(packet.launchCooldown);
                        cap.setAirBoostCooldown(packet.airBoostCooldown);
                        cap.setWindAvatarCooldown(packet.windAvatarCooldown);
                        cap.setWindAvatarActive(packet.windAvatarActive);
                        cap.setWindAvatarEndTime(packet.windAvatarEndTime);
                        cap.setExtraJumpsUsed(packet.extraJumpsUsed);
                        cap.setExtraJumpsCooldown(packet.extraJumpsCooldown);
                        // Anubis ability fields
                        cap.setUndeadCommandCooldown(packet.undeadCommandCooldown);
                        cap.setLifelinkCooldown(packet.lifelinkCooldown);
                        cap.setLifelinkActive(packet.lifelinkActive);
                        cap.setLifelinkEndTime(packet.lifelinkEndTime);
                        cap.setSummonUndeadCooldown(packet.summonUndeadCooldown);
                        cap.setAvatarOfDeathCooldown(packet.avatarOfDeathCooldown);
                        cap.setAvatarOfDeathActive(packet.avatarOfDeathActive);
                        cap.setAvatarOfDeathEndTime(packet.avatarOfDeathEndTime);
                    } catch (IllegalArgumentException e) {
                        // Invalid god name, ignore
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
