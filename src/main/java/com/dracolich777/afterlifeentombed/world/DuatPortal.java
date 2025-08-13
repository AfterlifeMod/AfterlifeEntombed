// package com.dracolich777.afterlifeentombed.world;

// import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
// import net.minecraft.core.BlockPos;
// import net.minecraft.server.level.ServerLevel;
// import net.minecraft.server.level.ServerPlayer;
// import net.minecraft.world.item.Items;
// import net.minecraft.world.level.Level;
// import net.minecraft.world.level.block.Blocks;
// import net.minecraft.world.level.block.state.BlockState;
// import net.minecraftforge.event.TickEvent;
// import net.minecraftforge.eventbus.api.SubscribeEvent;
// import net.minecraftforge.common.MinecraftForge;

// public class DuatPortal {

//     public static void register() {
//         MinecraftForge.EVENT_BUS.register(DuatPortal.class);
//     }

//     @SubscribeEvent
//     public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
//         if (event.player.level().isClientSide || !(event.player instanceof ServerPlayer player))
//             return;

//         if (player.getMainHandItem().is(Items.FLINT_AND_STEEL)) {
//             BlockPos pos = player.blockPosition();
//             if (validatePortalFrame(player.level(), pos)) {
//                 player.setPortalCooldown();
//                 ServerLevel targetWorld = player.server.getLevel(DuatDimension.DUAT_DIMENSION);
//                 if (targetWorld != null)
//                     player.changeDimension(targetWorld);
//             }
//         }
//     }

//     private static boolean validatePortalFrame(Level level, BlockPos pos) {
//         // Simplified frame detection logic (you should refine this):
//         for (int dx = -2; dx <= 2; dx++) {
//             for (int dy = 0; dy <= 6; dy++) {
//                 BlockState state = level.getBlockState(pos.offset(dx, dy, 0));
//                 if (dy == 0 || dy == 6 || dx == -2 || dx == 2) {
//                     if (Math.abs(dx) == 2 && (dy == 0 || dy == 6)) {
//                         if (state.getBlock() != Blocks.CHISELED_SANDSTONE)
//                             return false;
//                     } else if (dy == 3 && state.getBlock() != Blocks.GOLD_BLOCK) {
//                         return false;
//                     } else if (state.getBlock() != Blocks.CUT_SANDSTONE && state.getBlock() != Blocks.GOLD_BLOCK) {
//                         return false;
//                     }
//                 }
//             }
//         }
//         return true;
//     }
// }
