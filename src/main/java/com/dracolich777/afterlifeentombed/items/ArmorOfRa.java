package com.dracolich777.afterlifeentombed.items;

import com.dracolich777.afterlifeentombed.client.model.ArmorOfRaModel;
import com.dracolich777.afterlifeentombed.client.model.ModModelLayers;
import com.dracolich777.afterlifeentombed.init.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.function.Consumer;

public class ArmorOfRa extends ArmorItem {
    
    public ArmorOfRa(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide && hasFullSet(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, false, false, false));
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (hasFullSet(player)) {
                attacker.addEffect(new MobEffectInstance(ModEffects.HOLY_FIRE.get(), 200, 0));
            }
        }
    }

    private boolean hasFullSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ArmorOfRa &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorOfRa &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ArmorOfRa &&
               player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ArmorOfRa;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        tooltip.add(Component.translatable("item.afterlifeentombed.armor_of_ra.tooltip.line1")
            .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.afterlifeentombed.armor_of_ra.tooltip.set_bonus")
            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("item.afterlifeentombed.armor_of_ra.tooltip.fire_resistance")
            .withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("item.afterlifeentombed.armor_of_ra.tooltip.holy_fire")
            .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.afterlifeentombed.armor_of_ra.tooltip.unbreakable")
            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
                ModelPart modelPart = entityModels.bakeLayer(ModModelLayers.getArmorLayer(equipmentSlot));
                return new ArmorOfRaModel(modelPart);
            }
        });
    }
}