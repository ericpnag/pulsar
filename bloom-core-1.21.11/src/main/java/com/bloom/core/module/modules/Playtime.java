package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import static com.bloom.core.gui.BloomGui.*;

public class Playtime extends Module {
    private static long sessionStart = System.currentTimeMillis();

    public Playtime() {
        super("Playtime", "Shows session play time", false);
    }

    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return 12; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        long elapsed = (System.currentTimeMillis() - sessionStart) / 1000;
        long h = elapsed / 3600, m = (elapsed % 3600) / 60;
        String time = h > 0 ? String.format("Session: %dh %dm", h, m) : String.format("Session: %dm", m);
        context.drawText(client.textRenderer, text(time, 0xA0A0A0), 4, y, -1, true);
    }
}
