package com.dracolich777.afterlifeentombed.mobs;

import com.dracolich777.afterlifeentombed.init.ModEntityTypes;
import com.dracolich777.afterlifeentombed.events.ScorpionWhipHandler;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.item.Items;

public class ScorpionWhipProjectileEntity extends ThrowableItemProjectile {

    // REQUIRED by Forge registration
    public ScorpionWhipProjectileEntity(EntityType<? extends ScorpionWhipProjectileEntity> type, Level level) {
        super(type, level);
    }

    // Convenience constructor for firing the projectile in code
    public ScorpionWhipProjectileEntity(Level level, Player owner) {
        super(ModEntityTypes.SCORPION_WHIP_PROJECTILE.get(), owner, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        Entity entity = hitResult.getEntity();
        Entity owner = this.getOwner();

        if (entity instanceof LivingEntity && owner instanceof Player player) {
            entity.hurt(level().damageSources().thrown(this, owner), 4.0F);

            double dx = player.getX() - entity.getX();
            double dy = player.getEyeY() - entity.getEyeY();
            double dz = player.getZ() - entity.getZ();
            entity.setDeltaMovement(dx * 0.25, dy * 0.25, dz * 0.25);

            ScorpionWhipHandler.setDamageBoost(player, 10);  // 10 ticks = 0.5 seconds
        }

        this.discard();
    }

    @Override
    protected net.minecraft.world.item.Item getDefaultItem() {
        return Items.AIR;
    }
}
