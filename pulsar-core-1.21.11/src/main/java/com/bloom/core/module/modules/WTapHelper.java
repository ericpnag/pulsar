package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;

public class WTapHelper extends Module {
    public static boolean active = false;

    public WTapHelper() {
        super("W-Tap Helper", "Visual indicator for successful sprint resets", false);
    }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }

    @Override
    public void onTick(MinecraftClient client) {
        // Sprint reset detection handled by mixin
    }
}
