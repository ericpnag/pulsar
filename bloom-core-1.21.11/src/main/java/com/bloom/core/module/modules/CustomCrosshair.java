package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class CustomCrosshair extends Module {
    public static int crosshairColor = 0x00FF00;
    public static int crosshairSize = 6;
    public static int crosshairGap = 3;
    public static int crosshairThickness = 1;
    public static boolean dot = true;
    public static boolean dynamic = true; // changes color on entity hit

    public CustomCrosshair() {
        super("Custom Crosshair", "Fully customizable crosshair shape and color", false);
    }

    public static void renderCrosshair(DrawContext context, int screenW, int screenH) {
        int cx = screenW / 2, cy = screenH / 2;
        int s = crosshairSize, g = crosshairGap, t = crosshairThickness;
        int color = 0xFF000000 | crosshairColor;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (dynamic && mc.crosshairTarget != null &&
            mc.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.ENTITY) {
            color = 0xFFFF4444; // Red when targeting entity
        }

        // Top
        context.fill(cx - t / 2, cy - g - s, cx + (t + 1) / 2, cy - g, color);
        // Bottom
        context.fill(cx - t / 2, cy + g, cx + (t + 1) / 2, cy + g + s, color);
        // Left
        context.fill(cx - g - s, cy - t / 2, cx - g, cy + (t + 1) / 2, color);
        // Right
        context.fill(cx + g, cy - t / 2, cx + g + s, cy + (t + 1) / 2, color);

        // Dot
        if (dot) {
            context.fill(cx, cy, cx + 1, cy + 1, color);
        }
    }

    @Override public boolean hasHud() { return false; }
}
