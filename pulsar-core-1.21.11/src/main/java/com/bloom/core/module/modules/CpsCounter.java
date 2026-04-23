package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CpsCounter extends Module {
    private final List<Long> leftClicks = new ArrayList<>();
    private final List<Long> rightClicks = new ArrayList<>();
    private boolean wasLeftPressed = false;
    private boolean wasRightPressed = false;

    // Sparkline: last 50 frames of total CPS
    private final int[] sparkline = new int[50];
    private int sparkIdx = 0;
    private int frameTick = 0;

    public CpsCounter() {
        super("CPS Counter", "Show clicks per second (left + right)", true);
    }

    @Override
    public void onTick(MinecraftClient client) {
        long now = System.currentTimeMillis();
        leftClicks.removeIf(t -> now - t > 1000);
        rightClicks.removeIf(t -> now - t > 1000);

        // Update sparkline every 4 ticks (~200ms)
        frameTick++;
        if (frameTick % 4 == 0) {
            sparkline[sparkIdx % sparkline.length] = leftClicks.size() + rightClicks.size();
            sparkIdx++;
        }
    }

    @Override
    public boolean hasHud() { return true; }

    @Override
    public int getHudHeight() { return 22; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        if (client.getWindow() != null) {
            long handle = client.getWindow().getHandle();
            boolean leftDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            boolean rightDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

            if (leftDown && !wasLeftPressed) leftClicks.add(System.currentTimeMillis());
            if (rightDown && !wasRightPressed) rightClicks.add(System.currentTimeMillis());
            wasLeftPressed = leftDown;
            wasRightPressed = rightDown;
        }

        int left = leftClicks.size();
        int right = rightClicks.size();
        int total = left + right;

        // Color based on CPS intensity
        int color = total >= 15 ? 0xFFC678DD : total >= 8 ? 0xFF34D399 : 0xFFA0A0B0;

        String text = left + " | " + right;
        int tw = client.textRenderer.getWidth(text + "  CPS");
        int x = 2, pad = 8;
        int bgW = tw + pad * 2 + 52; // extra space for sparkline

        // Background
        context.fill(x, y - 2, x + bgW, y + 19, 0x8C0A0A12);
        // Accent bar
        context.fill(x, y - 2, x + 2, y + 19, color & 0x44FFFFFF);
        // Border
        context.fill(x, y - 2, x + bgW, y - 1, 0x14FFFFFF);
        context.fill(x, y + 18, x + bgW, y + 19, 0x14FFFFFF);

        // CPS numbers (bright)
        context.drawText(client.textRenderer, text, x + pad, y + 1, color, true);
        int numW = client.textRenderer.getWidth(text);
        context.drawText(client.textRenderer, "  CPS", x + pad + numW, y + 1, color & 0x60FFFFFF, true);

        // Sparkline
        int sparkX = x + pad + tw + 8;
        int sparkW = 40;
        int sparkH = 10;
        int sparkY = y + 4;

        // Find max for normalization
        int max = 1;
        for (int v : sparkline) if (v > max) max = v;

        // Draw sparkline bars
        int barW = Math.max(1, sparkW / sparkline.length);
        for (int i = 0; i < sparkline.length; i++) {
            int idx = (sparkIdx - sparkline.length + i + sparkline.length * 2) % sparkline.length;
            int val = sparkline[idx];
            int barH = (int)((float) val / max * sparkH);
            if (barH < 1 && val > 0) barH = 1;
            int bx = sparkX + i * barW;
            int alpha = 40 + (int)((float) i / sparkline.length * 80);
            context.fill(bx, sparkY + sparkH - barH, bx + Math.max(barW - 1, 1), sparkY + sparkH,
                    (alpha << 24) | (color & 0x00FFFFFF));
        }
    }
}
