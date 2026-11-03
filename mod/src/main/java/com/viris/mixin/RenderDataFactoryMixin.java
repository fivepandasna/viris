package com.viris.mixin;

import me.cortex.voxy.client.core.rendering.building.RenderDataFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Water's side faces on a section's border (neighbour empty or not ingested yet) show through the surface as a thin dark
 * line along the section grid. With the surface at one height (ShaderLoaderMixin) nothing needs them.
 */
@Mixin(value = RenderDataFactory.class, remap = false)
public class RenderDataFactoryMixin {

    /** Axis 0 is up/down (the water's surface), axis 1 the Z sides. */
    @Inject(method = "generateYZFluidOuterGeometry", at = @At("HEAD"), cancellable = true)
    private void viris$noFluidSideZ(int axis, CallbackInfo ci) {
        if (axis != 0) {
            ci.cancel();
        }
    }

    @Inject(method = "generateXOuterFluidGeometry", at = @At("HEAD"), cancellable = true)
    private void viris$noFluidSideX(CallbackInfo ci) {
        ci.cancel();
    }
}
