package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class DirectionHud extends Module {
    public DirectionHud() { super("Direction HUD", "Shows compass direction", false); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(DrawContext ctx, MinecraftClient client, int y) {
        if (client.player == null) return;
        float yaw = ((client.player.getYaw() % 360) + 360) % 360;
        String dir;
        if (yaw >= 315 || yaw < 45) dir = "S";
        else if (yaw >= 45 && yaw < 135) dir = "W";
        else if (yaw >= 135 && yaw < 225) dir = "N";
        else dir = "E";
        String text = dir + " (" + (int)yaw + ")";
        int tw = client.textRenderer.getWidth(text);
        ctx.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        ctx.fill(2, y - 1, 3, y + 10, 0x44C070DD);
        ctx.drawText(client.textRenderer, text, 6, y, 0xFFABB2BF, true);
    }
}
