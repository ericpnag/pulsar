package com.bloom.core.config;

import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleManager;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BloomConfig {
    private static Path getConfigPath() {
        Path gameDir = MinecraftClient.getInstance().runDirectory.toPath();
        return gameDir.resolve("bloom-config.properties");
    }

    public static void save(ModuleManager modules) {
        try {
            Properties props = new Properties();

            // Save module enabled states
            for (Module m : modules.getModules()) {
                props.setProperty("module." + m.getName().replace(" ", "_") + ".enabled", String.valueOf(m.isEnabled()));
            }

            // Save keybinds
            for (String id : new String[]{"zoom", "freelook", "mod_menu"}) {
                props.setProperty("keybind." + id, String.valueOf(KeyBindConfig.getKey(id)));
            }

            try (OutputStream out = Files.newOutputStream(getConfigPath())) {
                props.store(out, "Pulsar Client Config");
            }
        } catch (Exception ignored) {}
    }

    public static void load(ModuleManager modules) {
        try {
            Path path = getConfigPath();
            if (!Files.exists(path)) return;

            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(path)) {
                props.load(in);
            }

            // Load module enabled states
            for (Module m : modules.getModules()) {
                String key = "module." + m.getName().replace(" ", "_") + ".enabled";
                String val = props.getProperty(key);
                if (val != null) {
                    boolean shouldBeEnabled = Boolean.parseBoolean(val);
                    if (shouldBeEnabled != m.isEnabled()) {
                        m.toggle();
                    }
                }
            }

            // Load keybinds
            for (String id : new String[]{"zoom", "freelook", "mod_menu"}) {
                String val = props.getProperty("keybind." + id);
                if (val != null) {
                    try {
                        KeyBindConfig.setKey(id, Integer.parseInt(val));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception ignored) {}
    }
}
