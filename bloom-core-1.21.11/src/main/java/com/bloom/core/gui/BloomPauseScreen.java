package com.bloom.core.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.bloom.core.gui.BloomGui.*;

public class BloomPauseScreen extends Screen {
    private final List<Petal> petals = new ArrayList<>();
    private final Random rng = new Random();
    private long openTime;

    private static final int BTN_COUNT = 5;
    private final float[] hoverAnim = new float[BTN_COUNT];
    private static final String[] BTN_LABELS = {"Resume", "Pulsar Mods", "Cosmetics", "Settings", "Disconnect"};
    private static final boolean[] BTN_DANGER = {false, false, false, false, true};

    public BloomPauseScreen() { super(Text.literal("Game Menu")); }

    @Override
    protected void init() {
        openTime = System.currentTimeMillis();
        petals.clear();
        for (int i = 0; i < 20; i++) petals.add(newPetal());
        for (int i = 0; i < BTN_COUNT; i++) hoverAnim[i] = 0;
    }

    private Petal newPetal() {
        Petal p = new Petal();
        p.x = rng.nextFloat() * this.width;
        p.y = rng.nextFloat() * this.height;
        p.vx = 0.1f + rng.nextFloat() * 0.25f;
        p.vy = 0.12f + rng.nextFloat() * 0.2f;
        p.size = 1 + rng.nextInt(2);
        int[] purples = {0xC678DD, 0xBB70D6, 0xD19A66, 0xE06C75};
        p.color = purples[rng.nextInt(purples.length)];
        p.phase = rng.nextFloat() * 6.28f;
        p.wobble = 1 + rng.nextFloat() * 2;
        p.alpha = 0.3f + rng.nextFloat() * 0.3f;
        return p;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int w = this.width, h = this.height, cx = w / 2;
        float time = (System.currentTimeMillis() - openTime) / 1000f;
        float openProgress = Math.min(1.0f, time / 0.25f);
        float ease = easeOutCubic(openProgress);

        // Dark overlay
        int overlayAlpha = (int)(0xCC * ease);
        ctx.fillGradient(0, 0, w, h / 2, (overlayAlpha << 24) | 0x120a18, (overlayAlpha << 24) | 0x1a1028);
        ctx.fillGradient(0, h / 2, w, h, (overlayAlpha << 24) | 0x1a1028, (overlayAlpha << 24) | 0x0e0814);

        // Petals
        for (Petal p : petals) {
            p.x += p.vx * delta; p.y += p.vy * delta;
            p.phase += p.wobble * delta * 0.04f;
            p.x += (float) Math.sin(p.phase) * 0.08f * delta;
            if (p.y > h + 5 || p.x > w + 10) { p.x = -5; p.y = rng.nextFloat() * h * 0.3f; }
            int a = (int)(p.alpha * 180 * ease) << 24;
            int col = a | (p.color & 0x00FFFFFF);
            int s = (int) p.size;
            ctx.fill((int) p.x - s, (int) p.y, (int) p.x + s, (int) p.y + s, col);
        }

        // Panel with slide-in animation
        int pw = 220, ph = 190;
        int px = cx - pw / 2;
        int targetPy = h / 2 - ph / 2;
        int py = (int)(targetPy - 12 * (1 - ease));
        int panelAlpha = (int)(0xEE * ease);

        // Panel shadow
        drawRoundRect(ctx, px - 1, py - 1, pw + 2, ph + 2, (int)(0x20 * ease) << 24);
        // Panel background
        drawRoundRect(ctx, px, py, pw, ph, (panelAlpha << 24) | 0x0A0A0F);
        // Top border
        ctx.fill(px + 2, py, px + pw - 2, py + 1, ((int)(0x44 * ease) << 24) | 0xC678DD);
        // Bottom border
        ctx.fill(px + 2, py + ph - 1, px + pw - 2, py + ph, ((int)(0x22 * ease) << 24) | 0xC678DD);

        // "PULSAR" title in Inter at 2x
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().scale(2.0f, 2.0f);
        int bw = textW(this.textRenderer, "PULSAR");
        ctx.drawText(this.textRenderer, text("PULSAR", 0xC678DD),
            (int)(cx / 2.0f - bw / 2.0f), (int)((py + 10) / 2.0f), -1, false);
        ctx.getMatrices().popMatrix();

        // Subtitle
        String sub = "MINECRAFT CLIENT";
        int sw = textW(this.textRenderer, sub);
        ctx.drawText(this.textRenderer, text(sub, 0x3E4451), cx - sw / 2, py + 28, -1, false);

        // Separator
        ctx.fill(px + 20, py + 40, px + pw - 20, py + 41, ((int)(0x22 * ease) << 24) | 0xC678DD);

        // Buttons
        int btnW = 180, btnH = 22, gap = 4, sy = py + 48;

        for (int i = 0; i < BTN_COUNT; i++) {
            int by = sy + (btnH + gap) * i + (i == 4 ? 6 : 0);
            int bx = cx - btnW / 2;
            boolean hov = mx >= bx && mx <= bx + btnW && my >= by && my <= by + btnH;

            float target = hov ? 1.0f : 0.0f;
            hoverAnim[i] += (target - hoverAnim[i]) * 0.18f * delta;
            if (Math.abs(hoverAnim[i] - target) < 0.005f) hoverAnim[i] = target;
            float ha = hoverAnim[i];

            // Glow
            if (ha > 0.01f) {
                int gAlpha = (int)(ha * 0x12);
                drawRoundRect(ctx, bx - 2, by - 2, btnW + 4, btnH + 4, (gAlpha << 24) | 0xC070DD);
            }

            // Background
            int bgAlpha2 = (int)(0x12 + ha * (0x38 - 0x12));
            int bgColor = BTN_DANGER[i] ? 0xFF5050 : 0xC678DD;
            drawRoundRect(ctx, bx, by, btnW, btnH, (bgAlpha2 << 24) | (bgColor & 0xFFFFFF));

            // Top highlight
            int hlA = (int)(0x06 + ha * 0x3E);
            ctx.fill(bx + 1, by, bx + btnW - 1, by + 1, (hlA << 24) | (bgColor & 0xFFFFFF));

            // Left accent
            if (ha > 0.05f) {
                int accentH = (int)(btnH * ha);
                int accentY = by + (btnH - accentH) / 2;
                ctx.fill(bx, accentY, bx + 2, accentY + accentH, BTN_DANGER[i] ? 0xBBFF7070 : 0xBBC678DD);
            }

            // Label
            int textColor;
            if (BTN_DANGER[i]) textColor = lerpColor(0xFF8A5555, 0xFFFF9090, ha);
            else textColor = lerpColor(0xFFABB2BF, 0xFFE0E0E8, ha);
            int tw = textW(this.textRenderer, BTN_LABELS[i]);
            ctx.drawText(this.textRenderer, text(BTN_LABELS[i], textColor & 0xFFFFFF),
                cx - tw / 2, by + (btnH - 8) / 2, -1, false);
        }

        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        int cx = this.width / 2, btnW = 180, btnH = 22, gap = 4;
        int ph = 190, py = this.height / 2 - ph / 2, sy = py + 48;
        int x = cx - btnW / 2;
        double mouseX = click.x(), mouseY = click.y();
        for (int i = 0; i < BTN_COUNT; i++) {
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

    static class Petal {
        float x, y, size, vx, vy, phase, wobble, alpha;
        int color;
    }
}
