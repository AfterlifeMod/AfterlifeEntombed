package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class GodAvatarPackets {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AfterlifeEntombedMod.MOD_ID, "god_avatar"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.registerMessage(id(), SyncGodAvatarPacket.class,
                SyncGodAvatarPacket::encode,
                SyncGodAvatarPacket::decode,
                SyncGodAvatarPacket::handle);
        
        INSTANCE.registerMessage(id(), SyncGodAvatarDataPacket.class,
                SyncGodAvatarDataPacket::encode,
                SyncGodAvatarDataPacket::decode,
                SyncGodAvatarDataPacket::handle);
        
        INSTANCE.registerMessage(id(), ActivateAbilityPacket.class,
                ActivateAbilityPacket::encode,
                ActivateAbilityPacket::decode,
                ActivateAbilityPacket::handle);
        
        INSTANCE.registerMessage(id(), HudNotificationPacket.class,
                HudNotificationPacket::encode,
                HudNotificationPacket::new,
                HudNotificationPacket::handle);
    }
}
