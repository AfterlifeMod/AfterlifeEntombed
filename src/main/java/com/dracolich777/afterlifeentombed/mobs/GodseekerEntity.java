package com.dracolich777.afterlifeentombed.mobs;

import java.util.Random;

import com.dracolich777.afterlifeentombed.init.ModBlocks;
import com.dracolich777.afterlifeentombed.init.ModItems;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class GodseekerEntity extends AbstractVillager {

    private static final EntityDataAccessor<Integer> DATA_UNHAPPY_COUNTER = SynchedEntityData.defineId(GodseekerEntity.class, EntityDataSerializers.INT);
    private static final int TRADE_COOLDOWN = 6000; // 5 minutes
    private int tradeTimer = 0;
    private boolean hasTraded = false;

    public GodseekerEntity(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5D));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.35D));
        this.goalSelector.addGoal(3, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_UNHAPPY_COUNTER, 0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.tradeTimer > 0) {
                this.tradeTimer--;
            }

            if (this.hasTraded && this.tradeTimer <= 0) {
                this.hasTraded = false;
                this.updateTrades();
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!itemstack.is(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (this.getOffers().isEmpty()) {
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            } else {
                if (!this.level().isClientSide) {
                    this.setTradingPlayer(player);
                    this.openTradingScreen(player, this.getDisplayName(), 1);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void updateTrades() {
        MerchantOffers offers = this.getOffers();
        this.addOffersFromItemListings(offers, this.getGodseekerTrades(), 20);
    }

    private GodseekerTrade[] getGodseekerTrades() {
        return new GodseekerTrade[]{
            new GodseekerTrade(
            new ItemStack(Items.DIAMOND, 10),
            new ItemStack(Items.BLAZE_POWDER, 1),
            new ItemStack(ModItems.GODSTONE_OF_RA.get(), 1),
            5, 0, 0.05F
            ),
            // Soul Crystal - 3 in stock
            new GodseekerTrade(
            new ItemStack(Items.DIAMOND, 10),
            new ItemStack(Items.SHIELD, 1),
            new ItemStack(ModItems.GODSTONE_OF_HORUS.get(), 1),
            3, 0, 0.05F
            ),
            new GodseekerTrade(
            new ItemStack(Items.DIAMOND, 10),
            new ItemStack(Items.BOOK, 10),
            new ItemStack(ModItems.GODSTONE_OF_THOTH.get(), 1),
            3, 0, 0.05F
            ),
            new GodseekerTrade(
            new ItemStack(Items.DIAMOND, 10),
            new ItemStack(Items.DEEPSLATE_DIAMOND_ORE, 3),
            new ItemStack(ModItems.GODSTONE_OF_GEB.get(), 1),
            3, 0, 0.05F
            ),
            new GodseekerTrade(
            new ItemStack(Items.DIAMOND, 10),
            new ItemStack(Items.PHANTOM_MEMBRANE, 3),
            new ItemStack(ModItems.GODSTONE_OF_SHU.get(), 1),
            3, 0, 0.05F
            ),
            new GodseekerTrade(
            new ItemStack(Items.DIAMOND, 10),
            new ItemStack(Items.HEART_OF_THE_SEA, 3),
            new ItemStack(ModItems.GODSTONE_OF_ISIS.get(), 1),
            3, 0, 0.05F
            ),
            new GodseekerTrade(
            new ItemStack(Items.DIAMOND, 10),
            new ItemStack(Items.WITHER_ROSE, 5),
            new ItemStack(ModItems.GODSTONE_OF_SETH.get(), 1),
            2, 0, 0.05F
            ),
            new GodseekerTrade(
            new ItemStack(Items.DIAMOND_BLOCK, 2),
            new ItemStack(Items.GILDED_BLACKSTONE, 5),
            new ItemStack(ModItems.GODSTONE_OF_ANUBIS.get(), 1),
            3, 0, 0.05F
            ),
            new GodseekerTrade(
            new ItemStack(Items.DIAMOND, 3),
            new ItemStack(Items.GOLD_BLOCK, 20),
            new ItemStack(ModBlocks.DUSKSAND.get(), 4),
            5, 0, 0.05F
            ), // new GodseekerTrade(
        //     new ItemStack(Items.NETHER_STAR, 10),
        //     new ItemStack(ModItems.GODSEEKER_SWORD.get(), 1),
        //     1, 0, 0.05F
        // ),
        // new GodseekerTrade(
        //     new ItemStack(Items.OBSIDIAN, 20),
        //     new ItemStack(ModItems.SCALE_OF_APEP.get(), 1),
        //     1, 0, 0.05F
        // )
        };
    }

    protected void addOffersFromItemListings(MerchantOffers offers, GodseekerTrade[] trades, int maxTrades) {
        Random random = new Random();
        offers.clear();

        for (GodseekerTrade trade : trades) {
            if (offers.size() >= maxTrades) {
                break;
            }

            MerchantOffer offer = new MerchantOffer(
                    trade.baseCostA,
                    trade.baseCostB,
                    trade.result,
                    trade.maxUses,
                    trade.xp,
                    trade.priceMultiplier
            );
            offers.add(offer);
        }
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {

        return null;
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("TradeTimer", this.tradeTimer);
        compound.putBoolean("HasTraded", this.hasTraded);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.tradeTimer = compound.getInt("TradeTimer");
        this.hasTraded = compound.getBoolean("HasTraded");
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.rewardTradeXp(offer);
        this.hasTraded = true;
        this.tradeTimer = TRADE_COOLDOWN;
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        if (offer.shouldRewardExp()) {
            int xp = 3 + this.random.nextInt(4);
            this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5D, this.getZ(), xp));
        }
    }

    @Override
    public Vec3 getRopeHoldPosition(float partialTicks) {
        float f = Mth.lerp(partialTicks, this.yBodyRotO, this.yBodyRot) * ((float) Math.PI / 180F);
        Vec3 vec3 = new Vec3(0.0D, this.getBoundingBox().getYsize() - 1.0D, 0.2D);
        return this.getPosition(partialTicks).add(vec3.yRot(-f));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    // Custom trade class
    public static class GodseekerTrade {

        public final ItemStack baseCostA;
        public final ItemStack baseCostB;
        public final ItemStack result;
        public final int maxUses;
        public final int xp;
        public final float priceMultiplier;

        public GodseekerTrade(ItemStack baseCostA, ItemStack baseCostB, ItemStack result, int maxUses, int xp, float priceMultiplier) {
            this.baseCostA = baseCostA;
            this.baseCostB = baseCostB;
            this.result = result;
            this.maxUses = maxUses;
            this.xp = xp;
            this.priceMultiplier = priceMultiplier;
        }

        public GodseekerTrade(ItemStack baseCostA, ItemStack result, int maxUses, int xp, float priceMultiplier) {
            this(baseCostA, ItemStack.EMPTY, result, maxUses, xp, priceMultiplier);
        }
    }
}
