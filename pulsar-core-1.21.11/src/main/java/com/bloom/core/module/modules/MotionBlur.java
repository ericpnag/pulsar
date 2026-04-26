package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleSetting;
import java.util.List;

public class MotionBlur extends Module {
    public static float strength = 0.5f;

    public MotionBlur() {
        super("Motion Blur", "Screen trail effect on camera movement", false);
    }

    @Override public boolean hasHud() { return false; }

    @Override
    public List<ModuleSetting> getSettings() {
        return List.of(
            new ModuleSetting("Intensity", () -> strength, v -> strength = v, 0.1f, 1.0f, 0.05f)
        );
    }
}
