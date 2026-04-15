package com.pulsar.modules;

import com.pulsar.agent.MCReflect;
import com.pulsar.agent.PulsarModules;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class KeystrokesModule extends PulsarModule {
    public KeystrokesModule() { super("Keystrokes", false); }
    @Override public boolean hasHud() { return true; }
    @Override public int getHudHeight() { return 44; }

    @Override public void renderHud(int y) {
        try {
            int x = 4;
            int kw = 12; // key width
            int kh = 13; // key height
            int g = 1;   // gap

            boolean w = Keyboard.isKeyDown(Keyboard.KEY_W);
            boolean a = Keyboard.isKeyDown(Keyboard.KEY_A);
            boolean s = Keyboard.isKeyDown(Keyboard.KEY_S);
            boolean d = Keyboard.isKeyDown(Keyboard.KEY_D);
            boolean lmb = Mouse.isButtonDown(0);
            boolean rmb = Mouse.isButtonDown(1);

            // Row 1:    [W]
            drawKey("W", x + kw + g, y, kw, kh, w);

            // Row 2: [A][S][D]
            drawKey("A", x, y + kh + g, kw, kh, a);
            drawKey("S", x + kw + g, y + kh + g, kw, kh, s);
            drawKey("D", x + (kw + g) * 2, y + kh + g, kw, kh, d);

            // Row 3: [LMB][RMB]
            int mw = (kw * 3 + g * 2 - g) / 2; // half width for mouse buttons
            drawKey("L", x, y + (kh + g) * 2, mw, kh, lmb);
            drawKey("R", x + mw + g, y + (kh + g) * 2, mw, kh, rmb);
        } catch (Exception ignored) {}
    }

    private void drawKey(String label, int x, int y, int w, int h, boolean pressed) {
        PulsarModules.drawRect(x, y, x + w, y + h, pressed ? 0x88FFFFFF : 0x44000000);
        MCReflect.drawText(label, x + (w / 2) - 2, y + 3, pressed ? 0xFFFFFFFF : 0xFF888888);
    }
}
