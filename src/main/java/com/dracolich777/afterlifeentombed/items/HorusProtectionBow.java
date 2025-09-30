package com.dracolich777.afterlifeentombed.items;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class HorusProtectionBow extends BowItem {

    public HorusProtectionBow(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        boolean hasArrows = !player.getProjectile(itemstack).isEmpty();

        InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, level, player, hand, hasArrows);
        if (ret != null) return ret;

        if (!hasArrows && !player.getAbilities().instabuild) {
            return InteractionResultHolder.fail(itemstack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player) {
            boolean hasInfiniteArrows = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
            ItemStack arrowStack = player.getProjectile(stack);

            int useDuration = this.getUseDuration(stack) - timeLeft;
            useDuration = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, level, player, useDuration, !arrowStack.isEmpty() || hasInfiniteArrows);
            if (useDuration < 0) return;

            if (!arrowStack.isEmpty() || hasInfiniteArrows) {
                if (arrowStack.isEmpty()) {
                    arrowStack = new ItemStack(Items.ARROW);
                }

                float velocity = getPowerForTime(useDuration);
                if (!((double)velocity < 0.1D)) {
                    boolean shouldConsumeArrow = !hasInfiniteArrows && !player.getAbilities().instabuild && !(arrowStack.getItem() instanceof ArrowItem && ((ArrowItem)arrowStack.getItem()).isInfinite(arrowStack, stack, player));
                    if (!level.isClientSide) {
                        ArrowItem arrowitem = (ArrowItem)(arrowStack.getItem() instanceof ArrowItem ? arrowStack.getItem() : Items.ARROW);
                        
                        // Create regular arrow using the arrow item's createArrow method
                        AbstractArrow arrow = arrowitem.createArrow(level, arrowStack, player);
                        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity * 3.0F, 1.0F);
                        
                        // Disable gravity only for full charge arrows (velocity == 1.0F)
                        if (velocity == 1.0F) {
                            arrow.setCritArrow(true);
                            arrow.setNoGravity(true); // Disable gravity for full charge arrows
                        }

                        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                        if (powerLevel > 0) {
                            arrow.setBaseDamage(arrow.getBaseDamage() + (double)powerLevel * 0.5D + 0.5D);
                        }

                        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                        if (punchLevel > 0) {
                            arrow.setKnockback(punchLevel);
                        }

                        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                            arrow.setSecondsOnFire(100);
                        }

                        // Bow doesn't take damage (infinite durability)
                        // stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));

                        if (shouldConsumeArrow || player.getAbilities().instabuild && (arrowStack.getItem() == Items.SPECTRAL_ARROW || arrowStack.getItem() == Items.TIPPED_ARROW)) {
                            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        }

                        level.addFreshEntity(arrow);
                    }

                    level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
                    if (shouldConsumeArrow && !player.getAbilities().instabuild) {
                        arrowStack.shrink(1);
                        if (arrowStack.isEmpty()) {
                            player.getInventory().removeItem(arrowStack);
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    public static float getPowerForTime(int charge) {
        float f = (float)charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false; // Cannot be repaired since it has infinite durability
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false; // Infinite durability - never takes damage
    }
}
