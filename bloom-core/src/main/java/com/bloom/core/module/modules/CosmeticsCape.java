package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class CosmeticsCape extends Module {
    public static boolean showCape = true;
    public static int capeColor = 0xFFB7C9; // Cherry blossom pink

    public CosmeticsCape() {
        super("Bloom Cape", "Show your Bloom cosmetic cape", true);
    }

    @Override
    public void onEnable() {
        showCape = true;
    }

    @Override
    public void onDisable() {
        showCape = false;
    }

    @Override
    public boolean hasHud() { return false; }
}
