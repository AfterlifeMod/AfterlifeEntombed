package com.dracolich777.afterlifeentombed.items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.dracolich777.afterlifeentombed.init.ModEntityTypes;
import com.dracolich777.afterlifeentombed.init.ModItems;
import com.dracolich777.afterlifeentombed.mobs.ShabtiEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.Rarity;

public class ShabtiItem extends Item {

    private static final Map<UUID, ShabtiEntity> activeShabtis = new HashMap<>();

    public ShabtiItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            UUID playerUUID = player.getUUID();
            ShabtiEntity existingShabti = activeShabtis.get(playerUUID);

            if (existingShabti != null && existingShabti.isAlive()) {
                // Move existing shabti to where player is looking
                HitResult hitResult = player.pick(20.0D, 0.0F, false);
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    BlockPos pos = blockHitResult.getBlockPos().above();
                    existingShabti.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    player.sendSystemMessage(Component.literal("Shabti moved to new location"));
                } else {
                    // Move to position in front of player
                    Vec3 lookVec = player.getLookAngle();
                    Vec3 newPos = player.position().add(lookVec.scale(3.0));
                    existingShabti.teleportTo(newPos.x, newPos.y, newPos.z);
                    player.sendSystemMessage(Component.literal("Shabti moved to new location"));
                }
                
                return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
            } else {
                try {
                    // Create new shabti using proper entity creation
                    ShabtiEntity shabti = new ShabtiEntity(ModEntityTypes.SHABTI.get(), (ServerLevel) level);
                    Vec3 spawnPos = player.position().add(player.getLookAngle().scale(2.0));

                    // Validate spawn position
                    if (spawnPos.y < level.getMinBuildHeight() || spawnPos.y > level.getMaxBuildHeight()) {
                        spawnPos = new Vec3(spawnPos.x, player.getY(), spawnPos.z);
                    }

                    shabti.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

                    // Set the owner after creation
                    shabti.setOwner(serverPlayer);

                    if (level.addFreshEntity(shabti)) {
                        activeShabtis.put(playerUUID, shabti);
                        player.sendSystemMessage(Component.literal("Shabti summoned"));
                        
                        // Consume the item
                        if (!player.getAbilities().instabuild) {
                            itemStack.shrink(1);
                        }
                        
                        return InteractionResultHolder.consume(itemStack);
                    } else {
                        player.sendSystemMessage(Component.literal("Unable to summon Shabti"));
                        return InteractionResultHolder.fail(itemStack);
                    }
                } catch (Exception e) {
                    player.sendSystemMessage(Component.literal("Failed to create Shabti: " + e.getMessage()));
                    return InteractionResultHolder.fail(itemStack);
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    public static void removeShabti(UUID playerUUID) {
        activeShabtis.remove(playerUUID);
    }
    
    public static ShabtiEntity getActiveShabti(UUID playerUUID) {
        return activeShabtis.get(playerUUID);
    }
    
    // Method to give the player a shabti item when their shabti is broken
    public static void giveShabtiItem(ServerPlayer player) {
        ItemStack shabtiStack = new ItemStack(ModItems.SHABTI.get());
        if (!player.getInventory().add(shabtiStack)) {
            // If inventory is full, drop it at player's location
            player.drop(shabtiStack, false);
        }
    }

@Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }
}

