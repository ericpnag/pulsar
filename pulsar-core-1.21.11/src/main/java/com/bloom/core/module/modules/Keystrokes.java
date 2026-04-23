package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public class Keystrokes extends Module {
    private static final int KEY_SIZE = 18;
    private static final int KEY_GAP = 2;
    private static final int ACCENT = 0xFFC678DD;
    private static final int BG_IDLE = 0x8C0A0A12;
    private static final int BG_PRESSED = 0xBB3A2050;
    private static final int BORDER_IDLE = 0x14FFFFFF;
    private static final int BORDER_PRESSED = 0x44C678DD;

    // Animation smoothing (0-1 per key)
    private final float[] keyAnim = new float[7]; // W A S D LMB RMB Space

    public Keystrokes() {
        super("Keystrokes", "Show WASD and mouse keys on screen", true);
    }

    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return KEY_SIZE * 3 + KEY_GAP * 2 + 6; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        if (client.player == null || client.getWindow() == null) return;
        int baseX = 4;
        int baseY = y + 2;
        long handle = client.getWindow().getHandle();

        boolean[] pressed = {
            GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS,
            GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS,
            GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS,
            GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS,
            GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS,
            GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS,
            GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS,
        };

        // Smooth animations
        for (int i = 0; i < 7; i++) {
            float target = pressed[i] ? 1f : 0f;
            keyAnim[i] += (target - keyAnim[i]) * 0.35f;
            if (Math.abs(keyAnim[i] - target) < 0.01f) keyAnim[i] = target;
        }

        int step = KEY_SIZE + KEY_GAP;

        // Row 1: W (centered)
        drawKey(context, client, "W", baseX + step, baseY, keyAnim[0]);

        // Row 2: A S D
        drawKey(context, client, "A", baseX, baseY + step, keyAnim[1]);
        drawKey(context, client, "S", baseX + step, baseY + step, keyAnim[2]);
        drawKey(context, client, "D", baseX + step * 2, baseY + step, keyAnim[3]);

        // Row 3: LMB | RMB (wider buttons)
        int mouseY = baseY + step * 2;
        int mouseW = KEY_SIZE + KEY_SIZE / 2;
        drawMouseKey(context, client, "LMB", baseX, mouseY, mouseW, keyAnim[4]);
        drawMouseKey(context, client, "RMB", baseX + mouseW + KEY_GAP, mouseY, mouseW + 1, keyAnim[5]);
    }

    private void drawKey(DrawContext ctx, MinecraftClient client, String key, int x, int y, float anim) {
        int bg = lerpColor(BG_IDLE, BG_PRESSED, anim);
        int border = lerpColor(BORDER_IDLE, BORDER_PRESSED, anim);
        float scale = 1f - anim * 0.06f;

        // Background
        ctx.fill(x, y, x + KEY_SIZE, y + KEY_SIZE, bg);
        // Borders
        ctx.fill(x, y, x + KEY_SIZE, y + 1, border);
        ctx.fill(x, y + KEY_SIZE - 1, x + KEY_SIZE, y + KEY_SIZE, border);
        ctx.fill(x, y, x + 1, y + KEY_SIZE, border);
        ctx.fill(x + KEY_SIZE - 1, y, x + KEY_SIZE, y + KEY_SIZE, border);

        // Accent bottom bar when pressed
        if (anim > 0.1f) {
            int barAlpha = (int)(anim * 180);
            ctx.fill(x + 2, y + KEY_SIZE - 2, x + KEY_SIZE - 2, y + KEY_SIZE - 1, (barAlpha << 24) | (ACCENT & 0x00FFFFFF));
        }

        // Text
        int textColor = lerpColor(0xFF707070, 0xFFFFFFFF, anim);
        int tw = client.textRenderer.getWidth(key);
        ctx.drawText(client.textRenderer, key, x + (KEY_SIZE - tw) / 2, y + (KEY_SIZE - 8) / 2, textColor, true);
    }

    private void drawMouseKey(DrawContext ctx, MinecraftClient client, String key, int x, int y, int w, float anim) {
        int bg = lerpColor(BG_IDLE, BG_PRESSED, anim);
        int border = lerpColor(BORDER_IDLE, BORDER_PRESSED, anim);

        ctx.fill(x, y, x + w, y + KEY_SIZE, bg);
        ctx.fill(x, y, x + w, y + 1, border);
        ctx.fill(x, y + KEY_SIZE - 1, x + w, y + KEY_SIZE, border);
        ctx.fill(x, y, x + 1, y + KEY_SIZE, border);
        ctx.fill(x + w - 1, y, x + w, y + KEY_SIZE, border);

        if (anim > 0.1f) {
            int barAlpha = (int)(anim * 180);
            ctx.fill(x + 2, y + KEY_SIZE - 2, x + w - 2, y + KEY_SIZE - 1, (barAlpha << 24) | (ACCENT & 0x00FFFFFF));
        }

        int textColor = lerpColor(0xFF707070, 0xFFFFFFFF, anim);
        int tw = client.textRenderer.getWidth(key);
        ctx.drawText(client.textRenderer, key, x + (w - tw) / 2, y + (KEY_SIZE - 8) / 2, textColor, true);
    }

    private static int lerpColor(int a, int b, float t) {
        int aA = (a >> 24) & 0xFF, aR = (a >> 16) & 0xFF, aG = (a >> 8) & 0xFF, aB = a & 0xFF;
        int bA = (b >> 24) & 0xFF, bR = (b >> 16) & 0xFF, bG = (b >> 8) & 0xFF, bB = b & 0xFF;
        return ((int)(aA + (bA - aA) * t) << 24) | ((int)(aR + (bR - aR) * t) << 16)
             | ((int)(aG + (bG - aG) * t) << 8) | (int)(aB + (bB - aB) * t);
    }
}
