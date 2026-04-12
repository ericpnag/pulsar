package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;

import java.util.ArrayList;
import java.util.List;

public class TntTimer extends Module {
    public static boolean active = false;
    private final List<TntInfo> tntList = new ArrayList<>();

    public TntTimer() {
        super("TNT Timer", "Shows fuse countdown on TNT", false);
    }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; tntList.clear(); }
    @Override public boolean hasHud() { return true; }

    @Override
    public int getHudHeight() {
        return Math.max(12, tntList.size() * 12);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (!active || client.world == null || client.player == null) {
            tntList.clear();
            return;
        }

        tntList.clear();
        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof TntEntity tnt) {
                double dist = tnt.squaredDistanceTo(client.player);
                if (dist < 4096) { // within 64 blocks
                    int fuse = tnt.getFuse();
                    float seconds = fuse / 20.0f;
                    tntList.add(new TntInfo(seconds, dist));
                }
            }
        }
        // Sort by distance (closest first)
        tntList.sort((a, b) -> Double.compare(a.distance, b.distance));
    }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        if (tntList.isEmpty()) {
            String text = "TNT: None";
            int tw = client.textRenderer.getWidth(text);
            context.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
            context.fill(2, y - 1, 3, y + 10, 0x66FF4444);
            context.drawText(client.textRenderer, text, 6, y, 0xFFAAAAAA, true);
            return;
        }

        for (int i = 0; i < tntList.size() && i < 5; i++) {
            TntInfo info = tntList.get(i);
            int color = info.seconds < 1.0f ? 0xFFFF4444 : info.seconds < 2.0f ? 0xFFFFAA44 : 0xFFFFFF44;
            String text = String.format("TNT: %.1fs", info.seconds);
            int tw = client.textRenderer.getWidth(text);
            int drawY = y + (i * 12);
            context.fill(2, drawY - 1, tw + 8, drawY + 10, 0x44000000);
            context.fill(2, drawY - 1, 3, drawY + 10, color & 0x66FFFFFF);
            context.drawText(client.textRenderer, text, 6, drawY, color, true);
        }
    }

    private record TntInfo(float seconds, double distance) {}
}
