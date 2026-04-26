package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.List;

public class FovChanger extends Module {
    public static boolean active = false;
    public static float fovValue = 100.0f;

    public FovChanger() {
        super("FOV Changer", "Override field of view", false);
    }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }
    @Override public boolean hasHud() { return true; }

    @Override
    public List<ModuleSetting> getSettings() {
        return List.of(
            new ModuleSetting("FOV", () -> fovValue, v -> fovValue = v, 30f, 150f, 1f)
        );
    }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        String text = "FOV: " + (int) fovValue;
        int color = 0xFF88CCFF;
        int tw = client.textRenderer.getWidth(text);
        context.fill(2, y - 2, tw + 8, y + 11, 0x8C0A0A12);
        context.fill(2, y - 2, 3, y + 11, color & 0x44FFFFFF);
        context.fill(2, y - 2, tw + 8, y - 1, 0x14FFFFFF);
        context.fill(2, y + 10, tw + 8, y + 11, 0x14FFFFFF);
        context.drawText(client.textRenderer, text, 6, y, color, true);
    }
}
