package com.viris.mixin.vitrail;

import com.viris.VoxyFrame;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** The pack's full screen passes read Voxy's depth through vxDepthTexOpaque / vxDepthTexTrans. */
@Mixin(targets = "dev.vitrail.render.PackPass", remap = false)
public class PackPassMixin {

    @WrapOperation(method = "bindSamplers", at = @At(value = "INVOKE",
        target = "Ldev/vitrail/render/GraphicsApi;bindTexture(Lcom/mojang/renderpearl/api/commands/RenderPass;Ljava/lang/String;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lcom/mojang/renderpearl/api/textures/GpuSampler;)V"))
    private void viris$voxyDepth(RenderPass pass, String name, GpuTextureView view, GpuSampler sampler, Operation<Void> original) {
        // Null before Voxy's first frame (Photon reads it in a prepare pass): keep Vitrail's stand-in then.
        GpuTextureView depth = name.startsWith("vxDepthTex") ? VoxyFrame.vxDepthView(name) : null;
        original.call(pass, name, depth != null ? depth : view, sampler);
    }
}
