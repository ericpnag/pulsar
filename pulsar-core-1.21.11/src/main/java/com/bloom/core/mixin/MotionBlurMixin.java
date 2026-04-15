package com.bloom.core.mixin;

import com.bloom.core.module.modules.MotionBlur;
import com.bloom.core.PulsarCore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MotionBlurMixin {
    @Unique private float lastYaw = 0, lastPitch = 0;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (com.bloom.core.PulsarCore.MODULES != null) {
            var mod = com.bloom.core.PulsarCore.MODULES.getByName("Motion Blur");
            if (mod != null && mod.isEnabled()) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player == null) return;
                float yaw = mc.player.getYaw();
                float pitch = mc.player.getPitch();
                float delta = Math.abs(yaw - lastYaw) + Math.abs(pitch - lastPitch);
                float movement = Math.min(1.0f, delta / 10.0f);
                lastYaw = yaw;
                lastPitch = pitch;
                if (movement > 0.01f) {
                    int alpha = (int)(movement * MotionBlur.strength * 120);
                    context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), (alpha << 24));
                }
            }
        }
    }
}
