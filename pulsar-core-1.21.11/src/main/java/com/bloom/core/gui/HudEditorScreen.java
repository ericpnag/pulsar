package com.bloom.core.gui;

import com.bloom.core.PulsarCore;
import com.bloom.core.config.KeyBindConfig;
import com.bloom.core.config.PulsarConfig;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class HudEditorScreen extends Screen {
    private static final int SNAP_GRID = 4;
    private final List<HudElement> elements = new ArrayList<>();
    private HudElement dragging = null;
    private int dragOffsetX, dragOffsetY;

    public HudEditorScreen() {
        super(Text.literal("HUD Editor"));
    }

    @Override
    protected void init() {
        elements.clear();
        MinecraftClient mc = MinecraftClient.getInstance();

        int stackY = 4;
        for (Module m : PulsarCore.MODULES.getModules()) {
            if (!m.hasHud()) continue;

            int h = m.getHudHeight();
            if (h <= 0) h = 12;

            int w = mc.textRenderer.getWidth(m.getName()) + 24;
            if (w < 60) w = 60;
            m.setHudWidth(w);

            int x, y;
            if (m.hasCustomPosition()) {
                x = m.getHudX();
                y = m.getHudY();
            } else {
                x = 2;
                y = stackY;
            }
            int defaultY = stackY;
            stackY += m.getHudHeight() > 0 ? m.getHudHeight() : 12;

            elements.add(new HudElement(m, x, y, w, h, defaultY));
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Semi-transparent overlay
        ctx.fill(0, 0, this.width, this.height, 0x80000000);

        // Grid dots
        for (int gx = 0; gx < this.width; gx += SNAP_GRID * 8) {
            for (int gy = 0; gy < this.height; gy += SNAP_GRID * 8) {
                ctx.fill(gx, gy, gx + 1, gy + 1, 0x18FFFFFF);
            }
        }

        // Elements
        for (HudElement el : elements) {
            boolean hover = el == dragging || isHovered(el, mouseX, mouseY);
            boolean enabled = el.module.isEnabled();

            int bgColor = enabled ? (hover ? 0x50C678DD : 0x30C678DD) : 0x20666666;
            ctx.fill(el.x, el.y, el.x + el.w, el.y + el.h, bgColor);

            int borderColor = enabled ? (hover ? 0xAAC678DD : 0x60C678DD) : 0x30666666;
            ctx.fill(el.x, el.y, el.x + el.w, el.y + 1, borderColor);
            ctx.fill(el.x, el.y + el.h - 1, el.x + el.w, el.y + el.h, borderColor);
            ctx.fill(el.x, el.y, el.x + 1, el.y + el.h, borderColor);
            ctx.fill(el.x + el.w - 1, el.y, el.x + el.w, el.y + el.h, borderColor);

            int textColor = enabled ? 0xFFFFFFFF : 0xFF888888;
            String label = el.module.getName();
            int tw = this.textRenderer.getWidth(label);
            int tx = el.x + (el.w - tw) / 2;
            int ty = el.y + (el.h - 8) / 2;
            ctx.drawText(this.textRenderer, label, tx, ty, textColor, true);
        }

        // Title bar
        ctx.fill(0, 0, this.width, 16, 0xDD0A0A0F);
        ctx.fill(0, 15, this.width, 16, 0x30C678DD);
        ctx.drawText(this.textRenderer, "HUD EDITOR", 6, 4, 0xFFC678DD, true);
        String hint = "Drag to move  \u2022  Right-click to reset  \u2022  ESC to close";
        int hw = this.textRenderer.getWidth(hint);
        ctx.drawText(this.textRenderer, hint, this.width - hw - 6, 4, 0xFF666666, true);

        // Snap guides
        if (dragging != null) {
            int cx = dragging.x + dragging.w / 2;
            int cy = dragging.y + dragging.h / 2;
            int screenCX = this.width / 2;
            int screenCY = this.height / 2;

            if (Math.abs(cx - screenCX) < 6)
                ctx.fill(screenCX, 16, screenCX + 1, this.height, 0x40C678DD);
            if (Math.abs(cy - screenCY) < 6)
                ctx.fill(0, screenCY, this.width, screenCY + 1, 0x40C678DD);

            String pos = dragging.x + ", " + dragging.y;
            ctx.fill(dragging.x - 2, dragging.y - 12, dragging.x + this.textRenderer.getWidth(pos) + 4, dragging.y - 1, 0xCC0A0A0F);
            ctx.drawText(this.textRenderer, pos, dragging.x, dragging.y - 11, 0xFFC678DD, true);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        int mx = (int) click.x(), my = (int) click.y();
        int button = click.button();

        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement el = elements.get(i);
            if (isHovered(el, mx, my)) {
                if (button == 0) {
                    dragging = el;
                    dragOffsetX = mx - el.x;
                    dragOffsetY = my - el.y;
                    elements.remove(i);
                    elements.add(el);
                    return true;
                } else if (button == 1) {
                    el.module.setHudX(-1);
                    el.module.setHudY(-1);
                    init();
                    PulsarConfig.save(PulsarCore.MODULES);
                    return true;
                }
            }
        }
        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging != null && click.button() == 0) {
            int newX = (int) click.x() - dragOffsetX;
            int newY = (int) click.y() - dragOffsetY;

            // Snap to grid
            newX = Math.round((float) newX / SNAP_GRID) * SNAP_GRID;
            newY = Math.round((float) newY / SNAP_GRID) * SNAP_GRID;

            // Snap to screen center
            int cx = newX + dragging.w / 2;
            int cy = newY + dragging.h / 2;
            if (Math.abs(cx - this.width / 2) < 6) newX = this.width / 2 - dragging.w / 2;
            if (Math.abs(cy - this.height / 2) < 6) newY = this.height / 2 - dragging.h / 2;

            // Clamp to screen
            newX = Math.max(0, Math.min(newX, this.width - dragging.w));
            newY = Math.max(16, Math.min(newY, this.height - dragging.h));

            dragging.x = newX;
            dragging.y = newY;

            // Save position live
            dragging.module.setHudX(newX);
            dragging.module.setHudY(newY);
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public void close() {
        dragging = null;
        PulsarConfig.save(PulsarCore.MODULES);
        super.close();
    }

    private boolean isHovered(HudElement el, int mx, int my) {
        return mx >= el.x && mx <= el.x + el.w && my >= el.y && my <= el.y + el.h;
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    @Override
    public boolean shouldPause() { return false; }

    private static class HudElement {
        final Module module;
        int x, y;
        final int w, h;
        final int defaultY;

        HudElement(Module module, int x, int y, int w, int h, int defaultY) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.defaultY = defaultY;
        }
    }
}
