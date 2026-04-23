package com.bloom.core.config;

import org.lwjgl.glfw.GLFW;
import java.util.HashMap;
import java.util.Map;

public class KeyBindConfig {
    private static final Map<String, Integer> binds = new HashMap<>();
    private static final Map<String, Integer> defaults = new HashMap<>();

    static {
        defaults.put("zoom", GLFW.GLFW_KEY_C);
        defaults.put("freelook", GLFW.GLFW_KEY_F);
        defaults.put("mod_menu", GLFW.GLFW_KEY_RIGHT_SHIFT);
        defaults.put("hud_editor", GLFW.GLFW_KEY_GRAVE_ACCENT);
    }

    public static void init() {
        for (var entry : defaults.entrySet()) {
            binds.put(entry.getKey(), entry.getValue());
        }
    }

    public static int getKey(String id) {
        return binds.getOrDefault(id, defaults.getOrDefault(id, GLFW.GLFW_KEY_UNKNOWN));
    }

    public static void setKey(String id, int key) {
        binds.put(id, key);
        try {
            com.bloom.core.config.PulsarConfig.save(com.bloom.core.PulsarCore.MODULES);
        } catch (Exception ignored) {}
    }

    public static String getKeyName(int key) {
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) return name.toUpperCase();
        return switch (key) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "R.SHIFT";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "L.SHIFT";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "R.CTRL";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "L.CTRL";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "R.ALT";
            case GLFW.GLFW_KEY_LEFT_ALT -> "L.ALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            default -> "KEY" + key;
        };
    }

    // Returns the bind ID for a module name, or null if it has no keybind
    public static String getBindId(String moduleName) {
        return switch (moduleName) {
            case "Zoom" -> "zoom";
            case "Freelook" -> "freelook";
            default -> null;
        };
    }
}
