package com.bloom.core.config;

import com.bloom.core.module.Module;
import com.bloom.core.module.ModuleManager;
import com.bloom.core.module.modules.CosmeticsCape;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PulsarConfig {
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
            for (String id : new String[]{"zoom", "freelook", "mod_menu", "hud_editor"}) {
                props.setProperty("keybind." + id, String.valueOf(KeyBindConfig.getKey(id)));
            }

            // Save HUD positions
            for (Module m : modules.getModules()) {
                if (m.hasHud() && m.hasCustomPosition()) {
                    String prefix = "hud." + m.getName().replace(" ", "_");
                    props.setProperty(prefix + ".x", String.valueOf(m.getHudX()));
                    props.setProperty(prefix + ".y", String.valueOf(m.getHudY()));
                }
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
            for (String id : new String[]{"zoom", "freelook", "mod_menu", "hud_editor"}) {
                String val = props.getProperty("keybind." + id);
                if (val != null) {
                    try {
                        KeyBindConfig.setKey(id, Integer.parseInt(val));
                    } catch (NumberFormatException ignored) {}
                }
            }
            // Load HUD positions
            for (Module m : modules.getModules()) {
                if (m.hasHud()) {
                    String prefix = "hud." + m.getName().replace(" ", "_");
                    String xVal = props.getProperty(prefix + ".x");
                    String yVal = props.getProperty(prefix + ".y");
                    if (xVal != null && yVal != null) {
                        try {
                            m.setHudX(Integer.parseInt(xVal));
                            m.setHudY(Integer.parseInt(yVal));
                        } catch (NumberFormatException ignored2) {}
                    }
                }
            }
        } catch (Exception ignored) {}

        // Load equipped cape from pulsar-cosmetics.json
        loadEquippedCape();
    }

    private static final String[] CAPE_ID_TO_FILE = {
        "cape_blossom", "bloom_cape.png",
        "cape_midnight", "midnight_cape.png",
        "cape_frost", "frost_cape.png",
        "cape_flame", "flame_cape.png",
        "cape_ocean", "ocean_cape.png",
        "cape_emerald", "emerald_cape.png",
        "cape_sunset", "sunset_cape.png",
        "cape_galaxy", "galaxy_cape.png",
        "cape_void", "void_cape.png",
        "cape_lightning", "lightning_cape.png",
        "cape_blood", "blood_cape.png",
        "cape_arctic", "arctic_cape.png",
        "cape_phantom", "phantom_cape.png",
        "cape_neon", "neon_cape.png",
        "cape_lava", "lava_cape.png",
        "cape_sakura", "sakura_cape.png",
        "cape_storm", "storm_cape.png",
        "cape_solar", "solar_cape.png",
        "cape_amethyst", "amethyst_cape.png",
        "cape_inferno", "inferno_cape.png",
        "cape_drift", "drift_cape.png",
        "cape_obsidian", "obsidian_cape.png",
        "cape_blackhole", "blackhole_cape.png",
        "cape_creator", "creator_cape.png",
        "cape_youtube", "youtube_cape.png",
        "cape_twitch", "twitch_cape.png",
        "cape_tiktok", "tiktok_cape.png",
        "cape_og", "og_cape.png"
    };

    private static void loadEquippedCape() {
        try {
            Path gameDir = MinecraftClient.getInstance().runDirectory.toPath();
            Path pulsarDir = gameDir.getParent().getParent();
            Path cosmeticsFile = pulsarDir.resolve("pulsar-cosmetics.json");
            if (!Files.exists(cosmeticsFile)) return;

            String json = Files.readString(cosmeticsFile);

            // Parse equipped cape ID from {"equipped":{"cape":"cape_midnight"}}
            int eqIdx = json.indexOf("\"equipped\"");
            if (eqIdx < 0) return;
            int capeIdx = json.indexOf("\"cape\"", eqIdx);
            if (capeIdx < 0) return;
            int valStart = json.indexOf("\"", capeIdx + 6);
            if (valStart < 0) return;
            int valEnd = json.indexOf("\"", valStart + 1);
            if (valEnd < 0) return;
            String equippedId = json.substring(valStart + 1, valEnd);

            // Map cape ID to filename
            for (int i = 0; i < CAPE_ID_TO_FILE.length; i += 2) {
                if (CAPE_ID_TO_FILE[i].equals(equippedId)) {
                    CosmeticsCape.capeFile = CAPE_ID_TO_FILE[i + 1];
                    CosmeticsCape.showCape = true;
                    break;
                }
            }
        } catch (Exception ignored) {}
    }
}
