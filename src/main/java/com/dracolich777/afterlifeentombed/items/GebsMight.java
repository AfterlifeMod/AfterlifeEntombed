package com.dracolich777.afterlifeentombed.items;

import java.util.List;

import javax.annotation.Nullable;

import com.dracolich777.afterlifeentombed.init.ModEffects;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GebsMight extends SwordItem {

    private static final String COOLDOWN_KEY = "geb_might_cooldown";
    private static final int COOLDOWN_TICKS = 100; // 5 seconds (20 ticks per second)
    private static final double SHOCKWAVE_RADIUS = 8.0;
    private static final double KNOCKBACK_STRENGTH = 2.0;

    public GebsMight(Item.Properties properties) {
        super(Tiers.NETHERITE, 15 - 1, -2.4F, properties); // 8 damage
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(ModEffects.REVENGE_OF_GEB.get(), 20, 0));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Check cooldown
        if (itemStack.getTag() != null && itemStack.getTag().contains(COOLDOWN_KEY)) {
            long lastUse = itemStack.getTag().getLong(COOLDOWN_KEY);
            long currentTime = level.getGameTime();
            if (currentTime - lastUse < COOLDOWN_TICKS) {
                // Still on cooldown
                return InteractionResultHolder.pass(itemStack);
            }
        }

        if (!level.isClientSide) {
            performEarthShockwave(level, player);

            // Set cooldown
            if (itemStack.getTag() == null) {
                itemStack.getOrCreateTag();
            }
            itemStack.getTag().putLong(COOLDOWN_KEY, level.getGameTime());
            player.getCooldowns().addCooldown(this, 100);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    private void performEarthShockwave(Level level, Player player) {
        Vec3 playerPos = player.position();
        BlockPos centerPos = player.blockPosition();

        // Play sound effect
        level.playSound(null, centerPos, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.5F);

        // Create undulating shockwave effect with immediate execution for all waves
        if (level instanceof ServerLevel serverLevel) {
            for (int wave = 0; wave < 3; wave++) {
                double waveRadius = (wave + 1) * 3.0;

                // Create particle effects in a circle
                for (double angle = 0; angle < 2 * Math.PI; angle += 0.1) {
                    for (double r = Math.max(1, waveRadius - 1); r <= waveRadius; r += 0.5) {
                        double x = playerPos.x + r * Math.cos(angle);
                        double z = playerPos.z + r * Math.sin(angle);

                        BlockPos particlePos = new BlockPos((int) x, centerPos.getY() - 1, (int) z);
                        BlockState blockState = level.getBlockState(particlePos);

                        if (!blockState.isAir()) {
                            // Create block particles rising from the ground
                            // Add slight delay based on wave and distance for undulating effect
                            double particleDelay = wave * 0.1 + (r / waveRadius) * 0.2;

                            serverLevel.sendParticles(
                                    new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                                    x, centerPos.getY() + 0.1 + (wave * 0.2), z,
                                    3, 0.2, 0.3 + particleDelay, 0.2, 0.1
                            );
                        }
                    }
                }
            }
        }

        // Find and knockback nearby entities
        AABB searchArea = new AABB(playerPos.subtract(SHOCKWAVE_RADIUS, 3, SHOCKWAVE_RADIUS),
                playerPos.add(SHOCKWAVE_RADIUS, 3, SHOCKWAVE_RADIUS));

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, searchArea);

        for (LivingEntity entity : nearbyEntities) {
            if (entity == player) {
                continue; // Don't knockback the player
            }
            Vec3 entityPos = entity.position();
            double distance = entityPos.distanceTo(playerPos);

            if (distance <= SHOCKWAVE_RADIUS && distance > 0) {
                // Calculate knockback direction (away from player)
                Vec3 knockbackDir = entityPos.subtract(playerPos).normalize();

                // Apply knockback with diminishing strength based on distance
                double knockbackForce = KNOCKBACK_STRENGTH * (3.0 - (distance / SHOCKWAVE_RADIUS));
                Vec3 knockbackVelocity = knockbackDir.scale(knockbackForce);

                // Add upward component
                knockbackVelocity = knockbackVelocity.add(0, 0.3, 0);

                entity.setDeltaMovement(entity.getDeltaMovement().add(knockbackVelocity));
                entity.hurtMarked = true;
            }
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.afterlifeentombed.gebs_might.tooltip1")
                .withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("tooltip.afterlifeentombed.gebs_might.tooltip2")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.afterlifeentombed.gebs_might.tooltip3")
                .withStyle(ChatFormatting.DARK_GRAY));

        // Show cooldown if applicable
        if (stack.getTag() != null && stack.getTag().contains(COOLDOWN_KEY) && level != null) {
            long lastUse = stack.getTag().getLong(COOLDOWN_KEY);
            long currentTime = level.getGameTime();
            long remainingCooldown = COOLDOWN_TICKS - (currentTime - lastUse);

            if (remainingCooldown > 0) {
                float seconds = remainingCooldown / 20.0f;
                tooltip.add(Component.literal(String.format("Cooldown: %.1fs", seconds))
                        .withStyle(ChatFormatting.RED));
            } else {
                tooltip.add(Component.literal("Earth Shockwave Ready")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
    }
}
