// package com.dracolich777.afterlifeentombed.network;

// import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
// import net.minecraft.resources.ResourceLocation;
// import net.minecraftforge.network.NetworkDirection;
// import net.minecraftforge.network.NetworkRegistry;
// import net.minecraftforge.network.simple.SimpleChannel;

// public class NetworkHandler {
//     private static final String PROTOCOL_VERSION = "1";
    
//     public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
//         new ResourceLocation(AfterlifeEntombedMod.MOD_ID, "main"),
//         () -> PROTOCOL_VERSION,
//         PROTOCOL_VERSION::equals,
//         PROTOCOL_VERSION::equals
//     );
    
//     private static int packetId = 0;
    
//     public static void register() {
//         INSTANCE.messageBuilder(ShabtiConfigPacket.class, packetId++)
//             .encoder(ShabtiConfigPacket::encode)
//             .decoder(ShabtiConfigPacket::decode)
//             .consumerMainThread(ShabtiConfigPacket::handle)
//             .add();
            
//         INSTANCE.messageBuilder(OpenShabtiGuiPacket.class, packetId++)
//             .encoder(OpenShabtiGuiPacket::encode)
//             .decoder(OpenShabtiGuiPacket::decode)
//             .consumerMainThread(OpenShabtiGuiPacket::handle)
//             .add();
//     }
// }
