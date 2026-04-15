package com.pulsar.modules;
public class PingModule extends PulsarModule {
    public PingModule() { super("Ping Display", false); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(int y) { drawText("Ping: ?", 4, y, 0x55FF55); }
}
