package com.dracolich777.afterlibs.commands;

import com.dracolich777.afterlibs.api.AfterLibsAPI;
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
import net.minecraft.world.phys.Vec3;

/**
 * The /aparticle command for spawning AAA particles
 */
public class AParticleCommand {
    
    // Suggestion provider for particle names
    private static final SuggestionProvider<CommandSourceStack> PARTICLE_SUGGESTIONS = (context, builder) -> {
        AfterLibsAPI.getAvailableParticles().forEach(builder::suggest);
        return builder.buildFuture();
    };
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("aparticle")
                .requires(source -> source.hasPermission(2)) // Require OP level 2
                .then(Commands.argument("particle", StringArgumentType.string())
                    .suggests(PARTICLE_SUGGESTIONS)
                    .then(Commands.argument("position", Vec3Argument.vec3())
                        .executes(context -> {
                            return spawnParticle(context, Vec3Argument.getVec3(context, "position"), 1.0f);
                        })
                        .then(Commands.argument("scale", FloatArgumentType.floatArg(0.1f, 10.0f))
                            .executes(context -> {
                                float scale = FloatArgumentType.getFloat(context, "scale");
                                return spawnParticle(context, Vec3Argument.getVec3(context, "position"), scale);
                            })
                        )
                    )
                )
        );
    }
    
    private static int spawnParticle(CommandContext<CommandSourceStack> context, Vec3 position, float scale) 
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String particleName = StringArgumentType.getString(context, "particle");
        
        // Check if particle exists using the API
        if (!AfterLibsAPI.isParticleAvailable(particleName)) {
            source.sendFailure(Component.literal("Unknown particle: " + particleName + 
                ". Available particles: " + String.join(", ", AfterLibsAPI.getAvailableParticles())));
            return 0;
        }
        
        try {
            // Use the public API to spawn the particle
            boolean success = AfterLibsAPI.spawnAfterlifeParticle(
                source.getLevel(), particleName, 
                position.x, position.y, position.z, 
                scale
            );
            
            if (success) {
                source.sendSuccess(() -> Component.literal(
                    String.format("Spawned particle '%s' at (%.2f, %.2f, %.2f) with scale %.2f", 
                        particleName, position.x, position.y, position.z, scale)), true);
                return 1;
            } else {
                source.sendFailure(Component.literal("Failed to spawn particle: " + particleName));
                return 0;
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to spawn particle: " + e.getMessage()));
            return 0;
        }
    }
}
