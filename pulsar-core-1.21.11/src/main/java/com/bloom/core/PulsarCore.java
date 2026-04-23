package com.bloom.core;

import com.bloom.core.gui.HudEditorScreen;
import com.bloom.core.gui.ModuleScreen;
import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class PulsarCore implements ClientModInitializer {
    public static final String MOD_ID = "pulsar-core";
    public static final ModuleManager MODULES = new ModuleManager();
    private boolean wasPressed = false;
    private boolean wasHudEditorPressed = false;
    private boolean configLoaded = false;

    @Override
    public void onInitializeClient() {
        com.bloom.core.config.KeyBindConfig.init();
        com.bloom.core.presence.PulsarPresence.init();
        MODULES.init();

        // Initialize motion blur shader

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) return;

            // Mod menu keybind
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

            // HUD editor keybind
            boolean hudPressed = GLFW.glfwGetKey(client.getWindow().getHandle(),
                    com.bloom.core.config.KeyBindConfig.getKey("hud_editor")) == GLFW.GLFW_PRESS;

            if (hudPressed && !wasHudEditorPressed) {
                if (client.currentScreen instanceof HudEditorScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new HudEditorScreen());
                }
            }
            wasHudEditorPressed = hudPressed;

            if (client.player != null) {
                // Load config once when player is available (game dir is set)
                if (!configLoaded) {
                    com.bloom.core.config.PulsarConfig.load(MODULES);
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
            if (client.currentScreen instanceof HudEditorScreen) return; // Editor draws its own

            int stackY = 4;
            for (Module m : MODULES.getModules()) {
                if (m.isEnabled() && m.hasHud()) {
                    if (m.hasCustomPosition()) {
                        // Translate context so module renders at custom position
                        context.getMatrices().pushMatrix();
                        context.getMatrices().translate(m.getHudX() - 2, m.getHudY() - stackY);
                        m.renderHud(context, client, stackY);
                        context.getMatrices().popMatrix();
                        stackY += m.getHudHeight();
                    } else {
                        m.renderHud(context, client, stackY);
                        stackY += m.getHudHeight();
                    }
                }
            }
        });
    }
}
