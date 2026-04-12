package com.bloom.core.module.modules;

import com.bloom.core.cape.AnimatedCapeRenderer;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class CosmeticsCape extends Module {
    public static boolean showCape = true;
    public static boolean animated = true;
    public static int capeColor = 0xC678DD;
    public static String capeFile = "bloom_cape.png";

    public CosmeticsCape() {
        super("Pulsar Cape", "Show your Pulsar cosmetic cape", true);
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
    public void onTick(MinecraftClient client) {
        AnimatedCapeRenderer.tick();
    }

    @Override
    public boolean hasHud() { return false; }
}
