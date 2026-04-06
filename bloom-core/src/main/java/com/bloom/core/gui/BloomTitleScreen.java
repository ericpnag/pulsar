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
    private final Random rng = new Random();

    public BloomTitleScreen() {
        super(Text.literal("Bloom Client"));
    }

    @Override
    protected void init() {
        openTime = System.currentTimeMillis();
        petals.clear();
        for (int i = 0; i < 50; i++) petals.add(newPetal(true));
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
        p.alpha = 0.2f + rng.nextFloat() * 0.4f;
        int[] pinks = {0xFFB7C9, 0xFFC0CB, 0xFFD1DC, 0xF8A4B8, 0xFFE4E9};
        p.color = pinks[rng.nextInt(pinks.length)];
        return p;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int w = this.width, h = this.height, cx = w / 2;
        float time = (System.currentTimeMillis() - openTime) / 1000f;

        ctx.fillGradient(0, 0, w, h / 3, 0xFF0a0611, 0xFF1a1025);
        ctx.fillGradient(0, h / 3, w, h * 2 / 3, 0xFF1a1025, 0xFF2a1530);
        ctx.fillGradient(0, h * 2 / 3, w, h, 0xFF2a1530, 0xFF0a0611);

        for (int i = petals.size() - 1; i >= 0; i--) {
            Petal p = petals.get(i);
            p.x += p.vx * delta; p.y += p.vy * delta;
            p.phase += p.wobble * delta * 0.04f;
            p.x += (float) Math.sin(p.phase) * 0.15f * delta;
            if (p.y > h + 15 || p.x > w + 40) { petals.set(i, newPetal(false)); continue; }
            int a = (int)(p.alpha * 255) << 24;
            int col = a | (p.color & 0x00FFFFFF);
            int s = (int) p.size;
            ctx.fill((int)p.x - s, (int)p.y, (int)p.x, (int)p.y - s/2, col);
            ctx.fill((int)p.x, (int)p.y - s/2, (int)p.x + s, (int)p.y, col);
            ctx.fill((int)p.x - s, (int)p.y, (int)p.x, (int)p.y + s/2, col);
            ctx.fill((int)p.x, (int)p.y, (int)p.x + s, (int)p.y + s/2, col);
        }
        if (rng.nextFloat() < 0.2f) petals.add(newPetal(false));
        if (petals.size() > 60) petals.subList(60, petals.size()).clear();

        int logoY = h / 4 - 10;
        int ga = (int)(8 + Math.sin(time * 1.2) * 5);
        ctx.fill(cx - 70, logoY - 2, cx + 70, logoY + 28, (ga << 24) | 0xFFB0C0);
        String bloom = "BLOOM";
        int bw2 = this.textRenderer.getWidth(bloom);
        ctx.drawText(this.textRenderer, bloom, cx - bw2 / 2, logoY + 4, 0xFFFFD1DC, false);
        String cl = "client";
        int cw2 = this.textRenderer.getWidth(cl);
        ctx.drawText(this.textRenderer, cl, cx - cw2 / 2, logoY + 18, 0xFF8A7080, false);
        int sa = (int)(20 + Math.sin(time * 1.8) * 10);
        ctx.fill(cx - 40, logoY + 32, cx + 40, logoY + 33, (sa << 24) | 0xFFB0C0);

        int btnW = 150, btnH = 16, gap = 3;
        int startY = h / 4 + 36;
        ctx.fill(cx - btnW/2 - 6, startY - 4, cx + btnW/2 + 6, startY + (btnH + gap) * 5 + gap + 20, 0x880a0611);

        drawBtn(ctx, "Singleplayer", cx, startY, btnW, btnH, mx, my, false, time);
        drawBtn(ctx, "Multiplayer", cx, startY + (btnH+gap), btnW, btnH, mx, my, false, time);
        drawBtn(ctx, "Bloom Mods", cx, startY + (btnH+gap)*2, btnW, btnH, mx, my, false, time);
        drawBtn(ctx, "Cosmetics", cx, startY + (btnH+gap)*3, btnW, btnH, mx, my, false, time);
        drawBtn(ctx, "Settings", cx, startY + (btnH+gap)*4, btnW, btnH, mx, my, false, time);
        drawBtn(ctx, "Quit Game", cx, startY + (btnH+gap)*5 + 5, btnW, btnH, mx, my, true, time);

        ctx.fill(0, h - 14, w, h, 0x990a0611);
        ctx.drawText(this.textRenderer, "Bloom Client v1.0.0", 6, h - 11, 0xFF5A4550, false);
        String user = MinecraftClient.getInstance().getSession().getUsername();
        int uw = this.textRenderer.getWidth(user);
        ctx.drawText(this.textRenderer, user, w - uw - 6, h - 11, 0xFF8A7080, false);

        super.render(ctx, mx, my, delta);
    }

    private void drawBtn(DrawContext ctx, String label, int cx, int y, int bw, int bh, int mx, int my, boolean danger, float time) {
        int x = cx - bw / 2;
        boolean hov = mx >= x && mx <= x + bw && my >= y && my <= y + bh;
        ctx.fill(x, y, x + bw, y + bh, hov ? 0x33FFB7C9 : 0x18FFFFFF);
        ctx.fill(x, y, x + bw, y + 1, hov ? 0x44FFB7C9 : 0x0CFFFFFF);
        if (hov) ctx.fill(x, y, x + 2, y + bh, danger ? 0xBBFF7070 : 0xBBFFB7C9);
        int tw = this.textRenderer.getWidth(label);
        int color = danger ? (hov ? 0xFFFF9090 : 0xFF8A5555) : (hov ? 0xFFF0E4E8 : 0xFFBBA4AC);
        ctx.drawText(this.textRenderer, label, cx - tw / 2, y + (bh - 8) / 2, color, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int cx = this.width / 2, btnW = 150, btnH = 16, gap = 3;
        int startY = this.height / 4 + 36;
        int x = cx - btnW / 2;
        for (int i = 0; i < 6; i++) {
            int by = startY + (btnH + gap) * i + (i == 5 ? 5 : 0);
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
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean shouldCloseOnEsc() { return false; }

    static class Petal {
        float x, y, size, vx, vy, phase, wobble, alpha;
        int color;
    }
}
