package com.bloom.core.mixin;

import com.bloom.core.module.modules.MotionBlur;
import com.bloom.core.BloomCore;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MotionBlurMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (BloomCore.MODULES != null) {
            var mod = BloomCore.MODULES.getByName("Motion Blur");
            if (mod != null && mod.isEnabled()) {
                int alpha = (int)(MotionBlur.strength * 180);
                int color = (alpha << 24); // semi-transparent black overlay
                context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), color);
            }
        }
    }
}
