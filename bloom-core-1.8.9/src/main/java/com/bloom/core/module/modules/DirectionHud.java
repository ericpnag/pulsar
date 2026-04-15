package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;

public class DirectionHud extends Module {
    public DirectionHud() { super("Direction HUD", "Shows facing direction", false); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(MinecraftClient client, int y) {
        if (client.player == null) return;
        float yaw = client.player.yaw % 360;
        if (yaw < 0) yaw += 360;
        String dir;
        if (yaw >= 315 || yaw < 45) dir = "S";
        else if (yaw < 135) dir = "W";
        else if (yaw < 225) dir = "N";
        else dir = "E";
        String text = dir + " (" + String.format("%.0f", yaw) + ")";
        client.inGameHud.fill(2, y - 1, client.textRenderer.getStringWidth(text) + 8, y + 10, 0x44000000);
        client.textRenderer.drawWithShadow(text, 6, y, 0x60C0F0);
    }
}
