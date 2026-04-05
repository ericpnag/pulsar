package com.bloom.core.module;

import com.bloom.core.module.modules.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public void init() {
        modules.add(new ToggleSprint());
        modules.add(new Fullbright());
        modules.add(new FpsDisplay());
        modules.add(new Coordinates());
        modules.add(new Zoom());
        modules.add(new ArmorStatus());
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
