package com.bloom.core.module.modules;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;

public class Fullbright extends Module {
    private float oldGamma = 1.0f;
    public Fullbright() { super("Fullbright", "Max brightness everywhere", false); }
    @Override public void onEnable() {
        MinecraftClient c = MinecraftClient.getInstance();
        oldGamma = c.options.gamma;
        c.options.gamma = 15.0f;
    }
    @Override public void onDisable() {
        MinecraftClient.getInstance().options.gamma = oldGamma;
    }
    @Override public void onTick(MinecraftClient client) {
        client.options.gamma = 15.0f;
    }
}
