package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleSetting;
import java.util.List;

public class CameraTweaks extends Module {
    public static boolean disableBob = false;
    public static boolean disableSwing = false;

    public CameraTweaks() {
        super("Camera Tweaks", "Disable view bobbing and hand swing", false);
    }

    @Override
    public List<ModuleSetting> getSettings() {
        return List.of(
            new ModuleSetting("Disable Bob", () -> disableBob ? 1f : 0f, v -> disableBob = v >= 1f),
            new ModuleSetting("Disable Swing", () -> disableSwing ? 1f : 0f, v -> disableSwing = v >= 1f)
        );
    }
}
