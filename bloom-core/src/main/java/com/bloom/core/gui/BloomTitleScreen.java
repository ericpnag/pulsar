package com.bloom.core.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BloomTitleScreen extends Screen {
    private long openTime;
    private final List<Petal> petals = new ArrayList<>();
    private final List<Tree> trees = new ArrayList<>();
    private final Random rng = new Random();

    public BloomTitleScreen() {
        super(Text.literal("Bloom Client"));
    }

    @Override
    protected void init() {
        openTime = System.currentTimeMillis();
        petals.clear();
        trees.clear();

        // Spawn cherry blossom trees along the bottom
        int treeCount = Math.max(3, this.width / 180);
        for (int i = 0; i < treeCount; i++) {
            int tx = (int) (this.width * (0.1 + 0.8 * i / (treeCount - 1.0)));
            int ty = this.height - 20 - rng.nextInt(30);
            int th = 80 + rng.nextInt(60);
            trees.add(new Tree(tx, ty, th, rng.nextFloat() * 1000));
        }

        // Spawn initial petals
        for (int i = 0; i < 60; i++) {
            petals.add(newPetal(true));
        }
    }

    private Petal newPetal(boolean randomY) {
        Petal p = new Petal();
        p.x = rng.nextFloat() * (this.width + 100) - 50;
        p.y = randomY ? rng.nextFloat() * this.height : -10 - rng.nextFloat() * 40;
        p.size = 2 + rng.nextFloat() * 3;
        p.speedX = 0.3f + rng.nextFloat() * 0.8f;
        p.speedY = 0.4f + rng.nextFloat() * 0.6f;
        p.wobbleSpeed = 1.5f + rng.nextFloat() * 2f;
        p.wobbleAmp = 15 + rng.nextFloat() * 25;
        p.phase = rng.nextFloat() * 6.28f;
        p.alpha = 0.4f + rng.nextFloat() * 0.6f;
        // Pink shades
        int[] pinks = {0xFFB7C9, 0xFFC0CB, 0xFFD1DC, 0xF8A4B8, 0xF0C0D0, 0xFFE4E9, 0xE8899A};
        p.color = pinks[rng.nextInt(pinks.length)];
        p.rotation = rng.nextFloat() * 6.28f;
        p.rotSpeed = (rng.nextFloat() - 0.5f) * 0.08f;
        return p;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int w = this.width;
        int h = this.height;
        int cx = w / 2;
        float time = (System.currentTimeMillis() - openTime) / 1000f;

        // Sky gradient — soft dawn colors
        context.fillGradient(0, 0, w, h / 3, 0xFF1a1025, 0xFF2d1b3d);
        context.fillGradient(0, h / 3, w, h * 2 / 3, 0xFF2d1b3d, 0xFF4a2040);
        context.fillGradient(0, h * 2 / 3, w, h, 0xFF4a2040, 0xFF1a0f1a);

        // Ground
        context.fill(0, h - 25, w, h, 0xFF120a10);
        context.fill(0, h - 26, w, h - 25, 0xFF2a1520);

        // Grass tufts
        for (int gx = 0; gx < w; gx += 12) {
            int gh = 3 + (int)(Math.sin(gx * 0.1 + time) * 2);
            context.fill(gx, h - 25 - gh, gx + 2, h - 25, 0xFF1a0f15);
        }

        // Draw trees
        for (Tree tree : trees) {
            drawTree(context, tree, time);
        }

        // Update and draw petals
        for (int i = petals.size() - 1; i >= 0; i--) {
            Petal p = petals.get(i);
            p.x += p.speedX * delta;
            p.y += p.speedY * delta;
            p.phase += p.wobbleSpeed * delta * 0.05f;
            p.rotation += p.rotSpeed * delta;

            float wobbleX = (float) Math.sin(p.phase) * p.wobbleAmp * 0.02f;
            p.x += wobbleX * delta;

            if (p.y > h + 20 || p.x > w + 60) {
                petals.set(i, newPetal(false));
                continue;
            }

            int px = (int) p.x;
            int py = (int) p.y;
            int s = (int) p.size;
            int alpha = (int) (p.alpha * 255) << 24;
            int col = alpha | (p.color & 0x00FFFFFF);

            // Draw petal as a small diamond shape
            context.fill(px - s, py, px, py - s / 2, col);
            context.fill(px, py - s / 2, px + s, py, col);
            context.fill(px - s, py, px, py + s / 2, col);
            context.fill(px, py, px + s, py + s / 2, col);
        }

        // Spawn new petals
        if (rng.nextFloat() < 0.3f) {
            petals.add(newPetal(false));
        }

        // Logo with glow effect
        int logoY = h / 2 - 85;

        // Glow behind logo
        int glowAlpha = (int)(20 + Math.sin(time * 1.5) * 10);
        context.fill(cx - 90, logoY - 8, cx + 90, logoY + 38, (glowAlpha << 24) | 0xFFB0C0);

        drawCentered(context, "B L O O M", cx, logoY, 0xFFFFFF);
        drawCentered(context, "C  L  I  E  N  T", cx, logoY + 14, 0xCC99AABB);

        // Thin pink separator
        int sepAlpha = (int)(30 + Math.sin(time * 2) * 15);
        context.fill(cx - 50, logoY + 30, cx + 50, logoY + 31, (sepAlpha << 24) | 0xFFB0C0);

        // Menu buttons
        int btnW = 180;
        int btnH = 26;
        int gap = 3;
        int startY = h / 2 - 18;

        drawButton(context, "Singleplayer", cx, startY, btnW, btnH, mouseX, mouseY, time);
        drawButton(context, "Multiplayer", cx, startY + btnH + gap, btnW, btnH, mouseX, mouseY, time);
        drawButton(context, "Bloom Mods", cx, startY + (btnH + gap) * 2, btnW, btnH, mouseX, mouseY, time);
        drawButton(context, "Settings", cx, startY + (btnH + gap) * 3, btnW, btnH, mouseX, mouseY, time);
        drawButton(context, "Quit Game", cx, startY + (btnH + gap) * 4 + 6, btnW, btnH, mouseX, mouseY, time);

        // Bottom bar
        context.fill(0, h - 24, w, h - 23, 0x15FFB0C0);
        drawText(context, "Bloom Client v1.0.0", 8, h - 17, 0x665566);

        MinecraftClient mc = MinecraftClient.getInstance();
        String user = mc.getSession().getUsername();
        int uw = this.textRenderer.getWidth(user);
        drawText(context, user, w - uw - 8, h - 17, 0x998899);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawTree(DrawContext context, Tree tree, float time) {
        int tx = tree.x;
        int ty = tree.y;
        int th = tree.height;

        // Trunk
        int trunkW = 4 + th / 30;
        context.fill(tx - trunkW / 2, ty - th, tx + trunkW / 2, ty, 0xFF2a1518);
        context.fill(tx - trunkW / 2 - 1, ty - th, tx - trunkW / 2, ty, 0xFF1a0a0d);

        // Branches and canopy — clusters of pink circles
        int canopyY = ty - th;
        float sway = (float) Math.sin(time * 0.5 + tree.seed) * 3;

        drawCanopyBlob(context, tx + (int) sway, canopyY, th / 2, time + tree.seed);
        drawCanopyBlob(context, tx - th / 4 + (int) sway, canopyY + th / 6, th / 3, time + tree.seed + 1);
        drawCanopyBlob(context, tx + th / 4 + (int) sway, canopyY + th / 6, th / 3, time + tree.seed + 2);
        drawCanopyBlob(context, tx + (int) sway, canopyY + th / 4, th / 3, time + tree.seed + 3);
    }

    private void drawCanopyBlob(DrawContext context, int cx, int cy, int radius, float seed) {
        // Draw overlapping rectangles to simulate a round blossom canopy
        int steps = 6;
        for (int i = 0; i < steps; i++) {
            double angle = i * Math.PI * 2 / steps + seed * 0.3;
            int ox = (int) (Math.cos(angle) * radius * 0.4);
            int oy = (int) (Math.sin(angle) * radius * 0.3);
            int s = radius / 2 + (int)(Math.sin(seed + i) * radius / 6);

            // Pink blossom fill
            int alpha = 0x44 + (int)(Math.sin(seed + i * 0.7) * 0x11);
            int col = (alpha << 24) | 0xFFB0C0;
            context.fill(cx + ox - s, cy + oy - s / 2, cx + ox + s, cy + oy + s / 2, col);
        }
        // Bright center
        context.fill(cx - radius / 4, cy - radius / 6, cx + radius / 4, cy + radius / 6, 0x33FFC8D8);
    }

    private void drawButton(DrawContext ctx, String label, int cx, int y, int bw, int bh, int mx, int my, float time) {
        int x = cx - bw / 2;
        boolean hovered = mx >= x && mx <= x + bw && my >= y && my <= y + bh;

        ctx.fill(x, y, x + bw, y + bh, hovered ? 0x33FFB0C0 : 0x15FFFFFF);

        if (hovered) {
            ctx.fill(x, y, x + 2, y + bh, 0xCCFFB0C0);
            ctx.fill(x, y, x + bw, y + 1, 0x22FFB0C0);
            ctx.fill(x, y + bh - 1, x + bw, y + bh, 0x22FFB0C0);
        }

        int tw = this.textRenderer.getWidth(label);
        ctx.drawText(this.textRenderer, label, cx - tw / 2, y + (bh - 8) / 2, hovered ? 0xFFFFFF : 0xBBAA99, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int cx = this.width / 2;
        int btnW = 180;
        int btnH = 26;
        int gap = 3;
        int startY = this.height / 2 - 18;
        int x = cx - btnW / 2;

        for (int i = 0; i < 5; i++) {
            int by = startY + (btnH + gap) * i + (i == 4 ? 6 : 0);
            if (mouseX >= x && mouseX <= x + btnW && mouseY >= by && mouseY <= by + btnH) {
                switch (i) {
                    case 0 -> client.setScreen(new SelectWorldScreen(this));
                    case 1 -> client.setScreen(new MultiplayerScreen(this));
                    case 2 -> client.setScreen(new ModuleScreen());
                    case 3 -> client.setScreen(new OptionsScreen(this, client.options));
                    case 4 -> client.scheduleStop();
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawCentered(DrawContext ctx, String text, int cx, int y, int color) {
        int tw = this.textRenderer.getWidth(text);
        ctx.drawText(this.textRenderer, text, cx - tw / 2, y, color, false);
    }

    private void drawText(DrawContext ctx, String text, int x, int y, int color) {
        ctx.drawText(this.textRenderer, text, x, y, color, false);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    static class Petal {
        float x, y, size, speedX, speedY, wobbleSpeed, wobbleAmp, phase, alpha, rotation, rotSpeed;
        int color;
    }

    static class Tree {
        int x, y, height;
        float seed;
        Tree(int x, int y, int height, float seed) {
            this.x = x; this.y = y; this.height = height; this.seed = seed;
        }
    }
}
