package com.bloom.core.module.modules;

import com.bloom.core.module.Module;

public class NickHider extends Module {
    public static String fakeName = "Player";

    public NickHider() {
        super("Nick Hider", "Hide your real username with a fake name", false);
    }

    @Override public boolean hasHud() { return false; }
}
