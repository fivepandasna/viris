package com.viris.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.cortex.voxy.client.core.gl.shader.ShaderLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Voxy scales a block's top indentation with the LOD's voxel (water 1/8 block: 2/8 at level 1, 4/8 at level 2...), so the
 * sea steps along the section grid, and where the farther part is higher its front shows as a thin dark line. Water's top
 * now sits a fixed distance under the voxel's top: at sea level (a multiple of 2 from level 1 up, block top one less) that
 * is the height of the real water at every level. Water tops elsewhere can still step, by less than before.
 */
@Mixin(value = ShaderLoader.class, remap = false)
public class ShaderLoaderMixin {

    @ModifyReturnValue(method = "parse", at = @At("RETURN"))
    private static String viris$flatWater(String source) {
        if (!source.contains("void setupQuad(out QuadData quad")) {
            return source;
        }
        return source.replace("float lodScale;", "float lodScale;\n    float maxY;")
            .replace("quad.uvCorner = faceSize.xz;", """
                quad.uvCorner = faceSize.xz;
                quad.maxY = 1e9;
                float waterIndent = extractFaceIndentation(model.faceData[1]);
                if (lodLevel > 0u && modelIsTranslucent(model) && waterIndent > 0.0 && waterIndent < 0.25) {
                    if (face == 1u) {
                        quad.basePoint.y += waterIndent * lodScale - 1.0 - waterIndent;
                    } else if ((face >> 1) != 0u) {
                        float voxels = float((face >> 1) == 1u ? quadSize.y : quadSize.x);
                        quad.maxY = (float(extractPos(rawQuad).y) + voxels) * lodScale + float(baseSection.y << 5) - 1.0 - waterIndent;
                    }
                }
                """)
            .replace("vec4 pos = MVP * vec4(point, 1.0f);", "point.y = min(point.y, quad.maxY);\n    vec4 pos = MVP * vec4(point, 1.0f);");
    }
}
