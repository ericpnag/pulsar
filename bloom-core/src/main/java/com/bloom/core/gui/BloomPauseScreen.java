package com.bloom.core.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BloomPauseScreen extends Screen {
    private final List<Petal> petals = new ArrayList<>();
    private final Random rng = new Random();
    private long openTime;

    public BloomPauseScreen() {
        super(Text.literal("Game Menu"));
    }

    @Override
    protected void init() {
        openTime = System.currentTimeMillis();
        petals.clear();
        for (int i = 0; i < 35; i++) {
            petals.add(newPetal(true));
        }
    }

    private Petal newPetal(boolean randomY) {
        Petal p = new Petal();
        p.x = rng.nextFloat() * (this.width + 80) - 40;
        p.y = randomY ? rng.nextFloat() * this.height : -10 - rng.nextFloat() * 40;
        p.size = 2 + rng.nextFloat() * 3;
        p.speedX = 0.2f + rng.nextFloat() * 0.5f;
        p.speedY = 0.3f + rng.nextFloat() * 0.4f;
        p.wobbleSpeed = 1 + rng.nextFloat() * 2;
        p.wobbleAmp = 10 + rng.nextFloat() * 20;
        p.phase = rng.nextFloat() * 6.28f;
        p.alpha = 0.3f + rng.nextFloat() * 0.4f;
        int[] pinks = {0xFFB7C9, 0xFFC0CB, 0xFFD1DC, 0xF8A4B8, 0xF0C0D0, 0xFFE4E9};
        p.color = pinks[rng.nextInt(pinks.length)];
        return p;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int w = this.width;
        int h = this.height;
        int cx = w / 2;
        float time = (System.currentTimeMillis() - openTime) / 1000f;

        // Dark overlay with slight purple tint
        context.fill(0, 0, w, h, 0xCC0d0810);

        // Update and draw petals
        for (int i = petals.size() - 1; i >= 0; i--) {
            Petal p = petals.get(i);
            p.x += p.speedX * delta;
            p.y += p.speedY * delta;
            p.phase += p.wobbleSpeed * delta * 0.05f;
            p.x += (float) Math.sin(p.phase) * p.wobbleAmp * 0.015f * delta;

            if (p.y > h + 20 || p.x > w + 50) {
                petals.set(i, newPetal(false));
                continue;
            }

            int px = (int) p.x;
            int py = (int) p.y;
            int s = (int) p.size;
            int alpha = (int) (p.alpha * 255) << 24;
            int col = alpha | (p.color & 0x00FFFFFF);

            context.fill(px - s, py, px, py - s / 2, col);
            context.fill(px, py - s / 2, px + s, py, col);
            context.fill(px - s, py, px, py + s / 2, col);
            context.fill(px, py, px + s, py + s / 2, col);
        }

        if (rng.nextFloat() < 0.2f) petals.add(newPetal(false));
        if (petals.size() > 60) petals.subList(60, petals.size()).clear();

        // Panel
        int panelW = 200;
        int panelH = 210;
        int px = cx - panelW / 2;
        int py = h / 2 - panelH / 2;

        // Panel background with gradient effect
        context.fill(px, py, px + panelW, py + panelH, 0xDD150d18);
        // Borders
        context.fill(px, py, px + panelW, py + 1, 0x33FFB0C0);
        context.fill(px, py + panelH - 1, px + panelW, py + panelH, 0x33FFB0C0);
        context.fill(px, py, px + 1, py + panelH, 0x22FFB0C0);
        context.fill(px + panelW - 1, py, px + panelW, py + panelH, 0x22FFB0C0);

        // Glow behind title
        int glowA = (int)(15 + Math.sin(time * 1.5) * 8);
        context.fill(cx - 50, py + 6, cx + 50, py + 24, (glowA << 24) | 0xFFB0C0);

        // Title
        String title = "B L O O M";
        int tw = this.textRenderer.getWidth(title);
        context.drawText(this.textRenderer, title, cx - tw / 2, py + 12, 0xFFFFFF, false);

        // Separator
        context.fill(px + 20, py + 28, px + panelW - 20, py + 29, 0x22FFB0C0);

        // Buttons
        int btnW = 160;
        int btnH = 24;
        int gap = 3;
        int startY = py + 40;

        drawBtn(context, "Resume", cx, startY, btnW, btnH, mouseX, mouseY, false);
        drawBtn(context, "Bloom Mods", cx, startY + btnH + gap, btnW, btnH, mouseX, mouseY, false);
        drawBtn(context, "Settings", cx, startY + (btnH + gap) * 2, btnW, btnH, mouseX, mouseY, false);
        drawBtn(context, "Disconnect", cx, startY + (btnH + gap) * 3 + 10, btnW, btnH, mouseX, mouseY, true);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBtn(DrawContext ctx, String label, int cx, int y, int bw, int bh, int mx, int my, boolean danger) {
        int x = cx - bw / 2;
        boolean hovered = mx >= x && mx <= x + bw && my >= y && my <= y + bh;

        ctx.fill(x, y, x + bw, y + bh, hovered ? 0x33FFB0C0 : 0x11FFFFFF);
        if (hovered) {
            ctx.fill(x, y, x + 2, y + bh, danger ? 0xCCFF6666 : 0xCCFFB0C0);
            ctx.fill(x, y, x + bw, y + 1, 0x22FFB0C0);
            ctx.fill(x, y + bh - 1, x + bw, y + bh, 0x22FFB0C0);
        }

        int tw = this.textRenderer.getWidth(label);
        int color;
        if (danger) {
            color = hovered ? 0xFF8888 : 0xAA6666;
        } else {
            color = hovered ? 0xFFFFFF : 0xBBAA99;
        }
        ctx.drawText(this.textRenderer, label, cx - tw / 2, y + (bh - 8) / 2, color, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int cx = this.width / 2;
        int btnW = 160;
        int btnH = 24;
        int gap = 3;
        int panelH = 210;
        int py = this.height / 2 - panelH / 2;
        int startY = py + 40;
        int x = cx - btnW / 2;

        for (int i = 0; i < 4; i++) {
            int by = startY + (btnH + gap) * i + (i == 3 ? 10 : 0);
            if (mouseX >= x && mouseX <= x + btnW && mouseY >= by && mouseY <= by + btnH) {
                switch (i) {
                    case 0 -> client.setScreen(null);
                    case 1 -> client.setScreen(new ModuleScreen());
                    case 2 -> client.setScreen(new OptionsScreen(this, client.options));
                    case 3 -> {
                        client.world.disconnect();
                        client.disconnect();
                        client.setScreen(new BloomTitleScreen());
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    static class Petal {
        float x, y, size, speedX, speedY, wobbleSpeed, wobbleAmp, phase, alpha;
        int color;
    }
}
