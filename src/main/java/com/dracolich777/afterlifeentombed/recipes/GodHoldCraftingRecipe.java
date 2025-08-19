// package com.dracolich777.afterlifeentombed.recipes;

// import java.util.UUID;

// import com.dracolich777.afterlifeentombed.init.ModBlocks;
// import com.dracolich777.afterlifeentombed.init.ModRecipeTypes;
// import com.google.gson.JsonObject;

// import net.minecraft.core.NonNullList;
// import net.minecraft.core.RegistryAccess;
// import net.minecraft.nbt.CompoundTag;
// import net.minecraft.network.FriendlyByteBuf;
// import net.minecraft.resources.ResourceLocation;
// import net.minecraft.world.inventory.CraftingContainer;
// import net.minecraft.world.item.ItemStack;
// import net.minecraft.world.item.crafting.CraftingBookCategory;
// import net.minecraft.world.item.crafting.CraftingRecipe;
// import net.minecraft.world.item.crafting.Ingredient;
// import net.minecraft.world.item.crafting.RecipeSerializer;
// import net.minecraft.world.item.crafting.ShapedRecipe;
// import net.minecraft.world.level.Level;

// public class GodHoldCraftingRecipe implements CraftingRecipe {
//     private final ResourceLocation id;
//     private final ShapedRecipe baseRecipe;
    
//     public GodHoldCraftingRecipe(ResourceLocation id, ShapedRecipe baseRecipe) {
//         this.id = id;
//         this.baseRecipe = baseRecipe;
//     }
    
//     @Override
//     public boolean matches(CraftingContainer pContainer, Level pLevel) {
//         return baseRecipe.matches(pContainer, pLevel);
//     }
    
//     @Override
//     public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
//         // Create a shared inventory ID for both blocks
//         UUID sharedInventoryId = UUID.randomUUID();
        
//         // Create the first block with the shared inventory ID
//         ItemStack result = new ItemStack(ModBlocks.GODHOLD_ITEM.get(), 2);
//         CompoundTag tag = new CompoundTag();
//         tag.putString("SharedInventoryId", sharedInventoryId.toString());
//         result.setTag(tag);
        
//         return result;
//     }
    
//     @Override
//     public boolean canCraftInDimensions(int pWidth, int pHeight) {
//         return baseRecipe.canCraftInDimensions(pWidth, pHeight);
//     }
    
//     @Override
//     public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
//         return new ItemStack(ModBlocks.GODHOLD_ITEM.get(), 2);
//     }
    
//     @Override
//     public ResourceLocation getId() {
//         return id;
//     }
    
//     @Override
//     public RecipeSerializer<?> getSerializer() {
//         return ModRecipeTypes.GODHOLD_CRAFTING.get();
//     }
    
//     @Override
//     public CraftingBookCategory category() {
//         return CraftingBookCategory.MISC;
//     }
    
//     @Override
//     public NonNullList<Ingredient> getIngredients() {
//         return baseRecipe.getIngredients();
//     }
    
//     public static class Serializer implements RecipeSerializer<GodHoldCraftingRecipe> {
        
//         @Override
//         public GodHoldCraftingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
//             ShapedRecipe baseRecipe = RecipeSerializer.SHAPED_RECIPE.fromJson(pRecipeId, pJson);
//             return new GodHoldCraftingRecipe(pRecipeId, baseRecipe);
//         }
        
//         @Override
//         public GodHoldCraftingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
//             ShapedRecipe baseRecipe = RecipeSerializer.SHAPED_RECIPE.fromNetwork(pRecipeId, pBuffer);
//             return baseRecipe != null ? new GodHoldCraftingRecipe(pRecipeId, baseRecipe) : null;
//         }
        
//         @Override
//         public void toNetwork(FriendlyByteBuf pBuffer, GodHoldCraftingRecipe pRecipe) {
//             RecipeSerializer.SHAPED_RECIPE.toNetwork(pBuffer, pRecipe.baseRecipe);
//         }
//     }
// }
