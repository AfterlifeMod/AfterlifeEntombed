package com.dracolich777.afterlifeentombed.items;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

public class HammerOfGeb extends Item implements ICurioItem {

    private static final float GROUND_POUND_RADIUS = 7.0F; // Radius for hitting entities
    private static final float MIN_GROUND_POUND_DAMAGE = 3.0F; // Minimum damage for ground pound

    public HammerOfGeb(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity != null && !entity.level().isClientSide) {
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, false, false, false));
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        // No specific equip logic needed beyond what Curios handles by default
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity != null && !entity.level().isClientSide) {
            entity.removeEffect(MobEffects.DAMAGE_BOOST);
        }
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            if (CuriosApi.getCuriosHelper().findFirstCurio(player, ModItems.HAMMER_OF_GEB.get()).isPresent()) {
                float fallDistance = event.getDistance();

                if (fallDistance > 3.0F) { // Only activate if fall distance is greater than 3 blocks
                    float excessFallDistance = fallDistance - 1.0F;
                    // Calculate damage, ensuring a minimum and avoiding negative values
                    // Damage scales with excess fall distance, with a minimum value
                    float additionalDamage = Math.max(MIN_GROUND_POUND_DAMAGE, excessFallDistance * 1.0F);

                    boolean hitSomeone = false;
                    AABB impactArea = player.getBoundingBox().inflate(GROUND_POUND_RADIUS);

                    List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, impactArea);

                    for (LivingEntity target : entities) {
                        if (target != player && target.isAlive() && target.getType() != EntityType.ITEM && target.getType() != EntityType.EXPERIENCE_ORB) {
                            // Use a damage source that's considered a direct attack from the player
                            if (target.hurt(player.damageSources().mobAttack(player), additionalDamage)) {
                                hitSomeone = true;
                            }
                        }
                    }
                    if (hitSomeone) {
                        event.setCanceled(true);
                    } else {

                    }
                }
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }
    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(Component.translatable("item.afterlifeentombed.hammer_of_geb.tooltip.line1")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.afterlifeentombed.hammer_of_geb.tooltip.line2")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.afterlifeentombed.hammer_of_geb.tooltip.line3")
                .withStyle(ChatFormatting.AQUA));
    }
}