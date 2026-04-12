package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class FovChanger extends Module {
    public static boolean active = false;
    public static float fovValue = 100.0f;

    public FovChanger() {
        super("FOV Changer", "Override field of view", false);
    }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }
    @Override public boolean hasHud() { return true; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        String text = "FOV: " + (int) fovValue;
        int color = 0xFF88CCFF;
        int tw = client.textRenderer.getWidth(text);
        context.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        context.fill(2, y - 1, 3, y + 10, color & 0x66FFFFFF);
        context.drawText(client.textRenderer, text, 6, y, color, true);
    }
}
