package com.dracolich777.afterlifeentombed.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RingOfRa extends Item implements ICurioItem {
    // Static map to track temporary magma blocks across all instances
    private static final Map<BlockPos, Long> temporaryMagmaBlocks = new HashMap<>();
    private static final int MAGMA_BLOCK_DURATION = 100; // 5 seconds in ticks
    
    public RingOfRa(Properties properties) {
        super(properties);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true; // Allow right-click equipping
    }
    
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        
        // Play lava pop sound when equipped
        entity.level().playSound(null, entity.blockPosition(), 
            SoundEvents.LAVA_POP, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        // Add fire resistance effect
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 
            Integer.MAX_VALUE, 0, false, false, true));
    }
    
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        
        // Remove fire resistance effect when unequipped
        entity.removeEffect(MobEffects.FIRE_RESISTANCE);
    }
    
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Level level = entity.level();
        
        // Refresh fire resistance effect every 20 ticks (1 second) to ensure it stays active
        if (level.getGameTime() % 20 == 0) {
            if (!entity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 
                    Integer.MAX_VALUE, 0, false, false, true));
            }
        }
        
        // Clean up expired temporary magma blocks
        cleanupExpiredMagmaBlocks(level);
        
        // Lava walking ability - create temporary magma blocks
        if (entity.onGround() || entity.isInLava()) {
            createLavaWalkingBlocks(entity, level);
        }
    }
    
    private void createLavaWalkingBlocks(LivingEntity entity, Level level) {
        if (level.isClientSide) return; // Only run on server side
        
        BlockPos entityPos = entity.blockPosition();
        int radius = 2; // Radius around the player to convert lava
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos checkPos = entityPos.offset(x, 0, z);
                BlockPos belowPos = checkPos.below();
                
                // Check if there's lava at or below the position
                BlockState currentState = level.getBlockState(checkPos);
                BlockState belowState = level.getBlockState(belowPos);
                
                // Convert lava source blocks to temporary magma blocks
                if (currentState.is(Blocks.LAVA) && currentState.getValue(net.minecraft.world.level.block.LiquidBlock.LEVEL) == 0) {
                    // Only convert if the magma block would be walkable
                    if (level.getBlockState(checkPos.above()).isAir() || 
                        level.getBlockState(checkPos.above()).canBeReplaced()) {
                        
                        createTemporaryMagmaBlock(level, checkPos);
                    }
                }
                // Also check one block below for deeper lava
                else if (belowState.is(Blocks.LAVA) && belowState.getValue(net.minecraft.world.level.block.LiquidBlock.LEVEL) == 0) {
                    if (currentState.isAir() || currentState.canBeReplaced()) {
                        createTemporaryMagmaBlock(level, belowPos);
                    }
                }
            }
        }
    }
    
    private void createTemporaryMagmaBlock(Level level, BlockPos pos) {
        // Only create if this position isn't already a temporary magma block
        if (temporaryMagmaBlocks.containsKey(pos)) {
            return;
        }
        
        // Set the block to magma
        level.setBlock(pos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        
        // Track this block with its creation time
        temporaryMagmaBlocks.put(pos, level.getGameTime());
        
        // Play a soft hissing sound
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3f, 2.0f);
        
        // Add some particle effects
        if (level.isClientSide) {
            for (int i = 0; i < 8; i++) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.LAVA,
                    pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8,
                    0.0, 0.1, 0.0);
            }
        }
    }
    
    public void cleanupExpiredMagmaBlocks(Level level) {
        if (level.isClientSide) return; // Only run on server side
        
        long currentTime = level.getGameTime();
        Iterator<Map.Entry<BlockPos, Long>> iterator = temporaryMagmaBlocks.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Long> entry = iterator.next();
            BlockPos pos = entry.getKey();
            long creationTime = entry.getValue();
            
            // Check if the block has expired
            if (currentTime - creationTime >= MAGMA_BLOCK_DURATION) {
                // Check if the block is still magma (hasn't been broken/replaced)
                if (level.getBlockState(pos).is(Blocks.MAGMA_BLOCK)) {
                    // Revert back to lava
                    level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
                    
                    // Play a soft bubble sound
                    level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.5f, 1.0f);
                }
                
                // Remove from tracking regardless
                iterator.remove();
            }
        }
    }
    
    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        // Allow equipping in charm or ring slots
        String slotId = slotContext.identifier();
        return slotId.equals("charm") || slotId.equals("ring");
    }
    
    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true; // Can always be unequipped
    }
    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.afterlifeentombed.ring_of_ra.tooltip").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.afterlifeentombed.ring_of_ra.effect").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.afterlifeentombed.ring_of_ra.lava_walking").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}