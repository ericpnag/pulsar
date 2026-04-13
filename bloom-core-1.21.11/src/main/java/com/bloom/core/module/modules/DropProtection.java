package com.bloom.core.module.modules;

import com.bloom.core.module.Module;

public class DropProtection extends Module {
    public static boolean preventDrop = true;

    public DropProtection() {
        super("Drop Protection", "Prevent accidental item drops of important items", false);
    }

    @Override public void onEnable() { preventDrop = true; }
    @Override public void onDisable() { preventDrop = false; }
    @Override public boolean hasHud() { return false; }
}
