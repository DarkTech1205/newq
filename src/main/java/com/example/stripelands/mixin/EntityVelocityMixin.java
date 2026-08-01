package com.example.stripelands.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Same idea as EntityPrecisionMixin, but for velocity/motion. Bedrock stores
 * this as float too, so cumulative movement (falling, knockback, elytra, etc.)
 * inherits the same rounding error.
 *
 * Targets the setDeltaMovement(DDD)V primitive overload (confirmed to exist in
 * 26.2 official mappings) rather than the Vec3-argument overload used in the
 * previous version of this file, which was an unverified guess.
 */
@Mixin(Entity.class)
public abstract class EntityVelocityMixin {

    @ModifyVariable(method = "setDeltaMovement(DDD)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double stripelands$truncateMotionX(double x) {
        return (float) x;
    }

    @ModifyVariable(method = "setDeltaMovement(DDD)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double stripelands$truncateMotionY(double y) {
        return (float) y;
    }

    @ModifyVariable(method = "setDeltaMovement(DDD)V", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private double stripelands$truncateMotionZ(double z) {
        return (float) z;
    }
}
