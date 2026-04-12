package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class MemoryDisplay extends Module {
    public MemoryDisplay() { super("Memory Display", "Shows RAM usage", false); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(DrawContext ctx, MinecraftClient client, int y) {
        long used = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
        long max = Runtime.getRuntime().maxMemory() / 1048576;
        int pct = (int)(used * 100 / max);
        int color = pct < 60 ? 0xFF98C379 : pct < 80 ? 0xFFF0CC60 : 0xFFE06C75;
        String text = used + "/" + max + " MB";
        int tw = client.textRenderer.getWidth(text);
        ctx.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        ctx.fill(2, y - 1, 3, y + 10, color & 0x66FFFFFF);
        ctx.drawText(client.textRenderer, text, 6, y, color, true);
    }
}
