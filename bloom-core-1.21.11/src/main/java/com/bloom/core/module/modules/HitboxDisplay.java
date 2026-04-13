package com.bloom.core.module.modules;

import com.bloom.core.module.Module;

public class HitboxDisplay extends Module {
    public static boolean showHitboxes = false;

    public HitboxDisplay() {
        super("Hitbox Display", "Show entity hitboxes", false);
    }

    @Override public void onEnable() { showHitboxes = true; }
    @Override public void onDisable() { showHitboxes = false; }
    @Override public boolean hasHud() { return false; }
}
