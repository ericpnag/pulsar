package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

public class PingDisplay extends Module {
    public PingDisplay() { super("Ping Display", "Shows server ping", false); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(MinecraftClient client, int y) {
        int ping = 0;
        if (client.player != null && client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }
        int color = ping < 50 ? 0x55FF55 : ping < 100 ? 0xFFFF55 : 0xFF5555;
        String text = ping + " ms";
        client.inGameHud.fill(2, y - 1, client.textRenderer.getStringWidth(text) + 8, y + 10, 0x44000000);
        client.textRenderer.drawWithShadow(text, 6, y, color);
    }
}
