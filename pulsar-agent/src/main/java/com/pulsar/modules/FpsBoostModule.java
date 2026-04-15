package com.pulsar.modules;

import com.pulsar.agent.MCReflect;
import java.lang.reflect.Field;

/**
 * FPS Boost for 1.8.9 — applies optimal settings.
 *
 * GameSettings class: avh
 * Field mappings (verified from JAR):
 *   avh.c = renderDistanceChunks (int)
 *   avh.d = viewBobbing (boolean)
 *   avh.g = limitFramerate (int)
 *   avh.j = particleSetting (int) — 0=all, 1=decreased, 2=minimal
 *   avh.B = ambientOcclusion (int) — 0=off, 1=min, 2=max
 *   avh.D = entityShadows (boolean)
 *   avh.n = fancyGraphics (boolean)
 */
public class FpsBoostModule extends PulsarModule {
    private boolean applied = false;

    public FpsBoostModule() { super("FPS Boost", true); }

    @Override
    public void onTick() {
        if (applied) return;
        try {
            Object mc = MCReflect.getMinecraft();
            // ave.t = gameSettings (type avh)
            Field gsField = mc.getClass().getField("t");
            Object gs = gsField.get(mc);
            if (gs == null) return;

            // Apply all optimizations — maximum FPS
            setInt(gs, "c", 4);        // renderDistance: 4 chunks (huge FPS gain)
            setInt(gs, "g", 260);      // maxFramerate: 260
            setInt(gs, "j", 2);        // particles: minimal
            setInt(gs, "B", 0);        // smoothLighting: off
            setBool(gs, "d", false);   // viewBobbing: off
            setBool(gs, "D", false);   // entityShadows: off
            setBool(gs, "n", false);   // fancyGraphics: off (fast)
            setInt(gs, "S", 0);        // mipmapLevels: 0 (big gain)
            setBool(gs, "aO", false);  // useVbo: true for better performance
            setBool(gs, "aO", true);   // VBOs on

            applied = true;
            System.out.println("[Pulsar] FPS Boost applied — render:6, particles:min, lighting:off, fast graphics");
        } catch (Exception e) {
            System.err.println("[Pulsar] FPS Boost error: " + e.getMessage());
        }
    }

    private void setInt(Object gs, String field, int val) {
        try { gs.getClass().getField(field).setInt(gs, val); } catch (Exception ignored) {}
    }

    private void setBool(Object gs, String field, boolean val) {
        try { gs.getClass().getField(field).setBoolean(gs, val); } catch (Exception ignored) {}
    }

    @Override public void toggle() {
        super.toggle();
        applied = false;
    }
}
