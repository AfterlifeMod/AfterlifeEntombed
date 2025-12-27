package com.dracolich777.afterlifeentombed.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.SlotContext; 
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import net.minecraft.sounds.SoundEvents; 
import net.minecraft.sounds.SoundSource;


import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class WandOfIsis extends Item implements ICurioItem {

    public static final String ASSIGNED_BLOCK_NBT_KEY = "AssignedBlock";

    private static final String INITIALIZED_NBT_KEY = "Initialized";

    public WandOfIsis(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        // Only run on the server side to prevent desync and ensure consistent random block assignment.
        if (!pLevel.isClientSide) {
            CompoundTag nbt = pStack.getOrCreateTag();

 
            if (!nbt.getBoolean(INITIALIZED_NBT_KEY)) {

                Block randomBlock = getRandomSurvivalPlacableBlock(pLevel.getRandom());
                if (randomBlock != null) {

                    nbt.putString(ASSIGNED_BLOCK_NBT_KEY, ForgeRegistries.BLOCKS.getKey(randomBlock).toString());
                    nbt.putBoolean(INITIALIZED_NBT_KEY, true);
                    pStack.setTag(nbt);
                }
            }
        }
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }


    private Block getRandomSurvivalPlacableBlock(RandomSource randomSource) { 
    List<Block> placableBlocks = ForgeRegistries.BLOCKS.getValues().stream()
        .filter(block -> block.asItem() != net.minecraft.world.item.Items.AIR) // Must have an item form
        .filter(this::isNotOperatorOnlyBlock) // Filter out operator-only blocks
        .filter(this::isNormalSurvivalBlock) // Filter for normal survival blocks
        .collect(Collectors.toList());

    if (placableBlocks.isEmpty()) {
        return null;
    }
    
    return placableBlocks.get(randomSource.nextInt(placableBlocks.size())); 
}

private boolean isNotOperatorOnlyBlock(Block block) {
    // Filter out all operator-only blocks
    return block != Blocks.BEDROCK &&
           block != Blocks.BARRIER &&
           block != Blocks.LIGHT &&
           block != Blocks.STRUCTURE_VOID &&
           block != Blocks.STRUCTURE_BLOCK &&
           block != Blocks.JIGSAW &&
           block != Blocks.COMMAND_BLOCK &&
           block != Blocks.CHAIN_COMMAND_BLOCK &&
           block != Blocks.REPEATING_COMMAND_BLOCK &&
           !block.getDescriptionId().contains("debug") && // Debug blocks
           !block.getDescriptionId().contains("test"); // Test blocks
}

private boolean isNormalSurvivalBlock(Block block) {
    // Only include blocks that are:
    // 1. Not replaceable (solid blocks like dirt, stone, etc.)
    // 2. Not portal blocks
    // 3. Not air-like blocks
    // 4. Not liquids
    // 5. Not plants/decorative blocks that can be replaced
    // 6. Not multi-block structures (beds, doors, etc.)
    
    return !block.defaultBlockState().canBeReplaced() && // This filters OUT snow, glow lichen, etc.
           block != Blocks.END_PORTAL &&
           block != Blocks.END_PORTAL_FRAME &&
           block != Blocks.NETHER_PORTAL &&
           block != Blocks.WATER &&
           block != Blocks.LAVA &&
           block != Blocks.BUBBLE_COLUMN &&
           block != Blocks.CAVE_AIR &&
           block != Blocks.VOID_AIR &&
           !block.getDescriptionId().contains("_air") &&
           // Optional: Filter out some problematic blocks
           block != Blocks.FIRE &&
           block != Blocks.SOUL_FIRE &&
           block != Blocks.MOVING_PISTON &&
           block != Blocks.PISTON_HEAD &&
           // Filter out multi-block structures
           !isTwoBlockStructure(block) &&
           // You can add more specific filters here if needed
           true;
}

private boolean isTwoBlockStructure(Block block) {
    // Filter out beds
    if (block == Blocks.RED_BED || block == Blocks.BLACK_BED || block == Blocks.BLUE_BED ||
        block == Blocks.BROWN_BED || block == Blocks.CYAN_BED || block == Blocks.GRAY_BED ||
        block == Blocks.GREEN_BED || block == Blocks.LIGHT_BLUE_BED || block == Blocks.LIGHT_GRAY_BED ||
        block == Blocks.LIME_BED || block == Blocks.MAGENTA_BED || block == Blocks.ORANGE_BED ||
        block == Blocks.PINK_BED || block == Blocks.PURPLE_BED || block == Blocks.WHITE_BED ||
        block == Blocks.YELLOW_BED) {
        return true;
    }
    // Filter op stuff
    if (block == Blocks.NETHERITE_BLOCK || block == Blocks.DRAGON_EGG || block == Blocks.BEDROCK) {
        return true;
    }
    
    // Filter out doors
    if (block == Blocks.ACACIA_DOOR || block == Blocks.BIRCH_DOOR || block == Blocks.DARK_OAK_DOOR ||
        block == Blocks.JUNGLE_DOOR || block == Blocks.OAK_DOOR || block == Blocks.SPRUCE_DOOR ||
        block == Blocks.CRIMSON_DOOR || block == Blocks.WARPED_DOOR || block == Blocks.IRON_DOOR) {
        return true;
    }
    
    // Filter out tall plants/flowers
    if (block == Blocks.TALL_GRASS || block == Blocks.LARGE_FERN || block == Blocks.SUNFLOWER ||
        block == Blocks.LILAC || block == Blocks.ROSE_BUSH || block == Blocks.PEONY ||
        block == Blocks.TALL_SEAGRASS || block == Blocks.PITCHER_PLANT) {
        return true;
    }
    
    // Filter out other multi-block structures
    if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || // Can be double chests
        block == Blocks.LECTERN || // Has book interaction
        block == Blocks.BELL || // Has complex placement
        block == Blocks.CONDUIT || // Requires water
        block == Blocks.END_ROD || // Has directional placement issues
        block == Blocks.LIGHTNING_ROD) { // Has directional placement
        return true;
    }
    
    // Use string matching for any remaining multi-block structures
    String blockName = block.getDescriptionId().toLowerCase();
    return blockName.contains("_bed") || 
           blockName.contains("_door") || 
           blockName.contains("tall_") ||
           blockName.contains("large_");
}
    public static Block getAssignedBlock(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains(ASSIGNED_BLOCK_NBT_KEY)) {
            String blockId = nbt.getString(ASSIGNED_BLOCK_NBT_KEY);
            ResourceLocation rl = ResourceLocation.tryParse(blockId);
            if (rl != null) {
                return ForgeRegistries.BLOCKS.getValue(rl);
            }
        }
        return null;
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return getAssignedBlock(pStack) != null;
    }



    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        // Only allow equipping from use if the slot is 'hands'
        return "hands".equals(slotContext.identifier());
    }
    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        // Only allow equipping in the 'hands' curios slot
        return "hands".equals(slotContext.identifier());
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();


        entity.level().playSound(null, entity.blockPosition(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
  
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();


        entity.level().playSound(null, entity.blockPosition(),
            SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);

    }
    @Override
public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("item.afterlifeentombed.wand_of_isis.tooltip").withStyle(ChatFormatting.DARK_PURPLE));

    Block assignedBlock = getAssignedBlock(stack);
    if (assignedBlock != null) {
        Component blockName = assignedBlock.getName().copy().withStyle(ChatFormatting.RED);
        tooltip.add(Component.literal("Block: ").append(blockName));
    } else {
        tooltip.add(Component.literal("No block assigned").withStyle(ChatFormatting.GRAY));
    }

    super.appendHoverText(stack, level, tooltip, flag);
}
}