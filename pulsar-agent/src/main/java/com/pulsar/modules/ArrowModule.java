package com.pulsar.modules;
public class ArrowModule extends PulsarModule {
    public ArrowModule() { super("Arrow Counter", false); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(int y) { drawText("Arrows: ?", 4, y, 0xC0C0C0); }
}
