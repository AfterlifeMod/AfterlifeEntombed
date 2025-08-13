package com.dracolich777.afterlifeentombed.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

import com.dracolich777.afterlifeentombed.init.ModItems;

public class DusksandBlock extends FallingBlock {

    public DusksandBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.SAND));
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!world.isClientSide) {
            if (tool != null && tool.getItem() instanceof ShovelItem) {
                TieredItem tieredItem = (TieredItem) tool.getItem();
                if (tieredItem.getTier().getLevel() >= Tiers.DIAMOND.getLevel()) {
                    if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {
                        popResource(world, pos, new ItemStack(this));
                    } else {
                        int count = 0 + world.random.nextInt(2);
                        popResource(world, pos, new ItemStack(ModItems.DUSKSAND_CLUMP.get(), count));
                    }
                }
            }
        }
        super.playerDestroy(world, player, pos, state, blockEntity, tool);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        ItemStack tool = player.getMainHandItem();
        if (tool.getItem() instanceof ShovelItem) {
            TieredItem tieredItem = (TieredItem) tool.getItem();
            return tieredItem.getTier().getLevel() >= Tiers.DIAMOND.getLevel();
        }
        return false;
    }
}
