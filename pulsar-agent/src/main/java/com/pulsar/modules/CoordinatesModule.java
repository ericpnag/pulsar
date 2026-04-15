package com.pulsar.modules;
import com.pulsar.agent.MCReflect;
public class CoordinatesModule extends PulsarModule {
    public CoordinatesModule() { super("Coordinates", true); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(int y) {
        if (!MCReflect.hasPlayer()) return;
        drawText(String.format("%.0f / %.0f / %.0f", MCReflect.getPlayerX(), MCReflect.getPlayerY(), MCReflect.getPlayerZ()), 4, y, 0xFFFF55);
    }
}
