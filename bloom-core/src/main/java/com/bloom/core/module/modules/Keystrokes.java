package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public class Keystrokes extends Module {
    public Keystrokes() {
        super("Keystrokes", "Show WASD and mouse keys on screen", false);
    }

    @Override
    public boolean hasHud() { return true; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        if (client.player == null || client.getWindow() == null) return;
        int screenW = client.getWindow().getScaledWidth();
        int baseX = screenW / 2 - 26;
        int baseY = client.getWindow().getScaledHeight() - 70;
        long handle = client.getWindow().getHandle();

        drawKey(context, client, "W", baseX + 12, baseY, GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS);
        drawKey(context, client, "A", baseX, baseY + 14, GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS);
        drawKey(context, client, "S", baseX + 12, baseY + 14, GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS);
        drawKey(context, client, "D", baseX + 24, baseY + 14, GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS);

        // Mouse buttons
        boolean lmb = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rmb = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        drawKey(context, client, "L", baseX + 4, baseY + 28, lmb);
        drawKey(context, client, "R", baseX + 20, baseY + 28, rmb);
    }

    private void drawKey(DrawContext ctx, MinecraftClient client, String key, int x, int y, boolean pressed) {
        ctx.fill(x, y, x + 11, y + 12, pressed ? 0x88FFB0C0 : 0x44000000);
        int tw = client.textRenderer.getWidth(key);
        ctx.drawText(client.textRenderer, key, x + (11 - tw) / 2, y + 2, pressed ? 0xFFFFFF : 0x888888, false);
    }
}
