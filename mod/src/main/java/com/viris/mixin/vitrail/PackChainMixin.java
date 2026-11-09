package com.viris.mixin.vitrail;

import com.viris.VoxyFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Voxy's LOD gbuffer data goes into the pack's targets before its deferred passes run. */
@Mixin(targets = "dev.vitrail.render.PackChain", remap = false)
public class PackChainMixin {

    @Inject(method = "drawBeforeTranslucents", at = @At("HEAD"))
    private static void viris$lodGbuffer(CallbackInfo ci) {
        VoxyFrame.packComposite();
    }

    /** The scene seed paints the game's picture over the LODs in the pack's first draw buffer: write them again. */
    @Inject(method = "paintSeed", at = @At("TAIL"))
    private void viris$lodGbufferAfterSeed(CallbackInfo ci) {
        VoxyFrame.afterSceneSeed();
    }

    @Inject(method = "drawBeforeTranslucents", at = @At("TAIL"))
    private static void viris$packInputs(CallbackInfo ci) {
        VoxyFrame.packInputs();
    }
}
