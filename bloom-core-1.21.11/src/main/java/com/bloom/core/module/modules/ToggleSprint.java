package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ToggleSprint extends Module {
    private boolean sprinting = false;

    public ToggleSprint() {
        super("Toggle Sprint", "Auto-sprint without holding key", true);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        if (client.player.input.hasForwardMovement() && !client.player.isSneaking()) {
            client.player.setSprinting(true);
            sprinting = true;
        } else {
            sprinting = false;
        }
    }

    @Override
    public boolean hasHud() { return true; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        String text = sprinting ? "Sprinting" : "Sprint: ON";
        int tw = client.textRenderer.getWidth(text);
        int color = sprinting ? 0xFFFFB7C9 : 0xFF887778;
        context.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        context.fill(2, y - 1, 3, y + 10, sprinting ? 0x66FFB0C0 : 0x33554444);
        context.drawText(client.textRenderer, text, 6, y, color, true);
    }
}
