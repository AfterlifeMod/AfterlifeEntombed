// package com.dracolich777.afterlifeentombed.enchantments;

// import net.minecraft.world.entity.EquipmentSlot;
// import net.minecraft.world.entity.LivingEntity;
// import net.minecraft.world.item.ItemStack;
// import net.minecraft.world.item.enchantment.Enchantment;
// import net.minecraft.world.item.enchantment.EnchantmentCategory;
// import net.minecraft.world.phys.Vec3;
// import net.minecraftforge.event.entity.living.LivingAttackEvent;
// import net.minecraftforge.eventbus.api.SubscribeEvent;
// import net.minecraftforge.fml.common.Mod;
// import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;

// @Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID)
// public class Skybound extends Enchantment {
    
//     public Skybound() {
//         super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
//     }
    
//     @Override
//     public int getMinCost(int level) {
//         return 10 + (level - 1) * 8;
//     }
    
//     @Override
//     public int getMaxCost(int level) {
//         return getMinCost(level) + 15;
//     }
    
//     @Override
//     public int getMaxLevel() {
//         return 4;
//     }
    
//     @Override
//     public boolean canEnchant(ItemStack stack) {
//         return EnchantmentCategory.WEAPON.canEnchant(stack.getItem());
//     }
    
//     @SubscribeEvent
//     public static void onLivingAttack(LivingAttackEvent event) {
//         if (event.getSource().getEntity() instanceof LivingEntity attacker) {
//             ItemStack weapon = attacker.getMainHandItem();
            
//             if (!weapon.isEmpty()) {
//                 int enchantLevel = weapon.getEnchantmentLevel(ModEnchantments.SKYBOUND.get());
                
//                 if (enchantLevel > 0) {
//                     // Calculate upward velocity (1 + enchant level blocks)
//                     double upwardVelocity = 0.42 * (1 + enchantLevel); // 0.42 is approximately 1 block of upward motion
                    
//                     // Apply upward velocity to the attacker
//                     Vec3 currentMotion = attacker.getDeltaMovement();
//                     attacker.setDeltaMovement(currentMotion.x, upwardVelocity, currentMotion.z);
                    
//                     // Reset fall distance to prevent fall damage
//                     attacker.fallDistance = 0;
//                 }
//             }
//         }
//     }
// }