package com.bloom.core.mixin;

import com.bloom.core.module.modules.Freelook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When freelook is active, intercept mouse look changes so
 * they rotate the camera but NOT the player body.
 */
@Mixin(ClientPlayerEntity.class)
public class FreelookMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void bloomFreelook(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (Freelook.looking) {
            // Apply delta to the freelook camera angles instead of the player
            float sensitivity = 0.15f;
            Freelook.cameraYaw += (float)(cursorDeltaX * sensitivity);
            Freelook.cameraPitch = Math.max(-90, Math.min(90,
                Freelook.cameraPitch - (float)(cursorDeltaY * sensitivity)));
            ci.cancel();
        }
    }
}
