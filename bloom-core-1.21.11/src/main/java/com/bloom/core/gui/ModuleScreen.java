package com.bloom.core.gui;

import com.bloom.core.BloomCore;
import com.bloom.core.config.KeyBindConfig;
import com.bloom.core.module.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.List;

import static com.bloom.core.gui.BloomGui.*;

public class ModuleScreen extends Screen {
    private int scrollOffset = 0;
    private int selectedModule = -1;
    private boolean waitingForKey = false; // true when listening for keybind press

    /** Draw text using Inter font */
    private void drawT(DrawContext ctx, String s, int x, int y, int color, boolean shadow) {
        ctx.drawText(this.textRenderer, text(s, color & 0xFFFFFF), x, y, -1, false);
    }
    private int tw(String s) { return textW(this.textRenderer, s); }

    // Icon symbols for each module (matched by index in ModuleManager)
    private static final String[] ICONS = {
        ">>",  // Toggle Sprint
        "vv",  // Toggle Sneak
        "60",  // FPS Display
        "XYZ", // Coordinates
        "N",   // Direction HUD
        "()",  // Zoom
        "Eye", // Freelook
        "ms",  // Ping Display
        "CPS", // CPS Counter
        "x5",  // Combo Counter
        "3.0", // Reach Display
        "Fx",  // Potion Effects
        "T",   // Potion Timer
        "WASD",// Keystrokes
        "[]",  // Armor HUD
        "SAT", // Saturation
        "MB",  // Memory Display
        "IP",  // Server Display
        "PK",  // Pack Display
        "DAY", // Time Changer
        "SB",  // Scoreboard
        "~",   // Low Fire
        "|-|", // Low Shield
        "C",   // Bloom Cape
        "FPS", // FPS Boost
        "Eye", // Freelook (bundled)
        "Hit", // Better Hitreg (bundled)
    };

    private static final int[] ICON_COLORS = {
        0xFF6EE7A0, // Sprint - green
        0xFF6EE7A0, // Sneak - green
        0xFFFFB7C9, // FPS - pink
        0xFFF0CC60, // Coords - yellow
        0xFF60C0F0, // Direction - blue
        0xFFBB80FF, // Zoom - purple
        0xFFBB80FF, // Freelook - purple
        0xFFF0CC60, // Ping - yellow
        0xFFFF7070, // CPS - red
        0xFFFF7070, // Combo - red
        0xFFFF7070, // Reach - red
        0xFF6EE7A0, // Potion Effects - green
        0xFF6EE7A0, // Potion Timer - green
        0xFFFFB7C9, // Keystrokes - pink
        0xFF60C0F0, // Armor HUD - blue
        0xFFF0CC60, // Saturation - yellow
        0xFF60C0F0, // Memory - blue
        0xFFBBA4AC, // Server - gray
        0xFFBBA4AC, // Pack - gray
        0xFFF0CC60, // Time - yellow
        0xFFBBA4AC, // Scoreboard - gray
        0xFFFF7070, // Low Fire - red
        0xFF60C0F0, // Low Shield - blue
        0xFFFFB7C9, // Cape - pink
        0xFF6EE7A0, // FPS Boost - green
        0xFFBB80FF, // Freelook - purple
        0xFFFF7070, // Better Hitreg - red
    };

    public ModuleScreen() { super(Text.literal("Bloom Mods")); }

    @Override protected void init() { scrollOffset = 0; selectedModule = -1; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int w = this.width, h = this.height, cx = w / 2;
        ctx.fill(0, 0, w, h, 0xEE0a0611);

        // Title
        String title = "BLOOM MODS";
        int tw = tw(title);
        drawT(ctx, title, cx - tw / 2, 8, 0xFFFFD1DC, false);
        ctx.fill(cx - 40, 19, cx + 40, 20, 0x22FFB7C9);

        List<Module> modules = BloomCore.MODULES.getModules();

        // Module grid (left side or full width if no selection)
        int gridRight = selectedModule >= 0 ? w / 2 - 4 : w - 8;
        int cols = Math.max(2, Math.min(4, (gridRight - 12) / 100));
        int cardW = (gridRight - 12 - (cols - 1) * 6) / cols;
        int cardH = 48;
        int gapX = 6, gapY = 6;
        int gridW = cols * cardW + (cols - 1) * gapX;
        int startX = (gridRight - gridW) / 2 + 4;
        int startY = 26;

        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            int col = i % cols, row = i / cols;
            int x = startX + col * (cardW + gapX);
            int y = startY + row * (cardH + gapY) - scrollOffset;

            if (y + cardH < startY || y > h - 20) continue;

            boolean hov = mx >= x && mx <= x + cardW && my >= y && my <= y + cardH;
            boolean sel = i == selectedModule;
            boolean on = m.isEnabled();

            // Card
            int bg = sel ? 0x44FFB7C9 : (hov ? 0x28FFB7C9 : 0x15FFFFFF);
            ctx.fill(x, y, x + cardW, y + cardH, bg);
            if (sel) {
                ctx.fill(x, y, x + cardW, y + 1, 0x88FFB7C9);
                ctx.fill(x, y + cardH - 1, x + cardW, y + cardH, 0x88FFB7C9);
            }

            // Icon box
            int iconSize = 20;
            int iconX = x + 4, iconY = y + 4;
            int iconCol = i < ICON_COLORS.length ? ICON_COLORS[i] : 0xFFBBA4AC;
            ctx.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, (iconCol & 0x00FFFFFF) | 0x22000000);
            // Icon text
            String ico = i < ICONS.length ? ICONS[i] : "?";
            if (ico.length() > 2) ico = ico.substring(0, 2);
            int iw = tw(ico);
            drawT(ctx, ico, iconX + (iconSize - iw) / 2, iconY + 6, iconCol, false);

