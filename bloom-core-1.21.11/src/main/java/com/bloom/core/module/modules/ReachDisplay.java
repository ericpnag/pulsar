package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ReachDisplay extends Module {
    private double lastReach = 0;
    private long lastHitTime = 0;

    public ReachDisplay() { super("Reach Display", "Shows attack reach distance", false); }

    @Override public void onTick(MinecraftClient client) {
        if (client.player == null || client.crosshairTarget == null) return;
        if (client.player.handSwinging && client.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.ENTITY) {
            lastReach = client.crosshairTarget.getPos().distanceTo(client.player.getEyePos());
            lastHitTime = System.currentTimeMillis();
        }
    }

    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(DrawContext ctx, MinecraftClient client, int y) {
        if (System.currentTimeMillis() - lastHitTime > 3000) return;
        String text = String.format("%.1f blocks", lastReach);
        int tw = client.textRenderer.getWidth(text);
        ctx.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        ctx.fill(2, y - 1, 3, y + 10, 0x44C070DD);
        ctx.drawText(client.textRenderer, text, 6, y, 0xFFABB2BF, true);
    }
}
