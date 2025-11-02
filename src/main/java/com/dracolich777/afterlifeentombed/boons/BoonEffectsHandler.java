package com.dracolich777.afterlifeentombed.boons;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.capabilities.PlayerBoonsCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles the application and effects of divine boons and curses.
 * Implements unique mechanics based on the butler instructions.
 */
@Mod.EventBusSubscriber
public class BoonEffectsHandler {

    /**
     * Called every tick for players with active boons
     */
    public static void tickBoonEffects(Player player) {
        if (player.level().isClientSide) return;

        player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(cap -> {
            Level level = player.level();
            long dayTime = level.getDayTime() % 24000;
            boolean isDay = dayTime >= 0 && dayTime < 12000;
            boolean isNight = !isDay;
            int lightLevel = level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, player.blockPosition());
            boolean isDark = lightLevel <= 7;

            // Debug logging every 5 seconds
            if (player.tickCount % 100 == 0 && !cap.getActiveBoons().isEmpty()) {
                com.dracolich777.afterlifeentombed.AfterlifeEntombedMod.LOGGER.info(
                    "Player {} has {} active boons/curses. Day: {}, Night: {}, Dark: {}, Light: {}", 
                    player.getName().getString(),
                    cap.getActiveBoons().size(),
                    isDay, isNight, isDark, lightLevel
                );
            }

            for (ActiveBoon boon : cap.getActiveBoons()) {
                applyBoonEffect(player, boon.getType(), isDay, isNight, isDark, lightLevel);
            }
        });
    }

    private static void applyBoonEffect(Player player, BoonType boon, boolean isDay, boolean isNight, boolean isDark, int lightLevel) {
        // ===== RA BOONS (Fire/Light/Daytime themed) =====
        
        // RA_FIRE_IMMUNITY - Fire immunity and lava walking
        if (boon == BoonType.RA_FIRE_IMMUNITY) {
            player.clearFire();
            if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false));
            }
            applyLavaWalking(player);
        }
        
        // RA_LIGHT_BEARER - Emit light (grant night vision as proxy)
        if (boon == BoonType.RA_LIGHT_BEARER) {
            if (!player.hasEffect(MobEffects.NIGHT_VISION)) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
            }
        }
        
        // RA_SOLAR_FLIGHT - Creative flight during day  
        if (boon == BoonType.RA_SOLAR_FLIGHT && isDay) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        } else if (boon == BoonType.RA_SOLAR_FLIGHT && !isDay) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
        
        // RA_SOLAR_EFFICIENCY - Daytime instant build/mine
        if (boon == BoonType.RA_SOLAR_EFFICIENCY && isDay) {
            if (!player.hasEffect(MobEffects.DIG_SPEED)) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 255, false, false));
            }
        }

        // RA_SUNS_SCORN - Sunlight damage curse
        if (boon == BoonType.RA_SUNS_SCORN && isDay && player.level().canSeeSky(player.blockPosition())) {
            if (player.tickCount % 40 == 0) { // Every 2 seconds
                player.hurt(player.damageSources().magic(), 1.0F);
            }
        }
        
        // RA_DESERT_THIRST - Water evaporation curse
        if (boon == BoonType.RA_DESERT_THIRST) {
            evaporateWaterNearby(player);
        }
        
        // RA_SOLAR_PARALYSIS - Cannot jump/sprint/crouch during day
        if (boon == BoonType.RA_SOLAR_PARALYSIS && isDay) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, -10, false, false));
            player.setSprinting(false);
        }
        
        // RA_SOLAR_PRESENCE - Fire aura (5 block radius)
        if (boon == BoonType.RA_SOLAR_PRESENCE) {
            applyFireAura(player);
        }
        
        // RA_LAVA_MAGNETISM - Pull player towards lava
        if (boon == BoonType.RA_LAVA_MAGNETISM) {
            if (player.tickCount % 100 == 0) {
                com.dracolich777.afterlifeentombed.AfterlifeEntombedMod.LOGGER.info("RA_LAVA_MAGNETISM is active, calling pullTowardsLava()");
            }
            pullTowardsLava(player);
        }

        // ===== SETH BOONS (Chaos/Darkness/Wither themed) =====
        
        // SETH_CHAOS_WARD - Explosion immunity (handled in damage event)
        
        // SETH_NIGHT_FLIGHT - Creative flight during night/darkness
        if (boon == BoonType.SETH_NIGHT_FLIGHT && (isNight || isDark)) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        } else if (boon == BoonType.SETH_NIGHT_FLIGHT && !isNight && !isDark) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
        
        // SETH_SHADOW_EFFICIENCY - Darkness instant build/mine
        if (boon == BoonType.SETH_SHADOW_EFFICIENCY && (isNight || isDark)) {
            if (!player.hasEffect(MobEffects.DIG_SPEED)) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 255, false, false));
            }
        }
        
        // SETH_AFFLICTION_IMMUNITY - Effect immunities
        if (boon == BoonType.SETH_AFFLICTION_IMMUNITY) {
            removeNegativeEffects(player, MobEffects.WITHER, MobEffects.POISON, MobEffects.BLINDNESS, 
                                 MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.HUNGER,
                                 MobEffects.CONFUSION, MobEffects.DIG_SLOWDOWN);
        }
        
        // SETH_DARKNESS_AURA - Emit darkness/suppress light
        if (boon == BoonType.SETH_DARKNESS_AURA) {
            applyDarknessAura(player);
        }
        
        // SETH_DECAY_PRESENCE - Wither aura (5 block radius)
        if (boon == BoonType.SETH_DECAY_PRESENCE) {
            applyWitherAura(player);
        }

        // SETH_DARKNESS_BANE - Darkness damage curse
        if (boon == BoonType.SETH_DARKNESS_BANE && (isNight || isDark)) {
            if (player.tickCount % 40 == 0) { // Every 2 seconds
                player.hurt(player.damageSources().magic(), 1.0F);
            }
        }
        
        // SETH_SHADOW_PARALYSIS - Cannot jump/sprint/crouch during night/darkness
        if (boon == BoonType.SETH_SHADOW_PARALYSIS && (isNight || isDark)) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, -10, false, false));
            player.setSprinting(false);
        }
        
        // SETH_EXPLOSION_MAGNETISM - Pull player towards explosions (TNT entities)
        if (boon == BoonType.SETH_EXPLOSION_MAGNETISM) {
            if (player.tickCount % 100 == 0) {
                com.dracolich777.afterlifeentombed.AfterlifeEntombedMod.LOGGER.info("SETH_EXPLOSION_MAGNETISM is active, calling pullTowardsExplosives()");
            }
            pullTowardsExplosives(player);
        }

        // ===== HORUS BOONS (Projectile/Armor/Vision themed) =====
        
        // HORUS_PROJECTILE_IMMUNITY - Projectile immunity (handled in damage event)
        
        // HORUS_EAGLES_EYE - See invisible entities (handled in event)
        
        // HORUS_FEATHER_FALL - No fall damage
        if (boon == BoonType.HORUS_FEATHER_FALL) {
            player.fallDistance = 0;
        }
        
        // HORUS_DIVINE_FAVOR - Immune to blindness, darkness, slowness, nausea
        if (boon == BoonType.HORUS_DIVINE_FAVOR) {
            removeNegativeEffects(player, MobEffects.BLINDNESS, MobEffects.DARKNESS, 
                                 MobEffects.MOVEMENT_SLOWDOWN, MobEffects.CONFUSION);
        }

        // HORUS_PROJECTILE_WEAKNESS - Double projectile damage curse (handled in damage event)
        
        // HORUS_BLURRED_VISION - Blindness on damage curse (handled in damage event)
        
        // HORUS_HEAVY_BURDEN - Extra fall damage curse (handled in damage event)
    }

    /**
     * Handle damage events for boon effects
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(cap -> {
                for (ActiveBoon boon : cap.getActiveBoons()) {
                    handleDamageEffect(player, boon.getType(), event);
                }
            });
        }
        
        // Handle attacker boons
        if (event.getSource().getEntity() instanceof Player) {
            @SuppressWarnings("null")
            Player attacker = (Player) event.getSource().getEntity();
            attacker.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(cap -> {
                for (ActiveBoon boon : cap.getActiveBoons()) {
                    handleAttackerEffect(attacker, boon.getType(), event);
                }
            });
        }
    }

    private static void handleDamageEffect(Player player, BoonType boon, LivingHurtEvent event) {
        // Check if player should take fall damage (has flight boon but not feather fall)
        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
            player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(cap -> {
                boolean hasFlightBoon = cap.hasBoon(BoonType.RA_SOLAR_FLIGHT) || cap.hasBoon(BoonType.SETH_NIGHT_FLIGHT);
                boolean hasFeatherFall = cap.hasBoon(BoonType.HORUS_FEATHER_FALL);
                
                // If player has flight but not feather fall, they should still take fall damage
                // Note: This doesn't prevent the damage, it ensures feather fall is the ONLY way to avoid it
                if (hasFlightBoon && !hasFeatherFall) {
                    // Make sure fall damage actually applies by not interfering
                    // The HORUS_FEATHER_FALL check below will cancel if needed
                }
            });
        }
        
        // RA - Take no damage from fire if blessed
        if (boon == BoonType.RA_FIRE_IMMUNITY && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            event.setCanceled(true);
        }
        
        // RA - Extra fire damage curse
        if (boon == BoonType.RA_FIRE_VULNERABILITY && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            event.setAmount(event.getAmount() * 2.0F);
        }
        
        // SETH - Explosion immunity
        if (boon == BoonType.SETH_CHAOS_WARD && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            event.setCanceled(true);
        }
        
        // SETH - 50% chance to dodge damage
        if (boon == BoonType.SETH_UNPREDICTABLE && player.getRandom().nextFloat() < 0.5F) {
            event.setCanceled(true);
        }
        
        // SETH - Damage reflection
        if (boon == BoonType.SETH_WILD_MAGIC && event.getSource().getEntity() instanceof LivingEntity attacker) {
            attacker.hurt(player.damageSources().magic(), event.getAmount() * 0.5F);
        }
        
        // HORUS - No damage from above
        if (boon == BoonType.HORUS_SKY_PROTECTION) {
            if (event.getSource().getEntity() != null) {
                if (event.getSource().getEntity().getY() > player.getY() + 1.0) {
                    event.setCanceled(true);
                }
            }
        }
        
        // HORUS - No projectile damage
        if (boon == BoonType.HORUS_PROJECTILE_IMMUNITY && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
            event.setCanceled(true);
        }
        
        // HORUS - No fall damage
        if (boon == BoonType.HORUS_FEATHER_FALL && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
            event.setCanceled(true);
        }
        
        // HORUS - Double projectile damage curse
        if (boon == BoonType.HORUS_PROJECTILE_WEAKNESS && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
            event.setAmount(event.getAmount() * 2.0F);
        }
        
        // HORUS - Extra fall damage curse
        if (boon == BoonType.HORUS_HEAVY_BURDEN && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
            event.setAmount(event.getAmount() * 2.0F);
        }
        
        // HORUS - Blindness on damage curse
        if (boon == BoonType.HORUS_BLURRED_VISION) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
        }
        
        // Handle attacker effects on self when attacking
        if (event.getSource().getEntity() == player) {
            // RA - Set on fire when attacking curse
            if (boon == BoonType.RA_SELF_IMMOLATION) {
                player.setSecondsOnFire(10);
            }
            
            // SETH - Wither when attacking curse
            if (boon == BoonType.SETH_SELF_DECAY) {
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
            }
            
            // HORUS - Self damage when attacking curse
            if (boon == BoonType.HORUS_SELF_HARM) {
                player.hurt(player.damageSources().magic(), event.getAmount() * 0.5F);
            }
        }
    }

    private static void handleAttackerEffect(Player attacker, BoonType boon, LivingHurtEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity target = (LivingEntity) event.getEntity();
        
        // RA - Holy fire on attacks
        if (boon == BoonType.RA_HOLY_FIRE) {
            target.setSecondsOnFire(10);
            // Push out of water
            if (target.isInWater()) {
                target.setDeltaMovement(target.getDeltaMovement().add(0, 0.5, 0));
            }
        }
        
        // SETH - Wither on attacks
        if (boon == BoonType.SETH_WITHER_STRIKES) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
        }
        
        // HORUS - Reduce target armor temporarily
        if (boon == BoonType.HORUS_ARMOR_BREAKER) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
        }
        
        // HORUS - Double damage if target is far away
        if (boon == BoonType.HORUS_SNIPER) {
            double distance = attacker.distanceTo(target);
            if (distance >= 15.0) {
                event.setAmount(event.getAmount() * 2.0F);
            }
        }
    }

    /**
     * Handle attack events for boon effects
     */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(cap -> {
                for (ActiveBoon boon : cap.getActiveBoons()) {
                    // SETH - 25% chance not to die
                    if (boon.getType() == BoonType.SETH_DEATH_DEFIANCE && player.getHealth() - event.getAmount() <= 0) {
                        if (player.getRandom().nextFloat() < 0.25F) {
                            event.setCanceled(true);
                            player.setHealth(1.0F);
                        }
                    }
                }
            });
        }
        
        // Handle entities attacking player
        if (event.getSource().getEntity() instanceof LivingEntity) {
            @SuppressWarnings("null")
            LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
            // Check if player has aura effects
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                player.getCapability(PlayerBoonsCapability.PLAYER_BOONS_CAPABILITY).ifPresent(cap -> {
                    for (ActiveBoon boon : cap.getActiveBoons()) {
                        // RA - Entities that attack get set on fire
                        if (boon.getType() == BoonType.RA_FIRE_AURA || boon.getType() == BoonType.RA_BURNING_RETRIBUTION) {
                            attacker.setSecondsOnFire(10);
                        }
                        
                        // SETH - Entities that attack get wither
                        if (boon.getType() == BoonType.SETH_WITHER_AURA) {
                            attacker.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
                        }
                    }
                });
            }
        }
    }

    // Helper methods
    
    private static void applyLavaWalking(Player player) {
        BlockPos pos = player.blockPosition();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = pos.offset(x, -1, z);
                BlockState state = player.level().getBlockState(checkPos);
                if (state.is(Blocks.LAVA)) {
                    player.level().setBlock(checkPos.above(), Blocks.BASALT.defaultBlockState(), 11);
                    // Schedule block removal
                    player.level().scheduleTick(checkPos.above(), Blocks.BASALT, 60);
                }
            }
        }
    }

    private static void evaporateWaterNearby(Player player) {
        if (player.tickCount % 20 != 0) return; // Once per second
        
        BlockPos pos = player.blockPosition();
        for (int x = -5; x <= 5; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockState state = player.level().getBlockState(checkPos);
                    if (state.is(Blocks.WATER)) {
                        player.level().setBlock(checkPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static void applyDarknessAura(Player player) {
        if (player.tickCount % 20 != 0) return; // Once per second
        
        player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0), 
            entity -> entity != player).forEach(entity -> {
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
        });
    }
    
    private static void applyFireAura(Player player) {
        if (player.tickCount % 60 != 0) return; // Once every 3 seconds
        
        player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0), 
            entity -> entity != player).forEach(entity -> {
            entity.setSecondsOnFire(3);
        });
    }
    
    private static void applyWitherAura(Player player) {
        if (player.tickCount % 60 != 0) return; // Once every 3 seconds
        
        player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0), 
            entity -> entity != player).forEach(entity -> {
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
        });
    }

    private static void removeNegativeEffects(Player player, MobEffect... effects) {
        for (MobEffect effect : effects) {
            if (player.hasEffect(effect)) {
                player.removeEffect(effect);
            }
        }
    }
    
    /**
     * Pull player slightly towards nearby lava sources (RA curse)
     */
    private static void pullTowardsLava(Player player) {
        // Check more frequently for smoother pull
        if (player.tickCount % 2 != 0) return; // Check every 2 ticks
        
        BlockPos playerPos = player.blockPosition();
        BlockPos closestLava = null;
        double closestDistSq = Double.MAX_VALUE; // Use squared distance for comparison
        
        // Search for lava in a 10 block radius
        int lavaCount = 0;
        for (int x = -10; x <= 10; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    BlockState state = player.level().getBlockState(checkPos);
                    if (state.is(Blocks.LAVA)) {
                        lavaCount++;
                        double distSq = playerPos.distSqr(checkPos);
                        if (distSq < closestDistSq) {
                            closestLava = checkPos;
                            closestDistSq = distSq;
                        }
                    }
                }
            }
        }
        
        // Debug logging
        if (player.tickCount % 100 == 0) {
            AfterlifeEntombedMod.LOGGER.info("Lava magnetism check - found {} lava blocks, closest: {}", 
                lavaCount, closestLava != null ? closestLava : "NONE");
        }
        
        // Pull player towards closest lava
        if (closestLava != null) {
            double dx = closestLava.getX() + 0.5 - player.getX();
            double dy = closestLava.getY() + 0.5 - player.getY();
            double dz = closestLava.getZ() + 0.5 - player.getZ();
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            
            if (length > 0.1) { // Only pull if not already on the lava
                // Stronger pull - increased from 0.05 to 0.15 blocks per tick
                double pullStrength = 0.15;
                Vec3 currentVel = player.getDeltaMovement();
                Vec3 pull = new Vec3(
                    (dx / length) * pullStrength,
                    (dy / length) * pullStrength,
                    (dz / length) * pullStrength
                );
                player.setDeltaMovement(currentVel.add(pull));
                player.hurtMarked = true; // Force sync to client
                
                // Log when actually applying pull
                if (player.tickCount % 100 == 0) {
                    AfterlifeEntombedMod.LOGGER.info("APPLYING lava pull: distance={}, pull={}, newVel={}", 
                        String.format("%.2f", length), pull, player.getDeltaMovement());
                }
            }
        }
    }
    
    /**
     * Pull player slightly towards nearby TNT entities (SETH curse)
     */
    private static void pullTowardsExplosives(Player player) {
        // Check more frequently for smoother pull
        if (player.tickCount % 2 != 0) return; // Check every 2 ticks
        
        // Find nearby TNT entities
        java.util.List<PrimedTnt> tntEntities = player.level().getEntitiesOfClass(
            PrimedTnt.class, 
            player.getBoundingBox().inflate(15.0)
        );
        
        // Debug logging
        if (player.tickCount % 100 == 0) {
            AfterlifeEntombedMod.LOGGER.info("Explosion magnetism check - found {} TNT entities", tntEntities.size());
        }
        
        if (!tntEntities.isEmpty()) {
            // Find closest TNT
            PrimedTnt closestTnt = null;
            double closestDist = Double.MAX_VALUE;
            
            for (PrimedTnt tnt : tntEntities) {
                double dist = player.distanceToSqr(tnt);
                if (dist < closestDist) {
                    closestDist = dist;
                    closestTnt = tnt;
                }
            }
            
            if (closestTnt != null && closestDist > 1.0) { // Only pull if not already on the TNT
                Vec3 playerPos = player.position();
                Vec3 tntPos = closestTnt.position();
                Vec3 direction = tntPos.subtract(playerPos).normalize();
                
                // Stronger pull - increased from 0.08 to 0.20 blocks per tick
                double pullStrength = 0.20;
                Vec3 currentVel = player.getDeltaMovement();
                Vec3 pull = direction.scale(pullStrength);
                player.setDeltaMovement(currentVel.add(pull));
                player.hurtMarked = true; // Force sync to client
                
                // Log when actually applying pull
                if (player.tickCount % 100 == 0) {
                    AfterlifeEntombedMod.LOGGER.info("APPLYING TNT pull: distance={}, pull={}, newVel={}", 
                        String.format("%.2f", Math.sqrt(closestDist)), pull, player.getDeltaMovement());
                }
            }
        }
    }
}
