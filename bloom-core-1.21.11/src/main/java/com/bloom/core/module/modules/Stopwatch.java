package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import static com.bloom.core.gui.BloomGui.*;

public class Stopwatch extends Module {
    private long startTime = 0;

    public Stopwatch() {
        super("Stopwatch", "In-game stopwatch timer", false);
    }

    @Override public void onEnable() { startTime = System.currentTimeMillis(); }

    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return 12; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        long elapsed = System.currentTimeMillis() - startTime;
        long mins = (elapsed / 60000) % 60;
        long secs = (elapsed / 1000) % 60;
        long ms = (elapsed / 100) % 10;
        String time = String.format("Timer: %02d:%02d.%d", mins, secs, ms);
        context.drawText(client.textRenderer, text(time, 0xF0CC60), 4, y, -1, true);
    }
}
