package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
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
        
        INSTANCE.registerMessage(id(), SwitchGodPacket.class,
                SwitchGodPacket::encode,
                SwitchGodPacket::new,
                SwitchGodPacket::handle);
        
        INSTANCE.registerMessage(id(), SyncUnlockedGodsPacket.class,
                SyncUnlockedGodsPacket::encode,
                SyncUnlockedGodsPacket::decode,
                SyncUnlockedGodsPacket::handle);
        
        INSTANCE.registerMessage(id(), SyncOreMarkersPacket.class,
                SyncOreMarkersPacket::encode,
                SyncOreMarkersPacket::decode,
                SyncOreMarkersPacket::handle);
        
        // Boon system packets
        INSTANCE.registerMessage(id(), OpenBoonSelectionPacket.class,
                OpenBoonSelectionPacket::encode,
                OpenBoonSelectionPacket::decode,
                OpenBoonSelectionPacket::handle);
        
        INSTANCE.registerMessage(id(), SelectBoonPacket.class,
                SelectBoonPacket::encode,
                SelectBoonPacket::decode,
                SelectBoonPacket::handle);
        
        INSTANCE.registerMessage(id(), SyncPlayerBoonsPacket.class,
                SyncPlayerBoonsPacket::encode,
                SyncPlayerBoonsPacket::decode,
                SyncPlayerBoonsPacket::handle);
    }
    
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
    
    public static <MSG> void sendToPlayer(net.minecraft.server.level.ServerPlayer player, MSG message) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
