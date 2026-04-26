package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleSetting;
import java.util.List;

public class AutoReconnect extends Module {
    public static int delaySeconds = 5;
    public static boolean active = false;

    public AutoReconnect() {
        super("Auto Reconnect", "Reconnect automatically when disconnected", false);
    }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }

    @Override
    public List<ModuleSetting> getSettings() {
        return List.of(
            new ModuleSetting("Delay (s)", () -> (float) delaySeconds, v -> delaySeconds = Math.round(v), 1f, 30f, 1f)
        );
    }
}
