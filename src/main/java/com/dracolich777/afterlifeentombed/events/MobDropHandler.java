
// package com.dracolich777.afterlifeentombed.events;

// import com.dracolich777.afterlifeentombed.init.ModItems;
// import net.minecraft.world.entity.item.ItemEntity;
// import net.minecraft.world.entity.monster.Husk;
// import net.minecraft.world.entity.monster.WitherSkeleton;
// import net.minecraft.world.entity.monster.MagmaCube;
// import net.minecraft.world.entity.monster.Vex;
// import net.minecraft.world.entity.animal.Parrot;
// import net.minecraft.world.item.ItemStack;
// import net.minecraftforge.event.entity.living.LivingDeathEvent;
// import net.minecraftforge.eventbus.api.SubscribeEvent;
// import net.minecraftforge.fml.common.Mod;

// @Mod.EventBusSubscriber
// public class MobDropHandler {
    
//     private static final double RA_DROP_CHANCE = 0.5; // 5% chance
//     private static final double HORUS_DROP_CHANCE = 0.5;
//     private static final double THOTH_DROP_CHANCE = 0.3;
//     private static final double GEB_DROP_CHANCE = 0.5;  // 3% chance

//     @SubscribeEvent
//     public static void onLivingDeath(LivingDeathEvent event) {
//         if (event.getEntity().level().isClientSide()) {
//             return;
//         }

//         boolean shouldDrop = false;
        
//         // Check if the entity is a Husk or Wither Skeleton
//         if (event.getEntity() instanceof Husk || event.getEntity() instanceof WitherSkeleton) {
//             shouldDrop = true;
//         }

//         if (shouldDrop && Math.random() < RA_DROP_CHANCE) {
//             ItemStack ragodstone = new ItemStack(ModItems.GODSTONE_OF_RA.get());
//             ItemEntity itemEntity = new ItemEntity(
//                 event.getEntity().level(), 
//                 event.getEntity().getX(), 
//                 event.getEntity().getY(), 
//                 event.getEntity().getZ(), 
//                 ragodstone
//             );
//             event.getEntity().level().addFreshEntity(itemEntity);
//         }

//         if (event.getEntity() instanceof Parrot) {
//             shouldDrop = true;
//         }

//         if (shouldDrop && Math.random() < HORUS_DROP_CHANCE) {
//             ItemStack horusgodstone = new ItemStack(ModItems.GODSTONE_OF_HORUS.get());
//             ItemEntity itemEntity = new ItemEntity(
//                 event.getEntity().level(), 
//                 event.getEntity().getX(), 
//                 event.getEntity().getY(), 
//                 event.getEntity().getZ(), 
//                 horusgodstone
//             );
//             event.getEntity().level().addFreshEntity(itemEntity);
//         }
//         if (event.getEntity() instanceof Vex) {
//             shouldDrop = true;
//         }

//         if (shouldDrop && Math.random() < THOTH_DROP_CHANCE) {
//             ItemStack thothgodstone = new ItemStack(ModItems.GODSTONE_OF_THOTH.get());
//             ItemEntity itemEntity = new ItemEntity(
//                 event.getEntity().level(), 
//                 event.getEntity().getX(), 
//                 event.getEntity().getY(), 
//                 event.getEntity().getZ(), 
//                 thothgodstone
//             );
//             event.getEntity().level().addFreshEntity(itemEntity);
//     }
//     if (event.getEntity() instanceof MagmaCube) {
//             shouldDrop = true;
//         }

//         if (shouldDrop && Math.random() < GEB_DROP_CHANCE) {
//             ItemStack gebgodstone = new ItemStack(ModItems.GODSTONE_OF_GEB.get());
//             ItemEntity itemEntity = new ItemEntity(
//                 event.getEntity().level(), 
//                 event.getEntity().getX(), 
//                 event.getEntity().getY(), 
//                 event.getEntity().getZ(), 
//                 gebgodstone
//             );
//             event.getEntity().level().addFreshEntity(itemEntity);
// }
// }
// }