            // Name (right of icon)
            String name = m.getName();
            if (tw(name) > cardW - iconSize - 12) {
                while (tw(name + "..") > cardW - iconSize - 12 && name.length() > 3)
                    name = name.substring(0, name.length() - 1);
                name += "..";
            }
            drawT(ctx, name, iconX + iconSize + 4, iconY + 1, hov || sel ? 0xFFF0E4E8 : 0xFFBBA4AC, false);

            // Status
            String status = on ? "ON" : "OFF";
            int statusCol = on ? 0xFF6EE7A0 : 0xFF5A4550;
            drawT(ctx, status, iconX + iconSize + 4, iconY + 11, statusCol, false);

            // Toggle bar at bottom
            int barY = y + cardH - 4;
            ctx.fill(x, barY, x + cardW, barY + 3, on ? (0x446EE7A0) : 0x22554455);
        }

        // Settings panel (right side)
        if (selectedModule >= 0 && selectedModule < modules.size()) {
            Module m = modules.get(selectedModule);
            int px = w / 2 + 2, pw = w / 2 - 6;
            ctx.fill(px, 26, px + pw, h - 20, 0x22FFFFFF);
            ctx.fill(px, 26, px + pw, 27, 0x33FFB7C9);

            // Module name
            drawT(ctx, m.getName(), px + 10, 34, 0xFFFFD1DC, false);
            drawT(ctx, m.getDescription(), px + 10, 46, 0xFF8A7080, false);

            // Icon
            int iconCol = selectedModule < ICON_COLORS.length ? ICON_COLORS[selectedModule] : 0xFFBBA4AC;
            ctx.fill(px + pw - 30, 30, px + pw - 6, 54, (iconCol & 0x00FFFFFF) | 0x33000000);
            String ico = selectedModule < ICONS.length ? ICONS[selectedModule] : "?";
            int iw = tw(ico);
            drawT(ctx, ico, px + pw - 18 - iw / 2, 38, iconCol, false);

            ctx.fill(px + 10, 58, px + pw - 10, 59, 0x22FFB7C9);

            // Toggle button
            int btnX = px + 10, btnY = 66, btnW = pw - 20, btnH = 18;
            boolean btnHov = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;
            boolean on = m.isEnabled();
            ctx.fill(btnX, btnY, btnX + btnW, btnY + btnH, on ? (btnHov ? 0x885ECC70 : 0x6644AA55) : (btnHov ? 0x44FFFFFF : 0x22FFFFFF));
            String toggleText = on ? "Enabled — Click to Disable" : "Disabled — Click to Enable";
            int ttw = tw(toggleText);
            drawT(ctx, toggleText, btnX + btnW / 2 - ttw / 2, btnY + 5, on ? 0xFFFFFFFF : 0xFF8A7080, false);

            // Info section
            int infoY = 94;
            drawT(ctx, "Status:", px + 10, infoY, 0xFF8A7080, false);
            drawT(ctx, on ? "Active" : "Inactive", px + 50, infoY, on ? 0xFF6EE7A0 : 0xFF5A4550, false);

            drawT(ctx, "Type:", px + 10, infoY + 14, 0xFF8A7080, false);
            String type = m.hasHud() ? "HUD Overlay" : "Toggle";
            drawT(ctx, type, px + 50, infoY + 14, 0xFFBBA4AC, false);

            // Keybind section (if this module has one)
            String bindId = KeyBindConfig.getBindId(m.getName());
            if (bindId != null) {
                int kbY = infoY + 32;
                ctx.fill(px + 10, kbY - 4, px + pw - 10, kbY - 3, 0x15FFB7C9);
                drawT(ctx, "Keybind:", px + 10, kbY, 0xFF8A7080, false);

                // Keybind button
                int kbBtnX = px + 60, kbBtnW = pw - 80, kbBtnH = 16;
                boolean kbHov = mx >= kbBtnX && mx <= kbBtnX + kbBtnW && my >= kbY - 2 && my <= kbY + kbBtnH - 2;
                ctx.fill(kbBtnX, kbY - 2, kbBtnX + kbBtnW, kbY + kbBtnH - 2, waitingForKey ? 0x66FFB7C9 : (kbHov ? 0x33FFB7C9 : 0x22FFFFFF));

                String keyText = waitingForKey ? "> Press a key <" : KeyBindConfig.getKeyName(KeyBindConfig.getKey(bindId));
                int ktw = tw(keyText);
                drawT(ctx, keyText, kbBtnX + kbBtnW / 2 - ktw / 2, kbY + 2, waitingForKey ? 0xFFFFB7C9 : 0xFFF0E4E8, false);
            }

            // Close button
            int closeX = px + pw - 14, closeY = 28;
            boolean closeHov = mx >= closeX && mx <= closeX + 10 && my >= closeY && my <= closeY + 10;
            drawT(ctx, "x", closeX + 1, closeY, closeHov ? 0xFFFF7070 : 0xFF5A4550, false);
        }

        // Bottom bar
        ctx.fill(0, h - 16, w, h, 0xCC0a0611);
        ctx.fill(0, h - 16, w, h - 15, 0x15FFB7C9);
        long enabled = modules.stream().filter(Module::isEnabled).count();
        String info = modules.size() + " mods | " + enabled + " active | Right Shift to close";
        int infoW = tw(info);
        drawT(ctx, info, cx - infoW / 2, h - 12, 0xFF5A4550, false);

        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        List<Module> modules = BloomCore.MODULES.getModules();
        int w = this.width, h = this.height;
        double mx = click.x(), my = click.y();

        // Settings panel interactions
        if (selectedModule >= 0 && selectedModule < modules.size()) {
            int px = w / 2 + 2, pw = w / 2 - 6;

            // Close button
            int closeX = px + pw - 14, closeY = 28;
            if (mx >= closeX && mx <= closeX + 10 && my >= closeY && my <= closeY + 10) {
                selectedModule = -1; return true;
            }

            // Toggle button
            int btnX = px + 10, btnY = 66, btnW = pw - 20, btnH = 18;
            if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH) {
                modules.get(selectedModule).toggle(); return true;
            }

            // Keybind button
            Module m = modules.get(selectedModule);
            String bindId = KeyBindConfig.getBindId(m.getName());
            if (bindId != null) {
                int kbBtnX = px + 60, kbBtnW = pw - 80, kbY = 94 + 32;
                if (mx >= kbBtnX && mx <= kbBtnX + kbBtnW && my >= kbY - 2 && my <= kbY + 14) {
                    waitingForKey = true; return true;
                }
            }
        }

        // Module grid
        int gridRight = selectedModule >= 0 ? w / 2 - 4 : w - 8;
        int cols = Math.max(2, Math.min(4, (gridRight - 12) / 100));
        int cardW = (gridRight - 12 - (cols - 1) * 6) / cols;
        int cardH = 48;
        int gapX = 6, gapY = 6;
        int gridW = cols * cardW + (cols - 1) * gapX;
        int startX = (gridRight - gridW) / 2 + 4;
        int startY = 26;

        for (int i = 0; i < modules.size(); i++) {
            int col = i % cols, row = i / cols;
            int x = startX + col * (cardW + gapX);
            int y = startY + row * (cardH + gapY) - scrollOffset;
            if (mx >= x && mx <= x + cardW && my >= y && my <= y + cardH) {
                selectedModule = (selectedModule == i) ? -1 : i;
                return true;
            }
        }
        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput keyInput) {
        if (waitingForKey && selectedModule >= 0) {
            List<Module> modules = BloomCore.MODULES.getModules();
            if (selectedModule < modules.size()) {
                String bindId = KeyBindConfig.getBindId(modules.get(selectedModule).getName());
                if (bindId != null && keyInput.key() != org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                    KeyBindConfig.setKey(bindId, keyInput.key());
                }
            }
            waitingForKey = false;
            return true;
        }
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        scrollOffset -= (int)(vAmount * 20);
        if (scrollOffset < 0) scrollOffset = 0;
        List<Module> modules = BloomCore.MODULES.getModules();
        int gridRight = selectedModule >= 0 ? this.width / 2 - 4 : this.width - 8;
        int cols = Math.max(2, Math.min(4, (gridRight - 12) / 100));
        int rows = (modules.size() + cols - 1) / cols;
        int maxScroll = Math.max(0, rows * 54 - (this.height - 60));
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        return true;
    }

    @Override public boolean shouldPause() { return false; }
}
