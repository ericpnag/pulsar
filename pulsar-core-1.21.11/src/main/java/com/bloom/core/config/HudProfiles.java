package com.bloom.core.config;

import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleManager;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class HudProfiles {
    private static Path getProfilesPath() {
        Path gameDir = MinecraftClient.getInstance().runDirectory.toPath();
        return gameDir.resolve("pulsar-hud-profiles.json");
    }

    public static void saveProfile(String name, ModuleManager modules) {
        try {
            Map<String, String> allProfiles = readAllProfiles();

            // Build module entries for this profile
            StringBuilder moduleEntries = new StringBuilder();
            boolean first = true;
            for (Module m : modules.getModules()) {
                if (!first) moduleEntries.append(",");
                first = false;
                moduleEntries.append("\"").append(escapeJson(m.getName())).append("\":{");
                moduleEntries.append("\"x\":").append(m.getHudX()).append(",");
                moduleEntries.append("\"y\":").append(m.getHudY()).append(",");
                moduleEntries.append("\"enabled\":").append(m.isEnabled());
                moduleEntries.append("}");
            }

            allProfiles.put(name, "{\"name\":\"" + escapeJson(name) + "\",\"modules\":{" + moduleEntries + "}}");

            writeAllProfiles(allProfiles);
        } catch (Exception ignored) {}
    }

    public static void loadProfile(String name, ModuleManager modules) {
        try {
            Map<String, String> allProfiles = readAllProfiles();
            String profileJson = allProfiles.get(name);
            if (profileJson == null) return;

            // Parse modules from profile JSON
            int modulesIdx = profileJson.indexOf("\"modules\"");
            if (modulesIdx < 0) return;
            int braceStart = profileJson.indexOf("{", modulesIdx + 9);
            if (braceStart < 0) return;

            // For each module, find its entry and apply
            for (Module m : modules.getModules()) {
                String key = "\"" + escapeJson(m.getName()) + "\":{";
                int mIdx = profileJson.indexOf(key, braceStart);
                if (mIdx < 0) continue;

                int entryStart = mIdx + key.length();
                int entryEnd = profileJson.indexOf("}", entryStart);
                if (entryEnd < 0) continue;

                String entry = profileJson.substring(entryStart, entryEnd);

                // Parse x
                int xVal = parseIntField(entry, "\"x\":");
                // Parse y
                int yVal = parseIntField(entry, "\"y\":");
                // Parse enabled
                boolean enabled = parseBoolField(entry, "\"enabled\":");

                m.setHudX(xVal);
                m.setHudY(yVal);
                if (m.isEnabled() != enabled) {
                    m.toggle();
                }
            }

            // Save current config so positions persist
            PulsarConfig.save(modules);
        } catch (Exception ignored) {}
    }

    public static List<String> listProfiles() {
        List<String> names = new ArrayList<>();
        try {
            Map<String, String> allProfiles = readAllProfiles();
            names.addAll(allProfiles.keySet());
        } catch (Exception ignored) {}
        return names;
    }

    public static void deleteProfile(String name) {
        try {
            Map<String, String> allProfiles = readAllProfiles();
            allProfiles.remove(name);
            writeAllProfiles(allProfiles);
        } catch (Exception ignored) {}
    }

    // --- Internal helpers ---

    private static Map<String, String> readAllProfiles() {
        Map<String, String> profiles = new LinkedHashMap<>();
        try {
            Path path = getProfilesPath();
            if (!Files.exists(path)) return profiles;

            String json = Files.readString(path).trim();
            if (!json.startsWith("{") || json.length() < 2) return profiles;

            // Remove outer braces
            json = json.substring(1, json.length() - 1).trim();
            if (json.isEmpty()) return profiles;

            // Parse top-level keys: "profileName":{...}
            int pos = 0;
            while (pos < json.length()) {
                // Find key
                int keyStart = json.indexOf("\"", pos);
                if (keyStart < 0) break;
                int keyEnd = json.indexOf("\"", keyStart + 1);
                if (keyEnd < 0) break;
                String key = json.substring(keyStart + 1, keyEnd);

                // Find colon
                int colon = json.indexOf(":", keyEnd + 1);
                if (colon < 0) break;

                // Find matching brace for value
                int valStart = json.indexOf("{", colon + 1);
                if (valStart < 0) break;
                int valEnd = findMatchingBrace(json, valStart);
                if (valEnd < 0) break;

                String value = json.substring(valStart, valEnd + 1);
                profiles.put(key, value);

                // Move past comma
                pos = valEnd + 1;
                int comma = json.indexOf(",", pos);
                if (comma < 0) break;
                pos = comma + 1;
            }
        } catch (Exception ignored) {}
        return profiles;
    }

    private static void writeAllProfiles(Map<String, String> profiles) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : profiles.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":").append(entry.getValue());
        }
        sb.append("}");
        Files.writeString(getProfilesPath(), sb.toString());
    }

    private static int findMatchingBrace(String json, int openIdx) {
        int depth = 0;
        boolean inString = false;
        for (int i = openIdx; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && inString) {
                i++; // skip escaped char
                continue;
            }
            if (c == '"') {
                inString = !inString;
            } else if (!inString) {
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        return -1;
    }

    private static int parseIntField(String entry, String field) {
        int idx = entry.indexOf(field);
        if (idx < 0) return -1;
        int start = idx + field.length();
        int end = start;
        while (end < entry.length() && (Character.isDigit(entry.charAt(end)) || entry.charAt(end) == '-')) {
            end++;
        }
        try {
            return Integer.parseInt(entry.substring(start, end));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static boolean parseBoolField(String entry, String field) {
        int idx = entry.indexOf(field);
        if (idx < 0) return false;
        int start = idx + field.length();
        return entry.startsWith("true", start);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
