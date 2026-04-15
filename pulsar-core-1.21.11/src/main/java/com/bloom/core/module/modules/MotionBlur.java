package com.bloom.core.module.modules;

import com.bloom.core.module.Module;

public class MotionBlur extends Module {
    public static float strength = 0.5f;

    public MotionBlur() {
        super("Motion Blur", "Screen trail effect on camera movement", false);
    }

    @Override public boolean hasHud() { return false; }
}
