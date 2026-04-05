package com.bloom.core.gui;

import com.bloom.core.BloomCore;
import com.bloom.core.module.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.List;

public class ModuleScreen extends Screen {
    public ModuleScreen() { super(Text.literal("Bloom Client")); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int w = this.width; int h = this.height; int cx = w / 2;
        context.fill(0, 0, w, h, 0xDD0d0810);

        context.fill(cx - 80, 4, cx + 80, 30, 0xBB0d0810);
        String title = "BLOOM MODULES";
        int tw = this.textRenderer.getWidth(title);
        context.drawText(this.textRenderer, title, cx - tw / 2, 8, 0xFFFFFFFF, true);
        String sub = "Click to toggle | Right Shift to close";
        int sw = this.textRenderer.getWidth(sub);
        context.drawText(this.textRenderer, sub, cx - sw / 2, 20, 0xFF998899, true);
        context.fill(cx - 60, 31, cx + 60, 32, 0x33FFB0C0);

        List<Module> modules = BloomCore.MODULES.getModules();
        int cols = 2; int cardW = 110; int cardH = 32; int gapX = 4; int gapY = 3;
        int gridW = cols * cardW + gapX;
        int startX = cx - gridW / 2; int startY = 36;

        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            int c = i % cols; int r = i / cols;
            int x = startX + c * (cardW + gapX); int y = startY + r * (cardH + gapY);
            boolean hovered = mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH;
            boolean enabled = m.isEnabled();

            context.fill(x, y, x + cardW, y + cardH, hovered ? 0x55FFB0C0 : 0x440d0810);
            context.fill(x, y, x + 2, y + cardH, enabled ? 0xCC55DD88 : 0x55554444);
            context.fill(x, y, x + cardW, y + 1, 0x22FFB0C0);

            context.drawText(this.textRenderer, m.getName(), x + 6, y + 4, hovered ? 0xFFFFFFFF : 0xFFEEDDCC, true);
            String desc = m.getDescription();
            if (desc.length() > 18) desc = desc.substring(0, 16) + "..";
            context.drawText(this.textRenderer, desc, x + 6, y + 14, 0xFF887788, true);
            context.drawText(this.textRenderer, enabled ? "ON" : "OFF", x + 6, y + 23, enabled ? 0xFF55DD88 : 0xFF776666, true);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        List<Module> modules = BloomCore.MODULES.getModules();
        int cols = 2; int cardW = 110; int cardH = 32; int gapX = 4; int gapY = 3;
        int gridW = cols * cardW + gapX;
        int startX = this.width / 2 - gridW / 2; int startY = 36;
        double mx = click.x(); double my = click.y();
        for (int i = 0; i < modules.size(); i++) {
            int c = i % cols; int r = i / cols;
            int x = startX + c * (cardW + gapX); int y = startY + r * (cardH + gapY);
            if (mx >= x && mx <= x + cardW && my >= y && my <= y + cardH) {
                modules.get(i).toggle();
                return true;
            }
        }
        return super.mouseClicked(click, bl);
    }

    @Override public boolean shouldPause() { return false; }
}
