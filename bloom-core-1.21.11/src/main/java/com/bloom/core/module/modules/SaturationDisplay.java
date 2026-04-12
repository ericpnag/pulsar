package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class SaturationDisplay extends Module {
    public SaturationDisplay() { super("Saturation", "Shows food saturation level", false); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(DrawContext ctx, MinecraftClient client, int y) {
        if (client.player == null) return;
        float sat = client.player.getHungerManager().getSaturationLevel();
        int food = client.player.getHungerManager().getFoodLevel();
        String text = "Food: " + food + " | Sat: " + String.format("%.1f", sat);
        int tw = client.textRenderer.getWidth(text);
        int color = food > 14 ? 0xFF98C379 : food > 6 ? 0xFFF0CC60 : 0xFFE06C75;
        ctx.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        ctx.fill(2, y - 1, 3, y + 10, color & 0x66FFFFFF);
        ctx.drawText(client.textRenderer, text, 6, y, color, true);
    }
}
