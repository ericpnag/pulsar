package com.bloom.core.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.bloom.core.gui.BloomGui.*;

public class BloomTitleScreen extends Screen {
    private long openTime;
    private final List<Star> stars = new ArrayList<>();
    private final List<SpiralParticle> spirals = new ArrayList<>();
    private final Random rng = new Random();

    private static final int BTN_COUNT = 6;
    private final float[] hoverAnim = new float[BTN_COUNT];
    private static final String[] BTN_LABELS = {"Singleplayer", "Multiplayer", "Pulsar Mods", "Cosmetics", "Settings", "Quit Game"};
    private static final boolean[] BTN_DANGER = {false, false, false, false, false, true};

    public BloomTitleScreen() { super(Text.literal("Pulsar Client")); }

    @Override
    protected void init() {
        openTime = System.currentTimeMillis();
        stars.clear(); spirals.clear();
        for (int i = 0; i < 80; i++) stars.add(newStar(true));
        for (int i = 0; i < 30; i++) spirals.add(newSpiral());
        for (int i = 0; i < BTN_COUNT; i++) hoverAnim[i] = 0;
    }

    private Star newStar(boolean randomY) {
        Star s = new Star();
        s.x = rng.nextFloat() * (this.width + 40) - 20;
        s.y = randomY ? rng.nextFloat() * this.height : -5 - rng.nextFloat() * 30;
        s.size = 0.5f + rng.nextFloat() * 2f;
        s.vy = 0.05f + rng.nextFloat() * 0.15f;
        s.alpha = 0.1f + rng.nextFloat() * 0.6f;
        s.twinkleSpeed = 0.5f + rng.nextFloat() * 2f;
        s.phase = rng.nextFloat() * 6.28f;
        return s;
    }

