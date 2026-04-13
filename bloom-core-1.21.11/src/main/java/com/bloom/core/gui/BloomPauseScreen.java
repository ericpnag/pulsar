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
    private final List<Star> stars = new ArrayList<>();
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
        stars.clear();
        for (int i = 0; i < 40; i++) stars.add(newStar());
        for (int i = 0; i < BTN_COUNT; i++) hoverAnim[i] = 0;
    }

    private Star newStar() {
        Star s = new Star();
        s.x = rng.nextFloat() * this.width;
        s.y = rng.nextFloat() * this.height;
        s.vy = 0.03f + rng.nextFloat() * 0.1f;
        s.size = 0.5f + rng.nextFloat() * 1.5f;
        s.alpha = 0.1f + rng.nextFloat() * 0.4f;
        s.twinkleSpeed = 0.5f + rng.nextFloat() * 2f;
        s.phase = rng.nextFloat() * 6.28f;
        return s;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int w = this.width, h = this.height, cx = w / 2;
        float time = (System.currentTimeMillis() - openTime) / 1000f;
        float openProgress = Math.min(1.0f, time / 0.3f);
        float ease = easeOutCubic(openProgress);

        // Dark overlay with depth
        int overlayA = (int)(0xDD * ease);
        ctx.fill(0, 0, w, h, (overlayA << 24));

        // Stars
        for (Star s : stars) {
            s.y += s.vy * delta;
            s.phase += s.twinkleSpeed * delta * 0.03f;
            if (s.y > h + 3) { s.y = -2; s.x = rng.nextFloat() * w; }
            float twinkle = 0.4f + 0.6f * (float)Math.sin(s.phase);
            int a = (int)(s.alpha * twinkle * 200 * ease);
            if (a < 2) continue;
            int sz = Math.max(1, (int)s.size);
            ctx.fill((int)s.x, (int)s.y, (int)s.x + sz, (int)s.y + sz, (a << 24) | 0xFFFFFF);
        }

        // === Panel ===
        int pw = 230, ph = 200;
        int px = cx - pw / 2;
        int targetPy = h / 2 - ph / 2;
        int py = (int)(targetPy - 10 * (1 - ease));
        int panelA = (int)(0xD0 * ease);

        // Panel shadow
        drawRoundRect(ctx, px - 2, py - 2, pw + 4, ph + 4, (int)(0x30 * ease) << 24);
        // Panel background
        drawRoundRect(ctx, px, py, pw, ph, (panelA << 24) | 0x080808);
        // Panel border
        drawRoundRectOutline(ctx, px, py, pw, ph, (int)(0x15 * ease) << 24 | 0xFFFFFF);

        // Mini black hole in panel header
        int bhX = cx, bhY = py + 18;
        // Accretion rings
        for (int r = 20; r > 5; r--) {
            float t = (float)r / 20f;
            float pulse = (float)(1 + 0.2 * Math.sin(time * 2 + r * 0.1));
            int diskA = (int)((1 - t) * 15 * pulse * ease);
            int ry = (int)(r * 0.3f);
            ctx.fill(bhX - r, bhY - ry, bhX + r, bhY + ry, (diskA << 24) | 0xFFFFFF);
        }
        // Event horizon
        ctx.fill(bhX - 5, bhY - 2, bhX + 5, bhY + 2, (int)(255 * ease) << 24);

        // "PULSAR" at 2x
        int titleY = bhY + 8;
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().scale(2.0f, 2.0f);
        int bw = textW(this.textRenderer, "PULSAR");
        ctx.drawText(this.textRenderer, text("PULSAR", 0xFFFFFF),
            (int)(cx / 2.0f - bw / 2.0f), (int)(titleY / 2.0f), -1, false);
        ctx.getMatrices().popMatrix();

        // Subtitle
        String sub = "GAME PAUSED";
        int sw = textW(this.textRenderer, sub);
        ctx.drawText(this.textRenderer, text(sub, 0x404040), cx - sw / 2, titleY + 16, -1, false);

        // Separator
        int sepA = (int)(10 * ease);
        ctx.fill(px + 20, titleY + 28, px + pw - 20, titleY + 29, (sepA << 24) | 0xFFFFFF);

        // === Buttons ===
        int btnW = 190, btnH = 22, gap = 4;
        int sy = titleY + 36;

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
                int gA = (int)(ha * 0x12);
                drawRoundRect(ctx, bx - 2, by - 2, btnW + 4, btnH + 4, (gA << 24) | 0xFFFFFF);
            }

            // Background
            int bgA = (int)(0x0D + ha * 0x28);
            int bgCol = BTN_DANGER[i] ? 0xFF4040 : 0xFFFFFF;
            drawRoundRect(ctx, bx, by, btnW, btnH, (bgA << 24) | (bgCol & 0xFFFFFF));

            // Top highlight
            int hlA = (int)(0x04 + ha * 0x30);
            ctx.fill(bx + 1, by, bx + btnW - 1, by + 1, (hlA << 24) | (bgCol & 0xFFFFFF));

            // Left accent
            if (ha > 0.05f) {
                int accentH = (int)(btnH * ha);
                int accentY = by + (btnH - accentH) / 2;
                ctx.fill(bx, accentY, bx + 2, accentY + accentH, BTN_DANGER[i] ? 0xBBFF5050 : 0xBBFFFFFF);
            }

            // Label
            int textCol;
            if (BTN_DANGER[i]) textCol = lerpColor(0xFF774444, 0xFFFF9090, ha);
            else textCol = lerpColor(0xFF707070, 0xFFFFFFFF, ha);
            int tw = textW(this.textRenderer, BTN_LABELS[i]);
            ctx.drawText(this.textRenderer, text(BTN_LABELS[i], textCol & 0xFFFFFF),
                cx - tw / 2, by + (btnH - 8) / 2, -1, false);
        }

        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        int cx = this.width / 2, btnW = 190, btnH = 22, gap = 4;
        int bhY = this.height / 2 - 100 + 18;
        int titleY = bhY + 8;
        int sy = titleY + 36;
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

    static class Star { float x, y, size, vy, alpha, twinkleSpeed, phase; }
}
