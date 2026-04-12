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
    private final List<Petal> petals = new ArrayList<>();
    private final Random rng = new Random();

    private static final int BTN_COUNT = 6;
    private final float[] hoverAnim = new float[BTN_COUNT];
    private static final String[] BTN_LABELS = {"Singleplayer", "Multiplayer", "Bloom Mods", "Cosmetics", "Settings", "Quit Game"};
    private static final boolean[] BTN_DANGER = {false, false, false, false, false, true};

    public BloomTitleScreen() {
        super(Text.literal("Bloom Client"));
    }

    @Override
    protected void init() {
        openTime = System.currentTimeMillis();
        petals.clear();
        for (int i = 0; i < 50; i++) petals.add(newPetal(true));
        for (int i = 0; i < BTN_COUNT; i++) hoverAnim[i] = 0;
    }

    private Petal newPetal(boolean randomY) {
        Petal p = new Petal();
        p.x = rng.nextFloat() * (this.width + 80) - 40;
        p.y = randomY ? rng.nextFloat() * this.height : -10 - rng.nextFloat() * 40;
        p.size = 2 + rng.nextFloat() * 3;
        p.vx = 0.2f + rng.nextFloat() * 0.5f;
        p.vy = 0.3f + rng.nextFloat() * 0.4f;
        p.phase = rng.nextFloat() * 6.28f;
        p.wobble = 1 + rng.nextFloat() * 2;
        p.alpha = 0.15f + rng.nextFloat() * 0.35f;
        int[] pinks = {0xFFB7C9, 0xFFC0CB, 0xFFD1DC, 0xF8A4B8, 0xFFE4E9};
        p.color = pinks[rng.nextInt(pinks.length)];
        return p;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int w = this.width, h = this.height, cx = w / 2;
        float time = (System.currentTimeMillis() - openTime) / 1000f;
        float openProgress = Math.min(1.0f, time / 0.4f);
        float ease = easeOutCubic(openProgress);

        // Background gradient
        ctx.fillGradient(0, 0, w, h / 4, 0xFF120a18, 0xFF1a1028);
        ctx.fillGradient(0, h / 4, w, h / 2, 0xFF1a1028, 0xFF261438);
        ctx.fillGradient(0, h / 2, w, h * 3 / 4, 0xFF261438, 0xFF1e1030);
        ctx.fillGradient(0, h * 3 / 4, w, h, 0xFF1e1030, 0xFF0e0814);

        // Radial glow
        int glowA = (int)(6 + Math.sin(time * 0.8) * 3);
        ctx.fill(cx - w / 3, h / 4, cx + w / 3, h * 3 / 4, (glowA << 24) | 0x301020);

        // Petals
        for (int i = petals.size() - 1; i >= 0; i--) {
            Petal p = petals.get(i);
            p.x += p.vx * delta; p.y += p.vy * delta;
            p.phase += p.wobble * delta * 0.04f;
            p.x += (float) Math.sin(p.phase) * 0.12f * delta;
            if (p.y > h + 15 || p.x > w + 40) { petals.set(i, newPetal(false)); continue; }
            int a = (int)(p.alpha * 200 * ease) << 24;
            int col = a | (p.color & 0x00FFFFFF);
            int s = (int) p.size;
            // Round petal shape
            ctx.fill((int) p.x - s, (int) p.y - s / 2 + 1, (int) p.x + s, (int) p.y + s / 2 - 1, col);
            ctx.fill((int) p.x - s + 1, (int) p.y - s / 2, (int) p.x + s - 1, (int) p.y + s / 2, col);
        }
        if (rng.nextFloat() < 0.15f) petals.add(newPetal(false));
        if (petals.size() > 40) petals.subList(40, petals.size()).clear();

        // Logo area
        int logoY = h / 4 - 20;

        // Breathing glow behind logo
        int ga = (int)((8 + Math.sin(time * 1.0) * 5) * ease);
        drawRoundRect(ctx, cx - 90, logoY - 6, 180, 55, (ga << 24) | 0xFFB0C0);

        // "BLOOM" title in Inter at 3x
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().scale(3.0f, 3.0f);
        int bw3 = textW(this.textRenderer, "BLOOM");
        ctx.drawText(this.textRenderer, text("BLOOM", 0xFFD1DC), (int)(cx / 3.0f - bw3 / 2.0f), (int)(logoY / 3.0f), -1, false);
        ctx.getMatrices().popMatrix();

        // Subtitle in Inter
        String sub = "MINECRAFT CLIENT";
        int sw = textW(this.textRenderer, sub);
        ctx.drawText(this.textRenderer, text(sub, 0x6A5060), cx - sw / 2, logoY + 30, -1, false);

        // Separator
        int sa = (int)(15 + Math.sin(time * 1.8) * 8);
        ctx.fill(cx - 50, logoY + 44, cx + 50, logoY + 45, (sa << 24) | 0xFFB0C0);

        // Buttons
        int btnW = 160, btnH = 22, gap = 4;
        int startY = h / 4 + 44;

        // Panel behind buttons
        int panelAlpha = (int)(0x88 * ease);
        drawRoundRect(ctx, cx - btnW / 2 - 10, startY - 6, btnW + 20,
            (btnH + gap) * 5 + gap + btnH + 18, (panelAlpha << 24) | 0x0A0611);

        for (int i = 0; i < BTN_COUNT; i++) {
            int by = startY + (btnH + gap) * i + (i == 5 ? 6 : 0);
            int bx = cx - btnW / 2;
            boolean hov = mx >= bx && mx <= bx + btnW && my >= by && my <= by + btnH;

            // Smooth hover animation
            float target = hov ? 1.0f : 0.0f;
            hoverAnim[i] += (target - hoverAnim[i]) * 0.18f * delta;
            if (Math.abs(hoverAnim[i] - target) < 0.005f) hoverAnim[i] = target;

            float ha = hoverAnim[i];

            // Outer glow on hover
            if (ha > 0.01f) {
                int glowAlpha2 = (int)(ha * 0x12);
                drawRoundRect(ctx, bx - 2, by - 2, btnW + 4, btnH + 4, (glowAlpha2 << 24) | 0xFFB0C0);
            }

            // Button background
            int bgAlpha = (int)(0x15 + ha * (0x38 - 0x15));
            int bgColor = BTN_DANGER[i] ? 0xFF5050 : 0xFFB7C9;
            drawRoundRect(ctx, bx, by, btnW, btnH, (bgAlpha << 24) | (bgColor & 0xFFFFFF));

            // Top highlight
            int hlAlpha = (int)(0x08 + ha * (0x44 - 0x08));
            ctx.fill(bx + 1, by, bx + btnW - 1, by + 1, (hlAlpha << 24) | (bgColor & 0xFFFFFF));

            // Left accent bar slides in
            if (ha > 0.05f) {
                int accentH = (int)(btnH * ha);
                int accentY = by + (btnH - accentH) / 2;
                int accent = BTN_DANGER[i] ? 0xBBFF7070 : 0xBBFFB7C9;
                ctx.fill(bx, accentY, bx + 2, accentY + accentH, accent);
            }

            // Label in Inter font
            int textColor;
            if (BTN_DANGER[i]) textColor = lerpColor(0xFF8A5555, 0xFFFF9090, ha);
            else textColor = lerpColor(0xFFBBA4AC, 0xFFF0E4E8, ha);
            int tw = textW(this.textRenderer, BTN_LABELS[i]);
            ctx.drawText(this.textRenderer, text(BTN_LABELS[i], textColor & 0xFFFFFF),
                cx - tw / 2, by + (btnH - 8) / 2, -1, false);
        }

        // Bottom bar
        int barAlpha = (int)(0x99 * ease);
        ctx.fill(0, h - 16, w, h, (barAlpha << 24) | 0x0a0611);
        ctx.drawText(this.textRenderer, text("Bloom Client v1.2.0", 0x5A4550), 6, h - 12, -1, false);
        String user = MinecraftClient.getInstance().getSession().getUsername();
        int uw = textW(this.textRenderer, user);
        ctx.drawText(this.textRenderer, text(user, 0x8A7080), w - uw - 6, h - 12, -1, false);

        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        int cx = this.width / 2;
        int btnW = 160, btnH = 22, gap = 4;
        int startY = this.height / 4 + 44;
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

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    static class Petal {
        float x, y, size, vx, vy, phase, wobble, alpha;
        int color;
    }
}
