package com.bloom.core.server;

import com.bloom.core.PulsarCore;
import com.bloom.core.config.HudProfiles;
import com.bloom.core.toast.ToastManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ServerProfiles {
    private static String lastServerAddress = "";
    private static final Map<String, String> mappings = new LinkedHashMap<>();
    private static boolean loaded = false;

    private static Path getMappingsPath() {
        Path gameDir = MinecraftClient.getInstance().runDirectory.toPath();
        return gameDir.resolve("pulsar-server-profiles.json");
    }

    public static void setServerProfile(String serverPattern, String profileName) {
        ensureLoaded();
        mappings.put(serverPattern.toLowerCase(), profileName);
        saveMappings();
    }

    public static void removeServerProfile(String serverPattern) {
        ensureLoaded();
        mappings.remove(serverPattern.toLowerCase());
        saveMappings();
    }

    public static void checkAndSwitch(String serverAddress) {
        ensureLoaded();
        if (serverAddress == null || serverAddress.isEmpty()) return;

        String lower = serverAddress.toLowerCase();
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            if (lower.contains(entry.getKey())) {
                String profileName = entry.getValue();
                HudProfiles.loadProfile(profileName, PulsarCore.MODULES);
                ToastManager.show("Switched to " + profileName + " layout");
                return;
            }
        }
    }

    /**
     * Called from PulsarCore tick event to detect server changes.
     */
    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ServerInfo serverInfo = client.getCurrentServerEntry();

        String currentAddress = "";
        if (serverInfo != null && serverInfo.address != null) {
            currentAddress = serverInfo.address;
        }

        if (!currentAddress.equals(lastServerAddress)) {
            lastServerAddress = currentAddress;
            if (!currentAddress.isEmpty()) {
                checkAndSwitch(currentAddress);
            }
        }
    }

    public static Map<String, String> getMappings() {
        ensureLoaded();
        return Collections.unmodifiableMap(mappings);
    }

    // --- Persistence ---

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        loadMappings();
    }

    private static void loadMappings() {
        try {
            Path path = getMappingsPath();
            if (!Files.exists(path)) return;

            String json = Files.readString(path).trim();
            if (!json.startsWith("{") || json.length() < 2) return;

            // Remove outer braces
            json = json.substring(1, json.length() - 1).trim();
            if (json.isEmpty()) return;

            // Parse simple key-value pairs: "pattern":"profileName"
            int pos = 0;
            while (pos < json.length()) {
                int keyStart = json.indexOf("\"", pos);
                if (keyStart < 0) break;
                int keyEnd = json.indexOf("\"", keyStart + 1);
                if (keyEnd < 0) break;
                String key = json.substring(keyStart + 1, keyEnd);

                int colon = json.indexOf(":", keyEnd + 1);
                if (colon < 0) break;

                int valStart = json.indexOf("\"", colon + 1);
                if (valStart < 0) break;
                int valEnd = json.indexOf("\"", valStart + 1);
                if (valEnd < 0) break;
                String val = json.substring(valStart + 1, valEnd);

                mappings.put(key, val);

                int comma = json.indexOf(",", valEnd + 1);
                if (comma < 0) break;
                pos = comma + 1;
            }
        } catch (Exception ignored) {}
    }

    private static void saveMappings() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean first = true;
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
                sb.append("\"").append(escapeJson(entry.getValue())).append("\"");
            }
            sb.append("}");
            Files.writeString(getMappingsPath(), sb.toString());
        } catch (Exception ignored) {}
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
