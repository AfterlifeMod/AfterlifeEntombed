package com.dracolich777.afterlifeentombed.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DusksteelBlock extends Block {

    public DusksteelBlock() {
        // Custom properties similar to netherite but with reasonable mining time
        super(BlockBehaviour.Properties.of()
                .strength(10.0f, 1200.0f) // Hardness of 10 (between diamond block 5 and netherite 50), max blast resistance
                .sound(net.minecraft.world.level.block.SoundType.NETHERITE_BLOCK));
    }
}
