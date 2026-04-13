package com.bloom.core.module.modules;

import com.bloom.core.module.Module;

public class HitColor extends Module {
    public static float red = 1.0f;
    public static float green = 0.0f;
    public static float blue = 0.0f;
    public static float alpha = 0.4f;

    public HitColor() {
        super("Hit Color", "Customize the damage tint color on entities", false);
    }

    @Override public boolean hasHud() { return false; }
}
