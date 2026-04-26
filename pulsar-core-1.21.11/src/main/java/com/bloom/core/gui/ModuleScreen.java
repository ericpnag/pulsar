package com.bloom.core.gui;

import com.bloom.core.PulsarCore;
import com.bloom.core.config.KeyBindConfig;
import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleSetting;
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

    private static final String[] CATEGORIES = {"All", "HUD", "PvP", "Visual", "Utility", "Hypixel", "Cosmetics"};

    // Category icon letters
    private static final String[] CAT_ICONS = {"*", "H", "P", "V", "U", "X", "C"};

    // Module icon color cycling based on name hash
    private static final int[][] ICON_COLORS = {
        {0x335B3FA6, 0xFFC4B5FD}, // purple
        {0x33D85A30, 0xFFF0997B}, // coral
        {0x331D9E75, 0xFF5DCAA5}, // teal
        {0x33BA7517, 0xFFFAC775}, // amber
        {0x33378ADD, 0xFF85B7EB}, // blue
        {0x33D4537E, 0xFFF4C0D1}, // pink
    };

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

    private void fillRoundOutline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 1, y, x + w - 1, y + 1, color);           // top
        ctx.fill(x + 1, y + h - 1, x + w - 1, y + h, color);   // bottom
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);           // left
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);   // right
    }

    private int[] getIconColor(Module m) {
        int idx = Math.abs(m.getName().hashCode()) % ICON_COLORS.length;
        return ICON_COLORS[idx];
    }

    private int getCategoryCount(String cat) {
        if (cat.equals("All")) return PulsarCore.MODULES.getModules().size();
        return (int) PulsarCore.MODULES.getModules().stream()
            .filter(m -> getCategory(m).equals(cat)).count();
    }

    // === Panel geometry (computed once per frame) ===
    private int panelX, panelY, panelW, panelH;
    private int catSideW, modGridX, modGridW, detailX, detailW;
    private int headerH, footerH, bodyY, bodyH;

    private void computeLayout() {
        int sw = this.width, sh = this.height;
        panelW = Math.min((int)(sw * 0.92f), 720);
        panelH = Math.min((int)(sh * 0.86f), 600);
        panelX = (sw - panelW) / 2;
        panelY = (sh - panelH) / 2;
        headerH = 36;
        footerH = 26;
        bodyY = panelY + headerH;
        bodyH = panelH - headerH - footerH;
        catSideW = 160;
        detailW = (selectedModule >= 0) ? 220 : 0;
        modGridX = panelX + catSideW;
        modGridW = panelW - catSideW - detailW;
        detailX = panelX + panelW - detailW;
    }

    public ModuleScreen() { super(Text.literal("Pulsar Mods")); }

    @Override
    protected void init() {
        scrollOffset = 0;
        selectedModule = -1;
        searchQuery = "";
    }

    // ======================== RENDER ========================
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int w = this.width, h = this.height;
        animTick++;
        computeLayout();

        // === Full-screen dark backdrop (rgba(8,8,15,0.35) = ~89 alpha) ===
        ctx.fill(0, 0, w, h, 0x5908080F);

        // === Panel background (rgba(8,8,15,0.92) = ~234 alpha) ===
        fillRound(ctx, panelX, panelY, panelW, panelH, 0xEA08080F);
        // Panel border (#2A2A3E)
        fillRoundOutline(ctx, panelX, panelY, panelW, panelH, 0xFF2A2A3E);

        // ======================== HEADER ========================
        int hx = panelX, hy = panelY, hw = panelW, hh = headerH;
        // Header bg
        fillRound(ctx, hx + 1, hy + 1, hw - 2, hh - 1, 0x990A0A14);
        // Header bottom border
        ctx.fill(hx + 1, hy + hh - 1, hx + hw - 1, hy + hh, 0xFF1A1A28);

        // Logo circle (22px, purple gradient approx)
        int logoX = hx + 16, logoY = hy + 7, logoS = 22;
        fillRound(ctx, logoX, logoY, logoS, logoS, 0xFF4C1D95);
        fillRound(ctx, logoX + 3, logoY + 3, logoS - 6, logoS - 6, 0xFF8B5CF6);
        fillRound(ctx, logoX + 6, logoY + 6, logoS - 12, logoS - 12, 0xFF4C1D95);

        // Title "Pulsar modules"
        drawT(ctx, "Pulsar modules", logoX + logoS + 10, hy + 8, 0xF0EEFC, false);

        // Subtitle "X of Y enabled"
        List<Module> allMods = PulsarCore.MODULES.getModules();
        long enabledCount = allMods.stream().filter(Module::isEnabled).count();
        String subtitle = enabledCount + " of " + allMods.size() + " enabled";
        drawT(ctx, subtitle, logoX + logoS + 10, hy + 20, 0x6B6985, false);

        // Search input (right-aligned area)
        int searchW = 140, searchH = 18;
        int searchX = hx + hw - 16 - 24 - 8 - searchW; // leave room for close btn
        int searchY2 = hy + (hh - searchH) / 2;
        fillRound(ctx, searchX, searchY2, searchW, searchH, 0xCC0D0D17);
        fillRoundOutline(ctx, searchX, searchY2, searchW, searchH, 0xFF1F1F2E);
        String searchDisplay = searchQuery.isEmpty() ? "Search modules..." : searchQuery;
        int searchCol = searchQuery.isEmpty() ? 0x5A5870 : 0xE8E6F5;
        drawT(ctx, searchDisplay, searchX + 8, searchY2 + 5, searchCol, false);
        // "/" hint
        if (searchQuery.isEmpty()) {
            int slashX = searchX + searchW - 18;
            fillRound(ctx, slashX, searchY2 + 3, 14, searchH - 6, 0x0DFFFFFF);
            drawT(ctx, "/", slashX + 4, searchY2 + 5, 0x6B6985, false);
        }
        // Blinking cursor when typing
        if (!searchQuery.isEmpty() && (animTick / 15) % 2 == 0) {
            int cursorX = searchX + 8 + tw(searchQuery);
            ctx.fill(cursorX, searchY2 + 4, cursorX + 1, searchY2 + searchH - 4, 0xAAFFFFFF);
        }

        // Close button (X)
        int closeBtnX = hx + hw - 16 - 24, closeBtnY = hy + (hh - 24) / 2;
        fillRound(ctx, closeBtnX, closeBtnY, 24, 24, 0xCC0D0D17);
        fillRoundOutline(ctx, closeBtnX, closeBtnY, 24, 24, 0xFF1F1F2E);
        boolean closeHov = mx >= closeBtnX && mx < closeBtnX + 24 && my >= closeBtnY && my < closeBtnY + 24;
        drawT(ctx, "X", closeBtnX + 8, closeBtnY + 8, closeHov ? 0xF0EEFC : 0x8A88A8, false);

        // ======================== CATEGORY SIDEBAR ========================
        int cx = panelX, cy = bodyY, cw = catSideW, ch = bodyH;
        // Sidebar bg
        fillRound(ctx, cx + 1, cy, cw - 1, ch, 0x660A0A14);
        // Right border
        ctx.fill(cx + cw - 1, cy, cx + cw, cy + ch, 0xFF1A1A28);

        int catItemY = cy + 12;
        for (int i = 0; i < CATEGORIES.length; i++) {
            String cat = CATEGORIES[i];
            boolean active = cat.equals(selectedCategory);
            boolean hov = mx >= cx && mx < cx + cw && my >= catItemY && my < catItemY + 28;
            int count = getCategoryCount(cat);

            if (active) {
                fillRound(ctx, cx + 8, catItemY, cw - 16, 28, 0xFF1A1530);
            } else if (hov) {
                fillRound(ctx, cx + 8, catItemY, cw - 16, 28, 0x0AFFFFFF);
            }

            // Icon rectangle (14x14, colored)
            int iconX = cx + 18, iconY2 = catItemY + 7;
            int iconCol = active ? 0xFFC4B5FD : 0xFF5A5870;
            fillRound(ctx, iconX, iconY2, 14, 14, iconCol);
            // Icon letter inside
            drawT(ctx, CAT_ICONS[i], iconX + 4, iconY2 + 3, active ? 0x1A1530 : 0x0A0A14, false);

            // Category name
            int nameCol = active ? 0xF0EEFC : 0xC7C5DC;
            drawT(ctx, cat, iconX + 20, catItemY + 10, nameCol, false);

            // Count
            String countStr = String.valueOf(count);
            int countCol = active ? 0xC4B5FD : 0x6B6985;
            drawT(ctx, countStr, cx + cw - 24 - tw(countStr), catItemY + 10, countCol, false);

            catItemY += 32;
        }

        // HUD Editor button at bottom of sidebar
        int hudBtnY = cy + ch - 34;
        boolean hudHov = mx >= cx + 8 && mx < cx + cw - 8 && my >= hudBtnY && my < hudBtnY + 26;
        fillRound(ctx, cx + 8, hudBtnY, cw - 16, 26, hudHov ? 0x30C4B5FD : 0x15C4B5FD);
        fillRoundOutline(ctx, cx + 8, hudBtnY, cw - 16, 26, hudHov ? 0x505B3FA6 : 0x305B3FA6);
        String hudEdStr = "HUD Editor";
        drawT(ctx, hudEdStr, cx + cw / 2 - tw(hudEdStr) / 2, hudBtnY + 9, hudHov ? 0xC4B5FD : 0x8A88A8, false);

        // ======================== MODULE GRID ========================
        List<Module> filtered = getFilteredModules();
        int gridX = modGridX + 12, gridY2 = bodyY + 12;
        int gridW = modGridW - 24;
        int cols = 2;
        int gapX = 8, gapY = 8;
        int cardW2 = (gridW - (cols - 1) * gapX) / cols;
        int cardH2 = 38;

        // Clamp scroll
        int totalRows = (filtered.size() + cols - 1) / cols;
        int maxScroll = Math.max(0, totalRows * (cardH2 + gapY) - (bodyH - 24));
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        // Clip rendering to body area
        for (int i = 0; i < filtered.size(); i++) {
            Module m = filtered.get(i);
            int col = i % cols, row = i / cols;
            int cardX = gridX + col * (cardW2 + gapX);
            int cardY = gridY2 + row * (cardH2 + gapY) - scrollOffset;

            // Skip if outside visible area
            if (cardY + cardH2 < bodyY || cardY > bodyY + bodyH) continue;

            int globalIdx = PulsarCore.MODULES.getModules().indexOf(m);
            boolean hov = mx >= cardX && mx <= cardX + cardW2 && my >= cardY && my <= cardY + cardH2
                          && my >= bodyY && my <= bodyY + bodyH;
            boolean sel = globalIdx == selectedModule;
            boolean on = m.isEnabled();

            // Card bg (rgba(13,13,23,0.7))
            int cardBg;
            if (on) {
                cardBg = 0xB31A1530; // enabled: gradient-ish purple tint
            } else {
                cardBg = 0xB30D0D17; // normal
            }
            if (hov) {
                cardBg = 0xB3141420; // hover
            }
            fillRound(ctx, cardX, cardY, cardW2, cardH2, cardBg);

            // Card border
            if (sel) {
                fillRoundOutline(ctx, cardX, cardY, cardW2, cardH2, 0xFF5B3FA6);
            } else if (on) {
                fillRoundOutline(ctx, cardX, cardY, cardW2, cardH2, 0x805B3FA6);
            } else {
                fillRoundOutline(ctx, cardX, cardY, cardW2, cardH2, 0xFF1F1F2E);
            }

            // Module icon (24x24 colored rect)
            int[] iconCols = getIconColor(m);
            int miX = cardX + 9, miY = cardY + 7;
            fillRound(ctx, miX, miY, 24, 24, iconCols[0]);
            // Icon letter
            String iconLetter = m.getName().length() > 0 ? m.getName().substring(0, 1) : "?";
            drawT(ctx, iconLetter, miX + 8, miY + 8, iconCols[1] & 0xFFFFFF, false);

            // Module name
            int nameX = miX + 30;
            String name = m.getName();
            int maxNameW = cardW2 - 30 - 32 - 6;
            if (tw(name) > maxNameW) {
                while (tw(name + "..") > maxNameW && name.length() > 3) name = name.substring(0, name.length() - 1);
                name += "..";
            }
            drawT(ctx, name, nameX, cardY + 10, 0xF0EEFC, false);

            // Keybind text below name
            String bindId = KeyBindConfig.getBindId(m.getName());
            String keyText = bindId != null ? "[" + KeyBindConfig.getKeyName(KeyBindConfig.getKey(bindId)) + "]" : "";
            if (!keyText.isEmpty()) {
                drawT(ctx, keyText, nameX, cardY + 22, 0x6B6985, false);
            }

            // Mini toggle (22x12)
            int togX = cardX + cardW2 - 30, togY = cardY + 13;
            int togW = 22, togH = 12;
            int togBg = on ? 0xFF2A1A4D : 0xCC1F1F2E;
            fillRound(ctx, togX, togY, togW, togH, togBg);
            // Knob (10x10 circle approx)
            int knobX = on ? togX + 11 : togX + 1;
            int knobCol = on ? 0xFFC4B5FD : 0xFF5A5870;
            fillRound(ctx, knobX, togY + 1, 10, 10, knobCol);
        }

        if (filtered.isEmpty()) {
            String empty = searchQuery.isEmpty() ? "No modules in this category" : "No modules found";
            drawT(ctx, empty, modGridX + modGridW / 2 - tw(empty) / 2, bodyY + bodyH / 2, 0x6B6985, false);
        }

        // ======================== DETAIL PANEL ========================
        if (selectedModule >= 0 && selectedModule < PulsarCore.MODULES.getModules().size()) {
            Module m = PulsarCore.MODULES.getModules().get(selectedModule);
            int dx = detailX, dy = bodyY, dw = detailW, dh = bodyH;

            // Detail bg
            fillRound(ctx, dx, dy, dw, dh, 0x660A0A14);
            // Left border
            ctx.fill(dx, dy, dx + 1, dy + dh, 0xFF1A1A28);

            int pad = 14;
            int iy = dy + pad;

            // Module icon (32x32)
            int[] diCols = getIconColor(m);
            fillRound(ctx, dx + pad, iy, 32, 32, diCols[0]);
            String diLetter = m.getName().length() > 0 ? m.getName().substring(0, 1) : "?";
            drawT(ctx, diLetter, dx + pad + 11, iy + 12, diCols[1] & 0xFFFFFF, false);

            // Name + category tag
            drawT(ctx, m.getName(), dx + pad + 42, iy + 4, 0xF0EEFC, false);
            drawT(ctx, getCategory(m), dx + pad + 42, iy + 18, 0xC4B5FD, false);

            iy += 42;
            // Separator
            ctx.fill(dx + pad, iy, dx + dw - pad, iy + 1, 0xFF1A1A28);
            iy += 10;

            // Description
            String desc = m.getDescription();
            // Word-wrap description
            int descMaxW = dw - pad * 2;
            List<String> descLines = wrapText(desc, descMaxW);
            for (String line : descLines) {
                if (iy > dy + dh - 20) break;
                drawT(ctx, line, dx + pad, iy, 0x8A88A8, false);
                iy += 11;
            }
            iy += 6;

            // Keybind
            String bindId2 = KeyBindConfig.getBindId(m.getName());
            if (bindId2 != null) {
                drawT(ctx, "TOGGLE KEY", dx + pad, iy, 0x6B6985, false);
                iy += 14;

                int kbBtnW = dw - pad * 2, kbBtnH = 18;
                boolean kbHov = mx >= dx + pad && mx < dx + pad + kbBtnW && my >= iy && my < iy + kbBtnH;
                fillRound(ctx, dx + pad, iy, kbBtnW, kbBtnH, 0xCC0D0D17);
                fillRoundOutline(ctx, dx + pad, iy, kbBtnW, kbBtnH, waitingForKey ? 0xFF5B3FA6 : (kbHov ? 0xFF5B3FA6 : 0xFF1F1F2E));
                String keyDisp = waitingForKey ? "> Press key <" : KeyBindConfig.getKeyName(KeyBindConfig.getKey(bindId2));
                drawT(ctx, keyDisp, dx + pad + kbBtnW / 2 - tw(keyDisp) / 2, iy + 5, waitingForKey ? 0xC4B5FD : 0xE8E6F5, false);
                iy += kbBtnH + 10;
            }

            // Module settings (sliders/toggles)
            var settings = m.getSettings();
            if (!settings.isEmpty()) {
                drawT(ctx, "SETTINGS", dx + pad, iy, 0x6B6985, false);
                iy += 14;

                for (ModuleSetting s : settings) {
                    if (iy > dy + dh - 30) break;

                    if (s.type == ModuleSetting.Type.SLIDER) {
                        // Label + value row
                        float val = s.getter.get();
                        String valText = s.step >= 1 ? String.valueOf((int) val) : String.format("%.2f", val);
                        drawT(ctx, s.name, dx + pad, iy, 0xC7C5DC, false);
                        drawT(ctx, valText, dx + dw - pad - tw(valText), iy, 0xC4B5FD, false);
                        iy += 14;

                        // Slider track (3px)
                        int slX = dx + pad, slW = dw - pad * 2, slH = 3;
                        fillRound(ctx, slX, iy, slW, slH, 0xCC1F1F2E);

                        // Slider fill (purple gradient)
                        float pct = (val - s.min) / (s.max - s.min);
                        int fillW = (int)(pct * slW);
                        if (fillW > 0) fillRound(ctx, slX, iy, fillW, slH, 0xFF5B3FA6);

                        // Thumb (11x11 circle)
                        int thumbX = slX + fillW - 5;
                        if (thumbX < slX) thumbX = slX;
                        fillRound(ctx, thumbX, iy - 4, 11, 11, 0xFFC4B5FD);
                        // Dark border on thumb
                        fillRoundOutline(ctx, thumbX, iy - 4, 11, 11, 0xFF08080F);

                        iy += 18;
                    } else if (s.type == ModuleSetting.Type.TOGGLE) {
                        // Toggle row with bg
                        boolean on2 = s.getter.get() > 0.5f;
                        int rowH = 22;
                        fillRound(ctx, dx + pad, iy, dw - pad * 2, rowH, 0x990D0D17);
                        drawT(ctx, s.name, dx + pad + 8, iy + 7, 0xC7C5DC, false);
                        // Mini toggle
                        int mtX = dx + dw - pad - 28, mtY = iy + 5;
                        fillRound(ctx, mtX, mtY, 22, 12, on2 ? 0xFF2A1A4D : 0xCC1F1F2E);
                        fillRound(ctx, on2 ? mtX + 11 : mtX + 1, mtY + 1, 10, 10, on2 ? 0xFFC4B5FD : 0xFF5A5870);
                        iy += rowH + 4;
                    }
                }
            }
        }

        // ======================== FOOTER ========================
        int fx = panelX, fy = panelY + panelH - footerH, fw = panelW, fh = footerH;
        // Footer bg
        fillRound(ctx, fx + 1, fy, fw - 2, fh - 1, 0x990A0A14);
        // Footer top border
        ctx.fill(fx + 1, fy, fx + fw - 1, fy + 1, 0xFF1A1A28);

        int fiy = fy + 8;
        int fix = fx + 16;

        // RShift Close
        fillRound(ctx, fix, fiy - 1, tw("RShift") + 8, 12, 0x0DFFFFFF);
        fillRoundOutline(ctx, fix, fiy - 1, tw("RShift") + 8, 12, 0xFF1F1F2E);
        drawT(ctx, "RShift", fix + 4, fiy + 1, 0xC7C5DC, false);
        fix += tw("RShift") + 14;
        drawT(ctx, "Close", fix, fiy + 1, 0x6B6985, false);
        fix += tw("Close") + 14;

        // / Search
        fillRound(ctx, fix, fiy - 1, tw("/") + 8, 12, 0x0DFFFFFF);
        fillRoundOutline(ctx, fix, fiy - 1, tw("/") + 8, 12, 0xFF1F1F2E);
        drawT(ctx, "/", fix + 4, fiy + 1, 0xC7C5DC, false);
        fix += tw("/") + 14;
        drawT(ctx, "Search", fix, fiy + 1, 0x6B6985, false);
        fix += tw("Search") + 14;

        // Tab Edit HUD
        fillRound(ctx, fix, fiy - 1, tw("Tab") + 8, 12, 0x0DFFFFFF);
        fillRoundOutline(ctx, fix, fiy - 1, tw("Tab") + 8, 12, 0xFF1F1F2E);
        drawT(ctx, "Tab", fix + 4, fiy + 1, 0xC7C5DC, false);
        fix += tw("Tab") + 14;
        drawT(ctx, "Edit HUD", fix, fiy + 1, 0x6B6985, false);

        // Version (right-aligned)
        String versionStr = "Pulsar v2.1.0";
        // Green dot
        int dotX = fx + fw - 16 - tw(versionStr) - 10;
        ctx.fill(dotX, fiy + 2, dotX + 6, fiy + 8, 0xFF5DCAA5);
        drawT(ctx, versionStr, dotX + 10, fiy + 1, 0x5DCAA5, false);

        super.render(ctx, mx, my, delta);
    }

    // Simple word-wrap helper
    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() > 0 && tw(current + " " + word) > maxWidth) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                if (current.length() > 0) current.append(" ");
                current.append(word);
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }

    // ======================== MOUSE CLICKED ========================
    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        double mx = click.x(), my = click.y();
        computeLayout();

        // Close button (header X)
        int closeBtnX = panelX + panelW - 16 - 24, closeBtnY = panelY + (headerH - 24) / 2;
        if (mx >= closeBtnX && mx < closeBtnX + 24 && my >= closeBtnY && my < closeBtnY + 24) {
            this.close();
            return true;
        }

        // Search bar click
        int searchW2 = 140, searchH2 = 18;
        int searchX2 = panelX + panelW - 16 - 24 - 8 - searchW2;
        int searchY2 = panelY + (headerH - searchH2) / 2;
        if (mx >= searchX2 && mx < searchX2 + searchW2 && my >= searchY2 && my < searchY2 + searchH2) {
            return true; // Focus search
        }

        // HUD Editor button in sidebar
        int hudBtnY = bodyY + bodyH - 34;
        if (mx >= panelX + 8 && mx < panelX + catSideW - 8 && my >= hudBtnY && my < hudBtnY + 26) {
            client.setScreen(new HudEditorScreen());
            return true;
        }

        // Category sidebar clicks
        int catItemY = bodyY + 12;
        for (String cat : CATEGORIES) {
            if (mx >= panelX && mx < panelX + catSideW && my >= catItemY && my < catItemY + 28) {
                selectedCategory = cat;
                scrollOffset = 0;
                selectedModule = -1;
                return true;
            }
            catItemY += 32;
        }

        // Detail panel clicks
        if (selectedModule >= 0 && selectedModule < PulsarCore.MODULES.getModules().size()) {
            Module m = PulsarCore.MODULES.getModules().get(selectedModule);
            int dx = detailX, dw = detailW, dy = bodyY;
            int pad = 14;
            int iy = dy + pad + 42 + 10; // after icon+name, separator

            // Skip description lines
            String desc = m.getDescription();
            int descMaxW = dw - pad * 2;
            List<String> descLines = wrapText(desc, descMaxW);
            iy += descLines.size() * 11 + 6;

            // Keybind click
            String bindId = KeyBindConfig.getBindId(m.getName());
            if (bindId != null) {
                iy += 14; // "TOGGLE KEY" label
                int kbBtnW = dw - pad * 2, kbBtnH = 18;
                if (mx >= dx + pad && mx < dx + pad + kbBtnW && my >= iy && my < iy + kbBtnH) {
                    waitingForKey = true;
                    return true;
                }
                iy += kbBtnH + 10;
            }

            // Settings clicks
            var settings = m.getSettings();
            if (!settings.isEmpty()) {
                iy += 14; // "SETTINGS" label
                for (ModuleSetting s : settings) {
                    if (s.type == ModuleSetting.Type.SLIDER) {
                        iy += 14; // label row
                        int slX = dx + pad, slW = dw - pad * 2;
                        if (mx >= slX && mx <= slX + slW && my >= iy - 6 && my <= iy + 14) {
                            float pct = (float)((mx - slX) / slW);
                            pct = Math.max(0, Math.min(1, pct));
                            float val = s.min + pct * (s.max - s.min);
                            val = Math.round(val / s.step) * s.step;
                            s.setter.accept(val);
                            com.bloom.core.config.PulsarConfig.save(PulsarCore.MODULES);
                            return true;
                        }
                        iy += 18;
                    } else if (s.type == ModuleSetting.Type.TOGGLE) {
                        int mtX = dx + dw - pad - 28, mtY = iy + 5;
                        if (mx >= mtX && mx <= mtX + 22 && my >= mtY && my <= mtY + 12) {
                            s.setter.accept(s.getter.get() > 0.5f ? 0f : 1f);
                            com.bloom.core.config.PulsarConfig.save(PulsarCore.MODULES);
                            return true;
                        }
                        iy += 22 + 4;
                    }
                }
            }
        }

        // Module grid clicks
        List<Module> filtered = getFilteredModules();
        int gridX = modGridX + 12, gridY2 = bodyY + 12;
        int gridW2 = modGridW - 24;
        int cols = 2;
        int gapX = 8, gapY = 8;
        int cardW2 = (gridW2 - (cols - 1) * gapX) / cols;
        int cardH2 = 38;

        for (int i = 0; i < filtered.size(); i++) {
            int col = i % cols, row = i / cols;
            int cardX = gridX + col * (cardW2 + gapX);
            int cardY = gridY2 + row * (cardH2 + gapY) - scrollOffset;

            if (cardY + cardH2 < bodyY || cardY > bodyY + bodyH) continue;

            if (mx >= cardX && mx <= cardX + cardW2 && my >= cardY && my <= cardY + cardH2
                && my >= bodyY && my <= bodyY + bodyH) {
                int globalIdx = PulsarCore.MODULES.getModules().indexOf(filtered.get(i));

                // Click on toggle switch area -> toggle directly
                int togX = cardX + cardW2 - 30;
                if (mx >= togX) {
                    filtered.get(i).toggle();
                } else {
                    selectedModule = (selectedModule == globalIdx) ? -1 : globalIdx;
                }
                return true;
            }
        }

        return super.mouseClicked(click, bl);
    }

    // ======================== MOUSE DRAGGED ========================
    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (selectedModule >= 0 && selectedModule < PulsarCore.MODULES.getModules().size()) {
            computeLayout();
            Module m = PulsarCore.MODULES.getModules().get(selectedModule);
            int dx = detailX, dw = detailW, dy = bodyY;
            int pad = 14;
            int iy = dy + pad + 42 + 10;

            String desc = m.getDescription();
            int descMaxW = dw - pad * 2;
            List<String> descLines = wrapText(desc, descMaxW);
            iy += descLines.size() * 11 + 6;

            String bindId = KeyBindConfig.getBindId(m.getName());
            if (bindId != null) {
                iy += 14 + 18 + 10;
            }

            var settings = m.getSettings();
            if (!settings.isEmpty()) {
                iy += 14;
                for (ModuleSetting s : settings) {
                    if (s.type == ModuleSetting.Type.SLIDER) {
                        iy += 14;
                        int slX = dx + pad, slW = dw - pad * 2;
                        if (click.x() >= slX && click.x() <= slX + slW && click.y() >= iy - 6 && click.y() <= iy + 14) {
                            float pct = (float)((click.x() - slX) / slW);
                            pct = Math.max(0, Math.min(1, pct));
                            float val = s.min + pct * (s.max - s.min);
                            val = Math.round(val / s.step) * s.step;
                            s.setter.accept(val);
                            return true;
                        }
                        iy += 18;
                    } else if (s.type == ModuleSetting.Type.TOGGLE) {
                        iy += 22 + 4;
                    }
                }
            }
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    // ======================== KEY PRESSED ========================
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

        // Tab -> HUD Editor
        if (keyInput.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_TAB) {
            client.setScreen(new HudEditorScreen());
            return true;
        }

        return super.keyPressed(keyInput);
    }

    // ======================== MOUSE SCROLLED ========================
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        if (mouseX > panelX + catSideW && mouseX < panelX + panelW) {
            scrollOffset -= (int) (vAmount * 24);
            if (scrollOffset < 0) scrollOffset = 0;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
    }

    @Override public boolean shouldPause() { return false; }
}
