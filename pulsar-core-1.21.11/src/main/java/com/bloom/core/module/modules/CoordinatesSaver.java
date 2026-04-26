package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

public class CoordinatesSaver extends Module {
    public static List<String> savedCoords = new ArrayList<>();

    public CoordinatesSaver() {
        super("Coord Saver", "Save and recall coordinate locations", false);
    }

    @Override
    public boolean hasHud() { return true; }

    @Override
    public int getHudHeight() { return 12; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        String text = "Saved: " + savedCoords.size() + " coords";
        int color = 0xFFAADDFF;
        int tw = client.textRenderer.getWidth(text);
        context.fill(2, y - 2, tw + 8, y + 11, 0x8C0A0A12);
        context.fill(2, y - 2, 3, y + 11, color & 0x44FFFFFF);
        context.fill(2, y - 2, tw + 8, y - 1, 0x14FFFFFF);
        context.fill(2, y + 10, tw + 8, y + 11, 0x14FFFFFF);
        context.drawText(client.textRenderer, text, 6, y, color, true);
    }
}
