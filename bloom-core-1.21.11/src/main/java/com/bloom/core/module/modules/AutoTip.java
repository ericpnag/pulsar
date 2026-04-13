package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;

public class AutoTip extends Module {
    private int ticksSinceLastTip = 0;
    private static final int TIP_INTERVAL = 20 * 60 * 10; // Every 10 minutes

    public AutoTip() {
        super("Auto Tip", "Automatically tips players on Hypixel for rewards", false);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null) return;

        ticksSinceLastTip++;
        if (ticksSinceLastTip >= TIP_INTERVAL) {
            ticksSinceLastTip = 0;
            client.player.networkHandler.sendChatCommand("tip all");
        }
    }

    @Override public boolean hasHud() { return false; }
}
