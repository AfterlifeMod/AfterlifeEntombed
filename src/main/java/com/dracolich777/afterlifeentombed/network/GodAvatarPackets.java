package com.dracolich777.afterlifeentombed.network;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;

import net.minecraft.resources.ResourceLocation;
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
        INSTANCE.messageBuilder(SyncGodAvatarPacket.class, id())
                .encoder(SyncGodAvatarPacket::encode)
                .decoder(SyncGodAvatarPacket::decode)
                .consumerMainThread(SyncGodAvatarPacket::handle)
                .add();
        
        INSTANCE.messageBuilder(SyncGodAvatarDataPacket.class, id())
                .encoder(SyncGodAvatarDataPacket::encode)
                .decoder(SyncGodAvatarDataPacket::decode)
                .consumerMainThread(SyncGodAvatarDataPacket::handle)
                .add();
        
        INSTANCE.messageBuilder(ActivateAbilityPacket.class, id())
                .encoder(ActivateAbilityPacket::encode)
                .decoder(ActivateAbilityPacket::decode)
                .consumerMainThread(ActivateAbilityPacket::handle)
                .add();
        
        INSTANCE.messageBuilder(HudNotificationPacket.class, id())
                .encoder(HudNotificationPacket::encode)
                .decoder(HudNotificationPacket::new)
                .consumerMainThread(HudNotificationPacket::handle)
                .add();
        
        INSTANCE.messageBuilder(SwitchGodPacket.class, id())
                .encoder(SwitchGodPacket::encode)
                .decoder(SwitchGodPacket::new)
                .consumerMainThread(SwitchGodPacket::handle)
                .add();
        
        INSTANCE.messageBuilder(SyncUnlockedGodsPacket.class, id())
                .encoder(SyncUnlockedGodsPacket::encode)
                .decoder(SyncUnlockedGodsPacket::decode)
                .consumerMainThread(SyncUnlockedGodsPacket::handle)
                .add();
        
        INSTANCE.messageBuilder(SyncOreMarkersPacket.class, id())
                .encoder(SyncOreMarkersPacket::encode)
                .decoder(SyncOreMarkersPacket::decode)
                .consumerMainThread(SyncOreMarkersPacket::handle)
                .add();
        
        // Boon system packets
        INSTANCE.messageBuilder(OpenBoonSelectionPacket.class, id())
                .encoder(OpenBoonSelectionPacket::encode)
                .decoder(OpenBoonSelectionPacket::decode)
                .consumerMainThread(OpenBoonSelectionPacket::handle)
                .add();
        
        INSTANCE.messageBuilder(SelectBoonPacket.class, id())
                .encoder(SelectBoonPacket::encode)
                .decoder(SelectBoonPacket::decode)
                .consumerMainThread(SelectBoonPacket::handle)
                .add();
        
        INSTANCE.messageBuilder(SyncPlayerBoonsPacket.class, id())
                .encoder(SyncPlayerBoonsPacket::encode)
                .decoder(SyncPlayerBoonsPacket::decode)
                .consumerMainThread(SyncPlayerBoonsPacket::handle)
                .add();
    }
    
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
    
    public static <MSG> void sendToPlayer(net.minecraft.server.level.ServerPlayer player, MSG message) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
