package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ToggleSneak extends Module {
    public ToggleSneak() { super("Toggle Sneak", "Auto-sneak without holding key", false); }
    @Override public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        if (!client.player.isSprinting()) client.player.setSneaking(true);
    }
    @Override public void onDisable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.player.setSneaking(false);
    }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(DrawContext ctx, MinecraftClient client, int y) {
        String text = "Sneaking";
        int tw = client.textRenderer.getWidth(text);
        ctx.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        ctx.fill(2, y - 1, 3, y + 10, 0x44C070DD);
        ctx.drawText(client.textRenderer, text, 6, y, 0xFFABB2BF, true);
    }
}
