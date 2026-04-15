package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;

public class ReachDisplay extends Module {
    private double lastReach = 0;
    private long lastHitTime = 0;
    public ReachDisplay() { super("Reach Display", "Shows hit reach distance", false); }
    @Override public void onTick(MinecraftClient client) {
        if (client.player != null && client.targetedEntity != null) {
            lastReach = client.player.squaredDistanceTo(client.targetedEntity);
            lastReach = Math.sqrt(lastReach);
            lastHitTime = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - lastHitTime > 3000) lastReach = 0;
    }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(MinecraftClient client, int y) {
        if (lastReach <= 0) return;
        String text = String.format("Reach: %.2f", lastReach);
        client.inGameHud.fill(2, y - 1, client.textRenderer.getStringWidth(text) + 8, y + 10, 0x44000000);
        client.textRenderer.drawWithShadow(text, 6, y, 0xE06C75);
    }
}
