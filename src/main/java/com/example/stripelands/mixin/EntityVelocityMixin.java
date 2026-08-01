package com.example.stripelands.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Same idea as EntityPrecisionMixin, but for velocity/motion. Bedrock stores
 * this as float too, so cumulative movement (falling, knockback, elytra, etc.)
 * inherits the same rounding error.
 */
@Mixin(Entity.class)
public abstract class EntityVelocityMixin {

    @ModifyVariable(
            method = "setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Vec3 stripelands$truncateVelocity(Vec3 motion) {
        return new Vec3((float) motion.x, (float) motion.y, (float) motion.z);
    }
}
