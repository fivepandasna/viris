package com.viris.mixin;

import com.mojang.renderpearl.backend.vulkan.VulkanFeatureSets;
import com.viris.Interop;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import java.util.Set;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The game's VkDevice gets the (Win32 or fd) extensions OpenGL needs to share images and semaphores, when the GPU has them. */
@Mixin(VulkanFeatureSets.class)
public class VulkanFeatureSetsMixin {

    @Unique
    private static final FeatureSet VIRIS_INTEROP = new FeatureSet("VIRIS OpenGL interop",
        Set.of(Interop.vulkanExtensions()), Set.of());

    @Inject(method = "optionalFeatureSets", at = @At("RETURN"))
    private static void viris$interop(CallbackInfoReturnable<Set<FeatureSet>> cir) {
        cir.getReturnValue().add(VIRIS_INTEROP);
    }
}
