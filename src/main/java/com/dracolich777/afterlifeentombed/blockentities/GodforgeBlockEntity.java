package com.dracolich777.afterlifeentombed.blockentities;

import com.dracolich777.afterlifeentombed.init.ModBlockEntities;
import com.dracolich777.afterlifeentombed.init.ModRecipeTypes;
import com.dracolich777.afterlifeentombed.menu.GodforgeMenu;
import com.dracolich777.afterlifeentombed.recipes.GodforgeRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class GodforgeBlockEntity extends BlockEntity implements MenuProvider {
    // Slot indices
    public static final int FUEL_SLOT = 0;              // Top-left fuel slot (blaze powder, ignored for now)
    public static final int TARGET_ITEM_SLOT = 1;       // Red slot: Item to be modified (receives enchantment)
    public static final int BOOK_SLOT = 2;              // Purple slot top: Named book with enchantment name
    public static final int SOURCE_ENCHANTED_SLOT = 3;  // Purple slot bottom: Enchanted item to consume
    public static final int OUTPUT_SLOT = 4;            // Gold slot: Output
    
    private static final int SLOTS = 5;
    
    // Processing data indices for ContainerData
    private static final int PROGRESS = 0;
    private static final int MAX_PROGRESS = 1;
    private static final int DATA_SIZE = 2;
    
    private int progress = 0;
    private int maxProgress = 200; // 10 seconds at 20 ticks per second
    
    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (slot == OUTPUT_SLOT) {
                return false; // Cannot insert into output
            }
            return true;
        }
    };
    
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    
    // Container data for syncing progress to client
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case PROGRESS -> GodforgeBlockEntity.this.progress;
                case MAX_PROGRESS -> GodforgeBlockEntity.this.maxProgress;
                default -> 0;
            };
        }
        
        @Override
        public void set(int index, int value) {
            switch (index) {
                case PROGRESS -> GodforgeBlockEntity.this.progress = value;
                case MAX_PROGRESS -> GodforgeBlockEntity.this.maxProgress = value;
            }
        }
        
        @Override
        public int getCount() {
            return DATA_SIZE;
        }
    };
    
    public GodforgeBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.GODFORGE_BLOCK_ENTITY.get(), pPos, pBlockState);
    }
    
    @Override
    public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }
    
    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }
    
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }
    
    @Override
    protected void saveAdditional(@Nonnull CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("godforge.progress", progress);
        pTag.putInt("godforge.max_progress", maxProgress);
        super.saveAdditional(pTag);
    }
    
    @Override
    public void load(@Nonnull CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("godforge.progress");
        maxProgress = pTag.getInt("godforge.max_progress");
    }
    
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        
        if (level != null) {
            Containers.dropContents(level, worldPosition, inventory);
        }
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.afterlifeentombed.godforge");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @Nonnull Inventory pPlayerInventory, @Nonnull Player pPlayer) {
        return new GodforgeMenu(pContainerId, pPlayerInventory, this, this.data);
    }
    
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (pLevel.isClientSide()) {
            return;
        }
        
        if (hasRecipe()) {
            increaseCraftingProgress();
            setChanged(pLevel, pPos, pState);
            
            if (hasProgressFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }
    
    /**
     * Checks if the nearest player has enough XP for an enchantment transfer
     * Creative mode players always pass the check
     */
    private boolean hasEnoughXP(int requiredLevels) {
        if (level == null) return false;
        
        net.minecraft.world.entity.player.Player player = level.getNearestPlayer(
            worldPosition.getX() + 0.5, 
            worldPosition.getY() + 0.5, 
            worldPosition.getZ() + 0.5, 
            10.0, false
        );
        
        if (player == null) return false;
        
        // Creative mode players don't need XP
        if (player.isCreative()) return true;
        
        return player.experienceLevel >= requiredLevels;
    }
    
    /**
     * Deducts XP from the nearest player
     * Does not deduct from creative mode players
     */
    private void deductXP(int levels) {
        if (level == null) return;
        
        net.minecraft.world.entity.player.Player player = level.getNearestPlayer(
            worldPosition.getX() + 0.5, 
            worldPosition.getY() + 0.5, 
            worldPosition.getZ() + 0.5, 
            10.0, false
        );
        
        if (player != null && !player.isCreative()) {
            player.giveExperienceLevels(-levels);
        }
    }
    
    private void resetProgress() {
        progress = 0;
    }
    
    private void craftItem() {
        Optional<GodforgeRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) {
            return;
        }
        
        ItemStack result = recipe.get().assemble(getSimpleContainer(), level.registryAccess());
        
        // Check if this is an enchantment transfer (no fuel) or regular recipe (requires fuel)
        ItemStack slot2 = itemHandler.getStackInSlot(BOOK_SLOT);
        ItemStack slot3 = itemHandler.getStackInSlot(SOURCE_ENCHANTED_SLOT);
        ItemStack targetSlot = itemHandler.getStackInSlot(TARGET_ITEM_SLOT);
        
        // Check both orderings for enchantment transfer
        boolean isEnchantTransfer = (slot2.is(net.minecraft.world.item.Items.BOOK) && slot2.hasCustomHoverName() && slot3.isEnchanted()) ||
                                    (slot3.is(net.minecraft.world.item.Items.BOOK) && slot3.hasCustomHoverName() && slot2.isEnchanted());
        
        if (isEnchantTransfer) {
            // Deduct XP cost (10 XP levels per enchantment level)
            int enchantLevel = recipe.get().getEnchantmentLevel(getSimpleContainer());
            int xpCost = enchantLevel * 10;
            deductXP(xpCost);
            
            // Enchantment transfer: consume book, source, and target
            itemHandler.extractItem(TARGET_ITEM_SLOT, 1, false);
            itemHandler.extractItem(BOOK_SLOT, 1, false);
            itemHandler.extractItem(SOURCE_ENCHANTED_SLOT, 1, false);
        } else {
            // Regular recipe: consume fuel (if required) and inputs
            // Check if target slot is used (3-input recipe)
            boolean usesThirdInput = !targetSlot.isEmpty();
            
            if (recipe.get().requiresFuel()) {
                itemHandler.extractItem(FUEL_SLOT, 1, false);
            }
            
            if (usesThirdInput) {
                itemHandler.extractItem(TARGET_ITEM_SLOT, 1, false);
            }
            
            itemHandler.extractItem(BOOK_SLOT, 1, false);
            itemHandler.extractItem(SOURCE_ENCHANTED_SLOT, 1, false);
        }
        
        // Place result in output slot - use the actual result which contains enchantments/NBT
        ItemStack outputStack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else {
            outputStack.grow(result.getCount());
        }
    }
    
    private boolean hasProgressFinished() {
        return progress >= maxProgress;
    }
    
    private void increaseCraftingProgress() {
        progress++;
    }
    
    private boolean hasRecipe() {
        Optional<GodforgeRecipe> recipe = getCurrentRecipe();
        
        if (recipe.isEmpty()) {
            return false;
        }
        
        // Check if this is an enchantment transfer
        SimpleContainer inventory = getSimpleContainer();
        ItemStack slot2 = itemHandler.getStackInSlot(BOOK_SLOT);
        ItemStack slot3 = itemHandler.getStackInSlot(SOURCE_ENCHANTED_SLOT);
        
        // Check both orderings - either slot can be the book or enchanted item
        boolean isEnchantTransfer = (slot2.is(net.minecraft.world.item.Items.BOOK) && slot2.hasCustomHoverName() && slot3.isEnchanted()) ||
                                    (slot3.is(net.minecraft.world.item.Items.BOOK) && slot3.hasCustomHoverName() && slot2.isEnchanted());
        
        // Set maxProgress based on recipe type
        // Enchantment transfers are instant (1 tick), regular recipes use cook time
        maxProgress = isEnchantTransfer ? 1 : recipe.get().getCookTime();
        
        // For enchantment transfers, check if player has enough XP
        if (isEnchantTransfer) {
            int enchantLevel = recipe.get().getEnchantmentLevel(inventory);
            int xpCost = enchantLevel * 10;
            if (!hasEnoughXP(xpCost)) {
                return false;
            }
        } else {
            // For regular recipes, check if fuel is present when required
            if (recipe.get().requiresFuel()) {
                ItemStack fuelSlot = itemHandler.getStackInSlot(FUEL_SLOT);
                if (fuelSlot.isEmpty()) {
                    return false;
                }
            }
        }
        
        ItemStack result = recipe.get().getResultItem(level.registryAccess());
        
        return canInsertAmountIntoOutputSlot(result.getCount())
                && canInsertItemIntoOutputSlot(result.getItem());
    }
    
    private Optional<GodforgeRecipe> getCurrentRecipe() {
        SimpleContainer inventory = getSimpleContainer();
        
        return level.getRecipeManager()
                .getRecipeFor(ModRecipeTypes.GODFORGE_RECIPE_TYPE.get(), inventory, level);
    }
    
    private SimpleContainer getSimpleContainer() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        return inventory;
    }
    
    private boolean canInsertItemIntoOutputSlot(net.minecraft.world.item.Item item) {
        return itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()
                || itemHandler.getStackInSlot(OUTPUT_SLOT).is(item);
    }
    
    private boolean canInsertAmountIntoOutputSlot(int count) {
        return itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= 
                itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }
    
    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }
    
    public ContainerData getContainerData() {
        return data;
    }
}
