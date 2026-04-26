package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;

public class AutoText extends Module {
    public static String[] messages = {"gg", "Good luck!", "Nice shot!", "GG WP"};

    public AutoText() {
        super("Auto Text", "Send preset messages with hotkeys", false);
    }

    @Override
    public void onTick(MinecraftClient client) {
        // Hotkey handling done via keybind system
    }
}
