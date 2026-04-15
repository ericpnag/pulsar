package com.bloom.core.module;

import com.bloom.core.module.modules.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public void init() {
        // Sprint & Movement
        modules.add(new ToggleSprint());
        modules.add(new ToggleSneak());
        // HUD displays
        modules.add(new FpsDisplay());
        modules.add(new Coordinates());
        modules.add(new DirectionHud());
        modules.add(new PingDisplay());
        modules.add(new CpsCounter());
        modules.add(new ComboCounter());
        modules.add(new ReachDisplay());
        modules.add(new SpeedDisplay());
        modules.add(new ArrowCounter());
        modules.add(new PotionEffects());
        modules.add(new PotionTimer());
        modules.add(new Keystrokes());
        modules.add(new ArmorHud());
        modules.add(new SaturationDisplay());
        modules.add(new MemoryDisplay());
        modules.add(new ServerDisplay());
        modules.add(new PackDisplay());
        modules.add(new Clock());
        modules.add(new Stopwatch());
        modules.add(new Playtime());
        modules.add(new ItemCounter());
        // Visual
        modules.add(new Fullbright());
        modules.add(new FovChanger());
        modules.add(new TimeChanger());
        modules.add(new WeatherChanger());
        modules.add(new Scoreboard());
        modules.add(new BossBar());
        modules.add(new LowFire());
        modules.add(new LowShield());
        modules.add(new HurtCam());
        modules.add(new MotionBlur());
        modules.add(new CustomCrosshair());
        // Utility
        modules.add(new Zoom());
        modules.add(new Freelook());
        modules.add(new PerformanceBoost());
        modules.add(new DropProtection());
        modules.add(new TntTimer());
        modules.add(new DamageIndicator());
        // Hypixel
        modules.add(new AutoGG());
        modules.add(new AutoTip());
        modules.add(new BedwarsOverlay());
        // Cosmetics
        modules.add(new CosmeticsCape());
    }

    public List<Module> getModules() {
        return modules;
    }

    public Module getByName(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }
}
