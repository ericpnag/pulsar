package com.pulsar.modules;
import com.pulsar.agent.MCReflect;
import java.lang.reflect.Method;
public class ToggleSprintModule extends PulsarModule {
    public ToggleSprintModule() { super("Toggle Sprint", true); }
    @Override public void onTick() {
        try {
            if (!MCReflect.hasPlayer()) return;
            Object player = MCReflect.getPlayer();
            Method setSprinting = player.getClass().getMethod("d", boolean.class); // setSprinting
            Method isSneaking = player.getClass().getMethod("av"); // isSneaking
            if (!(boolean)isSneaking.invoke(player)) setSprinting.invoke(player, true);
        } catch (Exception ignored) {}
    }
    @Override public boolean hasHud() { return true; }
    @Override public void renderHud(int y) { drawText("Sprint: ON", 4, y, 0x55FF55); }
}