    private SpiralParticle newSpiral() {
        SpiralParticle p = new SpiralParticle();
        p.angle = rng.nextFloat() * 6.28f;
        p.radius = 30 + rng.nextFloat() * 120;
        p.speed = 0.003f + rng.nextFloat() * 0.008f;
        p.size = 1 + rng.nextFloat() * 2;
        p.alpha = 0.1f + rng.nextFloat() * 0.3f;
        p.decay = 0.998f + rng.nextFloat() * 0.001f;
        return p;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int w = this.width, h = this.height, cx = w / 2;
        float time = (System.currentTimeMillis() - openTime) / 1000f;
        float openProgress = Math.min(1.0f, time / 0.5f);
        float ease = easeOutCubic(openProgress);

        // === Deep space background ===
        ctx.fill(0, 0, w, h, 0xFF000000);

        // Subtle nebula gradient
        int nebulaA = (int)(8 + Math.sin(time * 0.3) * 3);
        ctx.fillGradient(0, 0, w, h / 2, 0x00000000, (nebulaA << 24) | 0x0a0a15);
        ctx.fillGradient(0, h / 2, w, h, (nebulaA << 24) | 0x0a0a15, 0x00000000);

        // === Stars ===
        for (int i = stars.size() - 1; i >= 0; i--) {
            Star s = stars.get(i);
            s.y += s.vy * delta;
            s.phase += s.twinkleSpeed * delta * 0.03f;
            if (s.y > h + 5) { stars.set(i, newStar(false)); continue; }
            float twinkle = 0.5f + 0.5f * (float)Math.sin(s.phase);
            int a = (int)(s.alpha * twinkle * 255 * ease);
            if (a < 2) continue;
            int col = (a << 24) | 0xFFFFFF;
            int sz = Math.max(1, (int)s.size);
            ctx.fill((int)s.x, (int)s.y, (int)s.x + sz, (int)s.y + sz, col);
            // Cross sparkle for bigger stars
            if (s.size > 1.5f && twinkle > 0.7f) {
                int sa = a / 3;
                ctx.fill((int)s.x - 1, (int)s.y, (int)s.x, (int)s.y + sz, (sa << 24) | 0xFFFFFF);
                ctx.fill((int)s.x + sz, (int)s.y, (int)s.x + sz + 1, (int)s.y + sz, (sa << 24) | 0xFFFFFF);
            }
        }
        if (rng.nextFloat() < 0.08f) stars.add(newStar(false));
        while (stars.size() > 100) stars.remove(stars.size() - 1);

        // === Black hole (center of screen, above buttons) ===
        int bhX = cx, bhY = h / 4 - 5;

        // Accretion disk glow
        for (int r = 90; r > 15; r -= 2) {
            float t = (float)r / 90f;
            float pulse = (float)(1 + 0.15 * Math.sin(time * 1.5 + r * 0.05));
            int diskA = (int)((1 - t) * 12 * pulse * ease);
            if (diskA < 1) continue;
            // Elliptical disk
            int ry = (int)(r * 0.35f);
            ctx.fill(bhX - r, bhY - ry, bhX + r, bhY + ry, (diskA << 24) | 0xFFFFFF);
        }

        // Spiral particles orbiting the black hole
        for (SpiralParticle p : spirals) {
            p.angle += p.speed * delta * 3;
            p.radius *= p.decay;
            float px = bhX + (float)Math.cos(p.angle) * p.radius;
            float py = bhY + (float)Math.sin(p.angle) * p.radius * 0.35f;
            if (p.radius < 8) { p.angle = rng.nextFloat() * 6.28f; p.radius = 30 + rng.nextFloat() * 120; }
            int pa = (int)(p.alpha * 255 * ease);
            // Color shifts from white to orange as it spirals in
            float colorT = 1 - (p.radius - 8) / 120f;
            int pr = 255, pg = (int)(255 - colorT * 120), pb = (int)(255 - colorT * 200);
            ctx.fill((int)px, (int)py, (int)px + (int)p.size, (int)py + (int)p.size,
                (pa << 24) | ((pr & 0xFF) << 16) | ((pg & 0xFF) << 8) | (pb & 0xFF));
        }

        // Event horizon (pure black circle)
        for (int r = 18; r >= 0; r--) {
            float fade = r > 12 ? (18 - r) / 6.0f : 1.0f;
            int ea = (int)(255 * fade * ease);
            int ry = (int)(r * 0.35f);
            ctx.fill(bhX - r, bhY - ry, bhX + r, bhY + ry, (ea << 24));
        }

        // Gravitational lensing ring
        float ringPulse = (float)(1 + 0.2 * Math.sin(time * 2));
        int ringR = (int)(22 * ringPulse);
        int ringRy = (int)(ringR * 0.35f);
        int ringA = (int)(30 * ease);
        // Top and bottom arcs
        ctx.fill(bhX - ringR, bhY - ringRy, bhX + ringR, bhY - ringRy + 1, (ringA << 24) | 0xFFFFFF);
        ctx.fill(bhX - ringR, bhY + ringRy - 1, bhX + ringR, bhY + ringRy, (ringA << 24) | 0xFFFFFF);

        // === "PULSAR" title ===
        int logoY = bhY + 28;

        // Glow behind title
        int ga = (int)((6 + Math.sin(time * 0.8) * 3) * ease);
        drawRoundRect(ctx, cx - 80, logoY - 4, 160, 34, (ga << 24) | 0xFFFFFF);

        // "PULSAR" at 3x scale
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().scale(3.0f, 3.0f);
        int bw3 = textW(this.textRenderer, "PULSAR");
        ctx.drawText(this.textRenderer, text("PULSAR", 0xFFFFFF),
            (int)(cx / 3.0f - bw3 / 2.0f), (int)(logoY / 3.0f), -1, false);
        ctx.getMatrices().popMatrix();

        // Subtitle
        String sub = "MINECRAFT CLIENT";
        int sw = textW(this.textRenderer, sub);
        ctx.drawText(this.textRenderer, text(sub, 0x505050), cx - sw / 2, logoY + 22, -1, false);

        // Separator line
        int sepA = (int)((12 + Math.sin(time * 1.5) * 6) * ease);
        ctx.fill(cx - 40, logoY + 34, cx + 40, logoY + 35, (sepA << 24) | 0xFFFFFF);

        // === Buttons ===
        int btnW = 170, btnH = 22, gap = 4;
        int startY = logoY + 44;

        // Panel behind buttons
        int panelA = (int)(0x40 * ease);
        drawRoundRect(ctx, cx - btnW / 2 - 12, startY - 8, btnW + 24,
            (btnH + gap) * 5 + gap + btnH + 22, (panelA << 24));
        // Panel border
        drawRoundRectOutline(ctx, cx - btnW / 2 - 12, startY - 8, btnW + 24,
            (btnH + gap) * 5 + gap + btnH + 22, (int)(0x10 * ease) << 24 | 0xFFFFFF);

        for (int i = 0; i < BTN_COUNT; i++) {
            int by = startY + (btnH + gap) * i + (i == 5 ? 6 : 0);
            int bx = cx - btnW / 2;
            boolean hov = mx >= bx && mx <= bx + btnW && my >= by && my <= by + btnH;

            float target = hov ? 1.0f : 0.0f;
            hoverAnim[i] += (target - hoverAnim[i]) * 0.18f * delta;
            if (Math.abs(hoverAnim[i] - target) < 0.005f) hoverAnim[i] = target;
            float ha = hoverAnim[i];

            // Outer glow
            if (ha > 0.01f) {
                int glowA = (int)(ha * 0x15);
                drawRoundRect(ctx, bx - 2, by - 2, btnW + 4, btnH + 4, (glowA << 24) | 0xFFFFFF);
            }

            // Button background
            int bgA = (int)(0x10 + ha * 0x25);
            int bgCol = BTN_DANGER[i] ? 0xFF4040 : 0xFFFFFF;
            drawRoundRect(ctx, bx, by, btnW, btnH, (bgA << 24) | (bgCol & 0xFFFFFF));

            // Top highlight
            int hlA = (int)(0x06 + ha * 0x30);
            ctx.fill(bx + 1, by, bx + btnW - 1, by + 1, (hlA << 24) | (bgCol & 0xFFFFFF));

            // Left accent bar
            if (ha > 0.05f) {
                int accentH = (int)(btnH * ha);
                int accentY = by + (btnH - accentH) / 2;
                ctx.fill(bx, accentY, bx + 2, accentY + accentH, BTN_DANGER[i] ? 0xBBFF5050 : 0xBBFFFFFF);
            }

            // Label
            int textCol;
            if (BTN_DANGER[i]) textCol = lerpColor(0xFF884444, 0xFFFF9090, ha);
            else textCol = lerpColor(0xFF808080, 0xFFFFFFFF, ha);
            int tw = textW(this.textRenderer, BTN_LABELS[i]);
            ctx.drawText(this.textRenderer, text(BTN_LABELS[i], textCol & 0xFFFFFF),
                cx - tw / 2, by + (btnH - 8) / 2, -1, false);
        }

        // === Bottom bar ===
        int barA = (int)(0x80 * ease);
        ctx.fill(0, h - 16, w, h, (barA << 24));
        ctx.fill(0, h - 16, w, h - 15, (int)(0x08 * ease) << 24 | 0xFFFFFF);
        ctx.drawText(this.textRenderer, text("Pulsar Client v1.8.0", 0x404040), 6, h - 12, -1, false);
        String user = MinecraftClient.getInstance().getSession().getUsername();
        int uw = textW(this.textRenderer, user);
        ctx.drawText(this.textRenderer, text(user, 0x606060), w - uw - 6, h - 12, -1, false);

        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        int cx = this.width / 2;
        int btnW = 170, btnH = 22, gap = 4;
        int bhY = this.height / 4 - 5;
        int logoY = bhY + 28;
        int startY = logoY + 44;
        int x = cx - btnW / 2;
        double mouseX = click.x(), mouseY = click.y();

        for (int i = 0; i < BTN_COUNT; i++) {
            int by = startY + (btnH + gap) * i + (i == 5 ? 6 : 0);
            if (mouseX >= x && mouseX <= x + btnW && mouseY >= by && mouseY <= by + btnH) {
                switch (i) {
                    case 0 -> client.setScreen(new SelectWorldScreen(this));
                    case 1 -> client.setScreen(new MultiplayerScreen(this));
                    case 2 -> client.setScreen(new ModuleScreen());
                    case 3 -> client.setScreen(new CosmeticsScreen(this));
                    case 4 -> client.setScreen(new OptionsScreen(this, client.options));
                    case 5 -> client.scheduleStop();
                }
                return true;
            }
        }
        return super.mouseClicked(click, bl);
    }

    @Override public boolean shouldCloseOnEsc() { return false; }

    static class Star { float x, y, size, vy, alpha, twinkleSpeed, phase; }
    static class SpiralParticle { float angle, radius, speed, size, alpha, decay; }
}
