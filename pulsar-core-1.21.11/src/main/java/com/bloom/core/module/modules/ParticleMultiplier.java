package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import java.util.List;

public class ParticleMultiplier extends Module {
    public static float multiplier = 1.0f;

    public ParticleMultiplier() {
        super("Particle Multiplier", "Adjust particle density", false);
    }

    @Override
    public void onTick(MinecraftClient client) {
        // Particle multiplication handled by mixin
    }

    @Override
    public List<ModuleSetting> getSettings() {
        return List.of(
            new ModuleSetting("Multiplier", () -> multiplier, v -> multiplier = v, 0.0f, 4.0f, 0.5f)
        );
    }
}
