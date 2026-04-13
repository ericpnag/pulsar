package com.bloom.core.module;

import com.bloom.core.module.modules.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public void init() {
        modules.add(new ToggleSprint());
        modules.add(new ToggleSneak());
        modules.add(new FpsDisplay());
        modules.add(new Coordinates());
        modules.add(new DirectionHud());
        modules.add(new Zoom());
        modules.add(new Freelook());
        modules.add(new PingDisplay());
        modules.add(new CpsCounter());
        modules.add(new ComboCounter());
        modules.add(new ReachDisplay());
        modules.add(new PotionEffects());
        modules.add(new PotionTimer());
        modules.add(new Keystrokes());
        modules.add(new ArmorHud());
        modules.add(new SaturationDisplay());
        modules.add(new MemoryDisplay());
        modules.add(new ServerDisplay());
        modules.add(new PackDisplay());
        modules.add(new TimeChanger());
        modules.add(new Scoreboard());
        modules.add(new LowFire());
        modules.add(new LowShield());
        modules.add(new CosmeticsCape());
        modules.add(new PerformanceBoost());
        modules.add(new Fullbright());
        modules.add(new FovChanger());
        modules.add(new HurtCam());
        modules.add(new WeatherChanger());
        modules.add(new AutoGG());
        modules.add(new TntTimer());
        // New mods
        modules.add(new MotionBlur());
        modules.add(new CustomCrosshair());
        modules.add(new BlockOutline());
        modules.add(new HitColor());
        modules.add(new HitboxDisplay());
        modules.add(new BossBar());
        modules.add(new ParticleMultiplier());
        modules.add(new DamageIndicator());
        modules.add(new NickHider());
        modules.add(new SpeedDisplay());
        modules.add(new ArrowCounter());
        modules.add(new Clock());
        modules.add(new Stopwatch());
        modules.add(new Playtime());
        // Bedwars & Hypixel
        modules.add(new BedwarsOverlay());
        modules.add(new AutoTip());
        modules.add(new ItemCounter());
        modules.add(new HealthDisplay());
        modules.add(new GlintColor());
        modules.add(new DropProtection());
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
