package com.example.stripelands.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Forces every write to an entity's position through a double -> float -> double
 * round trip, the same storage Bedrock Edition uses internally.
 *
 * Targets setPosRaw(DDD)V rather than setPos(DDD)V: setPosRaw is the terminal
 * method that actually stores the position field (setPos, move, teleport, etc.
 * all eventually funnel into it), so hooking it here catches every code path
 * in one place instead of chasing each caller individually.
 *
 * float32 has a 24-bit mantissa, so it can represent every integer exactly only
 * up to 2^24 (16,777,216). Below that magnitude this round trip is effectively a
 * no-op for gameplay purposes (sub-millimeter error). Past it, the mantissa runs
 * out of room and coordinates start snapping to larger and larger steps - which
 * is exactly the mechanism behind Bedrock's "Stripe Lands" glitch. We don't need
 * to special-case the 16,777,216 threshold; it falls out of IEEE 754 on its own.
 *
 * This also feeds collision, since Entity's bounding box is derived from this
 * same position, and rendering, since the renderer positions everything relative
 * to the entity/camera position that ultimately traces back to these fields.
 */
@Mixin(Entity.class)
public abstract class EntityPrecisionMixin {

    @ModifyVariable(method = "setPosRaw(DDD)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double stripelands$truncateX(double x) {
        return (float) x;
    }

    @ModifyVariable(method = "setPosRaw(DDD)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double stripelands$truncateY(double y) {
        return (float) y;
    }

    @ModifyVariable(method = "setPosRaw(DDD)V", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private double stripelands$truncateZ(double z) {
        return (float) z;
    }
}
