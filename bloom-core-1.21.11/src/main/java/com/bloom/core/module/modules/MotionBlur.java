package com.bloom.core.module.modules;

import com.bloom.core.module.Module;

public class MotionBlur extends Module {
    public static float strength = 0.65f;

    public MotionBlur() {
        super("Motion Blur", "Adds motion blur effect on camera movement", false);
    }

    @Override public boolean hasHud() { return false; }
}
