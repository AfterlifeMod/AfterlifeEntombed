package com.dracolich777.afterlifeentombed.events;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.boons.BoonEffectsHandler;
import com.dracolich777.afterlifeentombed.boons.BoonType;
import com.dracolich777.afterlifeentombed.items.GodType;
import com.dracolich777.afterlifeentombed.network.GodAvatarPackets;
import com.dracolich777.afterlifeentombed.network.OpenBoonSelectionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/**
 * Handles the random triggering of divine boons and curses from the gods
 */
@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DivineBoonHandler {
    
    // Chance per tick for a blessing to occur (1 in 144000 = ~1 per 2 hours of playtime per player)
    private static final int BLESSING_CHANCE = 144000;
    
    // Chance per tick for a curse to occur (1 in 72000 = ~1 per hour of playtime per player)
    private static final int CURSE_CHANCE = 72000;
    
    private static final Random random = new Random();
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Only process on server side during the END phase
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.level().isClientSide) return;
        
        // Apply active boon effects
        BoonEffectsHandler.tickBoonEffects(serverPlayer);
        
        // Random chance for blessing
        if (random.nextInt(BLESSING_CHANCE) == 0) {
            triggerBlessing(serverPlayer);
        }
        
        // Random chance for curse (separate roll)
        if (random.nextInt(CURSE_CHANCE) == 0) {
            triggerCurse(serverPlayer);
        }
    }
    
    /**
     * Trigger a random blessing from a random god
     */
    private static void triggerBlessing(ServerPlayer player) {
        GodType god = selectRandomGod();
        
        // Get 3 random blessings from this god
        BoonType[] blessings = BoonType.getRandomBlessings(god, 3);
        
        if (blessings.length == 0) {
            AfterlifeEntombedMod.LOGGER.warn("No blessings available for god: " + god);
            return;
        }
        
        AfterlifeEntombedMod.LOGGER.info("Player {} received blessing offer from {}", 
            player.getName().getString(), god.name());
        
        // Spawn particle effect at player location
        spawnBlessingParticle(player, god);
        
        // Send packet to open selection GUI on client
        GodAvatarPackets.sendToPlayer(player, new OpenBoonSelectionPacket(god, blessings, true));
    }
    
    /**
     * Trigger a random curse from a random god
     */
    private static void triggerCurse(ServerPlayer player) {
        GodType god = selectRandomGod();
        
        // Get 3 random curses from this god
        BoonType[] curses = BoonType.getRandomCurses(god, 3);
        
        if (curses.length == 0) {
            AfterlifeEntombedMod.LOGGER.warn("No curses available for god: " + god);
            return;
        }
        
        AfterlifeEntombedMod.LOGGER.info("Player {} received curse from {}", 
            player.getName().getString(), god.name());
        
        // Spawn particle effect at player location
        spawnCurseParticle(player, god);
        
        // Send packet to open selection GUI on client
        GodAvatarPackets.sendToPlayer(player, new OpenBoonSelectionPacket(god, curses, false));
    }
    
    /**
     * Select a random god (excluding NONE)
     */
    private static GodType selectRandomGod() {
        GodType[] gods = GodType.values();
        GodType selected;
        
        do {
            selected = gods[random.nextInt(gods.length)];
        } while (selected == GodType.NONE);
        
        return selected;
    }
    
    /**
     * Spawn god-specific particle effect for blessing
     */
    private static void spawnBlessingParticle(ServerPlayer player, GodType god) {
        if (!(player.level() instanceof ServerLevel level)) return;
        
        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();
        
        // Use AfterLibs API to spawn appropriate particle
        String particleName = getBlessingParticle(god);
        if (particleName != null) {
            com.dracolich777.afterlibs.api.AfterLibsAPI.spawnAfterlifeParticle(level, particleName, x, y, z, 1.0f);
        }
    }
    
    /**
     * Spawn god-specific particle effect for curse
     */
    private static void spawnCurseParticle(ServerPlayer player, GodType god) {
        if (!(player.level() instanceof ServerLevel level)) return;
        
        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();
        
        // Use AfterLibs API to spawn appropriate particle (darker/ominous versions)
        String particleName = getCurseParticle(god);
        if (particleName != null) {
            com.dracolich777.afterlibs.api.AfterLibsAPI.spawnAfterlifeParticle(level, particleName, x, y, z, 1.0f);
        }
    }
    
    /**
     * Get blessing particle name for each god
     */
    private static String getBlessingParticle(GodType god) {
        return switch (god) {
            case SETH -> "seth_fog";
            case RA -> "ra_halo";
            case HORUS -> "horus_shield";
            case SHU -> "shu_jump2";
            case ISIS -> "healing_burst";
            case GEB -> "earth_aura";
            case THOTH -> "spiral_up"; // Knowledge/wisdom flash
            case ANUBIS -> "anubis_nuke"; // Death energy
            default -> null;
        };
    }
    
    /**
     * Get curse particle name for each god
     */
    private static String getCurseParticle(GodType god) {
        // Use darker/ominous versions of particles for curses
        return switch (god) {
            case SETH -> "seth_fog";
            case RA -> "ra_column"; // Dark haze instead of bright halo
            case HORUS -> "shield_wall"; // Oppressive wall instead of protective shield
            case SHU -> "shu_jump2"; // Heavy air instead of light wind
            case ISIS -> "healing_burst"; // Corrupted magic
            case GEB -> "earth_aura"; // Crushing earth
            case THOTH -> "spiral_up"; // Confusion/forgetfulness
            case ANUBIS -> "blood_lance"; // Death curse
            default -> null;
        };
    }
}
