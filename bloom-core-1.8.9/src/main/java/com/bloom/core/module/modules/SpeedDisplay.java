package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;

public class SpeedDisplay extends Module {
    private double lastX, lastZ, speed;
    public SpeedDisplay() { super("Speed Display", "Shows movement speed", false); }
    @Override public void onTick(MinecraftClient client) {
        if (client.player != null) {
            double dx = client.player.x - lastX, dz = client.player.z - lastZ;
            speed = Math.sqrt(dx * dx + dz * dz) * 20;
            lastX = client.player.x; lastZ = client.player.z;
        }
    }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(MinecraftClient client, int y) {
        String text = String.format("Speed: %.1f b/s", speed);
        client.inGameHud.fill(2, y - 1, client.textRenderer.getStringWidth(text) + 8, y + 10, 0x44000000);
        client.textRenderer.drawWithShadow(text, 6, y, 0xC0C0C0);
    }
}
