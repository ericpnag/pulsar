package com.bloom.core.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BloomPauseScreen extends Screen {
    private final List<float[]> petals = new ArrayList<>();
    private final Random rng = new Random();

    public BloomPauseScreen() { super(Text.literal("Game Menu")); }

    @Override
    protected void init() {
        petals.clear();
        int[] pinks = {0xBBFFB7C9, 0xAAFFC0CB, 0x99FFD1DC, 0xBBF8A4B8};
        for (int i = 0; i < 20; i++) {
            petals.add(new float[]{
                rng.nextFloat() * this.width, rng.nextFloat() * this.height,
                0.1f + rng.nextFloat() * 0.25f, 0.12f + rng.nextFloat() * 0.2f,
                1 + rng.nextInt(2), Float.intBitsToFloat(pinks[rng.nextInt(pinks.length)]),
                rng.nextFloat() * 6.28f, 1 + rng.nextFloat() * 2
            });
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int w = this.width, h = this.height, cx = w / 2;
        ctx.fill(0, 0, w, h, 0xDD0a0611);

        // Petals
        for (float[] p : petals) {
            p[0] += p[2] * delta; p[1] += p[3] * delta;
            p[6] += p[7] * delta * 0.04f;
            p[0] += (float) Math.sin(p[6]) * 0.08f * delta;
            if (p[1] > h + 5 || p[0] > w + 10) { p[0] = -5; p[1] = rng.nextFloat() * h * 0.3f; }
            int s = (int) p[4];
            ctx.fill((int)p[0], (int)p[1], (int)p[0]+s, (int)p[1]+s, Float.floatToIntBits(p[5]));
        }

        // Panel
        int pw = 170, ph = 150;
        int px = cx - pw / 2, py = h / 2 - ph / 2;
        ctx.fill(px, py, px + pw, py + ph, 0xEE0a0611);
        ctx.fill(px, py, px + pw, py + 1, 0x33FFB7C9);
        ctx.fill(px, py + ph - 1, px + pw, py + ph, 0x22FFB7C9);

        // Title
        String t = "BLOOM";
        int tw = this.textRenderer.getWidth(t);
        ctx.drawText(this.textRenderer, t, cx - tw / 2, py + 12, 0xFFFFD1DC, false);
        String sub = "client";
        int sw = this.textRenderer.getWidth(sub);
        ctx.drawText(this.textRenderer, sub, cx - sw / 2, py + 24, 0xFF5A4550, false);
        ctx.fill(px + 30, py + 36, px + pw - 30, py + 37, 0x22FFB7C9);

        // Buttons
        int btnW = 140, btnH = 16, gap = 2, sy = py + 40;
        drawBtn(ctx, "Resume", cx, sy, btnW, btnH, mx, my, false);
        drawBtn(ctx, "Bloom Mods", cx, sy + btnH + gap, btnW, btnH, mx, my, false);
        drawBtn(ctx, "Cosmetics", cx, sy + (btnH+gap)*2, btnW, btnH, mx, my, false);
        drawBtn(ctx, "Settings", cx, sy + (btnH+gap)*3, btnW, btnH, mx, my, false);
        drawBtn(ctx, "Disconnect", cx, sy + (btnH+gap)*4 + 6, btnW, btnH, mx, my, true);

        super.render(ctx, mx, my, delta);
    }

    private void drawBtn(DrawContext ctx, String label, int cx, int y, int bw, int bh, int mx, int my, boolean danger) {
        int x = cx - bw / 2;
        boolean hov = mx >= x && mx <= x + bw && my >= y && my <= y + bh;
        ctx.fill(x, y, x + bw, y + bh, hov ? 0x33FFB7C9 : 0x15FFFFFF);
        ctx.fill(x, y, x + bw, y + 1, hov ? 0x33FFB7C9 : 0x08FFFFFF);
        if (hov) ctx.fill(x, y, x + 2, y + bh, danger ? 0xBBFF7070 : 0xBBFFB7C9);
        int tw = this.textRenderer.getWidth(label);
        int color = danger ? (hov ? 0xFFFF9090 : 0xFF8A5555) : (hov ? 0xFFF0E4E8 : 0xFFBBA4AC);
        ctx.drawText(this.textRenderer, label, cx - tw / 2, y + (bh - 8) / 2, color, false);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        int cx = this.width / 2, btnW = 140, btnH = 16, gap = 2;
        int ph = 150, py = this.height / 2 - ph / 2, sy = py + 40;
        int x = cx - btnW / 2;
        double mouseX = click.x(), mouseY = click.y();
        for (int i = 0; i < 5; i++) {
            int by = sy + (btnH + gap) * i + (i == 4 ? 6 : 0);
            if (mouseX >= x && mouseX <= x + btnW && mouseY >= by && mouseY <= by + btnH) {
                switch (i) {
                    case 0 -> client.setScreen(null);
                    case 1 -> client.setScreen(new ModuleScreen());
                    case 2 -> client.setScreen(new CosmeticsScreen(this));
                    case 3 -> client.setScreen(new OptionsScreen(this, client.options));
                    case 4 -> { client.disconnect(Text.literal("Disconnected")); client.setScreen(new BloomTitleScreen()); }
                }
                return true;
            }
        }
        return super.mouseClicked(click, bl);
    }

    @Override public boolean shouldPause() { return true; }
}
