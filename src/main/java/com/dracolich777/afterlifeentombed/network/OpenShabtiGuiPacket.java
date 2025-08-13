// package com.dracolich777.afterlifeentombed.network;

// import com.dracolich777.afterlifeentombed.client.gui.ShabtiConfigScreen;
// import com.dracolich777.afterlifeentombed.mobs.ShabtiEntity;
// import net.minecraft.client.Minecraft;
// import net.minecraft.network.FriendlyByteBuf;
// import net.minecraft.world.entity.Entity;
// import net.minecraftforge.network.NetworkEvent;

// import java.util.function.Supplier;

// public class OpenShabtiGuiPacket {
//     private final int entityId;
    
//     public OpenShabtiGuiPacket(int entityId) {
//         this.entityId = entityId;
//     }
    
//     public static void encode(OpenShabtiGuiPacket packet, FriendlyByteBuf buffer) {
//         buffer.writeInt(packet.entityId);
//     }
    
//     public static OpenShabtiGuiPacket decode(FriendlyByteBuf buffer) {
//         return new OpenShabtiGuiPacket(buffer.readInt());
//     }
    
//     public static void handle(OpenShabtiGuiPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
//         NetworkEvent.Context context = contextSupplier.get();
//         context.enqueueWork(() -> {
//             // Client-side handling
//             Minecraft minecraft = Minecraft.getInstance();
//             if (minecraft.level != null) {
//                 Entity entity = minecraft.level.getEntity(packet.entityId);
//                 if (entity instanceof ShabtiEntity shabtiEntity) {
//                     minecraft.setScreen(new ShabtiConfigScreen(shabtiEntity));
//                 }
//             }
//         });
//         context.setPacketHandled(true);
//     }
// }
