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
        modules.add(new CpsCounter());
        modules.add(new ComboCounter());
        modules.add(new ReachDisplay());
        modules.add(new PingDisplay());
        modules.add(new Keystrokes());
        modules.add(new ArmorStatus());

        modules.add(new ArrowCounter());
        modules.add(new SpeedDisplay());
        modules.add(new Fullbright());
        modules.add(new Clock());
    }

    public List<Module> getModules() { return modules; }
}
