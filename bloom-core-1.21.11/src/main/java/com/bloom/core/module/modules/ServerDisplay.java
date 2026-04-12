package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ServerDisplay extends Module {
    public ServerDisplay() { super("Server Display", "Shows current server IP", false); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(DrawContext ctx, MinecraftClient client, int y) {
        String server = "Singleplayer";
        if (client.getCurrentServerEntry() != null) server = client.getCurrentServerEntry().address;
        int tw = client.textRenderer.getWidth(server);
        ctx.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        ctx.fill(2, y - 1, 3, y + 10, 0x44C070DD);
        ctx.drawText(client.textRenderer, server, 6, y, 0xFFABB2BF, true);
    }
}
