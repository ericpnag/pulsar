package com.bloom.core.mixin;

import com.bloom.core.module.modules.FovChanger;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class FovMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void bloomFovChanger(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (FovChanger.active) {
            cir.setReturnValue(FovChanger.fovValue);
        }
    }
}
