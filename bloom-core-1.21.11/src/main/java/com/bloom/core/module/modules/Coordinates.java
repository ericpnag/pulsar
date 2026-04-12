package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class Coordinates extends Module {
    public Coordinates() {
        super("Coordinates", "Show XYZ coordinates on screen", true);
    }

    @Override
    public boolean hasHud() { return true; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        if (client.player == null) return;
        String coords = String.format("%.0f / %.0f / %.0f",
                client.player.getX(), client.player.getY(), client.player.getZ());
        int tw = client.textRenderer.getWidth(coords);
        context.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        context.fill(2, y - 1, 3, y + 10, 0x44C070DD);
        context.drawText(client.textRenderer, coords, 6, y, 0xFFCCBBAA, true);
    }
}
