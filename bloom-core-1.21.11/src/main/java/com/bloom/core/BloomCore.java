package com.bloom.core;

import com.bloom.core.gui.ModuleScreen;
import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class BloomCore implements ClientModInitializer {
    public static final String MOD_ID = "bloom-core";
    public static final ModuleManager MODULES = new ModuleManager();
    private boolean wasPressed = false;
    private boolean configLoaded = false;

    @Override
    public void onInitializeClient() {
        com.bloom.core.config.KeyBindConfig.init();
        com.bloom.core.presence.BloomPresence.init();
        MODULES.init();

        // Initialize motion blur shader
        com.bloom.core.module.modules.MotionBlur.initShader();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) return;

            boolean pressed = GLFW.glfwGetKey(client.getWindow().getHandle(),
                    com.bloom.core.config.KeyBindConfig.getKey("mod_menu")) == GLFW.GLFW_PRESS;

            if (pressed && !wasPressed) {
                if (client.currentScreen instanceof ModuleScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new ModuleScreen());
                }
            }
            wasPressed = pressed;

            if (client.player != null) {
                // Load config once when player is available (game dir is set)
                if (!configLoaded) {
                    com.bloom.core.config.BloomConfig.load(MODULES);
                    configLoaded = true;
                }
                for (Module m : MODULES.getModules()) {
                    if (m.isEnabled()) {
                        m.onTick(client);
                    }
                }
            }
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            int y = 4;
            for (Module m : MODULES.getModules()) {
                if (m.isEnabled() && m.hasHud()) {
                    m.renderHud(context, client, y);
                    y += m.getHudHeight();
                }
            }
        });
    }
}
