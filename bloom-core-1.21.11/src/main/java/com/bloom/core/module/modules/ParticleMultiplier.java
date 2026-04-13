package com.bloom.core.module.modules;

import com.bloom.core.module.Module;

public class ParticleMultiplier extends Module {
    public static int multiplier = 3; // 1x, 2x, 3x, 5x, 10x
    public static boolean alwaysCrit = true; // always show crit particles on hit

    public ParticleMultiplier() {
        super("Particles", "Multiply particle effects and always show crits", false);
    }

    @Override public boolean hasHud() { return false; }
}
