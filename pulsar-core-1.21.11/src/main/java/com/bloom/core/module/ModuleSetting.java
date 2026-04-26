package com.bloom.core.module;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModuleSetting {
    public enum Type { SLIDER, TOGGLE }

    public final String name;
    public final Type type;
    public final Supplier<Float> getter;
    public final Consumer<Float> setter;
    public final float min, max, step;

    /** Slider setting */
    public ModuleSetting(String name, Supplier<Float> getter, Consumer<Float> setter, float min, float max, float step) {
        this.name = name;
        this.type = Type.SLIDER;
        this.getter = getter;
        this.setter = setter;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    /** Toggle setting */
    public ModuleSetting(String name, Supplier<Float> getter, Consumer<Float> setter) {
        this.name = name;
        this.type = Type.TOGGLE;
        this.getter = getter;
        this.setter = setter;
        this.min = 0;
        this.max = 1;
        this.step = 1;
    }
}
