package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;

public class Zoom extends Module {
    public static boolean zooming = false;
    private int previousFov = 70;

    public Zoom() {
        super("Zoom", "Hold C to zoom in", true);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        boolean cHeld = org.lwjgl.glfw.GLFW.glfwGetKey(
                MinecraftClient.getInstance().getWindow().getHandle(),
                org.lwjgl.glfw.GLFW.GLFW_KEY_C
        ) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

        if (cHeld && !zooming) {
            zooming = true;
            previousFov = client.options.getFov().getValue();
            client.options.getFov().setValue(20);
        } else if (!cHeld && zooming) {
            zooming = false;
            client.options.getFov().setValue(previousFov);
        }
    }
}
