package com.pulsar.modules;
import com.pulsar.agent.MCReflect;
public class DirectionModule extends PulsarModule {
    public DirectionModule() { super("Direction", false); }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(int y) {
        if (!MCReflect.hasPlayer()) return;
        float yaw = MCReflect.getPlayerYaw() % 360;
        if (yaw < 0) yaw += 360;
        String dir = yaw >= 315 || yaw < 45 ? "S" : yaw < 135 ? "W" : yaw < 225 ? "N" : "E";
        drawText(dir + " (" + (int)yaw + ")", 4, y, 0x60C0F0);
    }
}
