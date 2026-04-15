package com.pulsar.modules;
public class ClockModule extends PulsarModule {
    public ClockModule() { super("Clock", false); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(int y) {
        java.time.LocalTime now = java.time.LocalTime.now();
        drawText(String.format("%02d:%02d", now.getHour(), now.getMinute()), 4, y, 0xC0C0C0);
    }
}
