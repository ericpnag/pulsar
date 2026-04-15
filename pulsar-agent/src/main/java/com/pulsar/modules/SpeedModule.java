package com.pulsar.modules;
import com.pulsar.agent.MCReflect;
public class SpeedModule extends PulsarModule {
    private double lastX, lastZ, speed;
    public SpeedModule() { super("Speed Display", false); }
    @Override public void onTick() {
        if (MCReflect.hasPlayer()) {
            double dx = MCReflect.getPlayerX() - lastX, dz = MCReflect.getPlayerZ() - lastZ;
            speed = Math.sqrt(dx*dx + dz*dz) * 20;
            lastX = MCReflect.getPlayerX(); lastZ = MCReflect.getPlayerZ();
        }
    }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(int y) { drawText(String.format("%.1f b/s", speed), 4, y, 0xC0C0C0); }
}
