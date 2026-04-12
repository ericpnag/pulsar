package com.bloom.core.module.modules;

import com.bloom.core.module.Module;

public class WeatherChanger extends Module {
    public static boolean active = false;

    public WeatherChanger() {
        super("Weather Changer", "Force clear weather client-side", false);
    }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }
}
