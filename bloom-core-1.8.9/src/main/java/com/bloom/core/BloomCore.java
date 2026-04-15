package com.bloom.core;

import com.bloom.core.gui.ModuleScreen;
import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.legacyfabric.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.legacyfabric.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.input.Keyboard;

public class BloomCore implements ClientModInitializer {
    public static final ModuleManager MODULES = new ModuleManager();
    private boolean wasPressed = false;

    @Override
    public void onInitializeClient() {
        MODULES.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean pressed = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            if (pressed && !wasPressed) {
                if (client.currentScreen instanceof ModuleScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new ModuleScreen());
                }
            }
            wasPressed = pressed;

            if (client.player != null) {
                for (Module m : MODULES.getModules()) {
                    if (m.isEnabled()) m.onTick(client);
                }
            }
        });

        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            int y = 4;
            for (Module m : MODULES.getModules()) {
                if (m.isEnabled() && m.hasHud()) {
                    m.renderHud(client, y);
                    y += m.getHudHeight();
                }
            }
        });
    }
}
