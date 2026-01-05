package com.dracolich777.afterlifeentombed.recipes;

import com.dracolich777.afterlifeentombed.AfterlifeEntombedMod;
import com.dracolich777.afterlifeentombed.init.ModRecipeTypes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Map;

public class GodforgeRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Ingredient input1;
    private final Ingredient input2;
    private final Ingredient input3; // Optional third input
    private final boolean requiresFuel;
    private final Ingredient fuel;
    private final ItemStack output;
    private final int cookTime;
    
    public GodforgeRecipe(ResourceLocation id, Ingredient input1, Ingredient input2, Ingredient input3, boolean requiresFuel, Ingredient fuel, ItemStack output, int cookTime) {
        this.id = id;
        this.input1 = input1;
        this.input2 = input2;
        this.input3 = input3;
        this.requiresFuel = requiresFuel;
        this.fuel = fuel;
        this.output = output;
        this.cookTime = cookTime;
    }
    
    @Override
    public boolean matches(@Nonnull SimpleContainer pContainer, @Nonnull Level pLevel) {
        if (pLevel.isClientSide()) {
            return false;
        }
        
        ItemStack fuelSlot = pContainer.getItem(0);          // Fuel slot
        ItemStack targetItem = pContainer.getItem(1);        // Red slot: item to be modified
        ItemStack slot2 = pContainer.getItem(2);             // Purple top
        ItemStack slot3 = pContainer.getItem(3);             // Purple bottom
        
        // Check for enchantment transfer recipe (doesn't require fuel)
        // Check both orderings of book and enchanted source
        if (isEnchantmentTransferRecipe(targetItem, slot2, slot3) || 
            isEnchantmentTransferRecipe(targetItem, slot3, slot2)) {
            return true;
        }
        
        // Check for regular crafting recipes
        boolean fuelCheck = !requiresFuel || (requiresFuel && fuel.test(fuelSlot));
        
        if (fuelCheck) {
            // Check if third input is required
            boolean hasThirdInput = !input3.isEmpty();
            
            if (hasThirdInput) {
                // 3-input recipe: check all three slots
                boolean input3Match = input3.test(targetItem);
                boolean purpleMatch = (input1.test(slot2) && input2.test(slot3)) ||
                                      (input1.test(slot3) && input2.test(slot2));
                return input3Match && purpleMatch;
            } else {
                // 2-input recipe: only check purple slots, red slot must be empty
                boolean redEmpty = targetItem.isEmpty();
                boolean purpleMatch = (input1.test(slot2) && input2.test(slot3)) ||
                                      (input1.test(slot3) && input2.test(slot2));
                return redEmpty && purpleMatch;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if this is an enchantment transfer recipe:
     * - targetItem: Item to receive the enchantment (must not be empty)
     * - bookStack: Named book matching an enchantment name
     * - sourceEnchanted: Enchanted item to consume
     */
    private boolean isEnchantmentTransferRecipe(ItemStack targetItem, ItemStack bookStack, ItemStack sourceEnchanted) {
        // Target item must exist
        if (targetItem.isEmpty()) {
            return false;
        }
        
        // Book must be a named book
        if (!bookStack.is(Items.BOOK) || !bookStack.hasCustomHoverName()) {
            return false;
        }
        
        // Source must be enchanted
        if (sourceEnchanted.isEmpty() || !sourceEnchanted.isEnchanted()) {
            return false;
        }
        
        // Get the book's name (case insensitive)
        String bookName = bookStack.getHoverName().getString().toLowerCase().trim();
        
        // Get enchantments on source item
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(sourceEnchanted);
        
        // Check if any enchantment name matches the book name (case insensitive)
        for (Enchantment enchant : enchantments.keySet()) {
            String enchantName = enchant.getFullname(enchantments.get(enchant)).getString().toLowerCase();
            // Match enchantment name (just the name, not the level)
            String[] parts = enchantName.split(" ");
            String enchantNameOnly = parts[0];
            
            if (enchantNameOnly.equals(bookName) || enchantName.contains(bookName)) {
                // Verify the enchantment can be applied to the target
                if (enchant.canEnchant(targetItem) || targetItem.is(Items.BOOK) || targetItem.is(Items.ENCHANTED_BOOK)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public @Nonnull ItemStack assemble(@Nonnull SimpleContainer pContainer, @Nonnull RegistryAccess pRegistryAccess) {
        ItemStack targetItem = pContainer.getItem(1);        // Red slot: item to be modified
        ItemStack slot2 = pContainer.getItem(2);             // Purple top
        ItemStack slot3 = pContainer.getItem(3);             // Purple bottom
        
        // Handle enchantment transfer - check both orderings
        if (isEnchantmentTransferRecipe(targetItem, slot2, slot3)) {
            return performEnchantmentTransfer(targetItem, slot2, slot3);
        } else if (isEnchantmentTransferRecipe(targetItem, slot3, slot2)) {
            return performEnchantmentTransfer(targetItem, slot3, slot2);
        }
        
        // Handle normal recipe
        return output.copy();
    }
    
    /**
     * Transfers an enchantment from the source enchanted item to the target item
     * Preserves all NBT data on the target item except adds the enchantment
     */
    private ItemStack performEnchantmentTransfer(ItemStack targetItem, ItemStack bookStack, ItemStack sourceEnchanted) {
        String bookName = bookStack.getHoverName().getString().toLowerCase().trim();
        Map<Enchantment, Integer> sourceEnchantments = EnchantmentHelper.getEnchantments(sourceEnchanted);
        
        // Find the matching enchantment
        for (Map.Entry<Enchantment, Integer> entry : sourceEnchantments.entrySet()) {
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();
            String enchantName = enchant.getFullname(level).getString().toLowerCase();
            String[] parts = enchantName.split(" ");
            String enchantNameOnly = parts[0];
            
            if (enchantNameOnly.equals(bookName) || enchantName.contains(bookName)) {
                // Check if enchantment can be applied to target item
                if (enchant.canEnchant(targetItem) || targetItem.is(Items.BOOK) || targetItem.is(Items.ENCHANTED_BOOK)) {
                    ItemStack result = targetItem.copy(); // Preserves all NBT including durability
                    
                    if (result.is(Items.BOOK)) {
                        // Convert book to enchanted book
                        result = new ItemStack(Items.ENCHANTED_BOOK);
                    }
                    
                    // Add the enchantment to the result (maintains existing enchantments)
                    Map<Enchantment, Integer> resultEnchantments = EnchantmentHelper.getEnchantments(result);
                    resultEnchantments.put(enchant, level);
                    EnchantmentHelper.setEnchantments(resultEnchantments, result);
                    
                    return result;
                }
            }
        }
        
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }
    
    @Override
    public @Nonnull ItemStack getResultItem(@Nonnull RegistryAccess pRegistryAccess) {
        return output.copy();
    }
    
    @Override
    public @Nonnull ResourceLocation getId() {
        return id;
    }
    
    @Override
    public @Nonnull RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.GODFORGE_SERIALIZER.get();
    }
    
    @Override
    public @Nonnull RecipeType<?> getType() {
        return ModRecipeTypes.GODFORGE_RECIPE_TYPE.get();
    }
    
    public int getCookTime() {
        return cookTime;
    }
    
    public boolean requiresFuel() {
        return requiresFuel;
    }
    
    /**
     * Gets the level of the enchantment being transferred (for XP cost calculation)
     * Returns 0 if not an enchantment transfer
     */
    public int getEnchantmentLevel(SimpleContainer container) {
        ItemStack slot2 = container.getItem(2);
        ItemStack slot3 = container.getItem(3);
        
        // Try slot2 as book, slot3 as enchanted
        ItemStack bookStack = slot2.is(Items.BOOK) && slot2.hasCustomHoverName() ? slot2 : null;
        ItemStack sourceEnchanted = slot3.isEnchanted() ? slot3 : null;
        
        // If not found, try reversed
        if (bookStack == null || sourceEnchanted == null) {
            bookStack = slot3.is(Items.BOOK) && slot3.hasCustomHoverName() ? slot3 : null;
            sourceEnchanted = slot2.isEnchanted() ? slot2 : null;
        }
        
        if (bookStack == null || sourceEnchanted == null) {
            return 0;
        }
        
        String bookName = bookStack.getHoverName().getString().toLowerCase().trim();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(sourceEnchanted);
        
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();
            String enchantName = enchant.getFullname(level).getString().toLowerCase();
            String[] parts = enchantName.split(" ");
            String enchantNameOnly = parts[0];
            
            if (enchantNameOnly.equals(bookName) || enchantName.contains(bookName)) {
                return level;
            }
        }
        
        return 0;
    }
    
    public static class Serializer implements RecipeSerializer<GodforgeRecipe> {
        @Override
        public @Nonnull GodforgeRecipe fromJson(@Nonnull ResourceLocation pRecipeId, @Nonnull JsonObject pJson) {
            Ingredient input1 = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "input1"));
            Ingredient input2 = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "input2"));
            
            // Input3 is optional for 3-ingredient recipes
            Ingredient input3 = pJson.has("input3") ? 
                Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "input3")) : 
                Ingredient.EMPTY;
            
            // Fuel requirement is optional - defaults to false if not specified
            boolean requiresFuel = GsonHelper.getAsBoolean(pJson, "requiresfuel", false);
            
            // Fuel ingredient is only used if requiresFuel is true
            Ingredient fuel = requiresFuel && pJson.has("fuel") ? 
                Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "fuel")) : 
                Ingredient.EMPTY;
            
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
            
            // Cook time defaults to 200 ticks (10 seconds) if not specified
            int cookTime = GsonHelper.getAsInt(pJson, "cookingtime", 200);
            
            return new GodforgeRecipe(pRecipeId, input1, input2, input3, requiresFuel, fuel, output, cookTime);
        }
        
        @Override
        public @Nullable GodforgeRecipe fromNetwork(@Nonnull ResourceLocation pRecipeId, @Nonnull FriendlyByteBuf pBuffer) {
            Ingredient input1 = Ingredient.fromNetwork(pBuffer);
            Ingredient input2 = Ingredient.fromNetwork(pBuffer);
            Ingredient input3 = Ingredient.fromNetwork(pBuffer);
            boolean requiresFuel = pBuffer.readBoolean();
            Ingredient fuel = Ingredient.fromNetwork(pBuffer);
            ItemStack output = pBuffer.readItem();
            int cookTime = pBuffer.readInt();
            
            return new GodforgeRecipe(pRecipeId, input1, input2, input3, requiresFuel, fuel, output, cookTime);
        }
        
        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf pBuffer, @Nonnull GodforgeRecipe pRecipe) {
            pRecipe.input1.toNetwork(pBuffer);
            pRecipe.input2.toNetwork(pBuffer);
            pRecipe.input3.toNetwork(pBuffer);
            pBuffer.writeBoolean(pRecipe.requiresFuel);
            pRecipe.fuel.toNetwork(pBuffer);
            pBuffer.writeItem(pRecipe.output);
            pBuffer.writeInt(pRecipe.cookTime);
        }
    }
}
