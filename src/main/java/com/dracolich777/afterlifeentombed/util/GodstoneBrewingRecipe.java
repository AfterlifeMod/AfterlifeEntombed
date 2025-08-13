package com.dracolich777.afterlifeentombed.util;

import com.dracolich777.afterlifeentombed.init.ModItems;
import com.dracolich777.afterlifeentombed.util.ModPotions;
import com.dracolich777.afterlifeentombed.items.GodType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public class GodstoneBrewingRecipe implements IBrewingRecipe {

    private final Item godstone;
    private final Potion outputPotion;

    public GodstoneBrewingRecipe(Item godstone, Potion outputPotion) {
        this.godstone = godstone;
        this.outputPotion = outputPotion;
    }

    @Override
    public boolean isInput(ItemStack input) {
        return input.getItem() == Items.POTION &&
               PotionUtils.getPotion(input) == net.minecraft.world.item.alchemy.Potions.AWKWARD;
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return ingredient.getItem() == godstone;
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (isInput(input) && isIngredient(ingredient)) {
            return PotionUtils.setPotion(new ItemStack(Items.POTION), outputPotion);
        }
        return ItemStack.EMPTY;
    }

    public static void registerAll() {
        for (GodType god : GodType.values()) {
            Item godstoneItem = getGodstoneFor(god);
            Potion resultPotion = getPotionForGod(god);

            if (godstoneItem != null && resultPotion != null) {
                BrewingRecipeRegistry.addRecipe(new GodstoneBrewingRecipe(godstoneItem, resultPotion));
            }
        }
    }

    // Maps GodType -> Godstone Item
    private static Item getGodstoneFor(GodType god) {
        switch (god) {
            case RA: return ModItems.GODSTONE_OF_RA.get();
            case ANUBIS: return ModItems.GODSTONE_OF_ANUBIS.get();
            case ISIS: return ModItems.GODSTONE_OF_ISIS.get();
            case SETH: return ModItems.GODSTONE_OF_SETH.get();
            case GEB: return ModItems.GODSTONE_OF_GEB.get();
            case HORUS: return ModItems.GODSTONE_OF_HORUS.get();
            case THOTH: return ModItems.GODSTONE_OF_THOTH.get();
            case SHU: return ModItems.GODSTONE_OF_SHU.get();
            case NONE: return null;
            default: return null;
        }
    }

    // Maps GodType -> Output Potion
    private static Potion getPotionForGod(GodType god) {
        switch (god) {
            case RA:
                return ModPotions.HOLY_FIRE.get();
            case ANUBIS:
                return ModPotions.REVENGE_OF_ANUBIS.get();
            case ISIS:
                return ModPotions.REVENGE_OF_ISIS.get();
            case SETH:
                return ModPotions.REVENGE_OF_SETH.get();
            case GEB:
                return ModPotions.REVENGE_OF_GEB.get();
            case HORUS:
                return ModPotions.REVENGE_OF_HORUS.get();
            case THOTH:
                return ModPotions.REVENGE_OF_THOTH.get();
            case SHU:
                return ModPotions.REVENGE_OF_SHU.get();
            case NONE:
                return null;
            default:
                return null;
        }
    }
}
