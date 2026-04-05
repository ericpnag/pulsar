package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class PingDisplay extends Module {
    public PingDisplay() {
        super("Ping Display", "Show your ping to the server", true);
    }

    @Override
    public boolean hasHud() { return true; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        if (client.player == null || client.getNetworkHandler() == null) return;
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        int ping = entry != null ? entry.getLatency() : 0;
        if (ping <= 0) return;
        int color = ping < 50 ? 0x55DD88 : ping < 100 ? 0xDDBB55 : 0xDD5566;
        String text = ping + " ms";
        int tw = client.textRenderer.getWidth(text);
        context.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        context.fill(2, y - 1, 3, y + 10, color & 0x66FFFFFF);
        context.drawText(client.textRenderer, text, 6, y, color, false);
    }
}
