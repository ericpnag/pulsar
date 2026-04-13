package com.bloom.core.module.modules;

import com.bloom.core.module.Module;

public class BlockOutline extends Module {
    public static int outlineColor = 0xFFFFFF;
    public static float outlineWidth = 2.0f;
    public static boolean fill = false;
    public static float fillAlpha = 0.15f;

    public BlockOutline() {
        super("Block Outline", "Customize block selection outline color and width", false);
    }

    @Override public boolean hasHud() { return false; }
}
