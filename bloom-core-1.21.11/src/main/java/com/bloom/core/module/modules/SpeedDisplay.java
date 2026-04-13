package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import static com.bloom.core.gui.BloomGui.*;

public class SpeedDisplay extends Module {
    private double lastX, lastZ;
    private double speed;

    public SpeedDisplay() {
        super("Speed Display", "Shows current movement speed", false);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player != null) {
            double dx = client.player.getX() - lastX;
            double dz = client.player.getZ() - lastZ;
            speed = Math.sqrt(dx * dx + dz * dz) * 20; // blocks per second
            lastX = client.player.getX();
            lastZ = client.player.getZ();
        }
    }

    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return 12; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        String text = String.format("Speed: %.1f b/s", speed);
        context.drawText(client.textRenderer, text(text, 0xC0C0C0), 4, y, -1, true);
    }
}
