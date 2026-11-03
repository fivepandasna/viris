package com.viris.mixin.vitrail;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.viris.VoxyFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** The pack's water reads the LOD depth too: Vitrail's black stand-in made all of it look shallow, so nearly clear. */
@Mixin(targets = "dev.vitrail.render.GeometryProgram", remap = false)
public class GeometryProgramMixin {

    @WrapOperation(method = "bind", at = @At(value = "INVOKE",
        target = "Ldev/vitrail/render/GraphicsApi;bindTexture(Lcom/mojang/renderpearl/api/commands/RenderPass;Ljava/lang/String;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lcom/mojang/renderpearl/api/textures/GpuSampler;)V"))
    private void viris$voxyDepth(RenderPass pass, String name, GpuTextureView view, GpuSampler sampler, Operation<Void> original) {
        GpuTextureView depth = name.startsWith("vxDepthTex") ? VoxyFrame.vxDepthView(name) : null;
        original.call(pass, name, depth != null ? depth : view, sampler);
    }
}
