package com.bloom.core.module.modules;

import com.bloom.core.module.Module;

public class BossBar extends Module {
    public static boolean hideBossBar = true;

    public BossBar() {
        super("Boss Bar", "Toggle the boss health bar visibility", false);
    }

    @Override public void onEnable() { hideBossBar = true; }
    @Override public void onDisable() { hideBossBar = false; }
    @Override public boolean hasHud() { return false; }
}
