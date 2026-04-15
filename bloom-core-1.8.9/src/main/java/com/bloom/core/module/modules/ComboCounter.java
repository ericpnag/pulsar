package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;

public class ComboCounter extends Module {
    private int combo = 0;
    private long lastHitTime = 0;
    private boolean wasSwinging = false;

    public ComboCounter() { super("Combo Counter", "Tracks consecutive hits in PvP", false); }

    @Override public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        if (System.currentTimeMillis() - lastHitTime > 3000 && combo > 0) combo = 0;
        boolean swinging = client.player.handSwinging;
        if (swinging && !wasSwinging) {
            combo++;
            lastHitTime = System.currentTimeMillis();
        }
        wasSwinging = swinging;
    }

    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(MinecraftClient client, int y) {
        if (combo <= 0) return;
        String text = combo + " Combo";
        client.inGameHud.fill(2, y - 1, client.textRenderer.getStringWidth(text) + 8, y + 10, 0x44000000);
        client.textRenderer.drawWithShadow(text, 6, y, 0xFFAA00);
    }
}
