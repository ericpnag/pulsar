package com.bloom.core.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class Module {
    private final String name;
    private final String description;
    private boolean enabled;
    private int hudX = -1; // -1 = use default position
    private int hudY = -1;
    private int hudWidth = 60;

    public Module(String name, String description, boolean enabledByDefault) {
        this.name = name;
        this.description = description;
        this.enabled = enabledByDefault;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isEnabled() { return enabled; }

    public int getHudX() { return hudX; }
    public int getHudY() { return hudY; }
    public void setHudX(int x) { this.hudX = x; }
    public void setHudY(int y) { this.hudY = y; }
    public int getHudWidth() { return hudWidth; }
    public void setHudWidth(int w) { this.hudWidth = w; }
    public boolean hasCustomPosition() { return hudX >= 0 && hudY >= 0; }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
        // Toast notification
        try {
            com.bloom.core.toast.ToastManager.show(
                name + (enabled ? " enabled" : " disabled"),
                enabled ? 0xFF34D399 : 0xFF9CA3AF
            );
        } catch (Exception ignored) {}
        // Auto-save config
        try {
            com.bloom.core.config.PulsarConfig.save(com.bloom.core.PulsarCore.MODULES);
        } catch (Exception ignored) {}
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) toggle();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick(MinecraftClient client) {}
    public boolean hasHud() { return false; }
    public int getHudHeight() { return 12; }
    public void renderHud(DrawContext context, MinecraftClient client, int y) {}
}
