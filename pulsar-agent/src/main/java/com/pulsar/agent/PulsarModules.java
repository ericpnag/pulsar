package com.pulsar.agent;

import com.pulsar.modules.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Display;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.util.ArrayList;
import java.util.List;

public class PulsarModules {
    private static final List<PulsarModule> modules = new ArrayList<>();
    private static boolean menuOpen = false;
    private static boolean wasShiftPressed = false;
    private static long lastClickTime = 0;
    private static int scrollOffset = 0;

    public static void init() {
        modules.add(new ToggleSprintModule());
        modules.add(new FpsModule());
        modules.add(new CpsModule());
        modules.add(new CoordinatesModule());
        modules.add(new DirectionModule());
        modules.add(new ComboModule());
        modules.add(new SpeedModule());
        modules.add(new ArrowModule());
        modules.add(new ClockModule());
        modules.add(new FullbrightModule());
        modules.add(new KeystrokesModule());
        modules.add(new PingModule());
        modules.add(new FpsBoostModule());
    }

    public static List<PulsarModule> getModules() { return modules; }
    public static boolean isMenuOpen() { return menuOpen; }

    public static void onTick() {
        try {
            if (!MCReflect.init()) return;
            boolean shiftDown = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            if (shiftDown && !wasShiftPressed) {
                menuOpen = !menuOpen;
                Mouse.setGrabbed(!menuOpen);
                Mouse.getDX(); Mouse.getDY();
                while (Mouse.next()) {}
            }
            wasShiftPressed = shiftDown;

            if (menuOpen) {
                Mouse.getDX(); Mouse.getDY();
                return;
            }

            for (PulsarModule m : modules) {
                if (m.isEnabled()) m.onTick();
            }
        } catch (Exception ignored) {}
    }

    public static void onRenderHud() {
        try {
            if (!MCReflect.init()) return;

            int sw = Display.getWidth();
            int sh = Display.getHeight();
            int scaleFactor = 2;
            try {
                Object mc = MCReflect.getMinecraft();
                Class<?> srClass = Class.forName("avp");
                Object sr = srClass.getConstructors()[0].newInstance(mc);
                for (java.lang.reflect.Field f : srClass.getDeclaredFields()) {
                    if (f.getType() == int.class) {
                        f.setAccessible(true);
                        int val = f.getInt(sr);
                        if (val >= 1 && val <= 4) { scaleFactor = val; break; }
                    }
                }
            } catch (Exception ignored) {}

            int guiW = sw / scaleFactor;
            int guiH = sh / scaleFactor;

            if (menuOpen) {
                Mouse.getDX();
                Mouse.getDY();
                if (Mouse.isGrabbed()) Mouse.setGrabbed(false);
                renderMenu(guiW, guiH, scaleFactor);
            } else {
                int y = 4;
                for (PulsarModule m : modules) {
                    if (m.isEnabled() && m.hasHud()) {
                        m.renderHud(y);
                        y += m.getHudHeight();
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private static void renderMenu(int guiW, int guiH, int scale) {
        int mx = Mouse.getX() / scale;
        int my = guiH - Mouse.getY() / scale;

        int wheel = Mouse.getDWheel();
        if (wheel > 0) scrollOffset = Math.max(0, scrollOffset - 20);
        if (wheel < 0) scrollOffset += 20;
        int maxScroll = Math.max(0, modules.size() * 20 - (guiH - 60));
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        drawRect(0, 0, guiW, guiH, 0xDD000000);

        int sideW = 70;
        drawRect(0, 0, sideW, guiH, 0x22FFFFFF);
        drawRect(sideW, 0, sideW + 1, guiH, 0x15FFFFFF);

        MCReflect.drawText("PULSAR", 8, 8, 0xFFFFFFFF);
        MCReflect.drawText("1.8.9", 8, 20, 0xFF555555);
        drawRect(8, 30, sideW - 8, 31, 0x15FFFFFF);

        long active = 0;
        for (PulsarModule m : modules) { if (m.isEnabled()) active++; }
        MCReflect.drawText(active + "/" + modules.size(), 8, guiH - 20, 0xFF444444);
        MCReflect.drawText("active", 8, guiH - 10, 0xFF333333);

        MCReflect.drawText("MODULES", sideW + 10, 8, 0xFFFFFFFF);
        MCReflect.drawText("Click to toggle | Scroll with wheel", sideW + 10, 20, 0xFF444444);
        drawRect(sideW + 10, 30, guiW - 10, 31, 0x10FFFFFF);

        int contentX = sideW + 8;
        int contentW = guiW - sideW - 16;
        int y = 36 - scrollOffset;

        // Use Mouse.isButtonDown for click detection — works even when events consumed
        boolean clicking = Mouse.isButtonDown(0);

        for (PulsarModule m : modules) {
            if (y + 18 < 32 || y > guiH) { y += 20; continue; }

            boolean on = m.isEnabled();
            boolean rowHover = mx >= contentX && mx < contentX + contentW && my >= y && my < y + 18;

            drawRect(contentX, y, contentX + contentW, y + 18, rowHover ? 0x18FFFFFF : 0x08FFFFFF);
            if (rowHover) drawRect(contentX, y, contentX + 2, y + 18, 0xAAFFFFFF);

            drawRect(contentX + 6, y + 7, contentX + 10, y + 11, on ? 0xFF4CAF50 : 0xFF444444);
            MCReflect.drawText(m.getName(), contentX + 14, y + 5, rowHover ? 0xFFFFFFFF : 0xFFBBBBBB);

            int swX = contentX + contentW - 28;
            drawRect(swX, y + 3, swX + 22, y + 15, on ? 0xFF4CAF50 : 0xFF333333);
            int knobX = on ? swX + 12 : swX + 2;
            drawRect(knobX, y + 5, knobX + 8, y + 13, 0xFFFFFFFF);

            if (rowHover && clicking) {
                long now = System.currentTimeMillis();
                if (now - lastClickTime > 300) {
                    m.toggle();
                    lastClickTime = now;
                }
            }

            y += 20;
        }
    }

    public static void drawRect(int left, int top, int right, int bottom, int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(r / 255f, g / 255f, b / 255f, a / 255f);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(left, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, top);
        GL11.glVertex2f(left, top);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
