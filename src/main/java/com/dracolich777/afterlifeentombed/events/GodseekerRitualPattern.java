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
            AfterlifeEntombedMod.LOGGER.debug("BlockPlaceEvent fired on client side or not a ServerLevel, skipping. Block: {}", event.getPlacedBlock().getBlock().getName().getString());
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos placedPos = event.getPos();
        BlockState placedState = event.getPlacedBlock();

        // AfterlifeEntombedMod.LOGGER.info("Block placed at: {} (Block: {})", placedPos, placedState.getBlock().getName().getString());

        if (!placedState.is(Blocks.WITHER_SKELETON_SKULL) && !placedState.is(Blocks.SKELETON_SKULL)) {
            AfterlifeEntombedMod.LOGGER.debug("Placed block is not a skull, skipping ritual check. Block: {}", placedState.getBlock().getName().getString());
            return;
        }
        AfterlifeEntombedMod.LOGGER.info("Skull placed at {}! Initiating ritual pattern check.", placedPos);

        BlockPos basePos = placedPos.offset(0, -3, 0);
        if (ritualCooldowns.containsKey(basePos) && level.getGameTime() < ritualCooldowns.get(basePos)) {
            AfterlifeEntombedMod.LOGGER.info("Ritual location {} is on cooldown. Remaining ticks: {}", basePos, ritualCooldowns.get(basePos) - level.getGameTime());
            return;
        }

        if (checkPattern(level, placedPos)) {
            AfterlifeEntombedMod.LOGGER.info("Pattern matched successfully at {}! Summoning Godseeker...", placedPos);
            performGodseekerSummon(level, placedPos);
            ritualCooldowns.put(basePos, level.getGameTime() + RITUAL_COOLDOWN_TICKS);
            AfterlifeEntombedMod.LOGGER.info("Ritual cooldown set for {} ticks at {}.", RITUAL_COOLDOWN_TICKS, basePos);
        } else {
            AfterlifeEntombedMod.LOGGER.info("Pattern mismatch at skull position: {}. Ritual failed.", placedPos);
        }
    }

    private static boolean checkPattern(Level level, BlockPos skullPos) {
        AfterlifeEntombedMod.LOGGER.info("Starting pattern check for skull at: {}. World Time: {}", skullPos, level.getGameTime());
        for (Map.Entry<BlockPos, Block> entry : RITUAL_PATTERN.entrySet()) {
            BlockPos relativePos = entry.getKey();
            Block requiredBlock = entry.getValue();

            BlockPos worldPos = skullPos.offset(relativePos);
            BlockState actualState = level.getBlockState(worldPos);
            // Block actualBlock = actualState.getBlock(); // You can get the Block object, but don't call .is() on it

            AfterlifeEntombedMod.LOGGER.info("  Checking relative: {} (World: {}) - Expected: {}, Found: {}",
                                              relativePos, worldPos, requiredBlock.getName().getString(), actualState.getBlock().getName().getString());

            // THE CRITICAL LINE: Always use actualState.is(requiredBlock)
            if (!actualState.is(requiredBlock)) {
                AfterlifeEntombedMod.LOGGER.warn("  PATTERN MISMATCH! At world position {}: Expected {}, Found {}. Ritual failed.",
                                                  worldPos, requiredBlock.getName().getString(), actualState.getBlock().getName().getString());
                return false;
            }
        }
        AfterlifeEntombedMod.LOGGER.info("All blocks in pattern matched successfully for skull at {}.", skullPos);
        return true;
    }

    private static void performGodseekerSummon(ServerLevel level, BlockPos skullPos) {
        AfterlifeEntombedMod.LOGGER.info("Executing Godseeker summon ritual at: {}", skullPos);

        for (Map.Entry<BlockPos, Block> entry : RITUAL_PATTERN.entrySet()) {
            BlockPos relativePos = entry.getKey();
            BlockPos worldPos = skullPos.offset(relativePos);
            level.setBlockAndUpdate(worldPos, Blocks.AIR.defaultBlockState());
            AfterlifeEntombedMod.LOGGER.debug("Removed block at: {}", worldPos);
        }
        level.setBlockAndUpdate(skullPos, Blocks.AIR.defaultBlockState());
        AfterlifeEntombedMod.LOGGER.debug("Removed skull at: {}", skullPos);

        level.playSound(null, skullPos, SoundEvents.ILLUSIONER_PREPARE_BLINDNESS, SoundSource.MASTER, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
        level.playSound(null, skullPos, SoundEvents.WARDEN_ROAR, SoundSource.MASTER, 1.5F, 0.5F + level.random.nextFloat() * 0.4F);
        AfterlifeEntombedMod.LOGGER.info("Playing ritual sounds.");

        GodseekerEntity godseeker = ModEntityTypes.GODSEEKER.get().create(level);
        if (godseeker != null) {
            godseeker.setPos(skullPos.getX() + 0.5, skullPos.getY() + 0.1, skullPos.getZ() + 0.5); // Adjusted Y slightly
            level.addFreshEntity(godseeker);
            godseeker.setPersistenceRequired();
            godseeker.setNoAi(false);
            godseeker.setCanPickUpLoot(false);
            AfterlifeEntombedMod.LOGGER.info("Godseeker summoned at: {}", godseeker.blockPosition());

            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, skullPos.getX() + 0.5, skullPos.getY() + 0.5, skullPos.getZ() + 0.5, 50, 0.5, 0.5, 0.5, 0.05);
            level.sendParticles(ParticleTypes.FLASH, skullPos.getX() + 0.5, skullPos.getY() + 0.5, skullPos.getZ() + 0.5, 5, 0.0, 0.0, 0.0, 0.0);
            AfterlifeEntombedMod.LOGGER.info("Spawning particles.");
        } else {
            AfterlifeEntombedMod.LOGGER.error("Failed to create Godseeker entity! ModEntityTypes.GODSEEKER.get() returned null.");
        }
    }
}