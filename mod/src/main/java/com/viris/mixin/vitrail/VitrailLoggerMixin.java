package com.viris.mixin.vitrail;

import com.viris.LunarVitrailLog;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lunar drops Vitrail's log lines: under Lunar they also go to logs/vitrail.log. */
@Mixin(targets = "dev.vitrail.Vitrail", remap = false)
public class VitrailLoggerMixin {

    @Inject(method = "logger", at = @At("RETURN"), cancellable = true)
    private static void viris$copyUnderLunar(CallbackInfoReturnable<Logger> cir) {
        Logger copied = LunarVitrailLog.wrap(cir.getReturnValue());
        if (copied != cir.getReturnValue()) {
            cir.setReturnValue(copied);
        }
    }
}
