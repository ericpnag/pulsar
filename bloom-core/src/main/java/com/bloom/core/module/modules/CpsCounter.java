package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CpsCounter extends Module {
    private final List<Long> clicks = new ArrayList<>();
    private boolean wasPressed = false;

    public CpsCounter() {
        super("CPS Counter", "Show clicks per second", false);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.getWindow() == null) return;
        boolean pressed = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (pressed && !wasPressed) {
            clicks.add(System.currentTimeMillis());
        }
        wasPressed = pressed;
        long now = System.currentTimeMillis();
        clicks.removeIf(t -> now - t > 1000);
    }

    @Override
    public boolean hasHud() { return true; }

    @Override
    public void renderHud(DrawContext context, MinecraftClient client, int y) {
        int cps = clicks.size();
        String text = cps + " CPS";
        int tw = client.textRenderer.getWidth(text);
        context.fill(2, y - 1, tw + 8, y + 10, 0x44000000);
        context.fill(2, y - 1, 3, y + 10, 0x44FFB0C0);
        context.drawText(client.textRenderer, text, 6, y, 0xFFB7C9, false);
    }
}
