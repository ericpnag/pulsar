package com.bloom.core.mixin;

import com.bloom.core.BloomCore;
import com.bloom.core.module.modules.CustomCrosshair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class CrosshairMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (BloomCore.MODULES != null) {
            var mod = BloomCore.MODULES.getByName("Custom Crosshair");
            if (mod != null && mod.isEnabled()) {
                int w = context.getScaledWindowWidth();
                int h = context.getScaledWindowHeight();
                CustomCrosshair.renderCrosshair(context, w, h);
                ci.cancel();
            }
        }
    }
}
