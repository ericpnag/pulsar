package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class FpsDisplay extends Module {
    public FpsDisplay() {
        super("FPS Display", "Show current FPS on screen", true);
    }

    @Override
    public boolean hasHud() { return true; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        int fps = client.getCurrentFps();
        int color = fps >= 120 ? 0xFF34D399 : fps >= 60 ? 0xFF55DD88 : fps >= 30 ? 0xFFFBBF24 : 0xFFF87171;
        String num = String.valueOf(fps);
        int tw = client.textRenderer.getWidth(num + " FPS");

        // Background with rounded feel
        int x = 2, pad = 8;
        int bgW = tw + pad * 2 + 2;
        context.fill(x, y - 2, x + bgW, y + 11, 0x8C0A0A12);
        // Accent left bar
        context.fill(x, y - 2, x + 2, y + 11, color & 0x55FFFFFF);
        // Border top/bottom
        context.fill(x, y - 2, x + bgW, y - 1, 0x14FFFFFF);
        context.fill(x, y + 10, x + bgW, y + 11, 0x14FFFFFF);
        // FPS number (bright)
        context.drawText(client.textRenderer, num, x + pad, y, color, true);
        // "FPS" label (dimmed)
        int numW = client.textRenderer.getWidth(num);
        context.drawText(client.textRenderer, " FPS", x + pad + numW, y, color & 0x80FFFFFF, true);
    }
}
