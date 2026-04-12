package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ComboCounter extends Module {
    private int combo = 0;
    private long lastHitTime = 0;
    private boolean wasSwinging = false;

    public ComboCounter() { super("Combo Counter", "Tracks consecutive hits", false); }

    @Override public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        // Reset combo after 3 seconds of no hits
        if (System.currentTimeMillis() - lastHitTime > 3000 && combo > 0) combo = 0;

        // Only count on the START of a swing (transition from not swinging to swinging)
        // AND only if targeting an entity
        boolean swinging = client.player.handSwinging;
        if (swinging && !wasSwinging && client.crosshairTarget != null &&
            client.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.ENTITY) {
            combo++;
            lastHitTime = System.currentTimeMillis();
        }
        wasSwinging = swinging;
    }

    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(DrawContext ctx, MinecraftClient client, int y) {
        if (combo <= 0) return;
        String text = combo + " Combo";
        int tw = client.textRenderer.getWidth(text);
        ctx.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        ctx.fill(2, y - 1, 3, y + 10, 0x44C070DD);
        ctx.drawText(client.textRenderer, text, 6, y, 0xFFC678DD, true);
    }
}
