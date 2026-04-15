package com.pulsar.modules;

import com.pulsar.agent.MCReflect;
import java.lang.reflect.Field;

public class FullbrightModule extends PulsarModule {
    public FullbrightModule() { super("Fullbright", false); }

    @Override
    public void onTick() {
        try {
            Object mc = MCReflect.getMinecraft();
            Object gs = mc.getClass().getField("t").get(mc);
            if (gs == null) return;

            // Try every float field — one of them is gamma
            for (Field f : gs.getClass().getFields()) {
                if (f.getType() == float.class) {
                    String name = f.getName();
                    // Skip mouseSensitivity (field 'a') and FOV
                    if (name.equals("a")) continue;
                    try {
                        float current = f.getFloat(gs);
                        // Gamma is normally between 0-1, so if we find one in that range, max it
                        if (current >= 0.0f && current <= 1.5f) {
                            f.setFloat(gs, 15.0f);
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
    }
}
