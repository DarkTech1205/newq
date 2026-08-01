package com.example.stripelands.mixin;

import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * WorldBorder#getMaxSize() is what the vanilla /worldborder command (and the
 * border-shrink logic) clamp against. Raising it lets the movable border itself
 * grow arbitrarily large. NOTE: this alone does not remove the separate,
 * hardcoded +-30,000,000 world edge - see LevelBoundsMixin for that part.
 */
@Mixin(WorldBorder.class)
public abstract class WorldBorderMixin {

    @Inject(method = "getMaxSize", at = @At("HEAD"), cancellable = true)
    private void stripelands$removeMaxSize(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(2.9999984E9);
    }
}
