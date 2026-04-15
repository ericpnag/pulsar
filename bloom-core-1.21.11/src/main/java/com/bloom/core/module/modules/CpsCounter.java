package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.bloom.core.gui.BloomGui.*;

public class CpsCounter extends Module {
    private final List<Long> leftClicks = new ArrayList<>();
    private final List<Long> rightClicks = new ArrayList<>();
    private boolean wasLeftPressed = false;
    private boolean wasRightPressed = false;

    public CpsCounter() {
        super("CPS Counter", "Show clicks per second (left + right)", true);
    }

    @Override
    public void onTick(MinecraftClient client) {
        // Clean old clicks
        long now = System.currentTimeMillis();
        leftClicks.removeIf(t -> now - t > 1000);
        rightClicks.removeIf(t -> now - t > 1000);
    }

    @Override
    public boolean hasHud() { return true; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        // Check clicks at render time (runs every frame = 60+ times/sec) for accuracy
        if (client.getWindow() != null) {
            long handle = client.getWindow().getHandle();
            boolean leftDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            boolean rightDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

            if (leftDown && !wasLeftPressed) leftClicks.add(System.currentTimeMillis());
            if (rightDown && !wasRightPressed) rightClicks.add(System.currentTimeMillis());
            wasLeftPressed = leftDown;
            wasRightPressed = rightDown;
        }

        int left = leftClicks.size();
        int right = rightClicks.size();
        String t = left + " | " + right + " CPS";
        context.drawText(client.textRenderer, text(t, 0xC0C0C0), 4, y, -1, true);
    }
}
