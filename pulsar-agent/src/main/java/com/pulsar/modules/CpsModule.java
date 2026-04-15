package com.pulsar.modules;
import java.util.ArrayList;
import java.util.List;
public class CpsModule extends PulsarModule {
    private final List<Long> clicks = new ArrayList<>();
    private boolean wasDown = false;
    public CpsModule() { super("CPS Counter", true); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(int y) {
        try {
            boolean down = org.lwjgl.input.Mouse.isButtonDown(0);
            if (down && !wasDown) clicks.add(System.currentTimeMillis());
            wasDown = down;
        } catch (Exception ignored) {}
        long now = System.currentTimeMillis();
        while (!clicks.isEmpty() && now - clicks.get(0) > 1000) clicks.remove(0);
        drawText(clicks.size() + " CPS", 4, y, 0xC0C0C0);
    }
}
