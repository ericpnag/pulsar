package com.bloom.core.mixin;

import com.bloom.core.module.modules.LowFire;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class LowFireMixin {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"))
    private static void bloomLowFire(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        if (LowFire.active) {
            matrices.translate(0, -0.5, 0);
        }
    }
}
