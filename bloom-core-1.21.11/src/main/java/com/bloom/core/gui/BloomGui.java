package com.bloom.core.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Shared rendering utilities for all Pulsar GUI screens. */
public class BloomGui {
    public static final StyleSpriteSource BLOOM_FONT =
        new StyleSpriteSource.Font(Identifier.of("bloom-core", "inter"));

    public static Text text(String s, int color) {
        return Text.literal(s).setStyle(Style.EMPTY.withFont(BLOOM_FONT).withColor(color));
    }

    public static int textW(TextRenderer tr, String s) {
        return tr.getWidth(text(s, 0xFFFFFF));
    }

    public static void drawRoundRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 1, y, x + w - 1, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    public static void drawRoundRectOutline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 1, y, x + w - 1, y + 1, color);
        ctx.fill(x + 1, y + h - 1, x + w - 1, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    public static int lerpColor(int from, int to, float t) {
        t = Math.max(0, Math.min(1, t));
        int fr = (from >> 16) & 0xFF, fg = (from >> 8) & 0xFF, fb = from & 0xFF;
        int tr2 = (to >> 16) & 0xFF, tg = (to >> 8) & 0xFF, tb = to & 0xFF;
        int r = (int)(fr + (tr2 - fr) * t);
        int g = (int)(fg + (tg - fg) * t);
        int b = (int)(fb + (tb - fb) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static float easeOutCubic(float t) {
        return 1 - (1 - t) * (1 - t) * (1 - t);
    }
}
