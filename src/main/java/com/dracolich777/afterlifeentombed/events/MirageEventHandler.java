// package com.dracolich777.afterlifeentombed.events;

// import com.dracolich777.afterlifeentombed.effects.MirageEffect;
// import com.dracolich777.afterlifeentombed.init.ModEffects; // Your mod's effects registry
// import net.minecraft.world.entity.player.Player;
// import net.minecraftforge.event.entity.living.MobEffectEvent;
// import net.minecraftforge.event.entity.player.PlayerEvent;
// import net.minecraftforge.eventbus.api.SubscribeEvent;
// import net.minecraftforge.fml.common.Mod;

// @Mod.EventBusSubscriber(modid = "afterlifeentombed") // Replace with your mod ID
// public class MirageEventHandler {
    
//     @SubscribeEvent
//     public static void onEffectRemoved(MobEffectEvent.Remove event) {
//         if (event.getEntity() instanceof Player player) {
//             // Check if the mirage effect is being removed
//             if (event.getEffect() == ModEffects.MIRAGE.get()) { // Replace with your effect registry
//                 MirageEffect.removeAllMirages(player);
//             }
//         }
//     }
    
//     @SubscribeEvent
//     public static void onEffectExpired(MobEffectEvent.Expired event) {
//         if (event.getEntity() instanceof Player player) {
//             // Check if the mirage effect has expired
//             if (event.getEffectInstance().getEffect() == ModEffects.MIRAGE.get()) { // Replace with your effect registry
//                 MirageEffect.removeAllMirages(player);
//             }
//         }
//     }
    
//     @SubscribeEvent
//     public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
//         Player player = event.getEntity();
//         // Clean up mirage data when player logs out
//         MirageEffect.cleanupPlayer(player);
//     }
    
//     @SubscribeEvent
//     public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
//         Player player = event.getEntity();
//         // Clean up any residual mirage data when player logs in
//         MirageEffect.cleanupPlayer(player);
//     }
// }