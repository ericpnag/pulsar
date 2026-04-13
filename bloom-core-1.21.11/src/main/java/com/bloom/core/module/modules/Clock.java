package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import static com.bloom.core.gui.BloomGui.*;

public class Clock extends Module {
    public Clock() {
        super("Clock", "Shows real-world time on HUD", false);
    }

    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return 12; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        java.time.LocalTime now = java.time.LocalTime.now();
        String time = String.format("%02d:%02d", now.getHour(), now.getMinute());
        context.drawText(client.textRenderer, text(time, 0xC0C0C0), 4, y, -1, true);
    }
}
