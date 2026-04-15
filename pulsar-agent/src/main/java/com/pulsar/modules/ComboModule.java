package com.pulsar.modules;
public class ComboModule extends PulsarModule {
    private int combo = 0;
    private long lastHit = 0;
    public ComboModule() { super("Combo Counter", false); }
    @Override public void onTick() {
        if (System.currentTimeMillis() - lastHit > 3000) combo = 0;
    }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(int y) {
        try {
            if (org.lwjgl.input.Mouse.isButtonDown(0)) { combo++; lastHit = System.currentTimeMillis(); }
        } catch (Exception ignored) {}
        if (combo > 0) drawText(combo + " Combo", 4, y, 0xFFAA00);
    }
}
