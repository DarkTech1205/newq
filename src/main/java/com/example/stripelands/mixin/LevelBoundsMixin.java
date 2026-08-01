package com.example.stripelands.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Separately from the movable WorldBorder, vanilla hardcodes an absolute +-30,000,000
 * X/Z edge (Level.isInWorldBounds) to keep block-position math inside safe integer
 * ranges. This is the check people mean when they say "the 30 million block border".
 *
 * HEADS UP: this is the part of the mod most likely to need a name fix. 26.x has been
 * through a big rendering/registration refactor and I can't fully verify this exact
 * method signature against the live 26.2 source from here. If Fabric Loader fails to
 * apply this mixin at launch, the crash log will print the class/method it couldn't
 * find - paste that into https://mcsrc.dev (Fabric's own decompiled-source browser for
 * unobfuscated 26.x) to find the current name/signature and adjust this file.
 */
@Mixin(Level.class)
public abstract class LevelBoundsMixin {

    @Inject(method = "isInWorldBounds", at = @At("HEAD"), cancellable = true)
    private static void stripelands$removeWorldBounds(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
