package com.bloom.core.gui;

import com.bloom.core.PulsarCore;
import com.bloom.core.config.KeyBindConfig;
import com.bloom.core.module.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.*;
import java.util.stream.Collectors;

import static com.bloom.core.gui.PulsarGui.*;

public class ModuleScreen extends Screen {
    private int scrollOffset = 0;
    private int selectedModule = -1;
    private boolean waitingForKey = false;
    private String searchQuery = "";
    private String selectedCategory = "All";
    private int animTick = 0;

    // Star particles for animated background
    private static final int STAR_COUNT = 40;
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSpeed = new float[STAR_COUNT];
    private final float[] starAlpha = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];
    private boolean starsInit = false;

    private static final String[] CATEGORIES = {"All", "HUD", "PvP", "Visual", "Utility", "Hypixel", "Cosmetics"};

    // Category assignments by module name
    private static final Map<String, String> MODULE_CATEGORIES = new HashMap<>();
    static {
        // HUD
        for (String n : new String[]{"FPS Display", "Coordinates", "Direction HUD", "Ping Display",
                "CPS Counter", "Combo Counter", "Reach Display", "Potion Effects", "Potion Timer",
                "Keystrokes", "Armor HUD", "Armor Status", "Saturation Display", "Memory Display",
                "Server Display", "Pack Display"})
            MODULE_CATEGORIES.put(n, "HUD");
        // PvP
        for (String n : new String[]{"Toggle Sprint", "Toggle Sneak", "Low Fire", "Low Shield",
                "Hurt Camera", "Auto GG", "Hit Color", "Damage Indicator", "Custom Crosshair"})
            MODULE_CATEGORIES.put(n, "PvP");
        // Visual
        for (String n : new String[]{"Fullbright", "FOV Changer", "Time Changer", "Weather Changer",
                "Scoreboard", "TNT Timer", "Motion Blur", "Boss Bar", "Custom Crosshair"})
            MODULE_CATEGORIES.put(n, "Visual");
        // Utility
        for (String n : new String[]{"Zoom", "Freelook", "FPS Boost", "Stopwatch", "Clock", "Playtime"})
            MODULE_CATEGORIES.put(n, "Utility");
        // Hypixel / Bedwars
        for (String n : new String[]{"Auto GG", "Auto Tip", "Bedwars Overlay"})
            MODULE_CATEGORIES.put(n, "Hypixel");
        // Cosmetics
        for (String n : new String[]{"Pulsar Cape"})
            MODULE_CATEGORIES.put(n, "Cosmetics");
    }

    private String getCategory(Module m) {
        return MODULE_CATEGORIES.getOrDefault(m.getName(), "Utility");
    }

    private List<Module> getFilteredModules() {
        return PulsarCore.MODULES.getModules().stream()
            .filter(m -> selectedCategory.equals("All") || getCategory(m).equals(selectedCategory))
            .filter(m -> searchQuery.isEmpty() || m.getName().toLowerCase().contains(searchQuery.toLowerCase()))
            .collect(Collectors.toList());
    }

    private void drawT(DrawContext ctx, String s, int x, int y, int color, boolean shadow) {
        ctx.drawText(this.textRenderer, text(s, color & 0xFFFFFF), x, y, -1, shadow);
    }
    private int tw(String s) { return textW(this.textRenderer, s); }

    private void fillRound(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 1, y, x + w - 1, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    public ModuleScreen() { super(Text.literal("Pulsar Mods")); }

    @Override
    protected void init() {
        scrollOffset = 0;
        selectedModule = -1;
        searchQuery = "";
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int w = this.width, h = this.height;
        animTick++;

        // === Animated background ===
        ctx.fill(0, 0, w, h, 0xF0000000);

        // Star particles
        if (!starsInit) {
            Random rand = new Random();
            for (int i = 0; i < STAR_COUNT; i++) {
                starX[i] = rand.nextFloat() * w;
                starY[i] = rand.nextFloat() * h;
                starSpeed[i] = 0.1f + rand.nextFloat() * 0.3f;
                starAlpha[i] = 0.1f + rand.nextFloat() * 0.5f;
                starSize[i] = 0.5f + rand.nextFloat() * 1.5f;
            }
            starsInit = true;
        }
        for (int i = 0; i < STAR_COUNT; i++) {
            starY[i] += starSpeed[i];
            if (starY[i] > h) { starY[i] = -2; starX[i] = (float)(Math.random() * w); }
            float twinkle = (float)(0.5f + 0.5f * Math.sin(animTick * 0.05f + i * 1.7f));
            int alpha = (int)(starAlpha[i] * twinkle * 255);
            int col = (alpha << 24) | 0xFFFFFF;
            int sx = (int) starX[i], sy = (int) starY[i], ss = (int) starSize[i];
            ctx.fill(sx, sy, sx + ss, sy + ss, col);
        }

        // Subtle radial glow
        int glowX = w / 2, glowY = h / 3;
        for (int r = 200; r > 0; r -= 4) {
            int a = (int)(3 * (1.0f - r / 200.0f));
            ctx.fill(glowX - r, glowY - r / 2, glowX + r, glowY + r / 2, (a << 24) | 0xFFFFFF);
        }

        // === Sidebar (categories) ===
        int sideW = 90;
        ctx.fill(0, 0, sideW, h, 0x30FFFFFF);
        ctx.fill(sideW, 0, sideW + 1, h, 0x15FFFFFF);

        // Logo
        drawT(ctx, "PULSAR", sideW / 2 - tw("PULSAR") / 2, 12, 0xFFFFFF, false);
        ctx.fill(10, 24, sideW - 10, 25, 0x15FFFFFF);

        // Category buttons
        int catY = 34;
        for (String cat : CATEGORIES) {
            boolean active = cat.equals(selectedCategory);
            boolean hov = mx >= 0 && mx < sideW && my >= catY && my < catY + 20;

            if (active) {
                fillRound(ctx, 4, catY, sideW - 8, 20, 0x25FFFFFF);
                ctx.fill(4, catY, 6, catY + 20, 0xFFFFFFFF); // left accent bar
            } else if (hov) {
                fillRound(ctx, 4, catY, sideW - 8, 20, 0x10FFFFFF);
            }

            // Category icon
            String icon = switch (cat) {
                case "All" -> "*";
                case "HUD" -> "H";
                case "PvP" -> "P";
                case "Visual" -> "V";
                case "Utility" -> "U";
                case "Hypixel" -> "X";
                case "Cosmetics" -> "C";
                default -> "?";
            };
            drawT(ctx, icon, 14, catY + 6, active ? 0xFFFFFF : 0x606060, false);
            drawT(ctx, cat, 26, catY + 6, active ? 0xFFFFFF : (hov ? 0xA0A0A0 : 0x606060), false);

            catY += 24;
        }

        // HUD Editor button at bottom of sidebar
        int hudBtnY = h - 50;
        boolean hudHov = mx >= 4 && mx < sideW - 4 && my >= hudBtnY && my < hudBtnY + 22;
        fillRound(ctx, 4, hudBtnY, sideW - 8, 22, hudHov ? 0x30C678DD : 0x15C678DD);
        drawT(ctx, "HUD Editor", sideW / 2 - tw("HUD Editor") / 2, hudBtnY + 7, hudHov ? 0xC678DD : 0x8060A0, false);

        // Module count at bottom of sidebar
        List<Module> allMods = PulsarCore.MODULES.getModules();
        long enabledCount = allMods.stream().filter(Module::isEnabled).count();
        drawT(ctx, enabledCount + "/" + allMods.size(), sideW / 2 - tw(enabledCount + "/" + allMods.size()) / 2, h - 24, 0x404040, false);
        drawT(ctx, "active", sideW / 2 - tw("active") / 2, h - 14, 0x303030, false);

        // === Main content area ===
        int contentX = sideW + 8;
        int contentW = (selectedModule >= 0 ? w * 3 / 5 : w) - contentX - 4;

        // Search bar
        int searchY = 8, searchH = 20;
        fillRound(ctx, contentX, searchY, Math.min(contentW, 220), searchH, 0x15FFFFFF);
        String searchDisplay = searchQuery.isEmpty() ? "Search mods..." : searchQuery;
        int searchCol = searchQuery.isEmpty() ? 0x404040 : 0xE0E0E0;
        drawT(ctx, searchDisplay, contentX + 8, searchY + 6, searchCol, false);
        // Search icon
        drawT(ctx, "Q", contentX + Math.min(contentW, 220) - 16, searchY + 6, 0x404040, false);

        // Blinking cursor
        if (!searchQuery.isEmpty() && (animTick / 15) % 2 == 0) {
            int cursorX = contentX + 8 + tw(searchQuery);
            ctx.fill(cursorX, searchY + 5, cursorX + 1, searchY + 15, 0xAAFFFFFF);
        }

        // === Module grid ===
        List<Module> filtered = getFilteredModules();
        int cols = Math.max(2, Math.min(4, contentW / 110));
        int cardW = (contentW - (cols - 1) * 6) / cols;
        int cardH = 52;
        int gapX = 6, gapY = 6;
        int startY = 36;

        // Clamp scroll
        int totalRows = (filtered.size() + cols - 1) / cols;
        int maxScroll = Math.max(0, totalRows * (cardH + gapY) - (h - startY - 10));
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        for (int i = 0; i < filtered.size(); i++) {
            Module m = filtered.get(i);
            int col = i % cols, row = i / cols;
            int x = contentX + col * (cardW + gapX);
            int y = startY + row * (cardH + gapY) - scrollOffset;

            if (y + cardH < startY || y > h) continue;

            int globalIdx = PulsarCore.MODULES.getModules().indexOf(m);
            boolean hov = mx >= x && mx <= x + cardW && my >= y && my <= y + cardH;
            boolean sel = globalIdx == selectedModule;
            boolean on = m.isEnabled();

            // Card background
            int bg = sel ? 0x20FFFFFF : (hov ? 0x12FFFFFF : 0x08FFFFFF);
            fillRound(ctx, x, y, cardW, cardH, bg);
            if (sel) {
                ctx.fill(x + 1, y, x + cardW - 1, y + 1, 0x66FFFFFF);
            }

            // Module name
            String name = m.getName();
            if (tw(name) > cardW - 50) {
                while (tw(name + "..") > cardW - 50 && name.length() > 3) name = name.substring(0, name.length() - 1);
                name += "..";
            }
            drawT(ctx, name, x + 10, y + 10, sel || hov ? 0xFFFFFF : 0xC0C0C0, false);

            // Category tag
            String cat = getCategory(m);
            drawT(ctx, cat, x + 10, y + 22, 0x505050, false);

            // Toggle switch (right side)
            int toggleX = x + cardW - 30, toggleY = y + 12;
            int toggleW = 22, toggleH = 12;
            int toggleBg = on ? 0xFF4CAF50 : 0xFF333333;
            fillRound(ctx, toggleX, toggleY, toggleW, toggleH, toggleBg);
            // Toggle knob
            int knobX = on ? toggleX + toggleW - 10 : toggleX + 2;
            ctx.fill(knobX, toggleY + 2, knobX + 8, toggleY + toggleH - 2, 0xFFFFFFFF);

            // Bottom accent line
            if (on) {
                ctx.fill(x + 2, y + cardH - 2, x + cardW - 2, y + cardH - 1, 0x444CAF50);
            }
        }

        if (filtered.isEmpty()) {
            String empty = searchQuery.isEmpty() ? "No mods in this category" : "No mods found";
            drawT(ctx, empty, contentX + contentW / 2 - tw(empty) / 2, h / 2, 0x404040, false);
        }

        // === Settings panel (right side) ===
        if (selectedModule >= 0 && selectedModule < PulsarCore.MODULES.getModules().size()) {
            Module m = PulsarCore.MODULES.getModules().get(selectedModule);
            int px = w * 3 / 5 + 4, pw = w * 2 / 5 - 8;

            // Panel background
            fillRound(ctx, px, 8, pw, h - 16, 0x18FFFFFF);
            ctx.fill(px, 8, px + 1, h - 8, 0x15FFFFFF);

            // Module name & description
            drawT(ctx, m.getName(), px + 14, 20, 0xFFFFFF, false);
            drawT(ctx, getCategory(m), px + 14 + tw(m.getName()) + 8, 20, 0x505050, false);
            ctx.fill(px + 14, 32, px + pw - 14, 33, 0x10FFFFFF);

            // Description
            String desc = m.getDescription();
            if (tw(desc) > pw - 28) {
                while (tw(desc + "..") > pw - 28 && desc.length() > 10) desc = desc.substring(0, desc.length() - 1);
                desc += "..";
            }
            drawT(ctx, desc, px + 14, 40, 0x707070, false);

            // Big toggle button
            int btnX = px + 14, btnY = 60, btnW = pw - 28, btnH = 24;
            boolean btnHov = mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH;
            boolean on = m.isEnabled();
            int btnBg = on ? (btnHov ? 0xFF5CBF60 : 0xFF4CAF50) : (btnHov ? 0x33FFFFFF : 0x1AFFFFFF);
            fillRound(ctx, btnX, btnY, btnW, btnH, btnBg);
            String toggleText = on ? "ENABLED" : "DISABLED";
            int ttw = tw(toggleText);
            drawT(ctx, toggleText, btnX + btnW / 2 - ttw / 2, btnY + 8, on ? 0xFFFFFF : 0x606060, false);

            // Info
            int infoY = 96;
            drawT(ctx, "Status", px + 14, infoY, 0x505050, false);
            drawT(ctx, on ? "Active" : "Inactive", px + pw - 14 - tw(on ? "Active" : "Inactive"), infoY, on ? 0xFF4CAF50 : 0x505050, false);

            drawT(ctx, "Type", px + 14, infoY + 16, 0x505050, false);
            String type = m.hasHud() ? "HUD Overlay" : "Toggle";
            drawT(ctx, type, px + pw - 14 - tw(type), infoY + 16, 0x808080, false);

            // Keybind
            String bindId = KeyBindConfig.getBindId(m.getName());
            if (bindId != null) {
                int kbY = infoY + 36;
                ctx.fill(px + 14, kbY - 4, px + pw - 14, kbY - 3, 0x08FFFFFF);
                drawT(ctx, "Keybind", px + 14, kbY, 0x505050, false);

                int kbBtnX = px + pw / 2, kbBtnW = pw / 2 - 14, kbBtnH = 16;
                boolean kbHov = mx >= kbBtnX && mx <= kbBtnX + kbBtnW && my >= kbY - 2 && my <= kbY + kbBtnH - 2;
                fillRound(ctx, kbBtnX, kbY - 2, kbBtnW, kbBtnH, waitingForKey ? 0x33FFFFFF : (kbHov ? 0x1AFFFFFF : 0x0DFFFFFF));
                String keyText = waitingForKey ? "> Press key <" : KeyBindConfig.getKeyName(KeyBindConfig.getKey(bindId));
                int ktw = tw(keyText);
                drawT(ctx, keyText, kbBtnX + kbBtnW / 2 - ktw / 2, kbY + 2, waitingForKey ? 0xFFFFFF : 0xA0A0A0, false);
            }

            // Close (X)
            int closeX = px + pw - 18, closeY = 16;
            boolean closeHov = mx >= closeX - 2 && mx <= closeX + 10 && my >= closeY - 2 && my <= closeY + 12;
            drawT(ctx, "x", closeX, closeY, closeHov ? 0xFF5050 : 0x404040, false);
        }

        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        double mx = click.x(), my = click.y();
        int w = this.width, h = this.height;
        int sideW = 90;

        // HUD Editor button
        int hudBtnY = h - 50;
        if (mx >= 4 && mx < sideW - 4 && my >= hudBtnY && my < hudBtnY + 22) {
            client.setScreen(new HudEditorScreen());
            return true;
        }

        // Category sidebar clicks
        int catY = 34;
        for (String cat : CATEGORIES) {
            if (mx >= 0 && mx < sideW && my >= catY && my < catY + 20) {
                selectedCategory = cat;
                scrollOffset = 0;
                selectedModule = -1;
                return true;
            }
            catY += 24;
        }

        // Settings panel clicks
        if (selectedModule >= 0 && selectedModule < PulsarCore.MODULES.getModules().size()) {
            int px = w * 3 / 5 + 4, pw = w * 2 / 5 - 8;
            Module m = PulsarCore.MODULES.getModules().get(selectedModule);

            // Close
            int closeX = px + pw - 18, closeY = 16;
            if (mx >= closeX - 2 && mx <= closeX + 10 && my >= closeY - 2 && my <= closeY + 12) {
                selectedModule = -1; return true;
            }

            // Toggle button
            int btnX = px + 14, btnY = 60, btnW = pw - 28, btnH = 24;
            if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH) {
                m.toggle(); return true;
            }

            // Keybind
            String bindId = KeyBindConfig.getBindId(m.getName());
            if (bindId != null) {
                int kbBtnX = px + pw / 2, kbBtnW = pw / 2 - 14, kbY = 96 + 36;
                if (mx >= kbBtnX && mx <= kbBtnX + kbBtnW && my >= kbY - 2 && my <= kbY + 14) {
                    waitingForKey = true; return true;
                }
            }
        }

        // Module grid clicks
        List<Module> filtered = getFilteredModules();
        int contentX = sideW + 8;
        int contentW = (selectedModule >= 0 ? w * 3 / 5 : w) - contentX - 4;
        int cols = Math.max(2, Math.min(4, contentW / 110));
        int cardW = (contentW - (cols - 1) * 6) / cols;
        int cardH = 52, gapX = 6, gapY = 6;
        int startY = 36;

        for (int i = 0; i < filtered.size(); i++) {
            int col = i % cols, row = i / cols;
            int x = contentX + col * (cardW + gapX);
            int y = startY + row * (cardH + gapY) - scrollOffset;
            if (mx >= x && mx <= x + cardW && my >= y && my <= y + cardH) {
                int globalIdx = PulsarCore.MODULES.getModules().indexOf(filtered.get(i));

                // Click on toggle switch area → toggle directly
                int toggleX = x + cardW - 30;
                if (mx >= toggleX) {
                    filtered.get(i).toggle();
                } else {
                    selectedModule = (selectedModule == globalIdx) ? -1 : globalIdx;
                }
                return true;
            }
        }

        // Search bar click
        int searchW = Math.min(contentW, 220);
        if (mx >= contentX && mx <= contentX + searchW && my >= 8 && my <= 28) {
            return true; // Focus search (typing handled in charTyped)
        }

        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput keyInput) {
        // Keybind listening
        if (waitingForKey && selectedModule >= 0) {
            List<Module> modules = PulsarCore.MODULES.getModules();
            if (selectedModule < modules.size()) {
                String bindId = KeyBindConfig.getBindId(modules.get(selectedModule).getName());
                if (bindId != null && keyInput.key() != org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                    KeyBindConfig.setKey(bindId, keyInput.key());
                }
            }
            waitingForKey = false;
            return true;
        }

        // Typing into search
        int key = keyInput.key();
        if (key >= org.lwjgl.glfw.GLFW.GLFW_KEY_A && key <= org.lwjgl.glfw.GLFW.GLFW_KEY_Z) {
            char c = (char)('a' + (key - org.lwjgl.glfw.GLFW.GLFW_KEY_A));
            searchQuery += c;
            scrollOffset = 0;
            return true;
        }
        if (key >= org.lwjgl.glfw.GLFW.GLFW_KEY_0 && key <= org.lwjgl.glfw.GLFW.GLFW_KEY_9) {
            searchQuery += (char)('0' + (key - org.lwjgl.glfw.GLFW.GLFW_KEY_0));
            scrollOffset = 0;
            return true;
        }
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE) { searchQuery += " "; scrollOffset = 0; return true; }

        // Backspace for search
        if (keyInput.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
            searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            scrollOffset = 0;
            return true;
        }

        // Escape clears search first, then closes
        if (keyInput.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            if (!searchQuery.isEmpty()) { searchQuery = ""; return true; }
            if (selectedModule >= 0) { selectedModule = -1; return true; }
        }

        return super.keyPressed(keyInput);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        if (mouseX > 90) { // Only scroll in content area
            scrollOffset -= (int) (vAmount * 24);
            if (scrollOffset < 0) scrollOffset = 0;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
    }

    @Override public boolean shouldPause() { return false; }
}
