package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.*;

import static com.bloom.core.gui.BloomGui.*;

public class BedwarsOverlay extends Module {
    // Resource tracking
    private int iron, gold, diamonds, emeralds;

    // Team colors for Bedwars
    private static final int[] TEAM_COLORS = {
        0xFF5555, // Red
        0x5555FF, // Blue
        0x55FF55, // Green
        0xFFFF55, // Yellow
        0xFFFFFF, // White
        0xFF55FF, // Pink
        0x00AAAA, // Cyan
        0xAAAAAA, // Gray
    };
    private static final String[] TEAM_NAMES = {"Red", "Blue", "Green", "Yellow", "White", "Pink", "Cyan", "Gray"};

    // Bed destruction alerts
    private static final List<String> alerts = new ArrayList<>();
    private static final List<Long> alertTimes = new ArrayList<>();

    public BedwarsOverlay() {
        super("Bedwars Overlay", "Resource tracker, bed alerts, team info for Hypixel Bedwars", false);
    }

    public static void addAlert(String message) {
        alerts.add(message);
        alertTimes.add(System.currentTimeMillis());
        // Keep last 5 alerts
        while (alerts.size() > 5) { alerts.remove(0); alertTimes.remove(0); }
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null) return;

        // Count resources in inventory
        iron = client.player.getInventory().count(Items.IRON_INGOT);
        gold = client.player.getInventory().count(Items.GOLD_INGOT);
        diamonds = client.player.getInventory().count(Items.DIAMOND);
        emeralds = client.player.getInventory().count(Items.EMERALD);

        // Check chat for bed destruction messages
        // (This is handled by the chat listener in onTick)

        // Clean up old alerts (> 5 seconds)
        long now = System.currentTimeMillis();
        while (!alertTimes.isEmpty() && now - alertTimes.get(0) > 5000) {
            alerts.remove(0);
            alertTimes.remove(0);
        }
    }

    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return 52; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        if (client.player == null) return;

        // Resource bar
        int x = 4;
        context.drawText(client.textRenderer, text("BW Resources", 0x808080), x, y, -1, true);
        y += 11;

        // Iron
        context.drawText(client.textRenderer, text("Fe", 0xC0C0C0), x, y, -1, true);
        context.drawText(client.textRenderer, text(" " + iron, 0xE0E0E0), x + 12, y, -1, true);

        // Gold
        context.drawText(client.textRenderer, text("Au", 0xFFD700), x + 40, y, -1, true);
        context.drawText(client.textRenderer, text(" " + gold, 0xE0E0E0), x + 52, y, -1, true);

        // Diamond
        context.drawText(client.textRenderer, text("D", 0x55FFFF), x + 78, y, -1, true);
        context.drawText(client.textRenderer, text(" " + diamonds, 0xE0E0E0), x + 86, y, -1, true);

        // Emerald
        context.drawText(client.textRenderer, text("E", 0x55FF55), x + 108, y, -1, true);
        context.drawText(client.textRenderer, text(" " + emeralds, 0xE0E0E0), x + 116, y, -1, true);
        y += 12;

        // Player count on server
        if (client.getNetworkHandler() != null) {
            int players = client.getNetworkHandler().getPlayerList().size();
            context.drawText(client.textRenderer, text("Players: " + players, 0x707070), x, y, -1, true);
        }
        y += 12;

        // Alerts
        long now = System.currentTimeMillis();
        for (int i = 0; i < alerts.size(); i++) {
            long age = now - alertTimes.get(i);
            float fade = age > 4000 ? 1.0f - (age - 4000) / 1000f : 1.0f;
            int alpha = (int)(fade * 255);
            if (alpha < 5) continue;
            int col = (alpha << 24) | 0xFF5555;
            context.drawText(client.textRenderer, text(alerts.get(i), 0xFF5555), x, y + i * 10, col, true);
        }
    }
}
