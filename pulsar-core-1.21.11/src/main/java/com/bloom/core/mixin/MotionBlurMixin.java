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
    @Unique private float smoothAlpha = 0;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (com.bloom.core.PulsarCore.MODULES == null) return;
        var mod = com.bloom.core.PulsarCore.MODULES.getByName("Motion Blur");
        if (mod == null || !mod.isEnabled()) {
            smoothAlpha = 0;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        float deltaYaw = Math.abs(yaw - lastYaw);
        float deltaPitch = Math.abs(pitch - lastPitch);
        // Normalize large yaw jumps (respawn, teleport)
        if (deltaYaw > 100) deltaYaw = 0;
        if (deltaPitch > 100) deltaPitch = 0;

        float movement = Math.min(1.0f, (deltaYaw + deltaPitch) / 8.0f);
        lastYaw = yaw;
        lastPitch = pitch;

        // Smooth alpha transition (ease in fast, ease out slow)
        float target = movement * MotionBlur.strength;
        if (target > smoothAlpha) {
            smoothAlpha += (target - smoothAlpha) * 0.6f;
        } else {
            smoothAlpha += (target - smoothAlpha) * 0.15f;
        }
        if (smoothAlpha < 0.005f) { smoothAlpha = 0; return; }

        int w = context.getScaledWindowWidth();
        int h = context.getScaledWindowHeight();

        // Radial blur effect: multiple semi-transparent layers from edges
        int alpha = (int)(smoothAlpha * 80);
        if (alpha > 0) {
            // Edge darkening (vignette-like blur)
            int a1 = Math.min(alpha, 40);
            int a2 = Math.min(alpha / 2, 20);
            int a3 = Math.min(alpha / 3, 12);

            // Top edge
            context.fill(0, 0, w, h / 6, (a1 << 24));
            context.fill(0, 0, w, h / 10, (a2 << 24));
            // Bottom edge
            context.fill(0, h - h / 6, w, h, (a1 << 24));
            context.fill(0, h - h / 10, w, h, (a2 << 24));
            // Left edge
            context.fill(0, 0, w / 6, h, (a1 << 24));
            context.fill(0, 0, w / 10, h, (a2 << 24));
            // Right edge
            context.fill(w - w / 6, 0, w, h, (a1 << 24));
            context.fill(w - w / 10, 0, w, h, (a2 << 24));

            // Very subtle full-screen tint for the motion feel
            context.fill(0, 0, w, h, (a3 << 24));
        }
    }
}
