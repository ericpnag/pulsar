package com.bloom.core.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BloomTitleScreen extends Screen {
    private long openTime;
    private final List<float[]> petals = new ArrayList<>();
    private final Random rng = new Random();

    @Override
    public void init() {
        openTime = System.currentTimeMillis();
        petals.clear();
        for (int i = 0; i < 40; i++) {
            petals.add(new float[]{
                rng.nextFloat() * width, rng.nextFloat() * height,
                0.2f + rng.nextFloat() * 0.5f, 0.3f + rng.nextFloat() * 0.4f,
                2 + rng.nextFloat() * 3, rng.nextFloat() * 0.4f + 0.15f
            });
        }
    }

    @Override
    public void render(int mx, int my, float delta) {
        int w = this.width, h = this.height, cx = w / 2;

        // Background gradient
        fillGradient(0, 0, w, h, 0xFF120a18, 0xFF1e1030);

        // Petals
        for (float[] p : petals) {
            p[0] += p[2] * delta; p[1] += p[3] * delta;
            if (p[1] > h + 10 || p[0] > w + 20) { p[0] = -5; p[1] = -rng.nextFloat() * 30; }
            int s = (int) p[4];
            int a = (int)(p[5] * 200) << 24;
            fill((int)p[0] - s, (int)p[1] - s/2, (int)p[0] + s, (int)p[1] + s/2, a | 0xFFB7C9);
        }

        // Title
        drawCenteredString(textRenderer, "BLOOM", cx, h / 4, 0xFFFFD1DC);
        drawCenteredString(textRenderer, "M I N E C R A F T   C L I E N T", cx, h / 4 + 14, 0xFF6A5060);

        // Buttons
        int btnW = 150, btnH = 20, gap = 3;
        int startY = h / 4 + 30;
        String[] labels = {"Singleplayer", "Multiplayer", "Bloom Mods", "Settings", "Quit Game"};
        for (int i = 0; i < labels.length; i++) {
            int by = startY + (btnH + gap) * i + (i == 4 ? 4 : 0);
            boolean hov = mx >= cx - btnW/2 && mx <= cx + btnW/2 && my >= by && my <= by + btnH;
            fill(cx - btnW/2, by, cx + btnW/2, by + btnH, hov ? 0x33FFB7C9 : 0x18FFFFFF);
            if (hov) fill(cx - btnW/2, by, cx - btnW/2 + 2, by + btnH, i == 4 ? 0xBBFF7070 : 0xBBFFB7C9);
            int color = i == 4 ? (hov ? 0xFFFF9090 : 0xFF8A5555) : (hov ? 0xFFF0E4E8 : 0xFFBBA4AC);
            drawCenteredString(textRenderer, labels[i], cx, by + (btnH - 8) / 2, color);
        }

        // Bottom info
        fill(0, h - 14, w, h, 0x990a0611);
        textRenderer.drawWithShadow("Bloom Client v1.0.0", 6, h - 11, 0xFF5A4550);
        String user = MinecraftClient.getInstance().getSession().getUsername();
        textRenderer.drawWithShadow(user, w - textRenderer.getStringWidth(user) - 6, h - 11, 0xFF8A7080);

        super.render(mx, my, delta);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0) return;
        int cx = width / 2, btnW = 150, btnH = 20, gap = 3;
        int startY = height / 4 + 30;
        MinecraftClient mc = MinecraftClient.getInstance();

        for (int i = 0; i < 5; i++) {
            int by = startY + (btnH + gap) * i + (i == 4 ? 4 : 0);
            if (mouseX >= cx - btnW/2 && mouseX <= cx + btnW/2 && mouseY >= by && mouseY <= by + btnH) {
                switch (i) {
                    case 0: mc.setScreen(new SelectWorldScreen(this)); break;
                    case 1: mc.setScreen(new MultiplayerScreen(this)); break;
                    case 2: mc.setScreen(new ModuleScreen()); break;
                    case 3: mc.setScreen(new SettingsScreen(this, mc.options)); break;
                    case 4: mc.scheduleStop(); break;
                }
                return;
            }
        }
    }

    @Override
    public boolean shouldPauseGame() { return false; }
}
