package com.bloom.core.module.modules;

import com.bloom.core.config.KeyBindConfig;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class Zoom extends Module {
    public static boolean zooming = false;
    private int previousFov = 70;

    public Zoom() { super("Zoom", "Hold key to zoom in", true); }

    @Override
    public void onDisable() {
        if (zooming) {
            zooming = false;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options != null) client.options.getFov().setValue(previousFov);
        }
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null || client.getWindow() == null) return;
        boolean held = GLFW.glfwGetKey(client.getWindow().getHandle(), KeyBindConfig.getKey("zoom")) == GLFW.GLFW_PRESS;
        if (held && !zooming) {
            zooming = true;
            previousFov = client.options.getFov().getValue();
            client.options.getFov().setValue(20);
        } else if (!held && zooming) {
            zooming = false;
            client.options.getFov().setValue(previousFov);
        }
    }
}
