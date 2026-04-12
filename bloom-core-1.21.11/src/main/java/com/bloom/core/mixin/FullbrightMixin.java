package com.bloom.core.mixin;

import com.bloom.core.module.modules.Fullbright;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fullbright via gamma override.
 *
 * Redirects SimpleOption.getValue() calls inside LightmapTextureManager.update().
 * If the SimpleOption being read is the gamma option AND fullbright is active,
 * we return 16.0 instead of the user's actual gamma setting.
 *
 * The vanilla gamma is capped at 1.0 (100% slider) but the lightmap formula
 * extrapolates fine — 16.0 produces a true fullbright effect.
 */
@Mixin(LightmapTextureManager.class)
public class FullbrightMixin {
    @Redirect(
        method = "update",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;")
    )
    private Object bloomFullbrightGamma(SimpleOption<?> instance) {
        if (Fullbright.active) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options != null && instance == mc.options.getGamma()) {
                return 16.0;
            }
        }
        return instance.getValue();
    }
}
