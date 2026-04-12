package com.bloom.core.module.modules;

import com.bloom.core.config.KeyBindConfig;
import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class Freelook extends Module {
    public static boolean looking = false;
    public static float cameraYaw, cameraPitch;
    private float savedYaw, savedPitch;
    private boolean wasPressed = false;

    public Freelook() { super("Freelook", "Hold key to look around freely", false); }

    @Override
    public void onDisable() {
        if (looking) {
            looking = false;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.setYaw(savedYaw);
                client.player.setPitch(savedPitch);
            }
        }
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null || client.getWindow() == null) return;
        boolean pressed = GLFW.glfwGetKey(client.getWindow().getHandle(), KeyBindConfig.getKey("freelook")) == GLFW.GLFW_PRESS;

        if (pressed && !wasPressed) {
            // Start freelook — save player angles, init camera to current angles
            looking = true;
            savedYaw = client.player.getYaw();
            savedPitch = client.player.getPitch();
            cameraYaw = savedYaw;
            cameraPitch = savedPitch;
        } else if (!pressed && wasPressed && looking) {
            // Stop freelook — restore player angles
            looking = false;
            client.player.setYaw(savedYaw);
            client.player.setPitch(savedPitch);
        }
        wasPressed = pressed;
    }
}
