package com.dracolich777.afterlifeentombed.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

public class ShuCurseBlock extends Block {

    public ShuCurseBlock() {
        // Hard block with maximum blast resistance but mineable with diamond+ tools
        super(BlockBehaviour.Properties.of()
                .strength(8.0f, Float.MAX_VALUE) // Harder than diamond block but not as hard as netherite
                .sound(net.minecraft.world.level.block.SoundType.STONE)
        );
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK; // Cannot be pushed by pistons
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        return false; // Don't drop when exploded
    }

    @Override
    public void onBlockExploded(BlockState state, net.minecraft.world.level.Level world, BlockPos pos, Explosion explosion) {
        // Override to prevent destruction from explosions - do nothing
    }

    @Override
    public float getExplosionResistance() {
        return Float.MAX_VALUE; // Maximum explosion resistance
    }
}