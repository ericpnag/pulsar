package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;

public class AutoGG extends Module {
    public static boolean active = false;
    private boolean sentGG = false;

    public AutoGG() {
        super("Auto GG", "Automatically sends gg in chat", false);
    }

    @Override
    public void onEnable() {
        active = true;
        sentGG = false;
    }

    @Override
    public void onDisable() {
        active = false;
        sentGG = false;
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (!active || client.player == null) return;

        // Detect respawn / game end: when player health resets to full after being dead
        // or simply send gg once on enable for macro-style usage
        if (!sentGG) {
            // Send "gg" once when module is enabled (macro style)
            client.player.networkHandler.sendChatMessage("gg");
            sentGG = true;
        }
    }
}
