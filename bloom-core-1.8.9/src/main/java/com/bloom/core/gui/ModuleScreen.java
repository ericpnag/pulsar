package com.bloom.core.gui;

import com.bloom.core.BloomCore;
import com.bloom.core.module.Module;
import net.minecraft.client.gui.screen.Screen;
import java.util.List;

public class ModuleScreen extends Screen {
    @Override
    public void render(int mx, int my, float delta) {
        int w = this.width, h = this.height, cx = w / 2;
        fill(0, 0, w, h, 0xEE0a0611);

        drawCenteredString(textRenderer, "BLOOM MODS", cx, 10, 0xFFFFD1DC);
        fill(cx - 40, 21, cx + 40, 22, 0x22FFB7C9);

        List<Module> modules = BloomCore.MODULES.getModules();
        int cols = 2, cardW = 130, cardH = 30, gapX = 6, gapY = 4;
        int gridW = cols * cardW + gapX;
        int startX = cx - gridW / 2, startY = 28;

        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            int col = i % cols, row = i / cols;
            int x = startX + col * (cardW + gapX);
            int y = startY + row * (cardH + gapY);
            boolean hov = mx >= x && mx <= x + cardW && my >= y && my <= y + cardH;
            boolean on = m.isEnabled();

            fill(x, y, x + cardW, y + cardH, hov ? 0x33FFB7C9 : 0x15FFFFFF);
            fill(x, y, x + 2, y + cardH, on ? 0xCC55DD88 : 0x44554444);

            textRenderer.drawWithShadow(m.getName(), x + 6, y + 4, hov ? 0xFFFFFF : 0xDDCCCC);
            String desc = m.getDescription();
            if (textRenderer.getStringWidth(desc) > cardW - 12) desc = desc.substring(0, 16) + "..";
            textRenderer.drawWithShadow(desc, x + 6, y + 14, 0x665566);
            textRenderer.drawWithShadow(on ? "ON" : "OFF", x + cardW - 20, y + 4, on ? 0x55DD88 : 0x776666);
        }

        drawCenteredString(textRenderer, "ESC to close | Click to toggle", cx, h - 14, 0xFF5A4550);
        super.render(mx, my, delta);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0) return;
        List<Module> modules = BloomCore.MODULES.getModules();
        int cx = width / 2, cols = 2, cardW = 130, cardH = 30, gapX = 6, gapY = 4;
        int gridW = cols * cardW + gapX;
        int startX = cx - gridW / 2, startY = 28;

        for (int i = 0; i < modules.size(); i++) {
            int col = i % cols, row = i / cols;
            int x = startX + col * (cardW + gapX);
            int y = startY + row * (cardH + gapY);
            if (mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH) {
                modules.get(i).toggle();
                return;
            }
        }
    }

    @Override
    public boolean shouldPauseGame() { return false; }
}
