package com.bloom.core.gui;

import com.bloom.core.BloomCore;
import com.bloom.core.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModuleScreen extends Screen {
    private final List<Petal> petals = new ArrayList<>();
    private final Random rng = new Random();
    private long openTime;

    public ModuleScreen() {
        super(Text.literal("Bloom Client"));
    }

    @Override
    protected void init() {
        openTime = System.currentTimeMillis();
        petals.clear();
        for (int i = 0; i < 30; i++) {
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
        p.alpha = 0.25f + rng.nextFloat() * 0.35f;
        int[] pinks = {0xFFB7C9, 0xFFC0CB, 0xFFD1DC, 0xF8A4B8, 0xF0C0D0};
        p.color = pinks[rng.nextInt(pinks.length)];
        return p;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int w = this.width;
        int h = this.height;
        int cx = w / 2;
        float time = (System.currentTimeMillis() - openTime) / 1000f;

        // Background
        context.fill(0, 0, w, h, 0xDD0d0810);

        // Petals
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

        if (rng.nextFloat() < 0.15f) petals.add(newPetal(false));
        if (petals.size() > 50) petals.subList(50, petals.size()).clear();

        // Header glow
        int glowA = (int)(12 + Math.sin(time * 1.5) * 6);
        context.fill(cx - 80, 8, cx + 80, 28, (glowA << 24) | 0xFFB0C0);

        // Title
        String title = "BLOOM MODULES";
        int tw = this.textRenderer.getWidth(title);
        context.drawText(this.textRenderer, title, cx - tw / 2, 14, 0xFFFFFF, false);

        String sub = "Click to toggle  |  Right Shift to close";
        int sw = this.textRenderer.getWidth(sub);
        context.drawText(this.textRenderer, sub, cx - sw / 2, 28, 0x665566, false);

        // Separator
        context.fill(cx - 80, 40, cx + 80, 41, 0x22FFB0C0);

        // Module grid
        List<Module> modules = BloomCore.MODULES.getModules();
        int cols = 2;
        int cardW = 160;
        int cardH = 52;
        int gapX = 8;
        int gapY = 6;
        int gridW = cols * cardW + (cols - 1) * gapX;
        int startX = cx - gridW / 2;
        int startY = 50;

        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * (cardW + gapX);
            int y = startY + row * (cardH + gapY);

            boolean hovered = mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH;
            boolean enabled = m.isEnabled();

            // Card
            context.fill(x, y, x + cardW, y + cardH, hovered ? 0x33FFB0C0 : 0x11FFFFFF);

            // Left accent
            int accent = enabled ? 0xCC55DD88 : 0x44554444;
            context.fill(x, y, x + 3, y + cardH, accent);

            // Hover border
            if (hovered) {
                context.fill(x, y, x + cardW, y + 1, 0x22FFB0C0);
                context.fill(x, y + cardH - 1, x + cardW, y + cardH, 0x22FFB0C0);
                context.fill(x + cardW - 1, y, x + cardW, y + cardH, 0x22FFB0C0);
            }

            // Status dot
            int dotCol = enabled ? 0xFF55DD88 : 0xFF553333;
            context.fill(x + cardW - 14, y + 10, x + cardW - 6, y + 18, dotCol);

            // Name
            context.drawText(this.textRenderer, m.getName(), x + 10, y + 8, hovered ? 0xFFFFFF : 0xDDCCCC, false);

            // Description
            String desc = m.getDescription();
            if (desc.length() > 24) desc = desc.substring(0, 22) + "..";
            context.drawText(this.textRenderer, desc, x + 10, y + 22, 0x665566, false);

            // Status
            String status = enabled ? "ON" : "OFF";
            int statusCol = enabled ? 0x55DD88 : 0x665566;
            context.drawText(this.textRenderer, status, x + 10, y + 36, statusCol, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        List<Module> modules = BloomCore.MODULES.getModules();
        int cols = 2;
        int cardW = 160;
        int cardH = 52;
        int gapX = 8;
        int gapY = 6;
        int gridW = cols * cardW + (cols - 1) * gapX;
        int startX = this.width / 2 - gridW / 2;
        int startY = 50;

        for (int i = 0; i < modules.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * (cardW + gapX);
            int y = startY + row * (cardH + gapY);

            if (mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH) {
                modules.get(i).toggle();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    static class Petal {
        float x, y, size, speedX, speedY, wobbleSpeed, wobbleAmp, phase, alpha;
        int color;
    }
}
