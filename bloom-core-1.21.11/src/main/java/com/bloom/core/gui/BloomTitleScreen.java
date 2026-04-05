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
        for (int i = 0; i < 40; i++) petals.add(newPetal(true));
    }

    private Petal newPetal(boolean randomY) {
        Petal p = new Petal();
        p.x = rng.nextFloat() * (this.width + 40) - 20;
        p.y = randomY ? rng.nextFloat() * this.height : -5 - rng.nextFloat() * 20;
        p.size = 1 + rng.nextInt(2);
        p.speedX = 0.15f + rng.nextFloat() * 0.3f;
        p.speedY = 0.2f + rng.nextFloat() * 0.3f;
        p.wobbleSpeed = 1 + rng.nextFloat() * 2;
        p.wobbleAmp = 5 + rng.nextFloat() * 10;
        p.phase = rng.nextFloat() * 6.28f;
        int[] pinks = {0xBBFFB7C9, 0xAAFFC0CB, 0x99FFD1DC, 0xBBF8A4B8, 0xAAF0C0D0};
        p.color = pinks[rng.nextInt(pinks.length)];
        return p;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int w = this.width;
        int h = this.height;
        int cx = w / 2;
        float time = (System.currentTimeMillis() - openTime) / 1000f;

        // Sky gradient
        context.fillGradient(0, 0, w, h / 2, 0xFF1a1025, 0xFF2d1b3d);
        context.fillGradient(0, h / 2, w, h, 0xFF2d1b3d, 0xFF1a0f1a);

        // Ground
        context.fill(0, h - 12, w, h, 0xFF120a10);

        // Petals
        for (int i = petals.size() - 1; i >= 0; i--) {
            Petal p = petals.get(i);
            p.x += p.speedX * delta;
            p.y += p.speedY * delta;
            p.phase += p.wobbleSpeed * delta * 0.05f;
            p.x += (float) Math.sin(p.phase) * p.wobbleAmp * 0.01f * delta;
            if (p.y > h + 10 || p.x > w + 20) { petals.set(i, newPetal(false)); continue; }
            int px = (int) p.x;
            int py = (int) p.y;
            int s = p.size;
            context.fill(px, py, px + s, py + s, p.color);
        }
        if (rng.nextFloat() < 0.2f) petals.add(newPetal(false));
        if (petals.size() > 50) petals.subList(50, petals.size()).clear();

        // Logo panel
        int logoY = h / 2 - 55;
        context.fill(cx - 80, logoY - 8, cx + 80, logoY + 30, 0xCC0d0810);
        int ga = (int)(15 + Math.sin(time * 1.5) * 8);
        context.fill(cx - 70, logoY - 2, cx + 70, logoY + 24, (ga << 24) | 0xFFB0C0);

        String bloom = "B L O O M";
        int bw = this.textRenderer.getWidth(bloom);
        context.drawText(this.textRenderer, bloom, cx - bw / 2, logoY + 2, 0xFFFFFFFF, true);

        String sub = "C L I E N T";
        int sw2 = this.textRenderer.getWidth(sub);
        context.drawText(this.textRenderer, sub, cx - sw2 / 2, logoY + 14, 0xFF99AABB, true);

        // Separator
        context.fill(cx - 40, logoY + 27, cx + 40, logoY + 28, 0x44FFB0C0);

        // Buttons
        int btnW = 140;
        int btnH = 16;
        int gap = 2;
        int startY = h / 2 - 8;

        context.fill(cx - btnW / 2 - 6, startY - 4, cx + btnW / 2 + 6, startY + (btnH + gap) * 5 + 10, 0xBB0d0810);

        drawButton(context, "Singleplayer", cx, startY, btnW, btnH, mouseX, mouseY);
        drawButton(context, "Multiplayer", cx, startY + btnH + gap, btnW, btnH, mouseX, mouseY);
        drawButton(context, "Bloom Mods", cx, startY + (btnH + gap) * 2, btnW, btnH, mouseX, mouseY);
        drawButton(context, "Settings", cx, startY + (btnH + gap) * 3, btnW, btnH, mouseX, mouseY);
        drawButton(context, "Quit Game", cx, startY + (btnH + gap) * 4 + 4, btnW, btnH, mouseX, mouseY);

        // Bottom bar
        context.fill(0, h - 12, w, h, 0xCC0d0810);
        context.drawText(this.textRenderer, "Bloom Client v1.0.0", 4, h - 10, 0xFF998899, true);
        String user = MinecraftClient.getInstance().getSession().getUsername();
        int uw = this.textRenderer.getWidth(user);
        context.drawText(this.textRenderer, user, w - uw - 4, h - 10, 0xFFBBAABB, true);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawButton(DrawContext ctx, String label, int cx, int y, int bw, int bh, int mx, int my) {
        int x = cx - bw / 2;
        boolean hovered = mx >= x && mx <= x + bw && my >= y && my <= y + bh;

        ctx.fill(x, y, x + bw, y + bh, hovered ? 0x66FFB0C0 : 0x44201520);
        ctx.fill(x, y, x + bw, y + 1, 0x33FFB0C0);
        ctx.fill(x, y + bh - 1, x + bw, y + bh, 0x33FFB0C0);
        if (hovered) ctx.fill(x, y, x + 1, y + bh, 0xDDFFB0C0);

        int tw = this.textRenderer.getWidth(label);
        ctx.drawText(this.textRenderer, label, cx - tw / 2, y + (bh - 8) / 2, hovered ? 0xFFFFFFFF : 0xFFDDCCBB, true);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        int cx = this.width / 2;
        int btnW = 140;
        int btnH = 16;
        int gap = 2;
        int startY = this.height / 2 - 8;
        int x = cx - btnW / 2;
        double mouseX = click.x();
        double mouseY = click.y();

        for (int i = 0; i < 5; i++) {
            int by = startY + (btnH + gap) * i + (i == 4 ? 4 : 0);
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
        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    static class Petal {
        float x, y, speedX, speedY, wobbleSpeed, wobbleAmp, phase;
        int size, color;
    }
}
