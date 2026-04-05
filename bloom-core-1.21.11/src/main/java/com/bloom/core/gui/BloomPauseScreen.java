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
        for (int i = 0; i < 25; i++) {
            petals.add(new float[]{
                rng.nextFloat() * this.width, rng.nextFloat() * this.height,
                0.1f + rng.nextFloat() * 0.3f, 0.15f + rng.nextFloat() * 0.25f,
                1 + rng.nextInt(2), Float.intBitsToFloat(pinks[rng.nextInt(pinks.length)]),
                rng.nextFloat() * 6.28f, 1 + rng.nextFloat() * 2
            });
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int w = this.width; int h = this.height; int cx = w / 2;
        context.fill(0, 0, w, h, 0xCC0d0810);

        for (float[] p : petals) {
            p[0] += p[2] * delta; p[1] += p[3] * delta;
            p[6] += p[7] * delta * 0.05f;
            p[0] += (float) Math.sin(p[6]) * 0.1f * delta;
            if (p[1] > h + 5 || p[0] > w + 10) { p[0] = -5; p[1] = rng.nextFloat() * h * 0.3f; }
            int s = (int) p[4];
            context.fill((int) p[0], (int) p[1], (int) p[0] + s, (int) p[1] + s, Float.floatToIntBits(p[5]));
        }

        int pw = 150; int ph = 130;
        int px = cx - pw / 2; int py = h / 2 - ph / 2;
        context.fill(px, py, px + pw, py + ph, 0xDD0d0810);
        context.fill(px, py, px + pw, py + 1, 0x44FFB0C0);
        context.fill(px, py + ph - 1, px + pw, py + ph, 0x44FFB0C0);

        String t = "B L O O M";
        int tw = this.textRenderer.getWidth(t);
        context.drawText(this.textRenderer, t, cx - tw / 2, py + 8, 0xFFFFFFFF, true);
        context.fill(px + 15, py + 20, px + pw - 15, py + 21, 0x33FFB0C0);

        int btnW = 120; int btnH = 14; int gap = 2; int sy = py + 26;
        drawBtn(context, "Resume", cx, sy, btnW, btnH, mouseX, mouseY, false);
        drawBtn(context, "Bloom Mods", cx, sy + btnH + gap, btnW, btnH, mouseX, mouseY, false);
        drawBtn(context, "Settings", cx, sy + (btnH + gap) * 2, btnW, btnH, mouseX, mouseY, false);
        drawBtn(context, "Disconnect", cx, sy + (btnH + gap) * 3 + 6, btnW, btnH, mouseX, mouseY, true);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBtn(DrawContext ctx, String label, int cx, int y, int bw, int bh, int mx, int my, boolean danger) {
        int x = cx - bw / 2;
        boolean hovered = mx >= x && mx <= x + bw && my >= y && my <= y + bh;
        ctx.fill(x, y, x + bw, y + bh, hovered ? 0x66FFB0C0 : 0x44201520);
        ctx.fill(x, y, x + bw, y + 1, 0x33FFB0C0);
        if (hovered) ctx.fill(x, y, x + 1, y + bh, danger ? 0xDDFF6666 : 0xDDFFB0C0);
        int tw = this.textRenderer.getWidth(label);
        int color = danger ? (hovered ? 0xFFFF8888 : 0xFFCC9988) : (hovered ? 0xFFFFFFFF : 0xFFDDCCBB);
        ctx.drawText(this.textRenderer, label, cx - tw / 2, y + (bh - 8) / 2, color, true);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        int cx = this.width / 2; int btnW = 120; int btnH = 14; int gap = 2;
        int ph = 130; int py = this.height / 2 - ph / 2; int sy = py + 26;
        int x = cx - btnW / 2;
        double mx = click.x(); double my = click.y();
        for (int i = 0; i < 4; i++) {
            int by = sy + (btnH + gap) * i + (i == 3 ? 6 : 0);
            if (mx >= x && mx <= x + btnW && my >= by && my <= by + btnH) {
                switch (i) {
                    case 0 -> client.setScreen(null);
                    case 1 -> client.setScreen(new ModuleScreen());
                    case 2 -> client.setScreen(new OptionsScreen(this, client.options));
                    case 3 -> { client.disconnect(Text.literal("Disconnected")); client.setScreen(new BloomTitleScreen()); }
                }
                return true;
            }
        }
        return super.mouseClicked(click, bl);
    }

    @Override public boolean shouldPause() { return true; }
}
