package com.bloom.core.module.modules;

import com.bloom.core.module.Module;

public class HealthDisplay extends Module {
    public static boolean showAboveHead = true;

    public HealthDisplay() {
        super("Health Display", "Show player health above their head in PvP", false);
    }

    @Override public boolean hasHud() { return false; }
}
