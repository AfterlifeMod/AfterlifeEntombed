package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.init.ModEntityTypes;
import com.dracolich777.afterlifeentombed.mobs.GodseekerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID)
public class GodseekerRitualPattern {

    private static final Map<BlockPos, Block> RITUAL_PATTERN = new HashMap<>();

    static {
        RITUAL_PATTERN.put(new BlockPos(0, -1, 0), Blocks.CHISELED_SANDSTONE);
        RITUAL_PATTERN.put(new BlockPos(0, -2, 0), Blocks.GOLD_BLOCK);
        RITUAL_PATTERN.put(new BlockPos(0, -3, 0), Blocks.CUT_SANDSTONE);
        RITUAL_PATTERN.put(new BlockPos(1, -1, 0), Blocks.GOLD_BLOCK);
        RITUAL_PATTERN.put(new BlockPos(-1, -1, 0), Blocks.GOLD_BLOCK);
        RITUAL_PATTERN.put(new BlockPos(0, -1, 1), Blocks.GOLD_BLOCK);
        RITUAL_PATTERN.put(new BlockPos(0, -1, -1), Blocks.GOLD_BLOCK);
    }

    private static final long RITUAL_COOLDOWN_TICKS = 20 * 60 * 10;
    private static final Map<BlockPos, Long> ritualCooldowns = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide() || !(event.getLevel() instanceof ServerLevel)) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos placedPos = event.getPos();
        BlockState placedState = event.getPlacedBlock();

        if (!placedState.is(Blocks.WITHER_SKELETON_SKULL) && !placedState.is(Blocks.SKELETON_SKULL)) {
            return;
        }

        BlockPos basePos = placedPos.offset(0, -3, 0);
        if (ritualCooldowns.containsKey(basePos) && level.getGameTime() < ritualCooldowns.get(basePos)) {
            return;
        }

        if (checkPattern(level, placedPos)) {
            performGodseekerSummon(level, placedPos);
            ritualCooldowns.put(basePos, level.getGameTime() + RITUAL_COOLDOWN_TICKS);
        }
    }

    private static boolean checkPattern(Level level, BlockPos skullPos) {
        for (Map.Entry<BlockPos, Block> entry : RITUAL_PATTERN.entrySet()) {
            BlockPos relativePos = entry.getKey();
            Block requiredBlock = entry.getValue();

            BlockPos worldPos = skullPos.offset(relativePos);
            BlockState actualState = level.getBlockState(worldPos);

            if (!actualState.is(requiredBlock)) {
                return false;
            }
        }
        return true;
    }

    private static void performGodseekerSummon(ServerLevel level, BlockPos skullPos) {
        for (Map.Entry<BlockPos, Block> entry : RITUAL_PATTERN.entrySet()) {
            BlockPos relativePos = entry.getKey();
            BlockPos worldPos = skullPos.offset(relativePos);
            level.setBlockAndUpdate(worldPos, Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(skullPos, Blocks.AIR.defaultBlockState());

        level.playSound(null, skullPos, SoundEvents.ILLUSIONER_PREPARE_BLINDNESS, SoundSource.MASTER, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
        level.playSound(null, skullPos, SoundEvents.WARDEN_ROAR, SoundSource.MASTER, 1.5F, 0.5F + level.random.nextFloat() * 0.4F);

        GodseekerEntity godseeker = ModEntityTypes.GODSEEKER.get().create(level);
        if (godseeker != null) {
            godseeker.setPos(skullPos.getX() + 0.5, skullPos.getY() + 0.1, skullPos.getZ() + 0.5);
            level.addFreshEntity(godseeker);
            godseeker.setPersistenceRequired();
            godseeker.setNoAi(false);
            godseeker.setCanPickUpLoot(false);

            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, skullPos.getX() + 0.5, skullPos.getY() + 0.5, skullPos.getZ() + 0.5, 50, 0.5, 0.5, 0.5, 0.05);
            level.sendParticles(ParticleTypes.FLASH, skullPos.getX() + 0.5, skullPos.getY() + 0.5, skullPos.getZ() + 0.5, 5, 0.0, 0.0, 0.0, 0.0);
        } else {
            AfterlifeEntombedMod.LOGGER.error("Failed to create Godseeker entity! ModEntityTypes.GODSEEKER.get() returned null.");
        }
    }
}