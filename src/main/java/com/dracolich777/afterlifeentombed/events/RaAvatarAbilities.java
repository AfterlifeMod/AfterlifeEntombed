package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.capabilities.GodAvatarCapability;
import com.dracolich777.afterlifeentombed.client.hud.GodAvatarHudHelper;
import com.dracolich777.afterlifeentombed.init.ModEffects;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.items.GodstoneItem;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.SyncGodAvatarPacket;
import com.dracolich777.afterlibs.api.AfterLibsAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Event handlers for Ra Avatar abilities - God of Sun & Light (Cleansing Fire)
 */
@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RaAvatarAbilities {

    // Ability IDs
    public static final int ABILITY_SOLAR_FLARE = 1;
    public static final int ABILITY_PURIFYING_LIGHT = 2;
    public static final int ABILITY_HOLY_INFERNO = 3;
    public static final int ABILITY_AVATAR_OF_SUN = 4;

    // Track active purifying light pillars: <player UUID, <position, end time>>
    private static final Map<UUID, Map<BlockPos, Long>> ACTIVE_LIGHT_PILLARS = new HashMap<>();

    /**
     * Check if player is Ra avatar
     */
    private static boolean isRaAvatar(Player player) {
        return player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY)
                .map(cap -> cap.getSelectedGod() == GodType.RA).orElse(false);
    }

    /**
     * Check if it's daytime in the player's dimension
     */
    private static boolean isDaytime(Player player) {
        long dayTime = player.level().getDayTime() % 24000;
        return dayTime >= 0 && dayTime < 13000; // Daytime is roughly 0-13000
    }

    /**
     * Check if it's noon (peak sun)
     */
    private static boolean isNoon(Player player) {
        long dayTime = player.level().getDayTime() % 24000;
        return dayTime >= 5000 && dayTime < 7000; // Noon is around 6000
    }

    /**
     * Check if player is in complete darkness
     */
    private static boolean isInCompleteDarkness(Player player) {
        return player.level().getMaxLocalRawBrightness(player.blockPosition()) == 0;
    }

    // ===== PASSIVE ABILITIES =====

    /**
     * Sun's Blessing - Regeneration during day, Strength at noon
     * Radiant Aura - Emit light around player
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide)
            return;

        Player player = event.player;
        if (!isRaAvatar(player))
            return;

        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();

            // Sun's Blessing - Daytime buffs
            if (isDaytime(player)) {
                if (!player.hasEffect(MobEffects.REGENERATION)) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false));
                }

                // Extra strength at noon
                if (isNoon(player)) {
                    if (!player.hasEffect(MobEffects.DAMAGE_BOOST)) {
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0, false, false));
                    }
                }
            }

            // Radiant Aura - Create light particles around player
            if (currentTime % 10 == 0 && player.level() instanceof ServerLevel serverLevel) {
                // Stronger during day
                int lightLevel = isDaytime(player) ? 8 : 4;

                // Spawn light particles in radius
                for (int i = 0; i < 5; i++) {
                    double offsetX = (player.getRandom().nextDouble() - 0.5) * 4;
                    double offsetY = player.getRandom().nextDouble() * 2;
                    double offsetZ = (player.getRandom().nextDouble() - 0.5) * 4;

                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            player.getX() + offsetX,
                            player.getY() + offsetY,
                            player.getZ() + offsetZ,
                            1, 0, 0.1, 0, 0);
                }
            }

            // Update active purifying light pillars
            updatePurifyingLightPillars(player, currentTime);

            // Handle Avatar of Sun effects
            if (cap.isAvatarOfSunActive()) {
                long activationTime = currentTime - cap.getAvatarOfSunCooldown();

                // Apply massive buffs
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 9, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 4, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20, 4, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 20, 4, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 254, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20, 254, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false));

                // Creative flight
                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }

                // Spawn radiant aura particles
                if (currentTime % 5 == 0 && player.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 10; i++) {
                        double offsetX = (player.getRandom().nextDouble() - 0.5) * 2;
                        double offsetY = player.getRandom().nextDouble() * 3;
                        double offsetZ = (player.getRandom().nextDouble() - 0.5) * 2;

                        serverLevel.sendParticles(ParticleTypes.FLAME,
                                player.getX() + offsetX,
                                player.getY() + offsetY,
                                player.getZ() + offsetZ,
                                2, 0.1, 0.1, 0.1, 0.02);

                        serverLevel.sendParticles(ParticleTypes.END_ROD,
                                player.getX() + offsetX,
                                player.getY() + offsetY,
                                player.getZ() + offsetZ,
                                1, 0.1, 0.1, 0.1, 0.01);
                    }
                }

                // 1 minute duration
                if (activationTime >= 1200 && player instanceof ServerPlayer) {
                    deactivateAvatarOfSun((ServerPlayer) player, cap, currentTime);
                }
            }
        });
    }

    /**
     * Night Weakness - Reduced damage and no regeneration at night
     */
    @SubscribeEvent
    public static void onNightWeakness(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player player && isRaAvatar(player)) {
            if (!isDaytime(player)) {
                // Reduce damage dealt by 30% at night
                event.setAmount(event.getAmount() * 0.7f);
            }
        }
    }

    /**
     * Sun's Wrath - Apply Holy Fire to enemies hit during daytime
     */
    @SubscribeEvent
    public static void onSunsWrath(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player && isRaAvatar(player)) {
            // Apply Holy Fire during daytime for 1 minute (1200 ticks)
            if (isDaytime(player)) {
                LivingEntity target = event.getEntity();
                target.addEffect(new MobEffectInstance(ModEffects.HOLY_FIRE.get(), 1200, 0, false, true));
            }
        }
    }

    /**
     * Shadow Bane - Take damage in complete darkness
     */
    @SubscribeEvent
    public static void onPlayerTickShadowBane(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide)
            return;

        Player player = event.player;
        if (!isRaAvatar(player))
            return;

        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            // Skip if in Avatar form
            if (cap.isAvatarOfSunActive())
                return;

            long currentTime = player.level().getGameTime();

            // Check every second (20 ticks)
            if (currentTime % 20 == 0 && isInCompleteDarkness(player)) {
                player.hurt(player.damageSources().magic(), 1.0f);
                // Darkness damage shown in HUD automatically
            }
        });
    }

    // ===== ACTIVATED ABILITIES =====

    /**
     * Handle ability activation from client
     */
    public static void activateAbility(ServerPlayer player, int abilityId) {
        if (!isRaAvatar(player))
            return;

        player.getCapability(GodAvatarCapability.GOD_AVATAR_CAPABILITY).ifPresent(cap -> {
            long currentTime = player.level().getGameTime();

            switch (abilityId) {
                case ABILITY_SOLAR_FLARE -> activateSolarFlare(player, cap, currentTime);
                case ABILITY_PURIFYING_LIGHT -> activatePurifyingLight(player, cap, currentTime);
                case ABILITY_HOLY_INFERNO -> activateHolyInferno(player, cap, currentTime);
                case ABILITY_AVATAR_OF_SUN -> activateAvatarOfSun(player, cap, currentTime);
            }
        });
    }

    /**
     * Solar Flare - Fire burning projectile that explodes on impact (10s cooldown)
     */
    private static void activateSolarFlare(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        boolean unlimited = cap.isAvatarOfSunActive();

        // Check cooldown - cooldown time must be greater than current time to still be
        // active
        if (!unlimited && cap.getSolarFlareCooldown() > currentTime) {
            long remaining = (cap.getSolarFlareCooldown() - currentTime) / 20;
            GodAvatarHudHelper.sendCooldownMessage(player, "Solar Flare", remaining);
            return;
        }

        // Fire a custom fireball projectile
        Vec3 lookVec = player.getLookAngle();
        SmallFireball fireball = new SmallFireball(player.level(), player,
                lookVec.x, lookVec.y, lookVec.z);
        fireball.setPos(player.getX() + lookVec.x * 2,
                player.getEyeY(),
                player.getZ() + lookVec.z * 2);

        player.level().addFreshEntity(fireball);

        // NOTE: Future enhancement - create custom fireball entity with golden
        // explosion and cleansing effect
        // Current implementation uses vanilla LargeFireball

        if (!unlimited) {
            cap.setSolarFlareCooldown(currentTime + 200); // 10 second cooldown
        }

        GodAvatarHudHelper.sendActivationMessage(player, "Solar Flare", GodAvatarHudHelper.COLOR_RA);
    }

    /**
     * Purifying Light - Create pillar of light at target location (30s cooldown,
     * lasts 10s)
     */
    private static void activatePurifyingLight(ServerPlayer player, GodAvatarCapability.IGodAvatar cap,
            long currentTime) {
        boolean unlimited = cap.isAvatarOfSunActive();

        // Check cooldown - must be LESS THAN OR EQUAL to current time to be ready
        if (!unlimited && cap.getPurifyingLightCooldown() > currentTime) {
            long remaining = (cap.getPurifyingLightCooldown() - currentTime) / 20;
            GodAvatarHudHelper.sendCooldownMessage(player, "Purifying Light", remaining);
            return;
        }

        // Raycast to find target location
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(30));
        BlockHitResult result = player.level().clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        if (result.getType() != HitResult.Type.MISS) {
            BlockPos targetPos = result.getBlockPos().above();

            // Store this pillar location - 10 seconds duration
            ACTIVE_LIGHT_PILLARS.computeIfAbsent(player.getUUID(), k -> new HashMap<>())
                    .put(targetPos, currentTime + 200); // 10 seconds duration

            // Track that ability is active
            cap.setPurifyingLightActive(true);
            cap.setPurifyingLightEndTime(currentTime + 200);

            // Create initial light pillar particles using AfterLibs
            if (player.level() instanceof ServerLevel serverLevel) {
                // Spawn ra_column particle at the target position
                AfterLibsAPI.spawnAfterlifeParticle(serverLevel, "ra_column",
                        targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.5f);
            }

            if (!unlimited) {
                cap.setPurifyingLightCooldown(currentTime + 600); // 30 second cooldown
            }

            GodAvatarHudHelper.sendActivationMessage(player, "Purifying Light", GodAvatarHudHelper.COLOR_RA);
        } else {
            GodAvatarHudHelper.sendNotification(player, "No valid target", GodAvatarHudHelper.COLOR_ERROR, 30);
        }
    }

    /**
     * Update and maintain purifying light pillars
     */
    private static void updatePurifyingLightPillars(Player player, long currentTime) {
        if (!(player.level() instanceof ServerLevel serverLevel))
            return;

        Map<BlockPos, Long> playerPillars = ACTIVE_LIGHT_PILLARS.get(player.getUUID());
        if (playerPillars == null || playerPillars.isEmpty())
            return;

        // Update each pillar
        playerPillars.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            long endTime = entry.getValue();

            // Check if pillar has expired
            if (currentTime >= endTime) {
                return true; // Remove from map
            }

            // Spawn pillar particles every 5 ticks
            if (currentTime % 5 == 0) {
                // Spawn ra_column particle at pillar location
                AfterLibsAPI.spawnAfterlifeParticle(serverLevel, "ra_column",
                        pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1.5f);
            }

            // Heal allies and damage enemies in radius every second
            if (currentTime % 20 == 0) {
                AABB areaBox = new AABB(pos).inflate(6);
                List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, areaBox);

                for (LivingEntity entity : entities) {
                    if (entity instanceof Player allyPlayer) {
                        // Heal allies
                        allyPlayer.heal(2.0f); // 1 heart per 3 seconds (roughly)

                        // Cleanse negative effects
                        allyPlayer.removeEffect(MobEffects.POISON);
                        allyPlayer.removeEffect(MobEffects.WITHER);
                        allyPlayer.removeEffect(MobEffects.WEAKNESS);
                        allyPlayer.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    } else if (!(entity instanceof Player)) {
                        // Damage hostile mobs (anything that's not a player)
                        entity.hurt(serverLevel.damageSources().magic(), 3.0f);
                        entity.setSecondsOnFire(5);
                    }
                }
            }

            return false; // Keep in map
        });
    }

    /**
     * Holy Inferno - Summon cone of cleansing fire (25s cooldown)
     */
    private static void activateHolyInferno(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        boolean unlimited = cap.isAvatarOfSunActive();

        // Check cooldown - cooldown time must be greater than current time to still be
        // active
        if (!unlimited && cap.getHolyInfernoCooldown() > currentTime) {
            long remaining = (cap.getHolyInfernoCooldown() - currentTime) / 20;
            GodAvatarHudHelper.sendCooldownMessage(player, "Holy Inferno", remaining);
            return;
        }

        // Get player's look direction
        Vec3 lookVec = player.getLookAngle();
        double angleRad = Math.toRadians(45); // 45 degree cone total (22.5 each side)

        if (player.level() instanceof ServerLevel serverLevel) {
            // Cast multiple rays in a cone
            for (int angle = -22; angle <= 22; angle += 2) {
                double radians = Math.toRadians(angle);
                double cos = Math.cos(radians);
                double sin = Math.sin(radians);

                // Rotate look vector around Y axis
                Vec3 rayDir = new Vec3(
                        lookVec.x * cos - lookVec.z * sin,
                        lookVec.y,
                        lookVec.x * sin + lookVec.z * cos).normalize();

                // Check each distance step in the cone
                for (double dist = 1; dist <= 8; dist += 0.5) {
                    Vec3 checkPos = player.position().add(rayDir.scale(dist));

                    // Spawn fire particles
                    serverLevel.sendParticles(ParticleTypes.FLAME,
                            checkPos.x, checkPos.y + 1, checkPos.z,
                            2, 0.2, 0.2, 0.2, 0.05);

                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            checkPos.x, checkPos.y + 1, checkPos.z,
                            1, 0.1, 0.1, 0.1, 0.02);

                    // Damage entities at this position
                    AABB damageBox = new AABB(checkPos.x - 0.5, checkPos.y, checkPos.z - 0.5,
                            checkPos.x + 0.5, checkPos.y + 2, checkPos.z + 0.5);
                    List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, damageBox);

                    for (LivingEntity entity : entities) {
                        if (entity != player) {
                            entity.hurt(serverLevel.damageSources().onFire(), 6.0f);
                            entity.setSecondsOnFire(10);
                        }
                    }

                    // Purify corrupted blocks
                    BlockPos blockPos = new BlockPos((int) checkPos.x, (int) checkPos.y, (int) checkPos.z);
                    BlockState state = serverLevel.getBlockState(blockPos);

                    if (state.is(Blocks.SOUL_SAND)) {
                        serverLevel.setBlock(blockPos, Blocks.SAND.defaultBlockState(), 3);
                    } else if (state.is(Blocks.NETHERRACK)) {
                        serverLevel.setBlock(blockPos, Blocks.STONE.defaultBlockState(), 3);
                    } else if (state.is(Blocks.SOUL_SOIL)) {
                        serverLevel.setBlock(blockPos, Blocks.DIRT.defaultBlockState(), 3);
                    }
                }
            }
        }

        if (!unlimited) {
            cap.setHolyInfernoCooldown(currentTime + 500); // 25 second cooldown
        }

        GodAvatarHudHelper.sendNotification(player, "HOLY INFERNO!", GodAvatarHudHelper.COLOR_RA, 40);
    }

    /**
     * Avatar of Sun - Ultimate transformation (1min duration, 5min cooldown)
     */
    private static void activateAvatarOfSun(ServerPlayer player, GodAvatarCapability.IGodAvatar cap, long currentTime) {
        // If already active, deactivate it
        if (cap.isAvatarOfSunActive()) {
            deactivateAvatarOfSun(player, cap, currentTime);
            return;
        }

        // Check cooldown - cooldown time must be greater than current time to still be
        // active
        if (cap.getAvatarOfSunCooldown() > currentTime) {
            long remaining = (cap.getAvatarOfSunCooldown() - currentTime) / 20;
            GodAvatarHudHelper.sendCooldownMessage(player, "Avatar of Sun", remaining);
            return;
        }

        // Check if holding a different god's stone to switch
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof GodstoneItem godstone) {
            GodType newGod = godstone.getGodType();
            if (newGod != GodType.RA && newGod != GodType.NONE) {
                // Switch gods!
                cap.setSelectedGod(newGod);

                // Consume the godstone
                mainHand.shrink(1);

                // Spawn swap particles based on new god
                if (player.level() instanceof ServerLevel level) {
                    String particleName = switch (newGod) {
                        case SETH -> "seth_fog";
                        case SHU -> "shujump";
                        case ANUBIS -> "anubis_nuke";
                        case HORUS, ISIS, GEB, THOTH -> "ra_halo"; // Default to ra_halo for other gods
                        default -> "ra_halo";
                    };
                    AfterLibsAPI.spawnAfterlifeParticle(level, particleName, player.getX(), player.getY() + 1,
                            player.getZ(), 2.0f);
                }

                // Switch to the new god's origin
                var server = player.getServer();
                if (server != null) {
                    String originId = switch (newGod) {
                        case SETH -> "afterlifeentombed:avatar_of_seth";
                        case RA -> "afterlifeentombed:avatar_of_ra";
                        case SHU -> "afterlifeentombed:avatar_of_shu";
                        case ANUBIS -> "afterlifeentombed:avatar_of_anubis";
                        case HORUS, ISIS, GEB, THOTH -> "afterlifeentombed:avatar_of_egypt";
                        default -> null;
                    };

                    if (originId != null) {
                        // Remove ALL existing avatar origins first
                        server.getCommands().performPrefixedCommand(
                                server.createCommandSourceStack(),
                                "origin revoke " + player.getGameProfile().getName()
                                        + " origins:origin afterlifeentombed:avatar_of_egypt");
                        server.getCommands().performPrefixedCommand(
                                server.createCommandSourceStack(),
                                "origin revoke " + player.getGameProfile().getName()
                                        + " origins:origin afterlifeentombed:avatar_of_seth");
                        server.getCommands().performPrefixedCommand(
                                server.createCommandSourceStack(),
                                "origin revoke " + player.getGameProfile().getName()
                                        + " origins:origin afterlifeentombed:avatar_of_ra");
                        server.getCommands().performPrefixedCommand(
                                server.createCommandSourceStack(),
                                "origin revoke " + player.getGameProfile().getName()
                                        + " origins:origin afterlifeentombed:avatar_of_shu");

                        // Now grant the new origin
                        server.getCommands().performPrefixedCommand(
                                server.createCommandSourceStack(),
                                "origin set " + player.getGameProfile().getName() + " origins:origin " + originId);
                    }
                }

                GodAvatarHudHelper.sendNotification(player, "Now avatar of " + newGod.name(),
                        GodAvatarHudHelper.COLOR_SPECIAL, 60);
                GodAvatarPackets.INSTANCE.sendToServer(new SyncGodAvatarPacket(newGod));
                return;
            }
        }

        // Activate Avatar of Sun
        cap.setAvatarOfSunActive(true);
        cap.setAvatarOfSunCooldown(currentTime);

        // Grant the Origins power for size increase
        var server = player.getServer();
        if (server != null) {
            server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    "power grant " + player.getGameProfile().getName() + " afterlifeentombed:ra_avatar_of_sun_active");
        }

        // Spawn transformation particles
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 100; i++) {
                double offsetX = (player.getRandom().nextDouble() - 0.5) * 4;
                double offsetY = player.getRandom().nextDouble() * 3;
                double offsetZ = (player.getRandom().nextDouble() - 0.5) * 4;

                serverLevel.sendParticles(ParticleTypes.FLAME,
                        player.getX() + offsetX,
                        player.getY() + offsetY,
                        player.getZ() + offsetZ,
                        5, 0.2, 0.2, 0.2, 0.1);

                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        player.getX() + offsetX,
                        player.getY() + offsetY,
                        player.getZ() + offsetZ,
                        3, 0.1, 0.1, 0.1, 0.05);
            }
        }

        GodAvatarHudHelper.sendNotification(player, "✦ AVATAR OF THE SUN ✦", GodAvatarHudHelper.COLOR_RA, 60);
    }

    private static void deactivateAvatarOfSun(ServerPlayer player, GodAvatarCapability.IGodAvatar cap,
            long currentTime) {
        cap.setAvatarOfSunActive(false);
        cap.setAvatarOfSunCooldown(currentTime + 6000); // 5 minute cooldown

        // Revoke the Origins power
        var server = player.getServer();
        if (server != null) {
            server.getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    "power revoke " + player.getGameProfile().getName() + " afterlifeentombed:ra_avatar_of_sun_active");
        }

        // Remove flight
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();

        // Add slow falling to prevent fall damage
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 0, false, true));

        GodAvatarHudHelper.sendNotification(player, "Avatar of Sun ended", GodAvatarHudHelper.COLOR_RA, 40);
    }
}
