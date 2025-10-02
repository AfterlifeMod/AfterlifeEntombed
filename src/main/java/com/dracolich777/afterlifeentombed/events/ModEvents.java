package com.dracolich777.afterlifeentombed.events;
import com.dracolich777.afterlifeentombed.items.CloakOfKonshu;
import net.minecraftforge.event.entity.living.LivingDeathEvent;


import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
// import com.dracolich777.afterlifeentombed.capabilities.GodseekerSwordCapability;
import com.dracolich777.afterlifeentombed.init.ModEffects;
import com.dracolich777.afterlifeentombed.init.ModItems;
import com.dracolich777.afterlifeentombed.items.GodType;
// import com.dracolich777.afterlifeentombed.items.GodseekerSword;
import com.dracolich777.afterlifeentombed.items.GodstoneItem;
import com.dracolich777.afterlifeentombed.items.WandOfIsis; // Import your new item

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.items.IItemHandler;

// Curios API Imports
import top.theillusivec4.curios.api.CuriosApi; // Corrected import for CuriosApi
import top.theillusivec4.curios.api.SlotResult; // Used for iterating Curios slots

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = AfterlifeEntombedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        CloakOfKonshu.onPlayerDeath(event);
    }

    private static final Map<GodType, MobEffect> POSITIVE_EFFECTS = new EnumMap<>(GodType.class);
    private static final Map<GodType, MobEffect> REVENGE_EFFECTS = new EnumMap<>(GodType.class);

    // This method will be called during FMLCommonSetupEvent
    public static void init() {
        // Initialize maps here, AFTER all registries are populated
        POSITIVE_EFFECTS.put(GodType.RA, MobEffects.FIRE_RESISTANCE);
        POSITIVE_EFFECTS.put(GodType.HORUS, MobEffects.DAMAGE_RESISTANCE);
        POSITIVE_EFFECTS.put(GodType.THOTH, MobEffects.REGENERATION);
        POSITIVE_EFFECTS.put(GodType.SHU, MobEffects.SLOW_FALLING);
        POSITIVE_EFFECTS.put(GodType.GEB, MobEffects.DAMAGE_BOOST);
        POSITIVE_EFFECTS.put(GodType.ISIS, MobEffects.SATURATION);
        POSITIVE_EFFECTS.put(GodType.SETH, MobEffects.NIGHT_VISION);
        POSITIVE_EFFECTS.put(GodType.ANUBIS, MobEffects.ABSORPTION);

        REVENGE_EFFECTS.put(GodType.HORUS, ModEffects.REVENGE_OF_HORUS.get());
        REVENGE_EFFECTS.put(GodType.RA, ModEffects.HOLY_FIRE.get());
        REVENGE_EFFECTS.put(GodType.THOTH, ModEffects.REVENGE_OF_THOTH.get());
        REVENGE_EFFECTS.put(GodType.SHU, ModEffects.REVENGE_OF_SHU.get());
        REVENGE_EFFECTS.put(GodType.GEB, ModEffects.REVENGE_OF_GEB.get());
        REVENGE_EFFECTS.put(GodType.ISIS, ModEffects.REVENGE_OF_ISIS.get());
        REVENGE_EFFECTS.put(GodType.SETH, ModEffects.REVENGE_OF_SETH.get());
        REVENGE_EFFECTS.put(GodType.ANUBIS, ModEffects.REVENGE_OF_ANUBIS.get());
    }

    // Your existing event handlers (onPlayerTick, onLivingDamage)
    // @SubscribeEvent
    // public void onPlayerTick(TickEvent.PlayerTickEvent event) {
    //     if (event.phase == TickEvent.Phase.END && event.player != null && !event.player.level().isClientSide) {
    //         Player player = event.player;
    //         ItemStack mainHandStack = player.getMainHandItem();

    //         if (mainHandStack.getItem() instanceof GodseekerSword) {
    //             mainHandStack.getCapability(GodseekerSwordCapability.GODSTONE_INVENTORY_CAPABILITY).ifPresent(handler -> {
    //                 ItemStack godstoneInSlot = handler.getStackInSlot(0); // Get item from the single slot

    //                 if (!godstoneInSlot.isEmpty() && godstoneInSlot.getItem() instanceof GodstoneItem godstone) {
    //                     GodType godType = godstone.getGodType();

    //                     MobEffect positiveEffect = POSITIVE_EFFECTS.get(godType);
    //                     if (positiveEffect != null) {
    //                         player.addEffect(new MobEffectInstance(positiveEffect, 20 * 2, 0, false, false));
    //                     }
    //                 }
    //             });
    //         }
    //     }
    // }

    // @SubscribeEvent
    // public void onLivingDamage(LivingDamageEvent event) {
    //     if (event.getSource().getEntity() instanceof Player player) {
    //         ItemStack mainHandStack = player.getMainHandItem();

    //         if (mainHandStack.getItem() instanceof GodseekerSword) {
    //             mainHandStack.getCapability(GodseekerSwordCapability.GODSTONE_INVENTORY_CAPABILITY).ifPresent(handler -> {
    //                 ItemStack godstoneInSlot = handler.getStackInSlot(0);

    //                 if (!godstoneInSlot.isEmpty() && godstoneInSlot.getItem() instanceof GodstoneItem godstone) {
    //                     LivingEntity target = event.getEntity();
    //                     GodType godType = godstone.getGodType();

    //                     MobEffect revengeEffect = REVENGE_EFFECTS.get(godType);
    //                     if (revengeEffect != null) {
    //                         target.addEffect(new MobEffectInstance(revengeEffect, 20 * 10, 1, false, true));
    //                     }
    //                 }
    //             });
    //         }
    //     }
    // }


    @SubscribeEvent
