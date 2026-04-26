package com.bloom.core.toast;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ToastManager {
    private static final List<Toast> toasts = new CopyOnWriteArrayList<>();
    private static final int MAX_VISIBLE = 4;
    private static final int TOAST_WIDTH = 180;
    private static final int TOAST_HEIGHT = 28;
    private static final int TOAST_GAP = 4;
    private static final int MARGIN = 6;
    private static final long DISPLAY_MS = 3000;
    private static final long FADE_IN_MS = 200;
    private static final long FADE_OUT_MS = 300;

    public static void show(String message, int color) {
        toasts.add(new Toast(message, color, System.currentTimeMillis()));
        // Cap at 8 max queued
        while (toasts.size() > 8) toasts.remove(0);
    }

    public static void show(String message) {
        show(message, 0xFFC678DD);
    }

    public static void showSuccess(String message) {
        show(message, 0xFF34D399);
    }

    public static void showError(String message) {
        show(message, 0xFFF87171);
    }

    public static void showWarning(String message) {
        show(message, 0xFFFBBF24);
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (toasts.isEmpty()) return;
        int screenW = client.getWindow().getScaledWidth();
        long now = System.currentTimeMillis();

        // Remove expired toasts (CopyOnWriteArrayList doesn't support Iterator.remove)
        toasts.removeIf(t -> now - t.createdAt > DISPLAY_MS + FADE_OUT_MS);

        int visibleIdx = 0;
        for (int i = toasts.size() - 1; i >= 0 && visibleIdx < MAX_VISIBLE; i--, visibleIdx++) {
            Toast t = toasts.get(i);
            long age = now - t.createdAt;

            // Calculate alpha for fade in/out
            float alpha;
            float slideX;
            if (age < FADE_IN_MS) {
                // Fade in + slide from right
                float p = (float) age / FADE_IN_MS;
                alpha = p;
                slideX = (1f - p) * 40;
            } else if (age > DISPLAY_MS) {
                // Fade out
                float p = (float)(age - DISPLAY_MS) / FADE_OUT_MS;
                alpha = 1f - p;
                slideX = p * 20;
            } else {
                alpha = 1f;
                slideX = 0;
            }

            if (alpha <= 0) continue;

            int tw = client.textRenderer.getWidth(t.message);
            int w = Math.max(tw + 24, TOAST_WIDTH);
            int x = (int)(screenW - w - MARGIN + slideX);
            int y = MARGIN + visibleIdx * (TOAST_HEIGHT + TOAST_GAP);

            int bgAlpha = (int)(alpha * 0.85f * 255);
            int borderAlpha = (int)(alpha * 0.15f * 255);
            int accentAlpha = (int)(alpha * 0.6f * 255);
            int textAlpha = (int)(alpha * 255);

            // Background
            context.fill(x, y, x + w, y + TOAST_HEIGHT, (bgAlpha << 24) | 0x0A0A12);

            // Border
            context.fill(x, y, x + w, y + 1, (borderAlpha << 24) | 0xFFFFFF);
            context.fill(x, y + TOAST_HEIGHT - 1, x + w, y + TOAST_HEIGHT, (borderAlpha << 24) | 0xFFFFFF);
            context.fill(x, y, x + 1, y + TOAST_HEIGHT, (borderAlpha << 24) | 0xFFFFFF);
            context.fill(x + w - 1, y, x + w, y + TOAST_HEIGHT, (borderAlpha << 24) | 0xFFFFFF);

            // Left accent bar
            context.fill(x, y, x + 2, y + TOAST_HEIGHT, (accentAlpha << 24) | (t.color & 0x00FFFFFF));

            // Text
            int textCol = (textAlpha << 24) | 0xFFFFFF;
            context.drawText(client.textRenderer, t.message, x + 10, y + (TOAST_HEIGHT - 8) / 2, textCol, true);
        }
    }

    private static class Toast {
        final String message;
        final int color;
        final long createdAt;

        Toast(String message, int color, long createdAt) {
            this.message = message;
            this.color = color;
            this.createdAt = createdAt;
        }
    }
}
