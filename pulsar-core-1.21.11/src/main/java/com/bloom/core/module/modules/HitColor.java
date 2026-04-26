package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import java.util.List;

public class HitColor extends Module {
    public static int hitColor = 0xFFFF4444;
    public static boolean active = false;

    public HitColor() {
        super("Hit Color", "Tint entities when hit with custom color", false);
    }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }

    @Override
    public void onTick(MinecraftClient client) {
        // Color application handled by mixin
    }

    @Override
    public List<ModuleSetting> getSettings() {
        return List.of(
            new ModuleSetting("Red tint", () -> active ? 1f : 0f, v -> active = v >= 1f)
        );
    }
}
