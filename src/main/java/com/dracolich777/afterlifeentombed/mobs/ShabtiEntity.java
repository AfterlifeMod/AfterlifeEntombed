package com.dracolich777.afterlifeentombed.mobs;

import java.util.UUID;

import com.dracolich777.afterlifeentombed.init.ModEntityTypes;
import com.dracolich777.afterlifeentombed.items.ShabtiItem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ShabtiEntity extends LivingEntity {

    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(ShabtiEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> OWNER_YAW = SynchedEntityData.defineId(ShabtiEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> OWNER_PITCH = SynchedEntityData.defineId(ShabtiEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> OWNER_YAW_HEAD = SynchedEntityData.defineId(ShabtiEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<CompoundTag> OWNER_INVENTORY = SynchedEntityData.defineId(ShabtiEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Integer> DISPLAY_FLAGS = SynchedEntityData.defineId(ShabtiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> OWNER_STATUS_INFO = SynchedEntityData.defineId(ShabtiEntity.class, EntityDataSerializers.STRING);

    // Display option flags (bitmask)
    public static final int SHOW_ARMOR = 1;
    public static final int SHOW_ITEMS = 2;
    public static final int SHOW_ROTATION = 4;
    public static final int SHOW_HEALTH = 8;
    public static final int SHOW_HUNGER = 16;
    public static final int SHOW_XP = 32;
    public static final int SHOW_COORDS = 64;
    public static final int SHOW_DIMENSION = 128;

    private UUID ownerUUID;
    private ServerPlayer cachedOwner;
    // Default display now includes health, XP, and coordinates as requested
    private int displayFlags = SHOW_ARMOR | SHOW_ITEMS | SHOW_ROTATION | SHOW_HEALTH | SHOW_XP | SHOW_COORDS;

    public ShabtiEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        // Remove invulnerability so it can be killed by owner
        this.setInvulnerable(false);
    }

    public ShabtiEntity(Level level, ServerPlayer owner) {
        this(ModEntityTypes.SHABTI.get(), level);
        this.ownerUUID = owner.getUUID();
        this.cachedOwner = owner;
        this.entityData.set(OWNER_UUID, owner.getUUID().toString());
        updateFromOwner();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER_UUID, "");
        this.entityData.define(OWNER_YAW, 0.0F);
        this.entityData.define(OWNER_PITCH, 0.0F);
        this.entityData.define(OWNER_YAW_HEAD, 0.0F);
        this.entityData.define(OWNER_INVENTORY, new CompoundTag());
        this.entityData.define(DISPLAY_FLAGS, SHOW_ARMOR | SHOW_ITEMS | SHOW_ROTATION | SHOW_HEALTH | SHOW_XP | SHOW_COORDS);
        this.entityData.define(OWNER_STATUS_INFO, "");
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            updateFromOwner();
        }
    }

    private void updateFromOwner() {
        if (cachedOwner == null || !cachedOwner.isAlive()) {
            if (ownerUUID != null && level().getServer() != null) {
                cachedOwner = level().getServer().getPlayerList().getPlayer(ownerUUID);
            }
            if (cachedOwner == null) {
                return; // Don't discard immediately, just skip update
            }
        }

        try {
            // Update rotation to match owner
            this.entityData.set(OWNER_YAW, cachedOwner.getYRot());
            this.entityData.set(OWNER_PITCH, cachedOwner.getXRot());
            this.entityData.set(OWNER_YAW_HEAD, cachedOwner.getYHeadRot());

            // Update inventory data
            CompoundTag inventoryTag = new CompoundTag();

            // Main hand item
            ItemStack mainHand = cachedOwner.getMainHandItem();
            if (!mainHand.isEmpty()) {
                CompoundTag mainHandTag = new CompoundTag();
                mainHand.save(mainHandTag);
                inventoryTag.put("MainHand", mainHandTag);
            }

            // Off hand item
            ItemStack offHand = cachedOwner.getOffhandItem();
            if (!offHand.isEmpty()) {
                CompoundTag offHandTag = new CompoundTag();
                offHand.save(offHandTag);
                inventoryTag.put("OffHand", offHandTag);
            }

            // Armor
            for (int i = 0; i < 4; i++) {
                ItemStack armor = cachedOwner.getInventory().getArmor(i);
                if (!armor.isEmpty()) {
                    CompoundTag armorTag = new CompoundTag();
                    armor.save(armorTag);
                    inventoryTag.put("Armor" + i, armorTag);
                }
            }

            this.entityData.set(OWNER_INVENTORY, inventoryTag);

            // Update owner status information
            updateOwnerStatusInfo();
        } catch (Exception e) {
            // If there's any error updating, just skip this tick
        }
    }

    private void updateOwnerStatusInfo() {
        if (cachedOwner == null) {
            return;
        }

        StringBuilder statusBuilder = new StringBuilder();

        // Always show health as requested
        if (hasDisplayFlag(SHOW_HEALTH)) {
            statusBuilder.append("Health: %.1f/%.1f".formatted(cachedOwner.getHealth(), cachedOwner.getMaxHealth()));
        }

        // Show armor level (defense points)
        if (statusBuilder.length() > 0) {
            statusBuilder.append("\n");
        }
        statusBuilder.append("Armor: %.1f".formatted(cachedOwner.getArmorValue()));

        if (hasDisplayFlag(SHOW_HUNGER)) {
            FoodData foodData = cachedOwner.getFoodData();
            if (statusBuilder.length() > 0) {
                statusBuilder.append("\n");
            }
            statusBuilder.append("Hunger: %d/20".formatted(foodData.getFoodLevel()));
        }

        // Always show XP as requested
        if (hasDisplayFlag(SHOW_XP)) {
            if (statusBuilder.length() > 0) {
                statusBuilder.append("\n");
            }
            statusBuilder.append("XP: %d (Lvl %d)".formatted(cachedOwner.totalExperience, cachedOwner.experienceLevel));
        }

        // Always show coordinates as requested
        if (hasDisplayFlag(SHOW_COORDS)) {
            if (statusBuilder.length() > 0) {
                statusBuilder.append("\n");
            }
            statusBuilder.append("Pos: %.1f, %.1f, %.1f".formatted(cachedOwner.getX(), cachedOwner.getY(), cachedOwner.getZ()));
        }

        if (hasDisplayFlag(SHOW_DIMENSION)) {
            if (statusBuilder.length() > 0) {
                statusBuilder.append("\n");
            }
            String dimensionName = cachedOwner.level().dimension().location().toString();
            statusBuilder.append("Dim: %s".formatted(dimensionName));
        }

        this.entityData.set(OWNER_STATUS_INFO, statusBuilder.toString());
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (ownerUUID != null) {
            ShabtiItem.removeShabti(ownerUUID);
        }
    }

    // Getters for client-side rendering
    public float getOwnerYaw() {
        return this.entityData.get(OWNER_YAW);
    }

    public float getOwnerPitch() {
        return this.entityData.get(OWNER_PITCH);
    }

    public float getOwnerYawHead() {
        return this.entityData.get(OWNER_YAW_HEAD);
    }

    public CompoundTag getOwnerInventory() {
        return this.entityData.get(OWNER_INVENTORY);
    }

    public UUID getOwnerUUID() {
        if (ownerUUID == null && !this.entityData.get(OWNER_UUID).isEmpty()) {
            ownerUUID = UUID.fromString(this.entityData.get(OWNER_UUID));
        }
        return ownerUUID;
    }

    public void setOwner(ServerPlayer owner) {
        this.ownerUUID = owner.getUUID();
        this.cachedOwner = owner;
        this.entityData.set(OWNER_UUID, owner.getUUID().toString());
        updateFromOwner();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        // Only allow the owner to hurt/kill this entity
        if (damageSource.getEntity() instanceof Player player) {
            if (this.ownerUUID != null && this.ownerUUID.equals(player.getUUID())) {
                // Set health to 0 to kill the entity
                this.setHealth(0.0f);
                boolean actuallyHurt = super.hurt(damageSource, amount);
                
                // Only give the item back if the entity actually died
                if (!level().isClientSide && player instanceof ServerPlayer serverPlayer && !this.isAlive()) {
                    ShabtiItem.giveShabtiItem(serverPlayer);
                    player.sendSystemMessage(Component.literal("Shabti returned to your inventory"));
                }
                
                return actuallyHurt;
            }
        }
        return false; // Invulnerable to all other damage
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
        // Don't push anything
    }

    @Override
    protected void pushEntities() {
        // Don't push entities
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        // Return the owner's armor items for visual rendering
        java.util.List<ItemStack> armorItems = new java.util.ArrayList<>();
        CompoundTag inventoryTag = getOwnerInventory();

        for (int i = 0; i < 4; i++) {
            if (inventoryTag.contains("Armor" + i)) {
                armorItems.add(ItemStack.of(inventoryTag.getCompound("Armor" + i)));
            } else {
                armorItems.add(ItemStack.EMPTY);
            }
        }

        return armorItems;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        // Parse the owner's inventory data and return the appropriate item
        CompoundTag inventoryTag = getOwnerInventory();

        switch (slot) {
            case MAINHAND:
                if (inventoryTag.contains("MainHand")) {
                    return ItemStack.of(inventoryTag.getCompound("MainHand"));
                }
                break;
            case OFFHAND:
                if (inventoryTag.contains("OffHand")) {
                    return ItemStack.of(inventoryTag.getCompound("OffHand"));
                }
                break;
            case HEAD:
                if (inventoryTag.contains("Armor3")) {
                    return ItemStack.of(inventoryTag.getCompound("Armor3"));
                }
                break;
            case CHEST:
                if (inventoryTag.contains("Armor2")) {
                    return ItemStack.of(inventoryTag.getCompound("Armor2"));
                }
                break;
            case LEGS:
                if (inventoryTag.contains("Armor1")) {
                    return ItemStack.of(inventoryTag.getCompound("Armor1"));
                }
                break;
            case FEET:
                if (inventoryTag.contains("Armor0")) {
                    return ItemStack.of(inventoryTag.getCompound("Armor0"));
                }
                break;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        // This entity doesn't actually hold items, so this method does nothing
        // The visual representation is handled through the owner's inventory data
    }

    @Override
    public HumanoidArm getMainArm() {
        // Return the owner's main arm preference, or default to right
        if (cachedOwner != null) {
            return cachedOwner.getMainArm();
        }
        return HumanoidArm.RIGHT;
    }

    // Required NBT serialization methods
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("OwnerUUID")) {
            this.ownerUUID = UUID.fromString(compound.getString("OwnerUUID"));
            this.entityData.set(OWNER_UUID, compound.getString("OwnerUUID"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (ownerUUID != null) {
            compound.putString("OwnerUUID", ownerUUID.toString());
        }
    }

    // Required for proper entity spawning
    protected void registerGoals() {
        // No AI goals needed for this entity
    }

    // Prevent natural spawning and ensure it can only be summoned
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public boolean requiresCustomPersistence() {
        return true;
    }

    // Display configuration methods
    public boolean hasDisplayFlag(int flag) {
        return (this.entityData.get(DISPLAY_FLAGS) & flag) != 0;
    }

    public void setDisplayFlag(int flag, boolean enabled) {
        int currentFlags = this.entityData.get(DISPLAY_FLAGS);
        if (enabled) {
            currentFlags |= flag;
        } else {
            currentFlags &= ~flag;
        }
        this.entityData.set(DISPLAY_FLAGS, currentFlags);
        this.displayFlags = currentFlags;
    }

    public int getDisplayFlags() {
        return this.entityData.get(DISPLAY_FLAGS);
    }

    public String getOwnerStatusInfo() {
        return this.entityData.get(OWNER_STATUS_INFO);
    }

    // Right-click interaction - modified to allow owner to kill it by punching
    @Override
    public InteractionResult interactAt(Player player, Vec3 hitVec, InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Check if the player is the owner
            if (this.ownerUUID != null && this.ownerUUID.equals(player.getUUID())) {
                // Check if player is not holding the shabti item
                ItemStack heldItem = player.getItemInHand(hand);
                if (!(heldItem.getItem() instanceof ShabtiItem)) {
                    // If empty hand, allow punching to kill
                    if (heldItem.isEmpty()) {
                        player.sendSystemMessage(Component.literal("Punch the Shabti to recall it"));
                        return InteractionResult.SUCCESS;
                    }
                    // Otherwise show config message
                    player.sendSystemMessage(Component.literal("Shabti Configuration GUI will be implemented"));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.interactAt(player, hitVec, hand);
    }

    // Custom name handling for "<Owner's> Shabti"
    @Override
    public Component getDisplayName() {
        if (cachedOwner != null) {
            return Component.literal(cachedOwner.getDisplayName().getString() + "'s Shabti");
        } else if (ownerUUID != null && level().getServer() != null) {
            ServerPlayer owner = level().getServer().getPlayerList().getPlayer(ownerUUID);
            if (owner != null) {
                return Component.literal(owner.getDisplayName().getString() + "'s Shabti");
            }
        }
        return Component.literal("Shabti");
    }

    @Override
    public boolean hasCustomName() {
        return true; // Always show the custom name
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }
}
