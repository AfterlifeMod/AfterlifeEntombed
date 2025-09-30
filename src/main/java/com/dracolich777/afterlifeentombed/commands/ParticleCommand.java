package com.dracolich777.afterlifeentombed.commands;

import com.dracolich777.afterlifeentombed.network.NetworkHandler;
import com.dracolich777.afterlifeentombed.network.ParticleEffectPacket;
import com.dracolich777.afterlifeentombed.util.ParticleManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class ParticleCommand {
    
    // Suggestion provider for particle names
    private static final SuggestionProvider<CommandSourceStack> PARTICLE_SUGGESTIONS = (context, builder) -> {
        ParticleManager.getAvailableParticles().forEach(builder::suggest);
        return builder.buildFuture();
    };
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("afterlife_particle")
                .requires(source -> source.hasPermission(2)) // Require OP level 2
                .then(Commands.argument("particle", StringArgumentType.string())
                    .suggests(PARTICLE_SUGGESTIONS)
                    .executes(context -> spawnParticle(context, null, 1.0f, 1.0f, 1.0f)) // Default at player position with scale 1
                    .then(Commands.argument("position", Vec3Argument.vec3())
                        .executes(context -> spawnParticle(context, 
                            Vec3Argument.getVec3(context, "position"), 1.0f, 1.0f, 1.0f)) // Specified position with scale 1
                        .then(Commands.argument("scale", FloatArgumentType.floatArg(0.1f, 10.0f))
                            .executes(context -> {
                                float scale = FloatArgumentType.getFloat(context, "scale");
                                return spawnParticle(context, 
                                    Vec3Argument.getVec3(context, "position"), scale, scale, scale);
                            }) // Uniform scale
                            .then(Commands.argument("scaleY", FloatArgumentType.floatArg(0.1f, 10.0f))
                                .then(Commands.argument("scaleZ", FloatArgumentType.floatArg(0.1f, 10.0f))
                                    .executes(context -> {
                                        float scaleX = FloatArgumentType.getFloat(context, "scale");
                                        float scaleY = FloatArgumentType.getFloat(context, "scaleY");
                                        float scaleZ = FloatArgumentType.getFloat(context, "scaleZ");
                                        return spawnParticle(context, 
                                            Vec3Argument.getVec3(context, "position"), scaleX, scaleY, scaleZ);
                                    }) // Individual X, Y, Z scale
                                )
                            )
                        )
                    )
                )
        );
    }
    
    private static int spawnParticle(CommandContext<CommandSourceStack> context, Vec3 position, 
                                   float scaleX, float scaleY, float scaleZ) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String particleName = StringArgumentType.getString(context, "particle");
        
        // Check if particle exists
        if (!ParticleManager.hasParticle(particleName)) {
            source.sendFailure(Component.literal("Unknown particle: " + particleName + 
                ". Available particles: " + ParticleManager.getAvailableParticles()));
            return 0;
        }
        
        // Determine position
        Vec3 spawnPos;
        if (position != null) {
            spawnPos = position;
        } else {
            // Use command sender's position
            spawnPos = source.getPosition();
        }
        
        try {
            // Create and send particle packet to nearby players
            ParticleEffectPacket packet = new ParticleEffectPacket(
                spawnPos.x, spawnPos.y, spawnPos.z, particleName, scaleX, scaleY, scaleZ
            );
            
            // Send to all players in the dimension (for now - could be optimized to nearby players)
            if (source.getLevel() != null) {
                NetworkHandler.INSTANCE.send(
                    PacketDistributor.DIMENSION.with(() -> source.getLevel().dimension()), 
                    packet
                );
            }
            
            // Send success message
            source.sendSuccess(() -> Component.literal(String.format(
                "Spawned particle '%s' at (%.2f, %.2f, %.2f) with scale (%.2f, %.2f, %.2f)", 
                particleName, spawnPos.x, spawnPos.y, spawnPos.z, scaleX, scaleY, scaleZ
            )), true);
            
            return 1;
            
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to spawn particle: " + e.getMessage()));
            return 0;
        }
    }
}