public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
    if (!(event.getEntity() instanceof Player player) || event.getLevel().isClientSide()) return;

    // Check if the player has a Wand of Isis equipped
    boolean hasWand = CuriosApi.getCuriosHelper()
        .findEquippedCurio(ModItems.WAND_OF_ISIS.get(), player)
        .isPresent();

    if (!hasWand) return;

    // Get the equipped Wand of Isis
    ItemStack wandStack = CuriosApi.getCuriosHelper()
    .findEquippedCurio(ModItems.WAND_OF_ISIS.get(), player)
    .get().getRight();

    // Get assigned block from that specific wand
    Block assignedBlock = WandOfIsis.getAssignedBlock(wandStack);
    if (assignedBlock == null) return;

    // Don't cancel the event, just replace the block state in the event
    BlockPos pos = event.getPos();
    Level level = (Level) event.getLevel();
    BlockState newState = assignedBlock.defaultBlockState();

    // Set the new block state in the event so it places the assigned block instead
    event.getLevel().setBlock(pos, newState, 3);

    // Play sound for the assigned block
    level.playSound(null, pos,
        newState.getSoundType().getPlaceSound(),
        SoundSource.BLOCKS,
        (newState.getSoundType().getVolume() + 1.0F) / 2.0F,
        newState.getSoundType().getPitch() * 0.8F
    );
}
@SubscribeEvent
public static void onLivingDamageCollar(LivingDamageEvent event) {
    // Check if the damage source is from a player
    if (event.getSource().getEntity() instanceof Player player) {
        // Check if the player has a Collar of Anubis equipped
        boolean hasCollar = CuriosApi.getCuriosHelper()
            .findEquippedCurio(ModItems.COLLAR_OF_ANUBIS.get(), player)
            .isPresent();

        if (hasCollar) {
            LivingEntity target = event.getEntity();
            
            // Don't apply to the player themselves
            if (target != player && !target.level().isClientSide) {
                // Apply Judged Unworthy effect to the target
                target.addEffect(new MobEffectInstance(
                    ModEffects.JUDGED_UNWORTHY.get(),
                    1, // 1 tick duration to trigger the conversion
                    0, // Level 0 (level I)
                    false, // Not ambient
                    true,  // Show particles
                    true   // Show icon
                ));
                
                // Play a sound effect to indicate the judgment
                target.level().playSound(null, target.blockPosition(),
                    SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5f, 2.0f);
            }
        }
    }
}
}
