package com.dracolich777.afterlifeentombed.items;


import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class CloakOfKonshu extends Item implements ICurioItem {
    // Used to track last tick's position for each player
    private static final java.util.Map<UUID, Double> lastZ = new java.util.HashMap<>();
    private static final java.util.Map<UUID, Double> lastX = new java.util.HashMap<>();

    public CloakOfKonshu(Properties properties) {
        super(properties);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        // Only allow equipping in the back slot
        return slotContext.identifier().equals("back");
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        // Only allow equipping in the back slot
        return slotContext.identifier().equals("back");
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        // Prevent manual unequipping
        return false;
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        // Prevent removal by re-equipping if possible (safety net, but main logic is in canUnequip)
        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player && !player.isCreative()) {
            // Re-equip the item if not creative (should not be possible, but extra safety)
            // (No-op: Curios should block removal, but this is a fallback)
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!(entity instanceof Player player) || entity.level().isClientSide) return;

        UUID uuid = player.getUUID();
        double prevX = lastX.getOrDefault(uuid, player.getX());
        double prevZ = lastZ.getOrDefault(uuid, player.getZ());
        double dx = player.getX() - prevX;
        double dz = player.getZ() - prevZ;
        double forward = 0;
        double backward = 0;

        // Calculate movement direction relative to look vector
        double lookX = -Math.sin(Math.toRadians(player.getYRot()));
        double lookZ = Math.cos(Math.toRadians(player.getYRot()));
        double dot = dx * lookX + dz * lookZ;

        if (dot > 0.08) {
            forward = dot;
        } else if (dot < -0.08) {
            backward = -dot;
        }

        // Only adjust time if player is on ground and not sneaking
        if (player.onGround() && !player.isCrouching()) {
            if (forward > 0) {
                addDaylightTime(player.level(), 2); // Add 2 ticks per step forward
            } else if (backward > 0) {
                addDaylightTime(player.level(), -2); // Subtract 2 ticks per step backward
            }
        }

        lastX.put(uuid, player.getX());
        lastZ.put(uuid, player.getZ());
    }

    private void addDaylightTime(Level level, int ticks) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        long dayTime = serverLevel.getDayTime();
        long newTime = Math.max(0, dayTime + ticks + 10);
        serverLevel.setDayTime(newTime);
    }

    // Prevent removal on death: re-add the item to the player if lost
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        // Check if the player had the cloak equipped
        top.theillusivec4.curios.api.CuriosApi.getCuriosHelper().findEquippedCurio(
            net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                new net.minecraft.resources.ResourceLocation("afterlifeentombed:cloak_of_konshu")
            ), player
        ).ifPresent(slot -> {
            // Re-add the cloak to the player after death
            player.getInventory().add(slot.getRight().copy());
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("A mystical cloak that binds to your soul.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Cannot be removed once equipped, even by death.").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("Wearing it lets you bend the day: step forward to speed up, back to slow down.").withStyle(ChatFormatting.AQUA));
    }
}
