package com.dracolich777.afterlifeentombed.items;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import com.dracolich777.afterlifeentombed.init.ModItems;

import java.util.EnumMap;
import java.util.function.Supplier;

public enum RaArmorMaterial implements ArmorMaterial {
    RA("ra", 50, Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 6);      // Higher than netherite (3)
        map.put(ArmorItem.Type.LEGGINGS, 9);   // Higher than netherite (6)
        map.put(ArmorItem.Type.CHESTPLATE, 11); // Higher than netherite (8)
        map.put(ArmorItem.Type.HELMET, 6);     // Higher than netherite (3)
    }), 25, SoundEvents.ARMOR_EQUIP_NETHERITE, 5.0F, 0.2F, RaArmorMaterial::getRepairIngredientStatic);

    private final String name;
    private final int durabilityMultiplier;
    private final EnumMap<ArmorItem.Type, Integer> protectionAmounts;
    private final int enchantability;
    private final SoundEvent sound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    RaArmorMaterial(String name, int durabilityMultiplier, EnumMap<ArmorItem.Type, Integer> protectionAmounts, 
                    int enchantability, SoundEvent sound, float toughness, float knockbackResistance, 
                    Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionAmounts = protectionAmounts;
        this.enchantability = enchantability;
        this.sound = sound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        // Base durability values for each armor type
        return switch (type) {
            case BOOTS -> 13 * this.durabilityMultiplier;
            case LEGGINGS -> 15 * this.durabilityMultiplier;
            case CHESTPLATE -> 16 * this.durabilityMultiplier;
            case HELMET -> 11 * this.durabilityMultiplier;
        };
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return this.protectionAmounts.get(type);
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.sound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public String getName() {
        return AfterlifeEntombedMod.MOD_ID + ":" + this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    private static Ingredient getRepairIngredientStatic() {
        return Ingredient.of(ModItems.GODSTEEL_INGOT_OF_RA.get());
    }
}