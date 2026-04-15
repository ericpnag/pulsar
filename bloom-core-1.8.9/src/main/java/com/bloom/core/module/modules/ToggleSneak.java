package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;

public class ToggleSneak extends Module {
    public ToggleSneak() { super("Toggle Sneak", "Toggle sneaking without holding key", false); }
    @Override public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        client.player.input.sneaking = true;
    }
    @Override public void onDisable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.player.input.sneaking = false;
    }
}
