package com.pulsar.modules;

import com.pulsar.agent.MCReflect;

public abstract class PulsarModule {
    private final String name;
    private boolean enabled;

    public PulsarModule(String name, boolean defaultEnabled) {
        this.name = name;
        this.enabled = defaultEnabled;
    }

    public String getName() { return name; }
    public boolean isEnabled() { return enabled; }
    public void toggle() { enabled = !enabled; }
    public void onTick() {}
    public boolean hasHud() { return false; }
    public int getHudHeight() { return 12; }
    public void renderHud(int y) {}

    protected void drawText(String text, int x, int y, int color) {
        MCReflect.drawText(text, x, y, color);
    }
}
