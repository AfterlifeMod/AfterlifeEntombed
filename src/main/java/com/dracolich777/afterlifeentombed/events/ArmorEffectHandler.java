package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.items.PricklyPearArmor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "afterlifeentombed")
public class ArmorEffectHandler {
    
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Check each armor piece
            for (ItemStack armorStack : player.getArmorSlots()) {
                if (armorStack.getItem() instanceof PricklyPearArmor armor) {
                    armor.onArmorTick(armorStack, player.level(), player);
                }
            }
        }
    }
}