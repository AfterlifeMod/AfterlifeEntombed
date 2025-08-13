// package com.dracolich777.afterlifeentombed.network;

// import com.dracolich777.afterlifeentombed.mobs.ShabtiEntity;
// import net.minecraft.network.FriendlyByteBuf;
// import net.minecraft.server.level.ServerPlayer;
// import net.minecraft.world.entity.Entity;
// import net.minecraftforge.network.NetworkEvent;

// import java.util.function.Supplier;

// public class ShabtiConfigPacket {
//     private final int entityId;
//     private final int displayFlags;
    
//     public ShabtiConfigPacket(int entityId, int displayFlags) {
//         this.entityId = entityId;
//         this.displayFlags = displayFlags;
//     }
    
//     public static void encode(ShabtiConfigPacket packet, FriendlyByteBuf buffer) {
//         buffer.writeInt(packet.entityId);
//         buffer.writeInt(packet.displayFlags);
//     }
    
//     public static ShabtiConfigPacket decode(FriendlyByteBuf buffer) {
//         return new ShabtiConfigPacket(buffer.readInt(), buffer.readInt());
//     }
    
//     public static void handle(ShabtiConfigPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
//         NetworkEvent.Context context = contextSupplier.get();
//         context.enqueueWork(() -> {
//             ServerPlayer player = context.getSender();
//             if (player != null) {
//                 Entity entity = player.level().getEntity(packet.entityId);
//                 if (entity instanceof ShabtiEntity shabtiEntity) {
//                     // Verify the player is the owner
//                     if (shabtiEntity.getOwnerUUID() != null && shabtiEntity.getOwnerUUID().equals(player.getUUID())) {
//                         // Update the display flags
//                         shabtiEntity.setDisplayFlag(ShabtiEntity.SHOW_ARMOR, (packet.displayFlags & ShabtiEntity.SHOW_ARMOR) != 0);
//                         shabtiEntity.setDisplayFlag(ShabtiEntity.SHOW_ITEMS, (packet.displayFlags & ShabtiEntity.SHOW_ITEMS) != 0);
//                         shabtiEntity.setDisplayFlag(ShabtiEntity.SHOW_ROTATION, (packet.displayFlags & ShabtiEntity.SHOW_ROTATION) != 0);
//                         shabtiEntity.setDisplayFlag(ShabtiEntity.SHOW_HEALTH, (packet.displayFlags & ShabtiEntity.SHOW_HEALTH) != 0);
//                         shabtiEntity.setDisplayFlag(ShabtiEntity.SHOW_HUNGER, (packet.displayFlags & ShabtiEntity.SHOW_HUNGER) != 0);
//                         shabtiEntity.setDisplayFlag(ShabtiEntity.SHOW_XP, (packet.displayFlags & ShabtiEntity.SHOW_XP) != 0);
//                         shabtiEntity.setDisplayFlag(ShabtiEntity.SHOW_COORDS, (packet.displayFlags & ShabtiEntity.SHOW_COORDS) != 0);
//                         shabtiEntity.setDisplayFlag(ShabtiEntity.SHOW_DIMENSION, (packet.displayFlags & ShabtiEntity.SHOW_DIMENSION) != 0);
//                     }
//                 }
//             }
//         });
//         context.setPacketHandled(true);
//     }
// }
