package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;

public class BlockOverlay extends Module {
    public static int overlayColor = 0x5B3FA6;
    public static boolean active = false;

    public BlockOverlay() {
        super("Block Overlay", "Highlight aimed block with colored outline", false);
    }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }

    @Override
    public void onTick(MinecraftClient client) {
        // Overlay rendering handled by mixin
    }
}
