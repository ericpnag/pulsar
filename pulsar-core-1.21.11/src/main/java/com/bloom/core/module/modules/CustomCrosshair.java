package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.List;

public class CustomCrosshair extends Module {
    public static int crosshairColor = 0x00FF00;
    public static int crosshairSize = 6;
    public static int crosshairGap = 3;
    public static int crosshairThickness = 1;
    public static boolean dot = true;
    public static boolean dynamic = true;

    public CustomCrosshair() {
        super("Custom Crosshair", "Fully customizable crosshair shape and color", false);
    }

    @Override
    public List<ModuleSetting> getSettings() {
        return List.of(
            new ModuleSetting("Size", () -> (float) crosshairSize, v -> crosshairSize = v.intValue(), 2f, 20f, 1f),
            new ModuleSetting("Gap", () -> (float) crosshairGap, v -> crosshairGap = v.intValue(), 0f, 10f, 1f),
            new ModuleSetting("Thickness", () -> (float) crosshairThickness, v -> crosshairThickness = v.intValue(), 1f, 5f, 1f),
            new ModuleSetting("Dynamic Color", () -> dynamic ? 1f : 0f, v -> dynamic = v > 0.5f)
        );
    }

    public static void renderCrosshair(DrawContext context, int screenW, int screenH) {
        int cx = screenW / 2, cy = screenH / 2;
        int s = crosshairSize, g = crosshairGap, t = crosshairThickness;
        int color = 0xFF000000 | crosshairColor;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (dynamic && mc.crosshairTarget != null &&
            mc.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.ENTITY) {
            color = 0xFFFF4444;
        }

        context.fill(cx - t / 2, cy - g - s, cx + (t + 1) / 2, cy - g, color);
        context.fill(cx - t / 2, cy + g, cx + (t + 1) / 2, cy + g + s, color);
        context.fill(cx - g - s, cy - t / 2, cx - g, cy + (t + 1) / 2, color);
        context.fill(cx + g, cy - t / 2, cx + g + s, cy + (t + 1) / 2, color);

        if (dot) {
            context.fill(cx, cy, cx + 1, cy + 1, color);
        }
    }

    @Override public boolean hasHud() { return false; }
}
