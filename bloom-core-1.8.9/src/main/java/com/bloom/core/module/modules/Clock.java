package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;

public class Clock extends Module {
    public Clock() { super("Clock", "Shows real-world time", false); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(MinecraftClient client, int y) {
        java.time.LocalTime now = java.time.LocalTime.now();
        String text = String.format("%02d:%02d", now.getHour(), now.getMinute());
        client.inGameHud.fill(2, y - 1, client.textRenderer.getStringWidth(text) + 8, y + 10, 0x44000000);
        client.textRenderer.drawWithShadow(text, 6, y, 0xC0C0C0);
    }
}
