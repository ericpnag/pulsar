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
        int color = fps >= 60 ? 0x55DD88 : fps >= 30 ? 0xDDBB55 : 0xDD5566;
        // Background pill
        String text = fps + " FPS";
        int tw = client.textRenderer.getWidth(text);
        context.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        context.fill(2, y - 1, 3, y + 10, color & 0x66FFFFFF);
        context.drawText(client.textRenderer, text, 6, y, color, false);
    }
}
