package com.bloom.core.module.modules;

import com.bloom.core.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DamageIndicator extends Module {
    private static final List<DamageNumber> numbers = new ArrayList<>();

    public DamageIndicator() {
        super("Damage Indicator", "Show floating damage numbers on entities", false);
    }

    public static void addDamage(double x, double y, double z, float damage) {
        numbers.add(new DamageNumber(x, y + 1.5, z, damage));
    }

    @Override
    public void onTick(MinecraftClient client) {
        Iterator<DamageNumber> it = numbers.iterator();
        while (it.hasNext()) {
            DamageNumber n = it.next();
            n.life--;
            n.y += 0.03;
            if (n.life <= 0) it.remove();
        }
    }

    public static List<DamageNumber> getNumbers() { return numbers; }

    @Override public boolean hasHud() { return false; }

    public static class DamageNumber {
        public double x, y, z;
        public float damage;
        public int life = 40; // ticks

        public DamageNumber(double x, double y, double z, float damage) {
            this.x = x; this.y = y; this.z = z; this.damage = damage;
        }
    }
}
