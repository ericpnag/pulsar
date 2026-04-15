package com.pulsar.modules;
import com.pulsar.agent.MCReflect;
public class FpsModule extends PulsarModule {
    public FpsModule() { super("FPS Display", true); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(int y) {
        int fps = MCReflect.getFPS();
        int color = fps >= 60 ? 0x55FF55 : fps >= 30 ? 0xFFFF55 : 0xFF5555;
        drawText(fps + " FPS", 4, y, color);
    }
}
