package com.dracolich777.afterlifeentombed.mobs;

import com.dracolich777.afterlifeentombed.init.ModEntityTypes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HorusArrowEntity extends Arrow {

    public HorusArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true); // Disable gravity for this arrow
    }

    public HorusArrowEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.HORUS_ARROW.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
        this.setNoGravity(true); // Disable gravity for this arrow
    }

    public HorusArrowEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.HORUS_ARROW.get(), level);
        this.setPos(x, y, z);
        this.setNoGravity(true); // Disable gravity for this arrow
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(net.minecraft.world.item.Items.ARROW);
    }

    @Override
    public void tick() {
        // Override tick to ensure gravity stays disabled
        super.tick();
        this.setNoGravity(true);
    }
}
