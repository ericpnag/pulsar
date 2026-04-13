package com.bloom.core.module.modules;

import com.bloom.core.module.Module;

public class GlintColor extends Module {
    public static int glintR = 160, glintG = 120, glintB = 255;

    public GlintColor() {
        super("Glint Color", "Customize enchantment glint color", false);
    }

    @Override public boolean hasHud() { return false; }
}
