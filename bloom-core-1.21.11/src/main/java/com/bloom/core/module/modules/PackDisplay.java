package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class PackDisplay extends Module {
    public PackDisplay() { super("Pack Display", "Shows active resource pack", false); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(DrawContext ctx, MinecraftClient client, int y) {
        int count = client.getResourcePackManager().getEnabledIds().size() - 1; // minus default
        String text = count > 0 ? count + " pack" + (count > 1 ? "s" : "") : "Default";
        int tw = client.textRenderer.getWidth(text);
        ctx.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        ctx.fill(2, y - 1, 3, y + 10, 0x44C070DD);
        ctx.drawText(client.textRenderer, text, 6, y, 0xFFABB2BF, true);
    }
}
