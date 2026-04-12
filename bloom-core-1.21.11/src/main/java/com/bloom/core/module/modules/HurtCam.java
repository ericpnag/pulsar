package com.bloom.core.module.modules;

import com.bloom.core.module.Module;

public class HurtCam extends Module {
    public static boolean active = false;

    public HurtCam() {
        super("No Hurt Cam", "Disables camera tilt when hurt", false);
    }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }
}
